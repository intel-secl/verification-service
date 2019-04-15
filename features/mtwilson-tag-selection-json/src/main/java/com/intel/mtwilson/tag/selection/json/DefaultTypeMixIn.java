/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.selection.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.mtwilson.tag.selection.xml.SelectionType;
import java.util.List;

/**
 * The commented out block applies only to xml serialization; currently
 * only json serialization is used.
 *
 * @author jbuhacoff
 */
public abstract class DefaultTypeMixIn {

    @JsonProperty("selections")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    protected List<SelectionType> selection;
}
