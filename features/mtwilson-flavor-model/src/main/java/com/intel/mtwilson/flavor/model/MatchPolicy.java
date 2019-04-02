/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.model;

import com.intel.mtwilson.jaxrs2.Document;

/**
 *
 * @author dtiwari
 */
public class MatchPolicy extends Document {
    
    public enum MatchType {
        ANY_OF,
        ALL_OF,
        LATEST;
    }
    
    public enum Required {
        REQUIRED,
        REQUIRED_IF_DEFINED;
    }
    
    private MatchType matchType;
    private Required required;
    
    public MatchPolicy() { }
    
    public MatchPolicy(MatchType matchType, Required required) {
        this.matchType = matchType;
        this.required = required;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public void setMatchType(MatchType matchType) {
        this.matchType = matchType;
    }

    public Required getRequired() {
        return required;
    }

    public void setRequired(Required required) {
        this.required = required;
    }
}
