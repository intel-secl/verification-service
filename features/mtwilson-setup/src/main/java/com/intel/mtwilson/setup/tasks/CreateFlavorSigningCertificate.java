package com.intel.mtwilson.setup.tasks;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.io.pem.Pem;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyBuilder;
import com.intel.mtwilson.My;
import com.intel.mtwilson.core.common.cms.client.jaxrs.CMSClient;
import com.intel.mtwilson.core.common.model.CertificateType;
import com.intel.mtwilson.setup.LocalSetupTask;

import java.io.*;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;
import javax.security.auth.x500.X500Principal;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;


/**
 * @author arijitgh
 */

public class CreateFlavorSigningCertificate extends LocalSetupTask {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateFlavorSigningCertificate.class);
    private String flavorSigningCSRDistinguishedName = "CN=VS Flavor Signing Certificate,OU=Verification Service";
    private static final String FLAVOR_SIGNING_KEY_ALIAS = "flavor.signing.key.alias";
    private static final String FLAVOR_SIGNER_CERTIFICATE_DN = "mtwilson.flavor.signing.dn";
    private static final String FLAVOR_SIGNER_KEYSTORE_FILE = "flavor.signer.keystore.file";
    private static final String FLAVOR_SIGNER_KEYSTORE_PASSWORD = "flavor.signer.keystore.password";
    private static final String CMS_BASE_URL = "cms.base.url";
    private static final String BEARER_TOKEN = "bearer.token";
    private static final String BEARER_TOKEN_ENV = "BEARER_TOKEN";
    private Properties properties = new Properties();

    @Override
    protected void configure() {
        if (getConfiguration().get(FLAVOR_SIGNER_KEYSTORE_PASSWORD) == null || getConfiguration().get(FLAVOR_SIGNER_KEYSTORE_PASSWORD).isEmpty()) {
            getConfiguration().set(FLAVOR_SIGNER_KEYSTORE_PASSWORD, RandomUtil.randomBase64String(16));
        }
        if (getConfiguration().get(FLAVOR_SIGNER_KEYSTORE_FILE) == null || getConfiguration().get(FLAVOR_SIGNER_KEYSTORE_FILE).isEmpty()) {
            getConfiguration().set(FLAVOR_SIGNER_KEYSTORE_FILE, My.configuration().getDirectoryPath() + File.separator + "mtwilson-flavor-signing-cert.p12");
        }
        if (getConfiguration().get(FLAVOR_SIGNER_CERTIFICATE_DN) == null || getConfiguration().get(FLAVOR_SIGNER_CERTIFICATE_DN).isEmpty()) {
            getConfiguration().set(FLAVOR_SIGNER_CERTIFICATE_DN, flavorSigningCSRDistinguishedName);
        }
        if (getConfiguration().get(FLAVOR_SIGNING_KEY_ALIAS) == null || getConfiguration().get(FLAVOR_SIGNING_KEY_ALIAS).isEmpty()) {
            getConfiguration().set(FLAVOR_SIGNING_KEY_ALIAS, "flavor-signing-key");
        }
        if (getConfiguration().get(CMS_BASE_URL) == null || getConfiguration().get(CMS_BASE_URL).isEmpty()) {
            configuration("CMS Base Url is not provided");
        }

        if (System.getenv(BEARER_TOKEN_ENV) == null || System.getenv(BEARER_TOKEN_ENV).isEmpty()) {
            configuration("AAS Bearer Token is not provided in environment");
        } else {
            properties.setProperty(BEARER_TOKEN, System.getenv(BEARER_TOKEN_ENV));
        }
        try {
            initializeKeystore();
        } catch (Exception exc) {
            configuration("Cannot initialize keystore");
        }
    }

    @Override
    protected void validate() throws Exception {
        if (!new File(getConfiguration().get(FLAVOR_SIGNER_KEYSTORE_FILE)).exists()) {
            validation("Flavor Signing keystore file is missing");
        }
        if (!getConfigurationFaults().isEmpty()) {
            return;
        }
        FileInputStream keystoreFIS = new FileInputStream(getConfiguration().get(FLAVOR_SIGNER_KEYSTORE_FILE));
        KeyStore keystore;
        try {
            keystore = KeyStore.getInstance("PKCS12");
            keystore.load(keystoreFIS, getConfiguration().get(FLAVOR_SIGNER_KEYSTORE_PASSWORD).toCharArray());
            keystoreFIS.close();
            if (!keystore.containsAlias(getConfiguration().get(FLAVOR_SIGNING_KEY_ALIAS))) {
                validation("Flavor Signing key is not present in keystore");
            }
        } catch (Exception ex) {
            validation(ex, "Cannot load flavor signing keystore");
        } finally {
            keystoreFIS.close();
        }
    }

    @Override
    protected void execute() throws Exception {
        TlsPolicy tlsPolicy = TlsPolicyBuilder.factory().insecure().build();
        File flavorSigningKeystoreFile = new File(getConfiguration().get(FLAVOR_SIGNER_KEYSTORE_FILE));

        if (flavorSigningKeystoreFile.createNewFile()) {
            log.debug("Flavor Signing keystore created");
        } else {
            log.debug("Flavor Signing keystore already exists");
        }
        // create a new key pair for flavor signing
        KeyPair flavorSigningKey = RsaUtil.generateRsaKeyPair(3072);

        CMSClient cmsClient = new CMSClient(properties, new TlsConnection(new URL(getConfiguration().get("cms.base.url")), tlsPolicy));
        X509Certificate cmsCACert = cmsClient.getCACertificate();
        X509Certificate flavorSigningCert = cmsClient.getCertificate(getCSR(flavorSigningKey).toString(), CertificateType.FLAVOR_SIGNING.getValue());

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
        X509Certificate[] certificateChain = new X509Certificate[2];
        certificateChain[0] = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(flavorSigningCert.getEncoded()));
        certificateChain[1] = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(cmsCACert.getEncoded()));

        storeKeyPair(flavorSigningKey, certificateChain);
        storeCertificateChain(cmsCACert, flavorSigningCert);
    }

    private void initializeKeystore() throws Exception {
        FileOutputStream keystoreFOS = new FileOutputStream(getConfiguration().get(FLAVOR_SIGNER_KEYSTORE_FILE));
        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(null, getConfiguration().get(FLAVOR_SIGNER_KEYSTORE_PASSWORD).toCharArray());
            keystore.store(keystoreFOS, getConfiguration().get(FLAVOR_SIGNER_KEYSTORE_PASSWORD).toCharArray());
        } catch (Exception exc) {
            throw new Exception("Error initializing keystore", exc);
        } finally {
            keystoreFOS.close();
        }
    }

    //Create CSR for flavor signing request
    private Pem getCSR(KeyPair flavorSigningKey) throws OperatorCreationException, IOException {
        PKCS10CertificationRequestBuilder csrBuilder = new JcaPKCS10CertificationRequestBuilder(new X500Principal(flavorSigningCSRDistinguishedName), flavorSigningKey.getPublic());
        ContentSigner signGen = new JcaContentSignerBuilder("SHA384withRSA").build(flavorSigningKey.getPrivate());
        PKCS10CertificationRequest certificateRequest = csrBuilder.build(signGen);
        return new Pem("CERTIFICATE REQUEST", certificateRequest.getEncoded());
    }

    private void storeKeyPair(KeyPair flavorSigningKey, X509Certificate[] certificateChain) throws Exception {
        FileInputStream keystoreFIS = new FileInputStream(getConfiguration().get(FLAVOR_SIGNER_KEYSTORE_FILE));
        KeyStore keystore;
        try {
            keystore = KeyStore.getInstance("PKCS12");
            keystore.load(keystoreFIS, getConfiguration().get(FLAVOR_SIGNER_KEYSTORE_PASSWORD).toCharArray());
        } catch (Exception exc) {
            throw new Exception("Error loading keystore", exc);
        } finally {
            keystoreFIS.close();
        }
        FileOutputStream keystoreFOS = new FileOutputStream(getConfiguration().get(FLAVOR_SIGNER_KEYSTORE_FILE));
        try {
            keystore.setKeyEntry(getConfiguration().get(FLAVOR_SIGNING_KEY_ALIAS), flavorSigningKey.getPrivate(), getConfiguration().get(FLAVOR_SIGNER_KEYSTORE_PASSWORD).toCharArray(), certificateChain);
            keystore.store(keystoreFOS, getConfiguration().get(FLAVOR_SIGNER_KEYSTORE_PASSWORD).toCharArray());
        } catch (Exception exc) {
            throw new Exception("Error storing keypair in keystore", exc);
        }finally {
            keystoreFOS.close();
        }
    }


    private void storeCertificateChain(X509Certificate cmsCACert, X509Certificate flavorSigningCert) throws CertificateException {
        Pem flavorSigningCertEncoded = new Pem("CERTIFICATE", flavorSigningCert.getEncoded());
        Pem flavorSigningCaCert = new Pem("CERTIFICATE", cmsCACert.getEncoded());
        String certificateChainPem = String.format("%s\n%s", flavorSigningCertEncoded.toString(), flavorSigningCaCert.toString());
        File certificateChainPemFile = new File(My.configuration().getDirectoryPath() + File.separator + "flavor-signer.crt.pem");
        try (FileOutputStream certificateChainPemFileOut = new FileOutputStream(certificateChainPemFile)) {
            IOUtils.write(certificateChainPem, certificateChainPemFileOut);
        } catch (IOException e) {
            validation(e, "Cannot write to flavor-signer.crt.pem");
        }
    }
}
