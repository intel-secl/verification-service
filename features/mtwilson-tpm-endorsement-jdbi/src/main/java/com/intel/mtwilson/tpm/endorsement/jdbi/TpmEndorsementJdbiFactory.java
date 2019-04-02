/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tpm.endorsement.jdbi;

import com.intel.mtwilson.My;
import com.intel.mtwilson.jdbi.util.JdbiUtil;

/**
 *
 * @author jbuhacoff
 */
public class TpmEndorsementJdbiFactory {

    public static TpmEndorsementDAO tpmEndorsementDAO() {
        try {
            return JdbiUtil.getDBI(My.jdbc().connection()).open(TpmEndorsementDAO.class);
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
