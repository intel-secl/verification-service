/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor;

import com.intel.dcsg.cpg.jpa.PersistenceManager;
import com.intel.mtwilson.My;
import com.intel.mtwilson.MyPersistenceManager;

/**
 *
 * @author rksavino
 */
public class FlavorPersistenceManager extends PersistenceManager {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FlavorPersistenceManager.class);
    
    @Override
    public void configure() {
        try {
            addPersistenceUnit("FlavorDataPU", MyPersistenceManager.getFlavorDataJpaProperties(My.configuration()));
        } catch (Exception e) {
            log.error("Cannot add persistence unit: {}", e.toString(), e);
        }
    }
}