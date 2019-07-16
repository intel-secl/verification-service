/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;

public class FlavorFlavorgroupLinkCreateCriteria {
    private UUID flavorId;

    public UUID getFlavorId() {
        return flavorId;
    }

    public void setFlavorId(UUID flavorId) {
        this.flavorId = flavorId;
    }
}
