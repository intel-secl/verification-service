/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.model;

import com.intel.dcsg.cpg.validation.Validator;
import com.intel.mtwilson.validators.ConnectionStringValidator;

import java.util.List;

/**
 *
 * @author srege
 */
public class FlavorCreateCriteria {
    private String connectionString;
    private String tlsPolicyId;
    private FlavorCollection flavorCollection;
    private String flavorgroupName;
    private List<String> partialFlavorTypes;
    
    @Validator(ConnectionStringValidator.class)
    public String getConnectionString() {
        return connectionString;
    }
    
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }
    
    public String getTlsPolicyId() {
        return tlsPolicyId;
    }
    
    public void setTlsPolicyId(String tlsPolicyId) {
        this.tlsPolicyId = tlsPolicyId;
    }
    
    public FlavorCollection getFlavorCollection() {
        return flavorCollection;
    }
    
    public void setFlavorCollection(FlavorCollection flavorCollection) {
        this.flavorCollection = flavorCollection;
    }
    
    public String getFlavorgroupName() {
        return flavorgroupName;
    }
    
    public void setFlavorgroupName(String flavorgroupName) {
        this.flavorgroupName = flavorgroupName;
    }
    
    public List<String> getPartialFlavorTypes() {
        return partialFlavorTypes;
    }
    
    public void setPartialFlavorTypes(List<String> partialFlavorTypes) {
        this.partialFlavorTypes = partialFlavorTypes;
    }
}
