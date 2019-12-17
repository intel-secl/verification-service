/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
ALTER TABLE mw_tag_certificate ALTER COLUMN id TYPE CHAR(36);
ALTER TABLE mw_tag_certificate ALTER COLUMN hardware_uuid TYPE CHAR(36);
