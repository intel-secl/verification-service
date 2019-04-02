/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.certificate.rest.v2.model;

import com.intel.mtwilson.repository.FilterCriteria;
import com.intel.mtwilson.jaxrs2.DefaultFilterCriteria;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class CaCertificateFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<CaCertificate>{
    
    /**
     * Domain over which the issuer has authority.
     * Possible values are "tls", "aik", "ek", or "saml".
     * 
     */
    @QueryParam("domain")
    public String domain;
}
