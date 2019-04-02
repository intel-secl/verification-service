/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.telemetry.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.Document;
import java.util.Date;


/**
 *
 * @author hdxia
 */
@JacksonXmlRootElement(localName = "record")
public class TelemetryRecord extends Document{
    private Date createDate;
    private Integer hostNum;

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Integer getHostNum() {
        return hostNum;
    }

    public void setHostNum(Integer hostNum) {
        this.hostNum = hostNum;
    }
}
