/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.telemetry.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.telemetry.controller.MwTelemetryJpaController;
import com.intel.mtwilson.telemetry.data.MwTelemetry;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.intel.mtwilson.My;
import com.intel.mtwilson.telemetry.rest.v2.model.TelemetryCollection;
import com.intel.mtwilson.telemetry.rest.v2.model.TelemetryRecord;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hxia5
 */
public class TelemetryRepository {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TelemetryRepository.class);
    private static final int MAX_COUNT = 90;
    
    public TelemetryCollection retrieve() {

        log.debug("telemetry:report - got request to search for records");
        TelemetryCollection teleCollection = new TelemetryCollection();
        try {            
            MwTelemetryJpaController mwTelemetryJpaController = My.jpa().mwTelemetry();
            List<MwTelemetry> mwTeleList = mwTelemetryJpaController.findMwTelemetryEntities();
            if (mwTeleList != null && !mwTeleList.isEmpty()) {
                for (MwTelemetry mwTel : mwTeleList) {
                    teleCollection.getTelmetries().add(convert(mwTel));
                }
            }            
            log.debug("telemetry:retrieve - returning back {} results", teleCollection.getTelmetries().size());
        } catch (IOException ex) {
            Logger.getLogger(TelemetryRepository.class.getName()).log(Level.SEVERE, null, ex);
        }
        return teleCollection;
    }

    @RequiresPermissions("telemetry:create")
    public void create(int hostNum) {
        log.debug("telemetry:create - got request to create an new entry");        
        try {
            MwTelemetry mwentry = new MwTelemetry();
            mwentry.setId(new UUID().toString());
            mwentry.setCreateDate(new Date());
            mwentry.setHostNum(hostNum);            
            MwTelemetryJpaController mwTelemetryJpaController = My.jpa().mwTelemetry();
            
            /* only MAX_COUNT entries are saved */
            int count= mwTelemetryJpaController.getMwTelemetryCount();       
            if (count >= MAX_COUNT) {
                /* remove the oldest one */
                MwTelemetry mwOldest = null;
                List<MwTelemetry> mwTeleList = mwTelemetryJpaController.findMwTelemetryOldest();
                if (mwTeleList != null && !mwTeleList.isEmpty()) {
                    for (MwTelemetry mwTel : mwTeleList) {
                        mwOldest = mwTel;
                    }
                    if (mwOldest != null)
                        mwTelemetryJpaController.destroy(mwOldest.getId());
                }
            }            
            mwTelemetryJpaController.create(mwentry);
        } catch (IOException ex) {
            Logger.getLogger(TelemetryRepository.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(TelemetryRepository.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    /*
    @RequiresPermissions("telemetry:delete")
    public void delete(Integer id) {
        log.debug("Deleting entry with id: {}", id);
    }
    */

    private TelemetryRecord convert(MwTelemetry mwTel) {
        TelemetryRecord teleRecord = new TelemetryRecord();
        teleRecord.setHostNum(mwTel.getHostNum());
        teleRecord.setCreateDate(mwTel.getCreateDate());
        return teleRecord;
    }
}
