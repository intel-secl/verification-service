/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson;

import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class MyConfigurationTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MyConfigurationTest.class);

    @Test
    public void testConfigurationUpdates() throws Exception {
        String sslrequired = My.configuration().getConfiguration().getString("mtwilson.ssl.required");
        log.debug("ssl required? {}", sslrequired);
        
        My.configuration().update("mtwilson.ssl.required", "true");
    }
}
