/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.features.queue.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.repository.Locator;

/**
 *
 * @author hmgowda
 */
public class QueueLocator implements Locator<Queue> {
    public UUID id;
    
    @Override
    public void copyTo(Queue item) {
        if (id != null) {
            item.setId(id);
        }
    }
}
