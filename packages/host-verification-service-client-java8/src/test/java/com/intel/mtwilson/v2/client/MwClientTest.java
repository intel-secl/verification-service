/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.v2.client;


/**
 *
 * @author jbuhacoff
 */
public class MwClientTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MwClientTest.class);
    
    public static class FeatureA {
        public void doA() { log.debug("A"); }
    }
    public static class FeatureB {
        public void doB() { log.debug("B"); }
    }
}
