/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.jaxrs2.DefaultFilterCriteria;
import com.intel.mtwilson.core.common.model.HostManifest;
import com.intel.mtwilson.repository.FilterCriteria;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.QueryParam;

/**
 *
 * @author srege
 */
public class FlavorFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<Flavor> {
    @QueryParam("id")
    public UUID id;
    @QueryParam("key")
    public String key;
    @QueryParam("value")
    public String value;
    @QueryParam("flavorgroupId")
    public UUID flavorgroupId;
    @QueryParam("flavorParts")
    public List<FlavorPart> flavorParts;
    public HostManifest hostManifest;
    public HashMap<String, Boolean> flavorPartsWithLatest;
}
