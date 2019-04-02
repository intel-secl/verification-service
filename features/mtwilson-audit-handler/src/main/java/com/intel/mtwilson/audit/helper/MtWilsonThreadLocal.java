/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.audit.helper;

import com.intel.mtwilson.audit.data.AuditContext;

/**
 *
 * @author dsmagadx
 */
public class MtWilsonThreadLocal {

    private static InheritableThreadLocal contextThreadLocal = new InheritableThreadLocal();

    public static void set(AuditContext context) {
        contextThreadLocal.set(context);
    }

    public static void unset() {
        contextThreadLocal.remove();
    }

    public static AuditContext get() {
        return (AuditContext) contextThreadLocal.get();
    }
}
