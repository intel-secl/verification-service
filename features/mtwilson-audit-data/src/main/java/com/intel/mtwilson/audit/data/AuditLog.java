/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.audit.data;

import java.util.Date;

/**
 *
 * @author dsmagadx
 */
public class AuditLog {

    private String id;
    private String entityId;
    private String entityType;
    private Integer apiClientId;
    private String action;
    private AuditTableData data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getApiClientId() {
        return apiClientId;
    }

    public void setApiClientId(Integer apiClientId) {
        this.apiClientId = apiClientId;
    }

    public AuditTableData getData() {
        return data;
    }

    public void setData(AuditTableData data) {
        this.data = data;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

}
