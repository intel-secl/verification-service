/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.tls.policy.filter;

import com.intel.mtwilson.My;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author hdxia
 */
public class HostTlsPolicyFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostTlsPolicyFilter.class);
    // default list in case mtwilson.tls.policy.allow is not configured
    private static final String[] DEFAULT_ALLOWED = {"certificate", "certificate-digest", "public-key", "public-key-digest"};
    private static final String TRUST_FIRST_CERTIFICATE = "TRUST_FIRST_CERTIFICATE";

    public HostTlsPolicyFilter() {
    }
    
    public static boolean isTlsPolicyAllowed(String tlsPolicyId) {
        String[] tlsAllowed = My.configuration().getConfiguration().getStringArray("mtwilson.tls.policy.allow");
        if (tlsAllowed == null || tlsAllowed.length == 0)
                tlsAllowed = DEFAULT_ALLOWED;
        
        //for debugging purpose
        for (String item : tlsAllowed) {
            log.trace("Allowed tls policy in configuration: {}", item);
        }
        
        List<String> tlsAllowedList = Arrays.asList(tlsAllowed);        
        return tlsAllowedList.contains(tlsPolicyId);
    }
    
    public static String getDefaultTlsPolicyType() {
        String tlsPolicyDefault = My.configuration().getConfiguration().getString("mtwilson.default.tls.policy.id");       
        // set to TRUST_FIRST_CERTIFICATE in case the default is not set in the configuration          
        if (tlsPolicyDefault == null || tlsPolicyDefault.isEmpty()) {
            tlsPolicyDefault = TRUST_FIRST_CERTIFICATE;
        }
        return tlsPolicyDefault;
    }
}
