/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.model;

import com.intel.mtwilson.core.flavor.common.FlavorPart;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rksavino
 */
public class FlavorTrustReportCollection {

    private List<FlavorTrustReport> flavorTrustReportList = new ArrayList();
    
    public List<FlavorTrustReport> getFlavorTrustReportList() {
        return flavorTrustReportList;
    }
    
    public void setFlavorTrustReportList(List<FlavorTrustReport> flavorTrustReportList) {
        this.flavorTrustReportList = flavorTrustReportList;
    }
    
    public List<FlavorPart> getFlavorParts() {
        List<FlavorPart> flavorPartList = new ArrayList();
        for (FlavorTrustReport flavorTrustReport : flavorTrustReportList) {
            flavorPartList.add(flavorTrustReport.getFlavorPart());
        }
        return flavorPartList;
    }
    
    public List<FlavorTrustReport> getFlavorTrustReports(FlavorPart flavorPart) {
        List<FlavorTrustReport> flavorPartReports = new ArrayList();
        for (FlavorTrustReport flavorTrustReport : flavorTrustReportList) {
            if (flavorTrustReport.getFlavorPart() == flavorPart) {
                flavorPartReports.add(flavorTrustReport);
            }
        }
        return flavorPartReports;
    }
}
