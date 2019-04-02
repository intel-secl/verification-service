/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.privacyca.v2.rpc;

import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.core.privacyca.PrivacyCA;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.core.common.tpm.model.IdentityProofRequest;
import com.intel.mtwilson.core.common.tpm.model.IdentityRequest;
import gov.niarl.his.privacyca.TpmIdentityProof;
import gov.niarl.his.privacyca.TpmIdentityRequest;
import gov.niarl.his.privacyca.TpmPubKey;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.Callable;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author jbuhacoff
 */
@RPC("aik_request_submit_response")
public class IdentityRequestSubmitResponse implements Callable<IdentityProofRequest> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdentityRequestSubmitResponse.class);

    private byte[] responseToChallenge;

    public byte[] getResponseToChallenge() {
        return responseToChallenge;
    }

    public void setResponseToChallenge(byte[] responseToChallenge) {
        this.responseToChallenge = responseToChallenge;
    }
    
    private IdentityRequest identityRequest;

    public IdentityRequest getIdentityRequest() {
        return identityRequest;
    }

    public void setIdentityRequest(IdentityRequest identityRequest) {
        this.identityRequest = identityRequest;
    }
    
    @Override
    @RequiresPermissions("host_aiks:certify")
    public IdentityProofRequest call() throws Exception {
        RSAPrivateKey caPrivKey = TpmUtils.privKeyFromP12(My.configuration().getPrivacyCaIdentityP12().getAbsolutePath(), My.configuration().getPrivacyCaIdentityPassword());
        X509Certificate caPubCert = TpmUtils.certFromP12(My.configuration().getPrivacyCaIdentityP12().getAbsolutePath(), My.configuration().getPrivacyCaIdentityPassword());
        int validityDays = My.configuration().getPrivacyCaIdentityValidityDays();

        //decrypt response
        TpmIdentityRequest returnedIR = new TpmIdentityRequest(responseToChallenge);
        byte[] decryptedIdentityRequestChallenge = returnedIR.decryptRaw(caPrivKey); // should be the same 32 bytes that we sent as the encrypted challenge

        TpmIdentityProof idProof;
        X509Certificate ekCert;
        // find the existing challenge and idproof
        // save the challenge and idproof for use in identity request submit response if the client successfully answers the challenge
        // the filename is the challenge (in hex) and the content is the idproof
        File datadir = My.repository().getDirectory("privacyca-aik-requests"); //new File(My.filesystem().getBootstrapFilesystem().getVarPath() + File.separator + "privacyca-aik-requests"); 
        if (!datadir.exists()) {
            datadir.mkdirs();
        }
        String filename = TpmUtils.byteArrayToHexString(decryptedIdentityRequestChallenge); //Hex.encodeHexString(identityRequestChallenge)
        log.debug("Filename: {}", filename);
        Path challengeFile = datadir.toPath().resolve(filename);
        if (!Files.exists(challengeFile)) {
            throw new RuntimeException("Invalid challenge response");
        }
        
        String ekcertFilename = filename + ".ekcert";
        File ekcertFile = datadir.toPath().resolve(ekcertFilename).toFile();
        try (FileInputStream in = new FileInputStream(ekcertFile)) {
            byte[] ekcertBytes = IOUtils.toByteArray(in);
            ekCert = X509Util.decodeDerCertificate(ekcertBytes);
        }       
        
        String optionsFilename = filename + ".opt";
        Path optiionsFile = datadir.toPath().resolve(optionsFilename);
        
        byte[] modulus = Files.readAllBytes(challengeFile);
        byte[] aikName = Files.readAllBytes(optiionsFile);
        RSAPublicKey aik = TpmUtils.makePubKey(modulus, TpmUtils.intToByteArray(65537));
        TpmPubKey k = new TpmPubKey(aik, 0x1, 0x4);
        byte[] certBytes = TpmUtils.makeCert(k, new String(aikName, StandardCharsets.UTF_8), caPrivKey, caPubCert, validityDays, 0).getEncoded();
        return PrivacyCA.processIdentityRequest(identityRequest, caPrivKey, (RSAPublicKey)caPubCert.getPublicKey(), (RSAPublicKey)ekCert.getPublicKey(), certBytes);
    }
}
