/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.DefaultFilterCriteria;
import com.intel.mtwilson.repository.FilterCriteria;
import javax.ws.rs.QueryParam;

/**
 *
 * @author rksavino
 */
public class FlavorgroupHostLinkFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<FlavorgroupHostLink> {
    @QueryParam("id")
    public UUID id;
    @QueryParam("flavorgroupId")
    public UUID flavorgroupId;
    @QueryParam("hostId")
    public UUID hostId;
}
