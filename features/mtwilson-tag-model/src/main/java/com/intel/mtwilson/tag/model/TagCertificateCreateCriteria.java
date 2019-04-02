/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tag.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.supplemental.asset.tag.model.TagKvAttribute;
import java.util.ArrayList;

/**
 *
 * @author dtiwari
 */
public class TagCertificateCreateCriteria {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Certificate.class);

    public UUID hardwareUuid;
    public UUID selectionId;
    public String selectionName;
    public ArrayList<TagKvAttribute> selectionContent;

    public UUID getHardwareUuid() {
        return hardwareUuid;
    }

    public void setHardwareUuid(UUID hardwareUuid) {
        this.hardwareUuid = hardwareUuid;
    }

    public UUID getSelectionId() {
        return selectionId;
    }

    public void setSelectionId(UUID selectionId) {
        this.selectionId = selectionId;
    }

    public String getSelectionName() {
        return selectionName;
    }

    public void setSelectionName(String selectionName) {
        this.selectionName = selectionName;
    }

    public ArrayList<TagKvAttribute> getSelectionContent() {
        return selectionContent;
    }

    public void setSelectionContent(ArrayList<TagKvAttribute> selectionContent) {
        this.selectionContent = selectionContent;
    }
   
}
