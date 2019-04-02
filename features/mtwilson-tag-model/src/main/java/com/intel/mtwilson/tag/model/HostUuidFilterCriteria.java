/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.repository.FilterCriteria;
import com.intel.mtwilson.jaxrs2.DefaultFilterCriteria;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class HostUuidFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<HostUuid>{
    
    @QueryParam("id")
    public UUID id;
    @QueryParam("hostId")
    public String hostId;
    @QueryParam("hostNameEqualTo")
    public String hostNameEqualTo;
    
}
