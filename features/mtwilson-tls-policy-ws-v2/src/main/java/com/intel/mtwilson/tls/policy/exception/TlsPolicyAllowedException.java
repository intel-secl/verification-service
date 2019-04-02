/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.tls.policy.exception;

/**
 *
 * @author hxia5
 */
public class TlsPolicyAllowedException extends RuntimeException {
    public TlsPolicyAllowedException() {
        super();
    }
    
    public TlsPolicyAllowedException(String message) {
        super(message);
    }
    
    public TlsPolicyAllowedException(Throwable cause) {
        super(cause);
    }
    
    public TlsPolicyAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}
