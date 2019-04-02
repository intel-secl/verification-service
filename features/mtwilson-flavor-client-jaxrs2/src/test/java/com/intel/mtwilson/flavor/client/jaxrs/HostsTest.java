/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.client.jaxrs;

import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.flavor.rest.v2.model.HostCreateCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.HostFilterCriteria;
import com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.creator.impl.CertificateTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.creator.impl.InsecureTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.creator.impl.InsecureTrustFirstPublicKeyTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.creator.impl.PublicKeyDigestTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import java.io.File;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author purvades
 */
public class HostsTest {

    Properties properties = new Properties();

    public HostsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        Extensions.register(TlsPolicyCreator.class, InsecureTlsPolicyCreator.class); // required for testInsecureV1 and testInsecureV2
        Extensions.register(TlsPolicyCreator.class, PublicKeyDigestTlsPolicyCreator.class); // required for testPublicKeyDigestTlsPolicyV2
        Extensions.register(TlsPolicyCreator.class, InsecureTrustFirstPublicKeyTlsPolicyCreator.class); // required for testTrustFirstPublicKeyTlsPolicyV2
        Extensions.register(TlsPolicyCreator.class, CertificateDigestTlsPolicyCreator.class);
        Extensions.register(TlsPolicyCreator.class, CertificateTlsPolicyCreator.class);
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        properties.put("mtwilson.api.url", "https://192.168.0.1:8443/mtwilson/v2");

        // basic authentication
        properties.put("mtwilson.api.username", "admin");
        properties.put("mtwilson.api.password", "password");
        properties.put("mtwilson.api.tls.policy.certificate.sha256", "e05effdbdfc20e59977e840f6f1c47e9c0cc424642d87cdd895632dae33bc86f");
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void testUpdateHosts() throws Exception {

        String keystorePath = Folders.configuration() + File.separator + "keystore.jks";
        File keyStoreFile = new File(keystorePath);
        Properties properties = new Properties();

        properties.put("mtwilson.api.url", "https://192.168.0.1:8443/mtwilson/v2");
        // basic authentication
        properties.setProperty("mtwilson.api.username", "admin");
        properties.setProperty("mtwilson.api.password", "password");
        properties.setProperty("mtwilson.api.tls.policy.certificate.sha256", "ed0f7eb43c1ed7fc4f6e09cf80d2f7439a03fcf2068a26aac3d22c0c722c7f4e");
        //properties.setProperty("mtwilson.api.tls.policy.certificate.keystore.file", keyStoreFile.getAbsolutePath());
        //properties.setProperty("mtwilson.api.tls.policy.certificate.keystore.password", "password");

        Hosts client = new Hosts(properties);
        HostCreateCriteria host = new HostCreateCriteria();
        host.setHostName("RHEL-Host");
        host.setConnectionString("intel:https://192.168.0.1:1443;u=trustagentUsername;p=trustagentPassword");
        host.setFlavorgroupName("samplename123");
        host.setDescription("Host created.");
        HostFilterCriteria filterCriteria = new HostFilterCriteria();
        filterCriteria.filter = false;
        client.create(host);
    }
}
