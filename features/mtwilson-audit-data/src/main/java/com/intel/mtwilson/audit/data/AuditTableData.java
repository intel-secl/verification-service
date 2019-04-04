/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.audit.data;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author dsmagadx
 */
public class AuditTableData {
    
    private List<AuditColumnData> columns = new ArrayList<AuditColumnData>();
    
    @JsonProperty("columns")
    public List<AuditColumnData> getColumns() {
        return columns;
    }
    
    @JsonProperty("columns")
    public void setColumns(List<AuditColumnData> columns) {
        this.columns = columns;
    }
    
}
