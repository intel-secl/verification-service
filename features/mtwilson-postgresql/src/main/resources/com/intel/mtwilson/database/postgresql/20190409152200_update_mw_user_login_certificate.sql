/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
/**
 * Created: Apr 09, 2019
 */
ALTER TABLE mw_user_login_certificate ADD COLUMN sha384_hash bytea;

INSERT INTO changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20190409152200,NOW(),'Added a column for sha384 digest algorithm in mw_user_login_certificate table');
