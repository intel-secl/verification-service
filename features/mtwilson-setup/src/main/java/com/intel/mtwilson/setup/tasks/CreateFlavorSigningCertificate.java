package com.intel.mtwilson.setup.tasks;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.io.pem.Pem;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyBuilder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.core.common.cms.client.jaxrs.CMSClient;
import com.intel.mtwilson.setup.LocalSetupTask;

import java.io.*;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;
import javax.security.auth.x500.X500Principal;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;


/**
 *
 * @author arijitgh
 */

public class CreateFlavorSigningCertificate extends LocalSetupTask{

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateFlavorSigningCertificate.class);
    private String flavorSigningCSRDistinguishedName = "CN=VS Flavor Signing Certificate,OU=Verification Service";
    private static final String FLAVOR_SIGNING_KEY_ALIAS = "flavor.signing.key.alias";
    private static final String FLAVOR_SIGNER_CERTIFICATE_DN = "flavor.signer.certificate.dn";
    private static final String FLAVOR_SIGNER_KEYSTORE_FILE = "flavor.signer.keystore.file";
    private static final String FLAVOR_SIGNER_KEYSTORE_PASSWORD = "flavor.signer.keystore.password";
    private static final String FLAVOR_SIGNER_KEY_PASSWORD = "flavor.signer.key.password";
    private Properties properties = new Properties();

    public String getFlavorSigningKeystoreFile() {
        return getConfiguration().get(FLAVOR_SIGNER_KEYSTORE_FILE, My.configuration().getDirectoryPath() + File.separator + "mtwilson-flavor-signing-cert.p12");
    }

    public void setFlavorSigningKeystoreFile(String flavorSigningKeystoreFile) {
        getConfiguration().set(FLAVOR_SIGNER_KEYSTORE_FILE, flavorSigningKeystoreFile);
    }

    public String getFlavorSigningKeystorePassword() {
        return getConfiguration().get(FLAVOR_SIGNER_KEYSTORE_PASSWORD, null); // no default here, will return null if not configured: only the configure() method will generate a new random password if necessary
    }

    public void setFlavorSigningKeystorePassword(String samlKeystorePassword) {
        getConfiguration().set(FLAVOR_SIGNER_KEYSTORE_PASSWORD, samlKeystorePassword);
    }

    public String getFlavorSigningKeyPassword() {
        return getConfiguration().get(FLAVOR_SIGNER_KEY_PASSWORD, null); // no default here, will return null if not configured: only the configure() method will generate a new random password if necessary
    }

    public void setFlavorSigningKeyPassword(String flavorSigningKeyPassword) {
        getConfiguration().set(FLAVOR_SIGNER_KEY_PASSWORD, flavorSigningKeyPassword);
    }

    public String getFlavorSigningCertificateDistinguishedName() {
        return getConfiguration().get(FLAVOR_SIGNER_CERTIFICATE_DN, "CN=mtwilson-flavor-signer,OU=mtwilson");
    }

    public void setFlavorSigningCertificateDistinguishedName(String samlDistinguishedName) {
        getConfiguration().set(FLAVOR_SIGNER_CERTIFICATE_DN, samlDistinguishedName);
    }

    public String getFlavorSigningKeyAlias() {
        return getConfiguration().get(FLAVOR_SIGNING_KEY_ALIAS, "flavor-signing-key");
    }

    public String getFlavorSignerDistinguishedName() {
        return flavorSigningCSRDistinguishedName;
    }

    public void setFlavorDistinguishedName(String distinguishedName) {
        this.flavorSigningCSRDistinguishedName = distinguishedName;
    }

    public void setFlavorSigningKeyAlias(String alias) {
        getConfiguration().set(FLAVOR_SIGNING_KEY_ALIAS, alias);
    }

    @Override
    protected void configure() {

        String flavorSigningKeystorePassword = getFlavorSigningKeystorePassword();

        if (flavorSigningKeystorePassword == null || flavorSigningKeystorePassword.isEmpty()) {
            setFlavorSigningKeystorePassword(RandomUtil.randomBase64String(16));
            setFlavorSigningKeyPassword(getFlavorSigningKeystorePassword());
        }

        if( getConfiguration().get("mtwilson.flavor.signing.dn") == null || getConfiguration().get("mtwilson.flavor.signing.dn").isEmpty()) {
            getConfiguration().set("mtwilson.flavor.signing.dn", flavorSigningCSRDistinguishedName);
        }

        if (getConfiguration().get("cms.api.url") == null || getConfiguration().get("cms.api.url").isEmpty()) {
            configuration("CMS API Url is not provided");
        }

        if (getConfiguration().get("aas.bearer.token") != null || !getConfiguration().get("aas.bearer.token").isEmpty()) {
            properties.setProperty("aas.bearer.token", getConfiguration().get("aas.bearer.token"));
        }
        else {
            configuration("AAS Bearer Token is not provided");
        }


    }

    @Override
    protected void validate() throws Exception {
        if (getFlavorSigningKeystorePassword() == null) {
            configuration("Flavor signing keystore password is not configured");
        }
        if (getFlavorSigningKeyPassword() == null) {
            configuration("Flavor signing key password is not configured");
        }
        if (!getConfigurationFaults().isEmpty()) {
            return;
        }

        if (!new File(getFlavorSigningKeystoreFile()).exists()) {
            validation("Flavor Signing keystore file is missing");
        }

        KeyStore keystore = KeyStore.getInstance("PKCS12", "SunJSSE");
        try {
            keystore.load(new FileInputStream(getFlavorSigningKeystoreFile()), getFlavorSigningKeystorePassword().toCharArray());
            if (!keystore.containsAlias(getFlavorSigningKeyAlias())) {
                validation("Flavor Signing key is not present in keystore");
            }
        } catch (Exception ex) {
            validation(ex, "Cannot load flavor signing keystore");
        }
    }

    @Override
    protected void execute() throws Exception {
        TlsPolicy tlsPolicy = TlsPolicyBuilder.factory().insecure().build();

        File flavorSigningKeystoreFile = new File(getFlavorSigningKeystoreFile());
        if (flavorSigningKeystoreFile.createNewFile()) {
            log.debug("Flavor Signing keystore created");
        } else {
            log.debug("Flavor Signing keystore already exists");
        }
        setFlavorSigningKeystoreFile(getFlavorSigningKeystoreFile());
        KeyStore keystore = KeyStore.getInstance("PKCS12", "SunJSSE");
        keystore.load(null, getFlavorSigningKeystorePassword().toCharArray());
        keystore.store(new FileOutputStream(getFlavorSigningKeystoreFile()), getFlavorSigningKeystorePassword().toCharArray());
        // create a new key pair for flavor signing
        KeyPair flavorSigningKey = RsaUtil.generateRsaKeyPair(3072);

        //Create CSR for flavor signing request
        PKCS10CertificationRequestBuilder csrBuilder = new JcaPKCS10CertificationRequestBuilder(new X500Principal(flavorSigningCSRDistinguishedName), flavorSigningKey.getPublic());
        ContentSigner signGen = new JcaContentSignerBuilder("SHA384withRSA").build(flavorSigningKey.getPrivate());
        PKCS10CertificationRequest certificateRequest = csrBuilder.build(signGen);
        Pem flavorSigningCSR = new Pem("CERTIFICATE REQUEST", certificateRequest.getEncoded());
        CMSClient cmsClient = new CMSClient(properties, new TlsConnection(new URL(getConfiguration().get("cms.api.url")), tlsPolicy));
        X509Certificate cmsCACert = cmsClient.getCACertificate();
        X509Certificate flavorSigningCert = cmsClient.getFlavorSigningCertificate(flavorSigningCSR.toString());
        CertificateFactory certificateFactory=CertificateFactory.getInstance("X509");
        keystore.load(new FileInputStream(getFlavorSigningKeystoreFile()), getFlavorSigningKeystorePassword().toCharArray());

        Pem flavorSigningCertEncoded = new Pem("CERTIFICATE", flavorSigningCert.getEncoded());
        Pem flavorSigningCaCert = new Pem("CERTIFICATE", cmsCACert.getEncoded());
        String certificateChainPem = String.format("%s\n%s", flavorSigningCertEncoded.toString(), flavorSigningCaCert.toString());
        X509Certificate[] certificateChain = new X509Certificate[2];
        certificateChain[0] = (X509Certificate)certificateFactory.generateCertificate(new ByteArrayInputStream(flavorSigningCert.getEncoded()));
        certificateChain[1] = (X509Certificate)certificateFactory.generateCertificate(new ByteArrayInputStream(cmsCACert.getEncoded()));
        keystore.setKeyEntry(getFlavorSigningKeyAlias(), flavorSigningKey.getPrivate(), getFlavorSigningKeystorePassword().toCharArray(), certificateChain);
        keystore.store(new FileOutputStream(getFlavorSigningKeystoreFile()), getFlavorSigningKeystorePassword().toCharArray());

        File certificateChainPemFile = new File(My.configuration().getDirectoryPath() + File.separator + "flavor-signer.crt.pem");
        try (FileOutputStream certificateChainPemFileOut = new FileOutputStream(certificateChainPemFile)) {
            IOUtils.write(certificateChainPem, certificateChainPemFileOut);
        } catch (IOException e) {
            validation(e, "Cannot write to flavor-signer.crt.pem");
        }

    }
}
