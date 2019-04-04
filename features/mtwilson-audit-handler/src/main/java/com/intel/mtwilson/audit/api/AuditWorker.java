/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.audit.api;

import com.intel.mtwilson.audit.data.AuditLogEntry;
import com.intel.mtwilson.audit.helper.AuditHandlerException;

/**
 *
 * @author dsmagadx
 */
public interface AuditWorker {
    public void addLog(AuditLogEntry log) throws AuditHandlerException;
}
