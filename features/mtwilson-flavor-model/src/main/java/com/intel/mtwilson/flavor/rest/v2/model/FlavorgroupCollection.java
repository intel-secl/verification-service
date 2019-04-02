/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.intel.mtwilson.jaxrs2.DocumentCollection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rksavino
 */
public class FlavorgroupCollection extends DocumentCollection<Flavorgroup> {
    private final ArrayList<Flavorgroup> flavorgroups = new ArrayList<>();
    
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JacksonXmlElementWrapper(localName="flavorgroups")
    @JacksonXmlProperty(localName="flavorgroup")    
    public List<Flavorgroup> getFlavorgroups() { return flavorgroups; }
    
    @Override
    public List<Flavorgroup> getDocuments() {
        return getFlavorgroups();
    }
}
