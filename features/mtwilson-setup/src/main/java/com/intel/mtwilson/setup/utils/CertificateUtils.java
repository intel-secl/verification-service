package com.intel.mtwilson.setup.utils;

import com.intel.dcsg.cpg.io.pem.Pem;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;

import javax.security.auth.x500.X500Principal;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;

public class CertificateUtils {


    public static Pem getCSR(KeyPair flavorSigningKey, String commonName) throws OperatorCreationException, IOException {
        PKCS10CertificationRequestBuilder csrBuilder = new JcaPKCS10CertificationRequestBuilder(new X500Principal(commonName), flavorSigningKey.getPublic());
        ContentSigner signGen = new JcaContentSignerBuilder("SHA384withRSA").build(flavorSigningKey.getPrivate());
        PKCS10CertificationRequest certificateRequest = csrBuilder.build(signGen);
        return new Pem("CERTIFICATE REQUEST", certificateRequest.getEncoded());
    }

}
