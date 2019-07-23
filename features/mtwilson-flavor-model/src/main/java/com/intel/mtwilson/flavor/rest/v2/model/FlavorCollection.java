/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.jaxrs2.DocumentCollection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author srege
 */
public class FlavorCollection extends DocumentCollection<Flavor> {
    private ArrayList<Flavor> flavors = new ArrayList<>();

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JacksonXmlElementWrapper(localName="flavors")
    @JacksonXmlProperty(localName="flavor")
    public List<Flavor> getFlavors() { return flavors; }

    public void setFlavors(ArrayList<Flavor> flavors){
        this.flavors = flavors;
    }

    @Override
    public List<Flavor> getDocuments() {
        return getFlavors();
    }
}
