/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.setup.tasks;

import com.intel.kms.setup.JettyTlsKeystore;
import com.intel.mtwilson.My;
import com.intel.mtwilson.MyConfiguration;

/**
 *
 * @author rawatar
 */
public class CreateTlsCertificate extends JettyTlsKeystore {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateTlsCertificate.class);

    @Override
    protected void configure() throws Exception {
        MyConfiguration config = My.configuration();

        username = config.getMtwilsonAdminUsername();
        if (username == null || username.isEmpty()) {
            configuration("Mtwilson api username is not set");
        }

        password = config.getMtwilsonAdminPassword();
        if (password == null || password.isEmpty()) {
            configuration("Mtwilson api password is not set");
        }

        super.configure();
    }

    @Override
    protected void validate() throws Exception {
        super.validate();
    }

    @Override
    protected void execute() throws Exception {
        super.execute();
    }
}
