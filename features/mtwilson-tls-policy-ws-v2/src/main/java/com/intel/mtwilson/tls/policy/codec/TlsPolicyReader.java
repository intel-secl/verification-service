/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tls.policy.codec;

import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;

/**
 *
 * @author jbuhacoff
 */
public interface TlsPolicyReader {
    /**
     * 
     * @param contentType for example "application/java-keystore" or "application/json"
     * @return true if the reader can read that content type
     */
    boolean accept(String contentType);
    TlsPolicyDescriptor read(byte[] content);
}
