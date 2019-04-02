/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.features.queue.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.DefaultFilterCriteria;
import com.intel.mtwilson.repository.FilterCriteria;
import java.util.List;

/**
 *
 * @author hmgowda
 */
public class QueueFilterCriteria extends DefaultFilterCriteria implements FilterCriteria<Queue> {
    public UUID id;
    public String action;
    public String parameter;
    public String value;
    public List<String> queueStates;
}
