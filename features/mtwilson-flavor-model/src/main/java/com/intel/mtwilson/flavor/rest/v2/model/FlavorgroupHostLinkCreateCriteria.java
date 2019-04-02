/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 *
 * @author hmgowda
 */
public class FlavorgroupHostLinkCreateCriteria {

   private String flavorgroupName;

   @JsonProperty("flavorgroupName")
   @JacksonXmlProperty(isAttribute = true, localName="flavorgroupName")
   public String getFlavorgroupName() {
        return flavorgroupName;
    }

    public void setFlavorgroupName(String flavorgroupName) {
        this.flavorgroupName = flavorgroupName;
    }
}
