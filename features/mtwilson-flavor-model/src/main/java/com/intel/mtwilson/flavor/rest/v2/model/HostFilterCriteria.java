/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.Validator;
import com.intel.mtwilson.jaxrs2.DefaultFilterCriteria;
import com.intel.mtwilson.repository.FilterCriteria;
import com.intel.mtwilson.validators.ConnectionStringValidator;
import javax.ws.rs.QueryParam;

/**
 *
 * @author hmgowda
 * @author purvades
 */
public class HostFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<Host> {
    @QueryParam("id")
    public UUID id;
    @QueryParam("nameEqualTo")
    public String nameEqualTo;
    @QueryParam("nameContains")
    public String nameContains;
    @QueryParam("hostHardwareId")
    public UUID hostHardwareId;
    @QueryParam("key")
    public String key;
    @QueryParam("value")
    public String value;
    @QueryParam("aikCertificate")
    public String aikCertificate;
    @QueryParam("description")
    public String description;
    @Validator(ConnectionStringValidator.class)
    @QueryParam("connectionString")
    public String connectionString;
    @QueryParam("flavorgroupName")
    public String flavorgroupName;
    @QueryParam("tlsPolicyId")
    public String tlsPolicyId;
}
