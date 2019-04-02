///*
// * Copyright (C) 2019 Intel Corporation
// * SPDX-License-Identifier: BSD-3-Clause
// */
//package com.intel.mtwilson.setup.tasks;
//
//import com.intel.dcsg.cpg.validation.Fault;
//import java.util.List;
//import org.junit.Test;
//
///**
// *
// * @author jbuhacoff
// */
//public class HashUserPasswordsTest {
//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HashUserPasswordsTest.class);
//    
//    @Test
//    public void testBenchmark() throws Exception {
//        MigrateUsers task = new MigrateUsers();
//        task.configure();
//    }
//    @Test
//    public void testFindTables() throws Exception {
//        MigrateUsers task = new MigrateUsers();
//        task.setIterations(1);
//        task.configure();
//        List<Fault> faults = task.getConfigurationFaults();
//        for(Fault fault : faults) {
//            log.error("Configuration fault: {}", fault);
//        }
//    }
//}
