/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.certificate.rest.v2.model;

import com.intel.mtwilson.repository.Locator;
import javax.ws.rs.PathParam;
import com.intel.dcsg.cpg.io.UUID;

/**
 *
 * @author ssbangal
 */
public class CaCertificateLocator implements Locator<CaCertificate>{
    @PathParam("id")
    public String id;

    @Override
    public void copyTo(CaCertificate item) {
        item.setId(UUID.valueOf(id));
    }
    
}
