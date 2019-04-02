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
public class SelectionLocator implements Locator<Selection> {

    @PathParam("id")
    public UUID id;

    @Override
    public void copyTo(Selection item) {
        item.setId(id);
    }
    
}
