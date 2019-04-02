/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.iso8601.Iso8601Date;
import com.intel.mtwilson.My;
import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.core.verifier.policy.TrustReport;
import com.intel.mtwilson.flavor.controller.MwReportJpaController;
import com.intel.mtwilson.flavor.data.MwReport;
import com.intel.mtwilson.flavor.model.FlavorsTrustStatus;
import com.intel.mtwilson.flavor.model.TrustInformation;
import com.intel.mtwilson.flavor.rest.v2.model.Report;
import com.intel.mtwilson.flavor.rest.v2.model.ReportFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.ReportCollection;
import com.intel.mtwilson.flavor.rest.v2.model.ReportLocator;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportRepository implements DocumentRepository<Report, ReportCollection, ReportFilterCriteria, ReportLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReportRepository.class);

    @Override
    public ReportCollection search(ReportFilterCriteria criteria) {
        log.debug("Got request to search for reports");
        ReportCollection objCollection = new ReportCollection();
        try {
            // If user specifies no criteria, an empty result will be returned to prevent an unintentional process intensive query. 
            if (criteria.filter
                    && criteria.id == null
                    && (criteria.hostId == null || criteria.hostId.isEmpty())
                    && (criteria.hostName == null || criteria.hostName.isEmpty())
                    && (criteria.hostHardwareId == null || criteria.hostHardwareId.isEmpty())
                    && (criteria.hostStatus == null || criteria.hostStatus.isEmpty())
                    && (criteria.numberOfDays == 0)
                    && (criteria.toDate == null || criteria.toDate.isEmpty())
                    && (criteria.fromDate == null || criteria.fromDate.isEmpty())
                    && (criteria.latestPerHost == null || criteria.latestPerHost.isEmpty())
                    && (criteria.limit == 10000)) {
                return objCollection; // Empty
            }
            
            MwReportJpaController jpaController = My.jpa().mwReport();
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            Iso8601Date toIso8601Date;
            Date toDate = null;
            Date fromDate = null;

            if (criteria.toDate != null && !criteria.toDate.isEmpty()) {
                toIso8601Date = Iso8601Date.valueOf(criteria.toDate);
                cal.setTime(toIso8601Date);
                toDate = dateFormat.parse(dateFormat.format(cal.getTime()));
            }
            if (criteria.fromDate != null && !criteria.fromDate.isEmpty()) {
                Iso8601Date fromIso8601Date = Iso8601Date.valueOf(criteria.fromDate);
                cal.setTime(fromIso8601Date); // This would set the time to ex:2015-05-30 00:00:00
                fromDate = dateFormat.parse(dateFormat.format(cal.getTime()));
            }

            String reportId = null;
            String hostId = null;
            String hostName = null;
            String hostHardwareUuid = null;
            String hostStatus = null;
            Boolean latestPerHost = true;

            if (criteria.id != null) {
                reportId = criteria.id.toString();
            }
            if (criteria.hostId != null) {
                hostId = criteria.hostId;
            }
            if (criteria.hostName != null && !criteria.hostName.isEmpty()) {
                hostName = criteria.hostName;
            }
            if (criteria.hostHardwareId != null) {
                hostHardwareUuid = criteria.hostHardwareId;
            }
            if (criteria.hostStatus != null && !criteria.hostStatus.isEmpty()) {
                hostStatus = criteria.hostStatus;
            }
            if (criteria.latestPerHost != null && !criteria.latestPerHost.isEmpty()) {
                latestPerHost = Boolean.valueOf(criteria.latestPerHost);
            }

            if (criteria.numberOfDays != 0) {
                log.debug("Number of days criteria is specified with value: {}", criteria.numberOfDays);
                // calculate from and to dates
                toDate = new Date(); // Get the current date and time
                cal.setTime(toDate);
                toDate = dateFormat.parse(dateFormat.format(cal.getTime()));
                // To get the fromDate, we substract the number of days fromm the current date.
                cal.add(Calendar.DATE, -(criteria.numberOfDays));
                fromDate = dateFormat.parse(dateFormat.format(cal.getTime()));
                log.debug("Reports between {} to {} will be retrieved", dateFormat.format(fromDate), dateFormat.format(toDate));
            }

            List<MwReport> reportList;
            if (criteria.fromDate == null && criteria.toDate == null && latestPerHost) {
                reportList = jpaController.findLatestMwReport(
                        reportId,
                        hostId,
                        hostName,
                        hostHardwareUuid,
                        hostStatus,
                        criteria.limit);
            } else {
                reportList = jpaController.findMwReport(
                        reportId,
                        hostId,
                        hostName,
                        hostHardwareUuid,
                        hostStatus,
                        fromDate,
                        toDate,
                        latestPerHost,
                        criteria.limit);
            }

            // Now that we have the final list, let us return it back.
            if (reportList != null && !reportList.isEmpty()) {
                for (MwReport objReport : reportList) {
                    objCollection.getReports().add(convert(objReport));
                }
            }

        } catch (IOException | ParseException ex) {
            log.error("Error during search for reports", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("Returning back {} of results", objCollection.getReports().size());
        return objCollection;
    }

    @Override
    public Report retrieve(ReportLocator locator) {
        MwReport mwReport = retrieveMwReport(locator);
        if (mwReport == null) {
            return null;
        }
        return convert(mwReport);
    }

    private MwReport retrieveMwReport(ReportLocator locator) {
        log.debug("Got request to retrieve report");
        if (locator == null || (locator.pathId == null && locator.id == null && locator.hostId == null)) {
            log.debug("ID or host ID must be specified");
            return null;
        }

        try {
            MwReportJpaController mwReportJpaController = My.jpa().mwReport();
            if (locator.pathId != null) {
                MwReport mwReport = mwReportJpaController.findMwReport(locator.pathId.toString());
                if (mwReport != null) {
                    return mwReport;
                }
            } else if (locator.id != null) {
                MwReport mwReport = mwReportJpaController.findMwReport(locator.id.toString());
                if (mwReport != null) {
                    return mwReport;
                }
            } else if (locator.hostId != null) {
                MwReport mwReport = mwReportJpaController.findMwReportByHostId(locator.hostId.toString());
                if (mwReport != null) {
                    return mwReport;
                }
            }
        } catch (Exception ex) {
            log.error("Error during search for report", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }
        return null;
    }

    @Override
    public void store(Report item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Report storeReport(Report item) {
        if (item == null || item.getHostId() == null) {
            log.error("Host ID  must be specified");
            throw new RepositoryInvalidInputException();
        }

        try {
            ReportLocator locator = convert(item);
            MwReport mwReport = retrieveMwReport(locator);
            if (mwReport == null) {
                return createReport(item);
            }

            if (item.getHostId() != null) {
                mwReport.setHostId(item.getHostId().toString());
            }
            if (item.getCreated() != null) {
                mwReport.setCreated(item.getCreated());
            }
            if (item.getExpiration() != null) {
                mwReport.setExpiration(item.getExpiration());
            }
            if (item.getTrustReport() != null) {
                mwReport.setTrustReport(item.getTrustReport());
            }
            if (item.getSaml() != null) {
                mwReport.setSaml(item.getSaml());
            }

            MwReportJpaController reportJpa = My.jpa().mwReport();
            reportJpa.edit(mwReport);
            return item;

        } catch (IOException | RepositoryInvalidInputException ex) {
            log.error("Error during reports update", ex);
            throw new RepositoryCreateException(ex);
        } catch (Exception ex) {
            log.error("Error during reports update", ex);
            throw new RepositoryCreateException(ex);
        }
    }

    @Override
    public void create(Report item) {
        storeReport(item);
    }

    private Report createReport(Report item) {
        log.debug("Got request to create a new report");
        UUID reportId;
        if (item.getId() != null) {
            reportId = item.getId();
        } else {
            reportId = new UUID();
        }

        ReportLocator locator = new ReportLocator();
        locator.id = reportId;

        if (item == null || item.getHostId() == null || item.getTrustInformation() == null || item.getTrustReport() == null || item.getSaml() == null) {
            log.error("Host ID trust report and saml report must be specified");
            throw new RepositoryInvalidInputException(locator);
        }
        try {
            MwReportJpaController mwReportJpaController = My.jpa().mwReport();
            MwReport mwReport = new MwReport();
            mwReport.setId(reportId.toString());
            mwReport.setHostId(item.getHostId().toString());
            mwReport.setCreated(item.getCreated());
            mwReport.setExpiration(item.getExpiration());
            mwReport.setTrustReport(item.getTrustReport());
            mwReport.setSaml(item.getSaml());

            mwReportJpaController.create(mwReport);
            log.debug("Report created for host with ID {}", item.getHostId().toString());
            return item;
        } catch (IOException ex) {
            log.error("Error during report creation", ex);
            throw new RepositoryCreateException(ex);
        } catch (RepositoryInvalidInputException Ex) {
            log.error("Error during report creation", Ex);
            throw new RepositoryCreateException(Ex);
        } catch (Exception ex) {
            log.error("Error during report creation", ex);
            throw new RepositoryCreateException(ex);
        }
    }

    @Override
    public void delete(ReportLocator locator) {
        if (locator == null || (locator.id == null && locator.pathId == null && locator.hostId == null)) {
            return;
        }
        log.debug("Got request to delete Report ");

        Report obj = retrieve(locator);
        if (obj != null) {
            try {
                My.jpa().mwReport().destroy(obj.getId().toString());
            } catch (Exception ex) {
                log.error("Error during Report delete.", ex);
                throw new RepositoryDeleteException(ex, locator);
            }
        }
    }

    @Override
    public void delete(ReportFilterCriteria criteria) {
        log.debug("Got request to delete Report by search criteria.");
        ReportCollection objCollection = search(criteria);
        try {
            for (Report obj : objCollection.getReports()) {
                ReportLocator locator = new ReportLocator();
                locator.id = obj.getId();
                delete(locator);
            }
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Error during Report delete.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }

    private Report convert(MwReport dbObj) {
        Report obj = new Report();

        obj.setId(UUID.valueOf(dbObj.getId()));
        obj.setHostId(UUID.valueOf(dbObj.getHostId()));
        obj.setSaml(dbObj.getSaml());
        obj.setCreated(dbObj.getCreated());
        obj.setExpiration(dbObj.getExpiration());
        obj.setTrustReport(dbObj.getTrustReport());
        obj.setTrustInformation(buildTrustInformation(dbObj.getTrustReport()));

        return obj;
    }

    private ReportLocator convert(Report item) {
        ReportLocator reportLocator = new ReportLocator();
        if (item.getId() != null) {
            reportLocator.id = item.getId();
        }
        if (item.getHostId() != null) {
            reportLocator.hostId = item.getHostId();
        }
        return reportLocator;
    }

    public TrustInformation buildTrustInformation(TrustReport trustReport) {
        Map<FlavorPart, FlavorsTrustStatus> flavorsTrustStatus = new HashMap();
        for (FlavorPart flavorPart : FlavorPart.values()) {
            if (!trustReport.getResultsForMarker(flavorPart.name()).isEmpty()) {
                flavorsTrustStatus.put(flavorPart, new FlavorsTrustStatus(trustReport.isTrustedForMarker(flavorPart.name()), trustReport.getResultsForMarker(flavorPart.name())));
            }
        }
        return new TrustInformation(trustReport.isTrusted(), flavorsTrustStatus);
    }
}
