/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.features.queue.model;

/**
 *
 * @author rksavino
 */
public enum QueueState {
    NEW,
    PENDING,
    COMPLETED,
    RETURNED,
    TIMEOUT,
    CONNECTION_FAILURE,
    ERROR;
}
