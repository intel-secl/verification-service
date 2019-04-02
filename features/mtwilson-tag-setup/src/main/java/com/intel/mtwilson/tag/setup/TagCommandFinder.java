/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.setup;

import com.intel.dcsg.cpg.console.Command;
import com.intel.dcsg.cpg.console.CommandFinder;
import com.intel.dcsg.cpg.console.HyphenatedCommandFinder;
import java.util.HashMap;

/**
 *
 * @author jbuhacoff
 */
public class TagCommandFinder implements CommandFinder {
    private final HyphenatedCommandFinder finder;
    
    public TagCommandFinder() {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put("mtwilson", "MtWilson");
        finder = new HyphenatedCommandFinder("com.intel.mtwilson.tag.setup.cmd", map);
    }
    
    @Override
    public Command forName(String commandName) {
        return finder.forName(commandName);
    }
}
