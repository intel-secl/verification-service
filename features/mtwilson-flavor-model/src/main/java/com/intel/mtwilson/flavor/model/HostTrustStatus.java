/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.model;

public class HostTrustStatus {

    public TrustInformation trustStatus;

    public HostTrustStatus() {
    }

    public HostTrustStatus(TrustInformation trustStatus) {
        this.trustStatus = trustStatus;
    }

    public TrustInformation getTrustStatus() {
        return trustStatus;
    }

    public void setTrustStatus(TrustInformation trustStatus) {
        this.trustStatus = trustStatus;
    }

}
