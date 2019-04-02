/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.audit.data.AuditLogEntry;
import com.intel.mtwilson.core.common.model.HostManifest;
import com.intel.mtwilson.flavor.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.flavor.controller.exceptions.PreexistingEntityException;
import com.intel.mtwilson.flavor.converter.HostManifestConverter;
import com.intel.mtwilson.flavor.converter.HostStatusConverter;
import com.intel.mtwilson.flavor.data.MwHostStatus;
import com.intel.mtwilson.flavor.model.HostStatusInformation;
import com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author rksavino
 */
public class MwHostStatusJpaController implements Serializable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MwHostStatusJpaController.class);

    public MwHostStatusJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MwHostStatus mwHostStatus) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            mwHostStatus.setCreated(Calendar.getInstance().getTime());
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(mwHostStatus);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findMwHostStatus(mwHostStatus.getId()) != null) {
                throw new PreexistingEntityException("The host status " + mwHostStatus + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(MwHostStatus mwHostStatus) throws NonexistentEntityException, Exception {
        EntityManager em = getEntityManager();
        EntityTransaction entityTransaction = em.getTransaction();
        HostStatusConverter hostStatusConverter = new HostStatusConverter();
        HostManifestConverter hostManifestConverter = new HostManifestConverter();
        try {
            mwHostStatus.setCreated(Calendar.getInstance().getTime());
            entityTransaction.begin();
            if (mwHostStatus.getHostId() != null && !mwHostStatus.getHostId().isEmpty()) {
                Query updateQuery;
                if (mwHostStatus.getHostManifest() == null) {
                    updateQuery = em.createNativeQuery("UPDATE mw_host_status SET status = ?, created = ? WHERE host_id = ?");
                    updateQuery.setParameter(1, hostStatusConverter.convertToDatabaseColumn(mwHostStatus.getStatus()))
                            .setParameter(2, mwHostStatus.getCreated())
                            .setParameter(3, mwHostStatus.getHostId());
                } else {
                    updateQuery = em.createNativeQuery("UPDATE mw_host_status SET status = ?, created = ?, host_report = ? WHERE host_id = ?");
                    updateQuery.setParameter(1, hostStatusConverter.convertToDatabaseColumn(mwHostStatus.getStatus()))
                            .setParameter(2, mwHostStatus.getCreated())
                            .setParameter(3, hostManifestConverter.convertToDatabaseColumn(mwHostStatus.getHostManifest()))
                            .setParameter(4, mwHostStatus.getHostId());
                }
                int updateCount = updateQuery.executeUpdate();
                // Insert a new record when updateCount is 0 i.e no records were updated for the given host id
                if (updateCount <= 0) {
                    mwHostStatus.setId(new UUID().toString());
                    Query insertQuery = em.createNativeQuery("INSERT INTO mw_host_status(id, host_id, status, created, host_report) "
                            + "VALUES (?, ?, ?, ?, ?)");
                    insertQuery.setParameter(1, mwHostStatus.getId())
                            .setParameter(2, mwHostStatus.getHostId())
                            .setParameter(3, hostStatusConverter.convertToDatabaseColumn(mwHostStatus.getStatus()))
                            .setParameter(4, mwHostStatus.getCreated())
                            .setParameter(5, hostManifestConverter.convertToDatabaseColumn(mwHostStatus.getHostManifest()))
                            .executeUpdate();
                }
            }
            entityTransaction.commit();
        } catch (RuntimeException e) {
            if (entityTransaction.isActive()) {
                entityTransaction.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void editHostStatusList(List<MwHostStatus> mwHostStatusList) {
        int batchSize = 100;
        EntityManager em = getEntityManager();
        EntityTransaction entityTransaction = em.getTransaction();
        HostStatusConverter hostStatusConverter = new HostStatusConverter();
        HostManifestConverter hostManifestConverter = new HostManifestConverter();
        try {
            entityTransaction.begin();
            for (int i = 0; i < mwHostStatusList.size(); i++) {
                // perform commits only in batches
                if (i > 0 && i % batchSize == 0) {
                    entityTransaction.commit();
                    entityTransaction.begin();
                    em.clear();
                }
                //initialize the host status entity class with the list record
                MwHostStatus mwHostStatus = mwHostStatusList.get(i);
                mwHostStatus.setCreated(Calendar.getInstance().getTime());
                if (mwHostStatus.getHostId() != null && !mwHostStatus.getHostId().isEmpty()) {
                    Query updateQuery = em.createNativeQuery("UPDATE mw_host_status SET status = ?, created = ? WHERE host_id = ?");
                    int updateCount = updateQuery.setParameter(1, hostStatusConverter.convertToDatabaseColumn(mwHostStatus.getStatus()))
                            .setParameter(2, mwHostStatus.getCreated())
                            .setParameter(3, mwHostStatus.getHostId())
                            .executeUpdate();
                    // Insert a new record when updateCount is 0 i.e no records were updated for the given host id
                    if (updateCount <= 0) {
                        mwHostStatus.setId(new UUID().toString());
                        Query insertQuery = em.createNativeQuery("INSERT INTO mw_host_status(id, host_id, status, created, host_report) "
                                + "VALUES (?, ?, ?, ?, ?)");
                        insertQuery.setParameter(1, mwHostStatus.getId())
                                .setParameter(2, mwHostStatus.getHostId())
                                .setParameter(3, hostStatusConverter.convertToDatabaseColumn(mwHostStatus.getStatus()))
                                .setParameter(4, mwHostStatus.getCreated())
                                .setParameter(5, hostManifestConverter.convertToDatabaseColumn(mwHostStatus.getHostManifest()))
                                .executeUpdate();
                    }
                }
            }
            entityTransaction.commit();
        } catch (RuntimeException e) {
            if (entityTransaction.isActive()) {
                entityTransaction.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void destroy(String id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            MwHostStatus mwHostStatus;
            try {
                mwHostStatus = em.getReference(MwHostStatus.class, id);
                mwHostStatus.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The host status with id " + id + " no longer exists.", enfe);
            }
            em.remove(mwHostStatus);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<MwHostStatus> findMwHostStatusEntities() {
        return findMwHostStatusEntities(true, -1, -1);
    }

    public List<MwHostStatus> findMwHostStatusEntities(int maxResults, int firstResult) {
        return findMwHostStatusEntities(false, maxResults, firstResult);
    }

    private List<MwHostStatus> findMwHostStatusEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwHostStatus.class));
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

    public MwHostStatus findMwHostStatus(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwHostStatus.class, id);
        } finally {
            em.close();
        }
    }

    public int getMwHostStatusCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwHostStatus> rt = cq.from(MwHostStatus.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public MwHostStatus findMwHostStatusByHostId(String hostId) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("MwHostStatus.findByHostId");
            query.setMaxResults(1);
            query.setParameter("hostId", hostId);
            MwHostStatus result = (MwHostStatus) query.getSingleResult();
            return result;
        } catch (NoResultException e) {
            log.warn("No host status exists for host with ID: {}", hostId);
            return null;
        } finally {
            em.close();
        }
    }

    public MwHostStatus findMwHostStatusByAikCertificate(String aikCertificate) {
        String jsonQueryText = String.format("host_report ->> 'aik_certificate' = '%s'", aikCertificate);
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNativeQuery(
                    "SELECT * FROM mw_host_status WHERE "
                    + jsonQueryText, MwHostStatus.class);
            MwHostStatus mwHostStatus = (MwHostStatus) query.getSingleResult();
            return mwHostStatus;
        } catch (NoResultException e) {
            log.warn("No host status exists for host with AIK: {}", aikCertificate);
            return null;
        } finally {
            em.close();
        }
    }

//    public MwHostStatus findMwHostStatusByAikPublicKey(String aikPublicKey) {
//        String jsonQueryText = String.format("host_report -> 'aik' ->> 'public_key' = '%s'", aikPublicKey);
//        MwHostStatus mwHostStatus = null;
//        EntityManager em = getEntityManager();
//        try {
//            Query query = em.createNativeQuery("SELECT * FROM mw_host_status WHERE " + jsonQueryText, MwHostStatus.class);
//            if (query.getResultList() != null && !query.getResultList().isEmpty()) {
//                mwHostStatus = (MwHostStatus) query.getSingleResult();
//            }
//            return mwHostStatus;
//        } finally {
//            em.close();
//        }
//    }
    public List<String> findMwHostListByKeyValue(String key, String value) {
        List<String> hostList = null;
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNativeQuery("SELECT host_id FROM mw_host_status WHERE host_report::text != 'null' AND host_report -> 'host_info' ->> ? = ?");
            query.setParameter(1, key);
            query.setParameter(2, value);

            if (query.getResultList() != null && !query.getResultList().isEmpty()) {
                hostList = query.getResultList();
            }
            return hostList;
        } finally {
            em.close();
        }
    }

    public List<MwHostStatus> findLatestMwHostStatus(String hostStatusId, String hostId, String hostName, String hardwareUuid,
            String aikCertificate, String hostState, int limit) {
        List<MwHostStatus> hostStatusList = null;
        EntityManager em = getEntityManager();
        try {

            //build table join string for host table if host identifier is set
            String tableJoinString = null;
            if ((hostName != null && !hostName.isEmpty())
                    || (hardwareUuid != null && !hardwareUuid.isEmpty())) {
                tableJoinString = String.format("INNER JOIN mw_host h on h.id = hs.host_id");
            }

            String hostIdentifierQueryString = hostIdentifierQueryString(hostName, hardwareUuid);
            String additionalOptionsQueryString = null;

            if (hostIdentifierQueryString != null && !hostIdentifierQueryString.isEmpty()) {
                additionalOptionsQueryString = String.format("%s", hostIdentifierQueryString);
            }

            //Build host ID partial query string and add it to the additional options query string
            if (hostId != null && !hostId.isEmpty()) {
                String hostIdQueryString = String.format("hs.host_id = '%s'", hostId);
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

            //Build host status ID partial query string and add it to the additional options query string
            if (hostStatusId != null && !hostStatusId.isEmpty()) {
                String hostStatusIdQueryString = String.format("hs.id = '%s'", hostStatusId);
                if (additionalOptionsQueryString == null || additionalOptionsQueryString.isEmpty()) {
                    additionalOptionsQueryString = String.format("%s", hostStatusIdQueryString);
                } else {
                    additionalOptionsQueryString = String.format("%s AND %s", additionalOptionsQueryString, hostStatusIdQueryString);
                }
            }

            //Build aik certificate query string and add it to the additional options query string
            if (aikCertificate != null && !aikCertificate.isEmpty()) {
                String aikCertificateQueryString = String.format("hs.host_report ->> aik_certificate = '%s'", aikCertificate);
                if (additionalOptionsQueryString == null || additionalOptionsQueryString.isEmpty()) {
                    additionalOptionsQueryString = String.format("%s", aikCertificateQueryString);
                } else {
                    additionalOptionsQueryString = String.format("%s AND %s", additionalOptionsQueryString, aikCertificateQueryString);
                }
            }

            // build final formatted string with additional options string and the table join string
            String formattedQuery = "SELECT hs.* FROM mw_host_status hs";
            if (tableJoinString != null && !tableJoinString.isEmpty()) {
                formattedQuery = String.format("%s %s", formattedQuery, tableJoinString);
            }
            if (additionalOptionsQueryString != null && !additionalOptionsQueryString.isEmpty()) {
                formattedQuery = String.format("%s WHERE %s", formattedQuery, additionalOptionsQueryString);
            }

            Query query = em.createNativeQuery(formattedQuery, MwHostStatus.class);
            query.setMaxResults(limit);
            if (query.getResultList() != null && !query.getResultList().isEmpty()) {
                hostStatusList = query.getResultList();
            }
            return hostStatusList;
        } finally {
            em.close();
        }
    }

    public List<MwHostStatus> findMwHostStatus(String hostStatusId, String hostId, String hostName, String hardwareUuid,
            String aikCertificate, String hostState, Date fromDate, Date toDate, boolean latestPerHost, int limit) {

        EntityManager em = getEntityManager();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {

            String auditLogAbbrv = "au";
            if (latestPerHost) {
                auditLogAbbrv = "auj";
            }
            //build table join string for host table if host identifier is set and aik certificate is null
            String tableJoinString = null;
            if (hostName != null && !hostName.isEmpty()) {
                tableJoinString = String.format("INNER JOIN mw_host h on h.id = %s.data -> 'columns' -> 1 ->> 'value'", auditLogAbbrv);
            }

            String additionalOptionsQueryString = String.format("WHERE %s.entity_type = 'MwHostStatus' ", auditLogAbbrv);

            //Build additional options query string is tabel join string set
            if (tableJoinString != null && !tableJoinString.isEmpty()) {
                //Build host ID partial query string and add it to the additional options query string
                if (hostId != null && !hostId.isEmpty()) {
                    String hostIdQueryString = String.format("h.id = '%s'", hostId);
                    additionalOptionsQueryString = String.format("%s AND %s", additionalOptionsQueryString, hostIdQueryString);
                }
                //calling hostIdentifierQueryString method to improve the performance by refering to the host table when host identifier is set
                additionalOptionsQueryString = String.format("%s AND %s", additionalOptionsQueryString, hostIdentifierQueryString(hostName, hardwareUuid));
            } else {
                //Build host ID partial query string and add it to the additional options query string
                if (hostId != null && !hostId.isEmpty()) {
                    String hostIdQueryString = String.format("%s.data -> 'columns' -> 1 ->> 'value' = '%s'", auditLogAbbrv, hostId);
                    additionalOptionsQueryString = String.format("%s AND %s", additionalOptionsQueryString, hostIdQueryString);
                }

                //Build host name partial query string and add it to the additional options query string
                if (hostName != null && !hostName.isEmpty()) {
                    String hostNameQueryString = String.format("h.name = '%s'", hostName);
                    additionalOptionsQueryString = String.format("%s AND %s", additionalOptionsQueryString, hostNameQueryString);
                }

                //Build hardware uuid partial query string and add it to the additional options query string
                if (hardwareUuid != null && !hardwareUuid.isEmpty()) {
                    String hardwareUuidQueryString = String.format("LOWER(%s.data -> 'columns' -> 4 -> 'value' -> 'host_info' ->> 'hardware_uuid') = '%s' ", auditLogAbbrv, hardwareUuid.toLowerCase());
                    additionalOptionsQueryString = String.format("%s AND %s", additionalOptionsQueryString, hardwareUuidQueryString);
                }
            }

            //Build host state partial query string and add it to the additional options query string
            if (hostState != null && !hostState.isEmpty()) {
                String hostStateQueryString = String.format("%s.data -> 'columns' -> 2 -> 'value' ->> 'host_state' = '%s'", auditLogAbbrv, hostState.toUpperCase());
                additionalOptionsQueryString = String.format("%s AND %s", additionalOptionsQueryString, hostStateQueryString);
            }

            //Build host status ID partial query string and add it to the additional options query string
            if (hostStatusId != null && !hostStatusId.isEmpty()) {
                String hostStatusIdQueryString = String.format("%s.entity_id = '%s'", auditLogAbbrv, hostStatusId);
                additionalOptionsQueryString = String.format("%s AND %s", additionalOptionsQueryString, hostStatusIdQueryString);
            }

            //Build aik certificate query string and add it to the additional options query string
            if (aikCertificate != null && !aikCertificate.isEmpty()) {
                String aikCertificateQueryString = String.format("%s.data -> 'columns' -> 4 -> 'value' ->> 'aik_certificate' = '%s'", auditLogAbbrv, aikCertificate);
                additionalOptionsQueryString = String.format("%s AND %s", additionalOptionsQueryString, aikCertificateQueryString);
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

            if (tableJoinString != null && !tableJoinString.isEmpty()) {
                additionalOptionsQueryString = String.format("%s %s", tableJoinString, additionalOptionsQueryString);
            }


            String formattedQuery = String.format("SELECT au.* "
                    + "FROM mw_audit_log_entry au ");

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
            return convertToHostStatus(auditLogEntryList);
        } finally {
            em.close();
        }
    }

    private List<MwHostStatus> convertToHostStatus(List<AuditLogEntry> auditLogEntryList) {
        List<MwHostStatus> hostStatusList = new ArrayList();
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

                //Set host status id if audit log entry entity id is not null
                String hostStatusId = null;
                if (auditLogEntry.getEntityId() != null
                        && !auditLogEntry.getEntityId().isEmpty()) {
                    hostStatusId = auditLogEntry.getEntityId();
                }

                //Set host id if audit log entry data field has host id value
                String hostId = null;
                if (auditLogEntry.getData().getColumns().get(1) != null
                        && auditLogEntry.getData().getColumns().get(1).getValue() != null) {
                    hostId = auditLogEntry.getData().getColumns().get(1).getValue().toString();
                }

                //Set host status information if audit log entry data field has host status information value
                HostStatusInformation hostStatusInformation = null;
                if (auditLogEntry.getData().getColumns().get(2) != null
                        && auditLogEntry.getData().getColumns().get(2).getValue() != null) {
                    ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
                    hostStatusInformation = mapper.convertValue(auditLogEntry.getData().getColumns().get(2).getValue(), HostStatusInformation.class);
                }

                //Set created if audit log entry data field has created value
                Date created = null;
                if (auditLogEntry.getData().getColumns().get(3) != null
                        && auditLogEntry.getData().getColumns().get(3).getValue() != null) {
                    String createdString = auditLogEntry.getData().getColumns().get(3).getValue().toString();
                    created = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(createdString);
                }

                //Set host manifest if audit log entry data field has host manifest value
                HostManifest hostManifest = null;
                if (auditLogEntry.getData().getColumns().get(4) != null
                        && auditLogEntry.getData().getColumns().get(4).getValue() != null) {
                    ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
                    hostManifest = mapper.convertValue(auditLogEntry.getData().getColumns().get(4).getValue(), HostManifest.class);
                }

                //Build host status entity object and add the object to the array list
                MwHostStatus hostStatus = new MwHostStatus();
                hostStatus.setId(hostStatusId);
                hostStatus.setHostId(hostId);
                hostStatus.setCreated(created);
                hostStatus.setStatus(hostStatusInformation);
                hostStatus.setHostManifest(hostManifest);
                hostStatusList.add(hostStatus);
            }

        } catch (ParseException ex) {
            log.error("Error converting audit log entry entity model to host status entity", ex);
        }
        return hostStatusList;
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
