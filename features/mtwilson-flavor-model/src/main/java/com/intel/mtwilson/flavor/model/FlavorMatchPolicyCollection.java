/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.flavor.model.MatchPolicy.MatchType;
import com.intel.mtwilson.flavor.model.MatchPolicy.Required;
import com.intel.mtwilson.jaxrs2.DocumentCollection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dtiwari
 */
@JacksonXmlRootElement(localName="flavormatchpolicies_collection")
public class FlavorMatchPolicyCollection extends DocumentCollection<FlavorMatchPolicy> {

    private final List<FlavorMatchPolicy> flavorMatchPolicies = new ArrayList<>();
    
    @JsonSerialize(include=JsonSerialize.Inclusion.ALWAYS) // jackson 1.9
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="flavormatchpolicies")
    @JacksonXmlProperty(localName="flavormatchpolicy")    
    public List<FlavorMatchPolicy> getFlavorMatchPolicies() { return flavorMatchPolicies; }

    @Override
    public List<FlavorMatchPolicy> getDocuments() {
        return getFlavorMatchPolicies();
    }
    
    public void addFlavorMatchPolicy(FlavorMatchPolicy flavorMatchPolicy) {
        flavorMatchPolicies.add(flavorMatchPolicy);
    }
    
    public MatchPolicy getmatchPolicy(FlavorPart part){
        for (FlavorMatchPolicy flavorMatchPolicy : flavorMatchPolicies){
            if (flavorMatchPolicy.getFlavorPart().equals(part)){
                return flavorMatchPolicy.getMatchPolicy();
            }
        }
        return null;
    }
    
    public List<FlavorPart> getFlavorPartsByMatchType(MatchType matchType){
        List<FlavorPart> flavorPart = new ArrayList<>();
        for (FlavorMatchPolicy flavorMatchPolicy : getFlavorMatchPolicies()){
            if (flavorMatchPolicy.getMatchPolicy().getMatchType().equals(matchType)){
                flavorPart.add(flavorMatchPolicy.getFlavorPart());
            }
        }
        return flavorPart;
    }
    
    public List<FlavorPart> getFlavorPartsByRequired(Required required){
        List<FlavorPart> flavorPart = new ArrayList<>();
        for (FlavorMatchPolicy flavorMatchPolicy : getFlavorMatchPolicies()){
            if (flavorMatchPolicy.getMatchPolicy().getRequired().equals(required)){
                flavorPart.add(flavorMatchPolicy.getFlavorPart());
            }
        }
        return flavorPart;
    }
}
