/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
/**
 * Author:  hmgowda
 * Created: Jul 11, 2018
 */
ALTER TABLE mw_audit_log_entry DROP data;
ALTER TABLE mw_audit_log_entry ADD COLUMN data json;

INSERT INTO changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20180711102000,NOW(),'Updated the data attribute datatype to json');