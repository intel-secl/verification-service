/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.repository.Locator;
import javax.ws.rs.PathParam;

/**
 *
 * @author ssbangal
 */
public class ConfigurationLocator implements Locator<Configuration> {

    @PathParam("id")
    public UUID id;

    @Override
    public void copyTo(Configuration item) {
        if (id != null) {
            item.setId(id);
        }
    }
    
}
