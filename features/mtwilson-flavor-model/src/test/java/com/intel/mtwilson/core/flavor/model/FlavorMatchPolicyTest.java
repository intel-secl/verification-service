/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.core.flavor.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.flavor.model.FlavorMatchPolicyCollection;
import com.intel.mtwilson.flavor.model.MatchPolicy;
import com.intel.mtwilson.flavor.model.MatchPolicy.MatchType;
import static com.intel.mtwilson.flavor.model.MatchPolicy.MatchType.ALL_OF;
import com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider;
import org.junit.Test;


/**
 * 
 * @author dtiwari
 */
public class FlavorMatchPolicyTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FlavorMatchPolicyTest.class);

    @Test
    public void testObjectMapping() throws Exception {
        ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        String matchPolicyAsString = Resources.toString(Resources.getResource("matchpolicy.json"), Charsets.UTF_8);
        log.info(matchPolicyAsString);
        MatchPolicy matchPolicy = mapper.readValue(matchPolicyAsString, MatchPolicy.class);
        if (matchPolicy == null){
            log.info("NULL");
        }
        log.info(matchPolicy.getRequired().name());
        
        String flavorMatchPolicyAsString = Resources.toString(Resources.getResource("flavormatchpolicy.json"), Charsets.UTF_8);
        FlavorMatchPolicyCollection flavorMatchPolicy = mapper.readValue(flavorMatchPolicyAsString, FlavorMatchPolicyCollection.class);
        if (flavorMatchPolicy == null){
            log.info("NULL");
        }
        log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(flavorMatchPolicy));
        
        for (FlavorPart flavorPart : flavorMatchPolicy.getFlavorPartsByMatchType(ALL_OF)){
            log.info(flavorPart.name());
        }
    }
}
