/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.privacyca.v2.rpc;

import com.google.common.io.Files;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.core.privacyca.PrivacyCA;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.core.common.tpm.model.IdentityProofRequest;
import com.intel.mtwilson.core.common.tpm.model.IdentityRequest;
import com.intel.mtwilson.tpm.endorsement.jdbi.TpmEndorsementDAO;
import com.intel.mtwilson.tpm.endorsement.jdbi.TpmEndorsementJdbiFactory;
import com.intel.mtwilson.tpm.endorsement.model.TpmEndorsement;
import gov.niarl.his.privacyca.TpmIdentityRequest;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.File;
import java.security.cert.CertificateEncodingException;
import java.util.List;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.commons.codec.binary.Base64;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;


/**
 *
 * @author jbuhacoff
 */
@RPC("aik_request_get_challenge")
public class IdentityRequestGetChallenge implements Callable<IdentityProofRequest> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityRequestGetChallenge.class);

    private IdentityRequest identityRequest;

    public IdentityRequest getIdentityRequest() {
        return identityRequest;
    }

    public void setIdentityRequest(IdentityRequest identityRequest) {
        this.identityRequest = identityRequest;
    }
    
    private byte[] endorsementCertificate;

    public void setEndorsementCertificate(byte[] endorsementCertificate) {
        this.endorsementCertificate = endorsementCertificate;
    }
    
    public byte[] getEndorsementCertificate() {
        return endorsementCertificate;
    }

    private Map<String, X509Certificate> getEndorsementCertificates() throws IOException, CertificateException {
        Map<String, X509Certificate> endorsementCerts = new HashMap<>();
        File ekCacertsPemFile = My.configuration().getPrivacyCaEndorsementCacertsFile();
        try (FileInputStream in = new FileInputStream(ekCacertsPemFile)) {
            String ekCacertsPem = IOUtils.toString(in); // throws IOException
            List<X509Certificate> ekCacerts = X509Util.decodePemCertificates(ekCacertsPem); // throws CertificateException
            for (X509Certificate ekCacert : ekCacerts) {
                LOG.debug("Adding issuer {}", ekCacert.getSubjectX500Principal().getName());
                endorsementCerts.put(ekCacert.getSubjectDN().getName(), ekCacert);
            }
        }
        return endorsementCerts;
    }

    @Override
    @RequiresPermissions("host_aiks:certify")
    public IdentityProofRequest call() throws Exception {
        LOG.debug("PrivacyCA.p12: {}", My.configuration().getPrivacyCaIdentityP12().getAbsolutePath());
        RSAPrivateKey caPrivKey = TpmUtils.privKeyFromP12(My.configuration().getPrivacyCaIdentityP12().getAbsolutePath(), My.configuration().getPrivacyCaIdentityPassword());
        X509Certificate caPubCert = TpmUtils.certFromP12(My.configuration().getPrivacyCaIdentityP12().getAbsolutePath(), My.configuration().getPrivacyCaIdentityPassword());

        // load the trusted ek cacerts
        Map<String, X509Certificate> endorsementCerts = getEndorsementCertificates();
        TpmIdentityRequest tempEC = new TpmIdentityRequest(endorsementCertificate);

        // Use the Sun crypto provider since Bouncy Castle fails to parse certain 
        // TPM manufacture certificates.
        byte[] ekBytes = tempEC.decryptRaw(caPrivKey);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate ekCert = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(ekBytes));

        LOG.debug("Validating endorsement certificate");
        if (!isEkCertificateVerifiedByAuthority(ekCert, endorsementCerts.get(ekCert.getIssuerDN().getName().replaceAll("\\x00", "")))
                && !isEkCertificateVerifiedByAnyAuthority(ekCert, endorsementCerts.values())
                && !isEkCertificateRegistered(ekCert)) {
            // cannot trust the EC because it's not signed by any of our trusted EC CAs and is not in the mw_tpm_endorements table
            LOG.error("EC is not trusted, cannot trust the EC because it's not signed by any of trusted EC CAs in EndorsmentCA.pem");
            throw new RuntimeException("The EK Certificate is not trusted by the EC CAs configured in HVS");
        }
        // check out the endorsement certificate
        // if the cert is good, issue challenge
        byte[] identityRequestChallenge = TpmUtils.createRandomBytes(32);
        // save the challenge and idproof for use in identity request submit response if the client successfully answers the challenge
        // the filename is the challenge (in hex) and the content is the idproof
        File datadir = My.repository().getDirectory("privacyca-aik-requests"); //new File(My.filesystem().getBootstrapFilesystem().getVarPath() + File.separator + "privacyca-aik-requests"); 
        if (!datadir.exists()) {
            datadir.mkdirs();
        }
        String filename = TpmUtils.byteArrayToHexString(identityRequestChallenge); //Hex.encodeHexString(identityRequestChallenge)
        LOG.debug("Filename: {}", filename);
        String optionsFilename = filename + ".opt";
        // store AIK and its name
        Files.write(identityRequest.getAikModulus(), datadir.toPath().resolve(filename).toFile());
        Files.write(identityRequest.getAikName(), datadir.toPath().resolve(optionsFilename).toFile());
        // also save the ekcert for the identity request submit response 
        String ekcertFilename = filename + ".ekcert";
        try (FileOutputStream out = new FileOutputStream(datadir.toPath().resolve(ekcertFilename).toFile())) {
            IOUtils.write(ekCert.getEncoded(), out);
        }
        IdentityProofRequest proofRequest = PrivacyCA.processIdentityRequest(identityRequest, caPrivKey, (RSAPublicKey)caPubCert.getPublicKey(), (RSAPublicKey)ekCert.getPublicKey(), identityRequestChallenge);
        return proofRequest;
    }

    private boolean isEkCertificateVerifiedByAuthority(X509Certificate ekCert, X509Certificate authority) {
        if (authority != null) {
            try {
                ekCert.verify(authority.getPublicKey()); // throws SignatureException
                return true;
            } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException | CertificateException e) {
                LOG.debug("Failed to verify EC using CA {}: {}", ekCert.getIssuerDN().getName().replaceAll("\\x00", ""), e.getMessage());
            }
        }
        return false;
    }

    private boolean isEkCertificateVerifiedByAnyAuthority(X509Certificate ekCert, Collection<X509Certificate> authorities) {
        for (X509Certificate authority : authorities) {
            try {
                ekCert.verify(authority.getPublicKey()); // throws SignatureException
                LOG.debug("Verified EC with authority: {}", authority.getSubjectX500Principal().getName());
                return true;
            } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException | CertificateException e) {
                LOG.debug("Failed to verify EC with authority: {}", authority.getSubjectX500Principal().getName());
            }
        }
        return false;
    }
	
   private String getCommonName(X509Certificate ekCert){
        String issuer =  ekCert.getIssuerDN().getName().replaceAll(" ", "");
        issuer = issuer.replaceAll("\\+", ",");
        String[] dnFields = issuer.split(",");
        for (String field: dnFields ) {
            if (field.contains("CN=")){
                String commName = field;
                return commName.split("=")[1];
            }
        }
        return "";
    }

    private boolean isEkCertificateRegistered(X509Certificate ekCert) {

        try (TpmEndorsementDAO dao = TpmEndorsementJdbiFactory.tpmEndorsementDAO()) {
	    String commonName = getCommonName(ekCert);
            if (commonName.isEmpty()){
	        LOG.debug("Common Name field is not present in issuer");
                return false;
            }
            TpmEndorsement tpmEndorsement = dao.findTpmEndorsementByIssuerEqualTo(commonName); // SHOULD REALLY BE BY CERT SHA256
            if (tpmEndorsement == null) {
                return false;
            }
            LOG.debug("EC is registered");
            return true;
	} catch (IOException  e) {
            LOG.debug("Cannot check if EC is registered", e);
            return false;
        }
    }

}
