/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.setup.cmd;

import com.intel.mtwilson.tag.setup.TagCommand;

/**
 *
 * @author jbuhacoff
 */
public class TagVersion extends TagCommand {

    @Override
    public void execute(String[] args) throws Exception {
        System.out.println("Provisioning Service version 0.1");
    }
    
}
