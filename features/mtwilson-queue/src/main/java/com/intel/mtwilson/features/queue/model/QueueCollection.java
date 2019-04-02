/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.features.queue.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.intel.mtwilson.jaxrs2.DocumentCollection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hmgowda
 */
public class QueueCollection extends DocumentCollection<Queue> {
    private final ArrayList<Queue> queue = new ArrayList<>();
    
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JacksonXmlElementWrapper(localName = "queues")
    @JacksonXmlProperty(localName = "queue")
    public List<Queue> getQueueEntries() {
        return queue;
    }
    
    @Override
    public List<Queue> getDocuments() {
        return getQueueEntries();
    }
}
