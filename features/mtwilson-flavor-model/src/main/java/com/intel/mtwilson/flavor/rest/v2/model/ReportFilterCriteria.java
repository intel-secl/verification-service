/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.Regex;
import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.mtwilson.repository.FilterCriteria;
import com.intel.mtwilson.jaxrs2.DefaultFilterCriteria;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class ReportFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<Report>{
    @QueryParam("id")
    public UUID id;
    @QueryParam("hostId")
    public String hostId;
    @QueryParam("hostName")
    public String hostName;
    @QueryParam("hostHardwareId")
    public String hostHardwareId;
    @QueryParam("hostStatus")
    public String hostStatus;
    @QueryParam("numberOfDays")
    public int numberOfDays;
    @QueryParam("fromDate")
    @Regex(RegexPatterns.ANY_VALUE)    
    public String fromDate;
    @QueryParam("toDate")
    @Regex(RegexPatterns.ANY_VALUE)
    public String toDate;
    @QueryParam("latestPerHost")
    public String latestPerHost;
}
