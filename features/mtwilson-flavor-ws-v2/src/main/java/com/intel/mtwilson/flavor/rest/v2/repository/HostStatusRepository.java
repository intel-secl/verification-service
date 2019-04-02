/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.iso8601.Iso8601Date;
import com.intel.mtwilson.My;
import com.intel.mtwilson.flavor.controller.MwHostStatusJpaController;
import com.intel.mtwilson.flavor.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.flavor.data.MwHostStatus;
import com.intel.mtwilson.flavor.rest.v2.model.HostStatus;
import com.intel.mtwilson.flavor.rest.v2.model.HostStatusCollection;
import com.intel.mtwilson.flavor.rest.v2.model.HostStatusFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.HostStatusLocator;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 * @author hmgowda
 */
public class HostStatusRepository {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostStatusRepository.class);

    public HostStatusCollection search(HostStatusFilterCriteria criteria) {
        log.debug("Got request to search for host status");
        HostStatusCollection objCollection = new HostStatusCollection();
        try {
            // If user specifies no criteria, an empty result will be returned to prevent an unintentional process intensive query.  
            if (criteria.filter
                    && criteria.hostId == null && criteria.id == null && criteria.hostHardwareId == null
                    && (criteria.hostName == null || criteria.hostName.isEmpty())
                    && (criteria.hostStatus == null || criteria.hostStatus.isEmpty())
                    && (criteria.numberOfDays == 0)
                    && (criteria.toDate == null || criteria.toDate.isEmpty())
                    && (criteria.fromDate == null || criteria.fromDate.isEmpty())
                    && (criteria.aikCertificate == null || criteria.aikCertificate.isEmpty())
                    && (criteria.latestPerHost == null || criteria.latestPerHost.isEmpty())
                    && (criteria.limit == 10000)) {
                return objCollection; //Empty
            }
           
            MwHostStatusJpaController hostStatusJpaController = My.jpa().mwHostStatus();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();

            Date toDate = null, fromDate = null;
            if (criteria.toDate != null && !criteria.toDate.isEmpty()) {
                Iso8601Date toIso8601Date = Iso8601Date.valueOf(criteria.toDate);
                cal.setTime(toIso8601Date);
                toDate = dateFormat.parse(dateFormat.format(cal.getTime()));
            }
            if (criteria.fromDate != null && !criteria.fromDate.isEmpty()) {
                Iso8601Date fromIso8601Date = Iso8601Date.valueOf(criteria.fromDate);
                cal.setTime(fromIso8601Date); // This would set the time to ex:2015-05-30 00:00:00
                fromDate = dateFormat.parse(dateFormat.format(cal.getTime()));
            }
            String hostStatusId = null;
            String hostId = null;
            String hostName = null;
            String hostHardwareUuid = null;
            String aikCertificate = null;
            String hostStatus = null;
            Boolean latestPerHost = true;
            
            if (criteria.id != null){
                hostStatusId = criteria.id.toString();
            }
            if (criteria.hostId != null){
                hostId = criteria.hostId.toString();
            }
            if (criteria.hostName != null && !criteria.hostName.isEmpty()){
                hostName = criteria.hostName;
            }
            if (criteria.hostHardwareId != null){
                hostHardwareUuid = criteria.hostHardwareId.toString();
            }
            if (criteria.aikCertificate != null && !criteria.aikCertificate.isEmpty()){
                aikCertificate = criteria.aikCertificate;
            }
            if (criteria.hostStatus != null && !criteria.hostStatus.isEmpty()){
                hostStatus = criteria.hostStatus;
            }
            if (criteria.latestPerHost != null && !criteria.latestPerHost.isEmpty()) {
                latestPerHost = Boolean.valueOf(criteria.latestPerHost);
            }
            
            List<MwHostStatus> hostStatusList;
            
           //set from date and to date parameters fromthe number of days
            if (criteria.numberOfDays != 0) {
                log.debug("Number of days criteria is specified with value: {}", criteria.numberOfDays);
                // calculate from and to dates
                toDate = new Date(); // Get the current date and time
                cal.setTime(toDate);
                toDate = dateFormat.parse(dateFormat.format(cal.getTime()));
                // To get the fromDate, we substract the number of days fromm the current date.
                cal.add(Calendar.DATE, -(criteria.numberOfDays));
                fromDate = dateFormat.parse(dateFormat.format(cal.getTime()));
                log.debug("Host status between {} to {} will be retrieved", dateFormat.format(fromDate), dateFormat.format(toDate));
            }

            if (fromDate == null && toDate == null && latestPerHost) {
                hostStatusList = hostStatusJpaController.findLatestMwHostStatus(
                        hostStatusId,
                        hostId,
                        hostName,
                        hostHardwareUuid,
                        aikCertificate,
                        hostStatus, 
                        criteria.limit);
            } else {
                hostStatusList = hostStatusJpaController.findMwHostStatus(
                        hostStatusId,
                        hostId,
                        hostName,
                        hostHardwareUuid,
                        aikCertificate,
                        hostStatus, 
                        fromDate,
                        toDate,
                        latestPerHost,
                        criteria.limit);
            }
           
            // Now that we have the final list, let us return it back
            if (hostStatusList != null && !hostStatusList.isEmpty()) {
                for (MwHostStatus objHostStatus : hostStatusList) {
                    objCollection.getHostStatus().add(convert(objHostStatus));
                }
            }
        } catch (IOException | ParseException ex) {
            log.error("Error during search for host status", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        
        return objCollection;
    }

    public HostStatus retrieve(HostStatusLocator locator){
        MwHostStatus mwHostStatus = retrieveMwHostStatus(locator);
        if(mwHostStatus == null)
            return null;
        return convert(mwHostStatus);
    }
    
    private MwHostStatus retrieveMwHostStatus(HostStatusLocator locator) {
        log.debug("Got request to retrieve host status");
        if (locator == null || (locator.id == null && locator.pathId == null && locator.hostId == null && locator.aikCertificate == null)) {
            log.debug("ID, host ID, or AIK certificate must be specified");
            return null;
        }
        
        try {
            MwHostStatusJpaController mwHostStatusJpaController = My.jpa().mwHostStatus();
            if (locator.pathId != null) {
                MwHostStatus mwHostStatus
                        = mwHostStatusJpaController.findMwHostStatus(locator.pathId.toString());
                if (mwHostStatus != null) {
                    return mwHostStatus;
                }
            } else if (locator.id != null) {
                MwHostStatus mwHostStatus
                        = mwHostStatusJpaController.findMwHostStatus(locator.id.toString());
                if (mwHostStatus != null) {
                    return mwHostStatus;
                }
            } else if (locator.hostId != null) {
                MwHostStatus mwHostStatus
                        = mwHostStatusJpaController.findMwHostStatusByHostId(locator.hostId.toString());
                if (mwHostStatus != null) {
                    return mwHostStatus;
                }
            } else if (locator.aikCertificate != null && !locator.aikCertificate.isEmpty()) {
                MwHostStatus mwHostStatus
                        = mwHostStatusJpaController.findMwHostStatusByAikCertificate(locator.aikCertificate);
                if (mwHostStatus != null) {
                    return mwHostStatus;
                }
            }
        } catch (Exception ex) {
            log.error("host_status:retrieve - error during search for host status.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }
        return null;
    }

    public HostStatus create(HostStatus item){
         log.debug("Got request to create a new host status entry");
        UUID hostStatusId;
        if (item.getId() != null) {
            hostStatusId = item.getId();
        } else {
            hostStatusId = new UUID();
        }
        
        HostStatusLocator locator = new HostStatusLocator();
        locator.id = hostStatusId;
        
        if (item == null || item.getHostId() == null || item.getStatus() == null) {
            log.error("Host ID and status must be specified");
            throw new RepositoryInvalidInputException(locator);
        }
        try {
            MwHostStatusJpaController hostStatusJpa = My.jpa().mwHostStatus();
            MwHostStatus mwHostStatus = new MwHostStatus();
            mwHostStatus.setId(hostStatusId.toString());
            mwHostStatus.setHostId(item.getHostId().toString());
            mwHostStatus.setHostManifest(item.getHostManifest());
            mwHostStatus.setStatus(item.getStatus());
            hostStatusJpa.create(mwHostStatus);
            
            return item;
        } catch (IOException | RepositoryInvalidInputException ex) {
            log.error("Error during host status creation", ex);
            throw new RepositoryCreateException(ex);
        } catch (Exception ex) {
            log.error("Error during host status creation", ex);
            throw new RepositoryCreateException(ex);
        }
    }
    
    public HostStatus store(HostStatus item) {
        log.debug("Got request to edit a host status entry");
        if (item == null) {
            log.error("Host name and connection string must be specified");
            throw new RepositoryInvalidInputException();
        }

        try {
            MwHostStatus mwHostStatus = new MwHostStatus();
            if (item.getHostId() != null) {
                mwHostStatus.setHostId(item.getHostId().toString());
            }
            if (item.getHostManifest() != null) {
                mwHostStatus.setHostManifest(item.getHostManifest());
            }
            if (item.getStatus() != null) {
                mwHostStatus.setStatus(item.getStatus());
            }

            MwHostStatusJpaController hostStatusJpa = My.jpa().mwHostStatus();
            hostStatusJpa.edit(mwHostStatus);
            return item;
        } catch (IOException | RepositoryInvalidInputException ex) {
            log.error("Error during host status update", ex);
            throw new RepositoryCreateException(ex);
        } catch (Exception ex) {
            log.error("Error during host status update", ex);
            throw new RepositoryCreateException(ex);
        }
    }

    public void delete(HostStatusLocator locator) {
        log.debug("Received request to delete host status");
        if (locator == null || (locator.id == null && locator.pathId == null
                && locator.aikCertificate == null && locator.hostId == null)){
            return;
        }

        HostStatus hostStatus = retrieve(locator);
        if (hostStatus != null) {
            try {
                My.jpa().mwHostStatus().destroy(hostStatus.getId().toString());
            } catch (IOException | NonexistentEntityException ex) {
                log.error("Error during deletion of host status", ex);
                throw new RepositoryDeleteException(ex);
            }
        }
    }

    public void delete(HostStatusFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private HostStatus convert(MwHostStatus mwHostStatus) {
        HostStatus hostStatus = new HostStatus();
        if (mwHostStatus != null) {
            hostStatus.setHostId(UUID.valueOf(mwHostStatus.getHostId()));
            hostStatus.setCreated(mwHostStatus.getCreated());
            hostStatus.setHostManifest(mwHostStatus.getHostManifest());
            hostStatus.setId(UUID.valueOf(mwHostStatus.getId()));
            hostStatus.setStatus(mwHostStatus.getStatus());
        }
        return hostStatus;
    }
    
    public MwHostStatus convertToMwHostStatus(HostStatus hostStatus) {
        MwHostStatus mwHostStatus = new MwHostStatus();
        if (hostStatus != null) {
           if (hostStatus.getHostId() != null) {
                mwHostStatus.setHostId(hostStatus.getHostId().toString());
            }
            if (hostStatus.getHostManifest() != null) {
                mwHostStatus.setHostManifest(hostStatus.getHostManifest());
            }
            if (hostStatus.getStatus() != null) {
                mwHostStatus.setStatus(hostStatus.getStatus());
            }
        }
        return mwHostStatus;
    }
}
