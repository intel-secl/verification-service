/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
import org.junit.Test;
/**
 *
 * @author srege
 */
public class AuditHandlerClassTest {
    @Test 
public void testAuditHandler() throws ClassNotFoundException{
    Class t = Class.forName("com.intel.mtwilson.audit.handler.impl.AuditEventHandlerImpl");
    System.out.println(t.getName());
}
}
