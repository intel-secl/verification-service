/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.esxi.host.jdbi;

import com.intel.mtwilson.My;
import com.intel.mtwilson.jdbi.util.JdbiUtil;

/**
 *
 * @author avaguayo
 */
public class EsxiHostJdbiFactory {
    
    public static EsxiHostDAO esxiHostDAO() {
        try {
            return JdbiUtil.getDBI(My.jdbc().connection()).open(EsxiHostDAO.class);
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
