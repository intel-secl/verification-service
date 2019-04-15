/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.selection.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.mtwilson.tag.selection.xml.AttributeType;
import com.intel.mtwilson.tag.selection.xml.SubjectType;
import java.util.List;

/**
 * The commented out block applies only to xml serialization; currently
 * only json serialization is used.
 *
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class SelectionTypeMixIn {
    @JsonProperty("subjects")
    protected List<SubjectType> subject;
    
    @JsonProperty("attributes")
    protected List<AttributeType> attribute;
}
