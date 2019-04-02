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
public class HostUuidLocator implements Locator<HostUuid> {

    @PathParam("id")
    public UUID id;

    @Override
    public void copyTo(HostUuid item) {
        if (id != null) {
            item.setId(id);
        }
    }
    
}
