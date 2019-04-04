/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.selection.json;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The commented out block applies only to xml serialization; currently
 * only json serialization is used.
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class AttributeTypeMixIn {
}
