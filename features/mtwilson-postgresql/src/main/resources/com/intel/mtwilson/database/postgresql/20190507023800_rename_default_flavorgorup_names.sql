/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
/**
 * Author:  hmgowda
 * Created: May 7, 2019
 */

UPDATE mw_flavorgroup SET name='automatic' WHERE name='mtwilson_automatic';
UPDATE mw_flavorgroup SET name='host_unique' WHERE name='mtwilson_unique';
UPDATE mw_flavorgroup SET name='platform_software' WHERE name='mtwilson_default_software';
