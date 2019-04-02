/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.flavor.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.flavor.controller.exceptions.PreexistingEntityException;
import com.intel.mtwilson.audit.data.AuditLogEntry;
import com.intel.mtwilson.core.verifier.policy.TrustReport;
import com.intel.mtwilson.flavor.data.MwReport;
import com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author rksavino
 */
public class MwReportJpaController implements Serializable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MwReportJpaController.class);

    public MwReportJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MwReport mwReport) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            if (mwReport.getCreated() == null) {
                mwReport.setCreated(Calendar.getInstance().getTime());
            }
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(mwReport);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findMwReport(mwReport.getId()) != null) {
                throw new PreexistingEntityException("The report " + mwReport + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(MwReport mwReport) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            if (mwReport.getCreated() == null) {
                mwReport.setCreated(Calendar.getInstance().getTime());
            }
            em = getEntityManager();
            em.getTransaction().begin();
            mwReport = em.merge(mwReport);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = mwReport.getId();
                if (findMwReport(id) == null) {
                    throw new NonexistentEntityException("The report with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(String id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            MwReport mwReport;
            try {
                mwReport = em.getReference(MwReport.class, id);
                mwReport.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The report with id " + id + " no longer exists.", enfe);
            }
            em.remove(mwReport);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<MwReport> findMwReportEntities() {
        return findMwReportEntities(true, -1, -1);
    }

    public List<MwReport> findMwReportEntities(int maxResults, int firstResult) {
        return findMwReportEntities(false, maxResults, firstResult);
    }

    private List<MwReport> findMwReportEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwReport.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public MwReport findMwReport(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwReport.class, id);
        } finally {
            em.close();
        }
    }

    public int getMwReportCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwReport> rt = cq.from(MwReport.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public MwReport findMwReportByHostId(String hostId) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("MwReport.findByHostId");
            query.setMaxResults(1);
            query.setParameter("hostId", hostId);
            MwReport result = (MwReport) query.getSingleResult();
            return result;
        } catch (NoResultException e) {
            log.trace("No report exists for host with ID: {}", hostId);
            return null;
        } finally {
            em.close();
        }
    }

    public List<MwReport> findLatestMwReport(String reportId, String hostId, String hostName, String hardwareUuid,
            String hostState, int limit) {
        List<MwReport> reportsList = null;
        EntityManager em = getEntityManager();
        try {

            //Build table join string for host table if host identifier is set
            String tableJoinString = null;
            if ((hostName != null && !hostName.isEmpty())
                    || (hardwareUuid != null && !hardwareUuid.isEmpty())) {
                tableJoinString = String.format("INNER JOIN mw_host h on h.id = r.host_id");
            }

            //Build table join string for host status statble if host state is set 
            if (hostState != null && !hostState.isEmpty()) {
                if (tableJoinString == null || tableJoinString.isEmpty()) {
                    tableJoinString = String.format("INNER JOIN mw_host_status hs on hs.host_id = r.host_id");
                } else {
                    tableJoinString = String.format("%s INNER JOIN mw_host_status hs on hs.host_id = r.host_id", tableJoinString);
                }
            }

            String hostIdentifierQueryString = hostIdentifierQueryString(hostName, hardwareUuid);
            String additionalOptionsQueryString = null;

            // Build the additional options query string if host identifier is set
            if (hostIdentifierQueryString != null && !hostIdentifierQueryString.isEmpty()) {
                additionalOptionsQueryString = String.format("%s", hostIdentifierQueryString);
            }

            //Build host id partial query string and add it to the additional options query string
            if (hostId != null && !hostId.isEmpty()) {
                String hostIdQueryString = String.format("r.host_id = '%s'", hostId);
                if (additionalOptionsQueryString == null || additionalOptionsQueryString.isEmpty()) {
                    additionalOptionsQueryString = String.format("%s", hostIdQueryString);
                } else {
                    additionalOptionsQueryString = String.format("%s AND %s", additionalOptionsQueryString, hostIdQueryString);
                }
            }

            //Build host state partial query string and add it to the additional options query string
            if (hostState != null && !hostState.isEmpty()) {
                String hostStateQueryString = String.format("hs.status ->> 'host_state' = '%s'", hostState.toUpperCase());
                if (additionalOptionsQueryString == null || additionalOptionsQueryString.isEmpty()) {
                    additionalOptionsQueryString = String.format("%s", hostStateQueryString);
                } else {
                    additionalOptionsQueryString = String.format("%s AND %s", additionalOptionsQueryString, hostStateQueryString);
                }
            }

            //Build reports id partial query string and add it to the additional options query string
            if (reportId != null && !reportId.isEmpty()) {
                String reportIdQueryString = String.format("r.id = '%s'", reportId);
                if (additionalOptionsQueryString == null || additionalOptionsQueryString.isEmpty()) {
                    additionalOptionsQueryString = String.format("%s", reportIdQueryString);
                } else {
                    additionalOptionsQueryString = String.format("%s AND %s", additionalOptionsQueryString, reportIdQueryString);
                }
            }

            // build final formatted string with additional options string and the table join string
            String formattedQuery = "SELECT r.* FROM mw_report r";
            if (tableJoinString != null && !tableJoinString.isEmpty()) {
                formattedQuery = String.format("%s %s", formattedQuery, tableJoinString);
            }
            if (additionalOptionsQueryString != null && !additionalOptionsQueryString.isEmpty()) {
                formattedQuery = String.format("%s WHERE %s", formattedQuery, additionalOptionsQueryString);
            }

            Query query = em.createNativeQuery(formattedQuery, MwReport.class);
            query.setMaxResults(limit);
            if (query.getResultList() != null && !query.getResultList().isEmpty()) {
                reportsList = query.getResultList();
            }
            return reportsList;
        } finally {
            em.close();
        }
    }

    public List<MwReport> findMwReport(String reportId, String hostId, String hostName, String hardwareUuid,
            String hostState, Date fromDate, Date toDate, boolean latestPerHost, int limit) {

        EntityManager em = getEntityManager();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {

            String auditLogAbbrv = "au";
            if (latestPerHost) {
                auditLogAbbrv = "auj";
            }
            //build table join string for host table if host identifier is set and aik certificate is null
            String tableJoinString = null;
            String hostIdentifierString = null;
            String additionalOptionsQueryString = String.format("WHERE %s.entity_type = 'MwReport'", auditLogAbbrv);

            //Build table join string with host table if host identifier is set
            if ((hostName != null && !hostName.isEmpty())
                    || (hardwareUuid != null && !hardwareUuid.isEmpty())) {
                tableJoinString = String.format("INNER JOIN mw_host h on h.id = %s.data -> 'columns' -> 1 ->> 'value'", auditLogAbbrv);
                hostIdentifierString = hostIdentifierQueryString(hostName, hardwareUuid);
            }

            //Build table join string with host status table if host state identifier is set
            if (hostState != null && !hostState.isEmpty()) {
                if (tableJoinString == null || tableJoinString.isEmpty()) {
                    tableJoinString = String.format("INNER JOIN mw_host_status hs on hs.host_id = %s.data -> 'columns' -> 1 ->> 'value'", auditLogAbbrv);
                } else {
                    tableJoinString = String.format("%s INNER JOIN mw_host_status hs on hs.host_id = %s.data -> 'columns' -> 1 ->> 'value'", tableJoinString, auditLogAbbrv);
                }
            }

            //Build additional options query string if host identifier is set
            if (hostIdentifierString != null && !hostIdentifierString.isEmpty()) {
                additionalOptionsQueryString = String.format("%s AND %s", additionalOptionsQueryString, hostIdentifierString);
            }

            //Build host ID partial query string and add it to the additional options query string
            if (hostId != null && !hostId.isEmpty()) {
                String hostIdQueryString = String.format("%s.data -> 'columns' -> 1 ->> 'value' = '%s'", auditLogAbbrv, hostId);
                additionalOptionsQueryString = String.format("%s AND %s", additionalOptionsQueryString, hostIdQueryString);
            }

            //Build host state partial query string and add it to the additional options query string
            if (hostState != null && !hostState.isEmpty()) {
                String hostStateQueryString = String.format("hs.status ->> 'host_state' = '%s'", hostState.toUpperCase());
                additionalOptionsQueryString = String.format("%s AND %s", additionalOptionsQueryString, hostStateQueryString);
            }

            //Build from date query string and add it to the additional options query string
            if (fromDate != null) {
                String fromDateQueryString = String.format("CAST(%s.data -> 'columns' -> 3 ->> 'value' AS TIMESTAMP) >= CAST(? AS TIMESTAMP)", auditLogAbbrv);
                additionalOptionsQueryString = String.format("%s AND %s", additionalOptionsQueryString, fromDateQueryString);
            }

            //Build to date and add it to the additional options query string
            if (toDate != null) {
                String toDateQueryString = String.format("CAST(%s.data -> 'columns' -> 3 ->> 'value' AS TIMESTAMP) <= CAST(? AS TIMESTAMP)", auditLogAbbrv);
                additionalOptionsQueryString = String.format("%s AND %s", additionalOptionsQueryString, toDateQueryString);
            }

            //Add table join string to additional options query string
            if (tableJoinString != null && !tableJoinString.isEmpty()) {
                additionalOptionsQueryString = String.format("%s %s", tableJoinString, additionalOptionsQueryString);
            }

            //Build final formatted query string
            String formattedQuery = String.format("SELECT au.* "
                    + "FROM mw_audit_log_entry au");

            // Build final formatted query string for latest per host filter criteria
            if (latestPerHost) {
                String maxDateQueryString = String.format("INNER JOIN (SELECT entity_id, max(auj.data -> 'columns' -> 3 ->> 'value') AS max_date "
                        + "FROM mw_audit_log_entry auj %s GROUP BY entity_id)a "
                        + "ON a.entity_id = au.entity_id "
                        + "AND a.max_date = au.data -> 'columns' -> 3 ->> 'value'", additionalOptionsQueryString);
                formattedQuery = String.format("%s %s ORDER BY au.data -> 'columns' -> 3 ->> 'columnName' DESC", formattedQuery, maxDateQueryString);
            } else {
                formattedQuery = String.format("%s %s", formattedQuery, additionalOptionsQueryString);
            }

            List<AuditLogEntry> auditLogEntryList = null;

            Query query = em.createNativeQuery(formattedQuery, AuditLogEntry.class);
            if (fromDate != null) {
                query.setParameter(1, dateFormat.format(fromDate));
            }
            if (toDate != null) {
                if (fromDate == null) {
                    query.setParameter(1, dateFormat.format(toDate));
                } else {
                    query.setParameter(2, dateFormat.format(toDate));
                }
            }

            query.setMaxResults(limit);
            if (query.getResultList() != null && !query.getResultList().isEmpty()) {
                auditLogEntryList = query.getResultList();

            }

            return convertToReport(auditLogEntryList);

        } finally {
            em.close();
        }
    }

    private List<MwReport> convertToReport(List<AuditLogEntry> auditLogEntryList) {
        List<MwReport> reportsList = new ArrayList();
        if (auditLogEntryList == null || auditLogEntryList.isEmpty()) {
            return null;
        }

        try {
            for (AuditLogEntry auditLogEntry : auditLogEntryList) {
                if (auditLogEntry == null
                        || auditLogEntry.getData() == null
                        || auditLogEntry.getData().getColumns() == null) {
                    continue;
                }

                String reportId = null;
                if (auditLogEntry.getEntityId() != null
                        && !auditLogEntry.getEntityId().isEmpty()) {
                    reportId = auditLogEntry.getEntityId();
                }

                String hostId = null;
                if (auditLogEntry.getData().getColumns().get(1) != null
                        && auditLogEntry.getData().getColumns().get(1).getValue() != null) {
                    hostId = auditLogEntry.getData().getColumns().get(1).getValue().toString();
                }

                TrustReport trustReport = null;
                if (auditLogEntry.getData().getColumns().get(2) != null
                        && auditLogEntry.getData().getColumns().get(2).getValue() != null) {
                    ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
                    trustReport = mapper.convertValue(auditLogEntry.getData().getColumns().get(2).getValue(), TrustReport.class);
                }

                Date created = null;
                if (auditLogEntry.getData().getColumns().get(3) != null
                        && auditLogEntry.getData().getColumns().get(3).getValue() != null) {
                    String createdString = auditLogEntry.getData().getColumns().get(3).getValue().toString();
                    created = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(createdString);
                }

                Date expiration = null;
                if (auditLogEntry.getData().getColumns().get(4) != null
                        && auditLogEntry.getData().getColumns().get(4).getValue() != null) {
                    String expirationString = auditLogEntry.getData().getColumns().get(4).getValue().toString();
                    expiration = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(expirationString);
                }

                String saml = null;
                if (auditLogEntry.getData().getColumns().get(5) != null
                        && auditLogEntry.getData().getColumns().get(5).getValue() != null) {
                    saml = auditLogEntry.getData().getColumns().get(5).getValue().toString();
                }

                MwReport report = new MwReport();
                report.setId(reportId);
                report.setHostId(hostId);
                report.setTrustReport(trustReport);
                report.setCreated(created);
                report.setExpiration(expiration);
                report.setSaml(saml);
                reportsList.add(report);
            }
        } catch (ParseException ex) {
            log.error("Error converting audit log entry entity to reports entity", ex);
        }

        return reportsList;
    }

    /* *
     * Returns the list of host IDs for which the reports are going to expire in the given 
     * time
     * */
    public List<String> findHostsWithExpiredCache(Integer expiryThresholdInSeconds) {
        EntityManager em = getEntityManager();
        try {
            // To find the list of hosts which would have their attestation report getting expired, we calculate what is the earliest create date for which the SAML would expire
            // and also add a buffer time of about 5 min so that we might get to processing the host before it actually expires.
            //Query query = em.createNativeQuery("SELECT h.id FROM mw_host as h WHERE NOT EXISTS ( SELECT ID FROM mw_report as t WHERE h.ID = t.host_id AND t.created > ? )", MwReport.class);
            Query query = em.createNativeQuery("select h.id from mw_host as h where not exists (select t.host_id from (select row_number() over (partition by host_id order by expiration desc) rn, host_id from mw_report where expiration > CAST(? AS TIMESTAMP)) as t where h.id=t.host_id and t.rn=1)");
            Calendar maxCache = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            maxCache.add(Calendar.SECOND, expiryThresholdInSeconds);
            query.setParameter(1, sdf.format(maxCache.getTime()));
            Date startDate = new Date();
            List<String> results = query.getResultList();
            log.debug("Time taken to find hosts with expired cache {} ms.", (new Date().getTime() - startDate.getTime()));
            return results;
        } finally {
            em.close();
        }
    }

    private String hostIdentifierQueryString(String hostName, String hardwareUuid) {

        String hostIdentifierQueryString = null;

        //Build host name partial query string and add it to the additional options query string
        if (hostName != null && !hostName.isEmpty()) {
            String hostNameQueryString = String.format("h.name = '%s'", hostName);
            hostIdentifierQueryString = String.format("%s", hostNameQueryString);
        }

        //Build hardware uuid partial query string and add it to the additional options query string
        if (hardwareUuid != null && !hardwareUuid.isEmpty()) {
            String hardwareUuidQueryString = String.format("h.hardware_uuid = '%s'", hardwareUuid);
            if (hostIdentifierQueryString == null || hostIdentifierQueryString.isEmpty()) {
                hostIdentifierQueryString = String.format("%s", hardwareUuidQueryString);
            } else {
                hostIdentifierQueryString = String.format("%s AND %s", hostIdentifierQueryString, hardwareUuidQueryString);
            }
        }

        return hostIdentifierQueryString;
    }

}
