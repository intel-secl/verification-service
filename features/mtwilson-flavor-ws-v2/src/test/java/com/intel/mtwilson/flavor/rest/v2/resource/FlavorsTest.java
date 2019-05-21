/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.resource;

import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.CertificateDigestTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.PublicKeyDigestTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.PublicKeyTlsPolicy;
import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.core.host.connector.HostConnector;
import com.intel.mtwilson.core.host.connector.HostConnectorFactory;
import com.intel.mtwilson.flavor.data.MwHost;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorCollection;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorRepository;
import com.intel.mtwilson.core.common.model.HostManifest;
import java.io.IOException;
import org.junit.Test;

import static com.intel.mtwilson.core.flavor.common.FlavorPart.*;

/**
 *
 * @author ssbangal
 */
public class FlavorsTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FlavorsTest.class);
    
    //@Test
    public void testSearchForFlavor() throws Exception {
        FlavorFilterCriteria searchCriteria = new FlavorFilterCriteria();
        searchCriteria.key = "flavor_part";
        searchCriteria.value = PLATFORM.getValue();
        
        FlavorRepository repository = new FlavorRepository();
        FlavorCollection objCollection = repository.search(searchCriteria);
        for (Flavor obj: objCollection.getFlavors()) {
            log.info(obj.getMeta().getDescription().toString());
        }
    }
    
    private void registerTlsPolicyClasses() throws Exception {
        Extensions.register(TlsPolicy.class, CertificateDigestTlsPolicy.class);
        Extensions.register(TlsPolicy.class, InsecureTlsPolicy.class);
        Extensions.register(TlsPolicy.class, PublicKeyDigestTlsPolicy.class);
        Extensions.register(TlsPolicy.class, PublicKeyTlsPolicy.class);
    }
    
    @Test
    public void test() throws IOException, Exception {
        registerTlsPolicyClasses();
        String connectionString = "intel:https://192.168.0.1:1443/;tagent-admin;password";
        String hostName = "N16RU09";
        
        MwHost mwHost = new MwHost();
        mwHost.setConnectionString(connectionString);
        mwHost.setDescription("test");
        mwHost.setHardwareUuid(new UUID().toString());
        mwHost.setId(new UUID().toString());
        mwHost.setName(hostName);
        mwHost.setTlsPolicyId("TRUST_FIRST_CERTIFICATE");
        
        TlsPolicy tlsPolicy = new InsecureTlsPolicy();
        
        HostConnectorFactory hostConnectorFactory = new HostConnectorFactory();
        HostConnector hostConnector = hostConnectorFactory.getHostConnector(connectionString, tlsPolicy);
        HostManifest hostManifest = hostConnector.getHostManifest();
    }
}
