/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.version.cmd;

import com.intel.dcsg.cpg.console.Command;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author jbuhacoff
 */
public class Version implements Command {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Version.class);
    @Override
    public void setOptions(Configuration options) {
        // no options are needed for version command
    }

    @Override
    public void execute(String[] args) throws Exception {
        com.intel.mtwilson.Version version = com.intel.mtwilson.Version.getInstance();
        System.out.println(String.format("Version %s", version.getVersion()));
        System.out.println(String.format("Build %s at %s", version.getBranch(), version.getTimestamp()));
    }
    
}
