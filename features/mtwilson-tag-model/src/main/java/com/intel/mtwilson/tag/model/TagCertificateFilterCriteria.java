/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.model;

import com.intel.mtwilson.core.common.tag.model.TagCertificate;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.repository.FilterCriteria;
import com.intel.mtwilson.jaxrs2.DefaultFilterCriteria;
import java.util.Date;
import javax.ws.rs.QueryParam;
import com.intel.dcsg.cpg.validation.Regex;
import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.dcsg.cpg.validation.Unchecked;


/**
 *
 * @author ssbangal
 */
public class TagCertificateFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<TagCertificate>{

    @QueryParam("id")
    public UUID id;
    @QueryParam("subjectEqualTo")
    public String subjectEqualTo;
    @QueryParam("subjectContains")
    public String subjectContains;
    @QueryParam("issuerEqualTo")
    @Regex(RegexPatterns.ANY_VALUE)    
    public String issuerEqualTo;
    @QueryParam("issuerContains")
    @Regex(RegexPatterns.ANY_VALUE)    
    public String issuerContains;
    @QueryParam("statusEqualTo")
    public String statusEqualTo;
    @QueryParam("validOn")
    public Date validOn;
    @QueryParam("validBefore")
    public Date validBefore;
    @QueryParam("validAfter")
    public Date validAfter;
    @QueryParam("hardwareUuid")
    public UUID hardwareUuid;
}
