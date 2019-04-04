/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.DocumentCollection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="host_uuid_collection")
public class HostUuidCollection extends DocumentCollection<HostUuid> {
    private final ArrayList<HostUuid> hostUuids = new ArrayList<HostUuid>();
    
    @JsonSerialize(include=JsonSerialize.Inclusion.ALWAYS) // jackson 1.9
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="host_uuids")
    @JacksonXmlProperty(localName="host_uuid")    
    public List<HostUuid> getHostUuids() { return hostUuids; }

    @Override
    public List<HostUuid> getDocuments() {
        return getHostUuids();
    }
    
}
