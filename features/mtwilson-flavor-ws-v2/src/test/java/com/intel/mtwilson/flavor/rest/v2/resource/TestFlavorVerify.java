/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.resource;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.mtwilson.core.flavor.PlatformFlavor;
import com.intel.mtwilson.core.flavor.PlatformFlavorFactory;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.core.verifier.policy.TrustMarker;
import com.intel.mtwilson.core.verifier.policy.TrustReport;
import com.intel.mtwilson.flavor.business.policy.rule.RuleAllOfFlavors;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorCollection;
import com.intel.mtwilson.jackson.bouncycastle.BouncyCastleModule;
import com.intel.mtwilson.jackson.validation.ValidationModule;
import com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider;
import com.intel.mtwilson.core.common.model.HostManifest;
import com.intel.mtwilson.core.common.tag.model.X509AttributeCertificate;
import java.io.File;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author dtiwari
 */
public class TestFlavorVerify {
    
    ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
    File tempPrivacyCA;
    File temptagCA;
    String hostManifestwithTagCertificateAsJson;
    TrustReport combinedTrustReport, individualTrustReport;
    PlatformFlavor platformFlavor;
    ArrayList<Flavor> flavors = new ArrayList();

    @BeforeClass
    public static void registerJacksonModules() {
        Extensions.register(Module.class, BouncyCastleModule.class);
        Extensions.register(Module.class, ValidationModule.class);
    }
    
    @Before
    public void setUp() throws Exception {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        String combinedTrustReportInString = Resources.toString(Resources.getResource("combined_trustreport.json"), Charsets.UTF_8);
        //String individualTrustReportInString = Resources.toString(Resources.getResource(pathPrefix + "/individual_trustreport.json"), Charsets.UTF_8);

        combinedTrustReport = mapper.readValue(combinedTrustReportInString, TrustReport.class);
        //individualTrustReport = mapper.readValue(individualTrustReportInString, TrustReport.class);
        
        String hostManifestAsJson = Resources.toString(Resources.getResource("hostmanifest.json"), Charsets.UTF_8);
        String tagCerAsJson = Resources.toString(Resources.getResource("tagcer.json"), Charsets.UTF_8);

        X509AttributeCertificate tagCer = mapper.readValue(tagCerAsJson, X509AttributeCertificate.class);
        HostManifest hostManifest = mapper.readValue(hostManifestAsJson, HostManifest.class);
        hostManifest.setTagCertificate(tagCer);
        hostManifestwithTagCertificateAsJson = mapper.writeValueAsString(hostManifest);

        PlatformFlavorFactory factory = new PlatformFlavorFactory();
        //TODO: add code to retrieve tag certificate from mw_tag_certificate
        PlatformFlavor platformFlavor = factory.getPlatformFlavor(hostManifest, null);
        for (String flavorPart : platformFlavor.getFlavorPartNames()){
            Flavor flavor = mapper.readValue(platformFlavor.getFlavorPart(flavorPart), Flavor.class);
            flavors.add(flavor);  
            System.out.println(mapper.writeValueAsString(flavor));
        }

    }
    
    @Test
    public void testCombinedTrustReportResults() throws Exception {
        FlavorCollection flavorCollection = new FlavorCollection();
        flavorCollection.setFlavors(flavors);
        RuleAllOfFlavors rule = new RuleAllOfFlavors(flavorCollection, "", "");
        String[] marker = {TrustMarker.PLATFORM.name()};
        rule.setMarkers(marker);
        combinedTrustReport = rule.addFaults(combinedTrustReport);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(combinedTrustReport));
    }
    
    
}
