/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
/**
 * Author:  ddhawal
 * Created: Sept 09, 2018
 */

ALTER TABLE mw_flavor ADD COLUMN label char(255) NOT NULL UNIQUE;