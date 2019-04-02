/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.business;

/**
 *
 * @author rksavino
 */
public class FlavorVerifyException extends RuntimeException {
    public FlavorVerifyException() {
        super();
    }
    
    public FlavorVerifyException(String message) {
        super(message);
    }
    
    public FlavorVerifyException(Throwable cause) {
        super(cause);
    }
    
    public FlavorVerifyException(String message, Throwable cause) {
        super(message, cause);
    }
}
