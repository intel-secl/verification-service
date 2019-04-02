/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.flavor.model.FlavorMatchPolicy;
import com.intel.mtwilson.flavor.model.FlavorMatchPolicyCollection;
import com.intel.mtwilson.flavor.model.MatchPolicy;
import com.intel.mtwilson.jaxrs2.Document;
import java.util.List;

import static com.intel.mtwilson.core.flavor.common.FlavorPart.*;
import static com.intel.mtwilson.flavor.model.MatchPolicy.MatchType.ALL_OF;
import static com.intel.mtwilson.flavor.model.MatchPolicy.MatchType.ANY_OF;
import static com.intel.mtwilson.flavor.model.MatchPolicy.MatchType.LATEST;
import static com.intel.mtwilson.flavor.model.MatchPolicy.Required.REQUIRED;
import static com.intel.mtwilson.flavor.model.MatchPolicy.Required.REQUIRED_IF_DEFINED;

/**
 *
 * @author rksavino
 */
@JacksonXmlRootElement(localName="flavorgroup")
public class Flavorgroup extends Document {
    public static final String AUTOMATIC_FLAVORGROUP = "mtwilson_automatic";
    public static final String UNIQUE_FLAVORGROUP = "mtwilson_unique";
    public static final String DEFAULT_SOFTWARE_FLAVORGROUP = "mtwilson_default_software";

    private String name;
    private FlavorMatchPolicyCollection flavorMatchPolicyCollection;
    private List<UUID> flavorIds;
    private List<Flavor> flavors;
    
    public List<Flavor> getFlavors() {
        return flavors;
    }

    public void setFlavors(List<Flavor> flavors) {
        this.flavors = flavors;
    }

    public List<UUID> getFlavorIds() {
        return flavorIds;
    }

    public void setFlavorIds(List<UUID> flavorIds) {
        this.flavorIds = flavorIds;
    }

    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public FlavorMatchPolicyCollection getFlavorMatchPolicyCollection() {
        return flavorMatchPolicyCollection;
    }
    
    public void setFlavorMatchPolicyCollection(FlavorMatchPolicyCollection flavorMatchPolicyCollection) {
        this.flavorMatchPolicyCollection = flavorMatchPolicyCollection;
    }

    public static FlavorMatchPolicyCollection getAutomaticFlavorMatchPolicy() {
        FlavorMatchPolicyCollection policy = new FlavorMatchPolicyCollection();
        policy.addFlavorMatchPolicy(new FlavorMatchPolicy(PLATFORM, new MatchPolicy(ANY_OF, REQUIRED)));
        policy.addFlavorMatchPolicy(new FlavorMatchPolicy(OS, new MatchPolicy(ANY_OF, REQUIRED)));
        policy.addFlavorMatchPolicy(new FlavorMatchPolicy(SOFTWARE, new MatchPolicy(ALL_OF, REQUIRED_IF_DEFINED)));
        policy.addFlavorMatchPolicy(new FlavorMatchPolicy(ASSET_TAG, new MatchPolicy(LATEST, REQUIRED_IF_DEFINED)));
        policy.addFlavorMatchPolicy(new FlavorMatchPolicy(HOST_UNIQUE, new MatchPolicy(LATEST, REQUIRED_IF_DEFINED)));
        return policy;
    }

    public static FlavorMatchPolicyCollection getIseclSoftwareFlavorMatchPolicy() {
        FlavorMatchPolicyCollection policy = new FlavorMatchPolicyCollection();
        policy.addFlavorMatchPolicy(new FlavorMatchPolicy(SOFTWARE, new MatchPolicy(ANY_OF, REQUIRED)));
        return policy;
    }

}
