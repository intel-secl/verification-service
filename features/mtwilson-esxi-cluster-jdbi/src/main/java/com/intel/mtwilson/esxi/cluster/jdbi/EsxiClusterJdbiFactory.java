/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.esxi.cluster.jdbi;

import com.intel.mtwilson.jdbi.util.JdbiUtil;
import com.intel.mtwilson.My;

/**
 *
 * @author avaguayo
 */
public class EsxiClusterJdbiFactory {

    public static EsxiClusterDAO esxiClusterDAO() {
        try {
            return JdbiUtil.getDBI(My.jdbc().connection()).open(EsxiClusterDAO.class);
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
