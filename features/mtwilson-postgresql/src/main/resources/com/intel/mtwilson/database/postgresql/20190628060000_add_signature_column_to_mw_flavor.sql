/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
/**
 * Author:  arijitgh
 * Created: June 28, 2019
 */

ALTER TABLE mw_flavor ADD COLUMN signature text UNIQUE;
CREATE INDEX idx_mw_flavor_signature ON mw_flavor (signature);