/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.controller;

import com.intel.mtwilson.flavor.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.flavor.controller.exceptions.PreexistingEntityException;
import com.intel.mtwilson.flavor.data.MwHost;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class MwHostJpaController implements Serializable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MwHostJpaController.class);
    
    public MwHostJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MwHost mwHost) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(mwHost);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findMwHost(mwHost.getId()) != null) {
                throw new PreexistingEntityException("The host " + mwHost + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(MwHost mwHost) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.merge(mwHost);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = mwHost.getId();
                if (findMwHost(id) == null) {
                    throw new NonexistentEntityException("The host with id " + id + " no longer exists.");
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
            MwHost mwHost;
            try {
                mwHost = em.getReference(MwHost.class, id);
                mwHost.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The host with id " + id + " no longer exists.", enfe);
            }
            em.remove(mwHost);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<MwHost> findMwHostEntities() {
        return findMwHostEntities(true, -1, -1);
    }

    public List<MwHost> findMwHostEntities(int maxResults, int firstResult) {
        return findMwHostEntities(false, maxResults, firstResult);
    }

    private List<MwHost> findMwHostEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwHost.class));
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

    public MwHost findMwHost(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwHost.class, id);
        } finally {
            em.close();
        }
    }

    public int getMwHostCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwHost> rt = cq.from(MwHost.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    public MwHost findMwHostByName(String name) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("MwHost.findByName");
            query.setParameter("name", name);
            MwHost result = (MwHost) query.getSingleResult();
            return result;

        } catch(NoResultException e){
            log.trace("No Host exists with the specified name: {}", name);
            return null;
        } finally {
            em.close();
        }
    }
    
    public List<MwHost> findMwHostByNameLike(String name) {
        List<MwHost> mwHostList = null;
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("MwHost.findByNameLike");
            query.setParameter("name", "%" + name + "%");
            if (query.getResultList() != null && !query.getResultList().isEmpty()) {
                mwHostList = query.getResultList();
            }
            return mwHostList;
        } finally {
            em.close();
        }
    }
    
    public MwHost findMwHostByHardwareUuid(String hardwareUuid) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("MwHost.findByHardwareUuid");
            query.setParameter("hardwareUuid", hardwareUuid);
            MwHost result = (MwHost) query.getSingleResult();
            return result;

        } catch(NoResultException e){
            log.trace("No Host exists with the specified hardware UUID: {}", hardwareUuid);
            return null;
        }finally {
            em.close();
        }
    }
    
    public MwHost findMwHostByTlsPolicyId(String tlsPolicyId) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("MwHost.findByTlsPolicyId");
            query.setParameter("tlsPolicyId", tlsPolicyId);
            MwHost result = (MwHost) query.getSingleResult();
            return result;

        } catch(NoResultException e){
            log.trace("No Host exists with the specified tlsPolicy ID: {}", tlsPolicyId);
            return null;
        } finally {
            em.close();
        }
    }
    
    public List<MwHost> findMwHostForHostStatus(String status, int limit) {
        List<MwHost> hostList = null;
        EntityManager em = getEntityManager();
        String formattedQuery;
        try {
            if (status == null || status.isEmpty()) {
                status = "CONNECTED";
            }
            status = status.toUpperCase();
            formattedQuery = "SELECT h.* " +
                    "FROM mw_host h " +
                    "INNER JOIN mw_host_status hs ON hs.host_id = h.id " +
                    "INNER JOIN " +
                    "(SELECT host_id,MAX(created) AS max_date " +
                    "FROM mw_host_status " +
                    "GROUP BY host_id)a " +
                    "ON a.host_id = hs.host_id AND a.max_date = created " +
                    "AND hs.status->>'host_state' = ? " +
                    "ORDER BY hs.created DESC";
            Query query = em.createNativeQuery(formattedQuery, MwHost.class);
            query.setParameter(1, status);
            query.setMaxResults(limit);
            if (query.getResultList() != null && !query.getResultList().isEmpty()) {
                hostList = query.getResultList();
            }
            return hostList;
        } finally {
            em.close();
        }
    }
    
    //This method is used to get the list of hostId and forceUpdate values
    public Map<String, Boolean> filterHostIdFromMwQueue(List<String> hostIdList) {
        EntityManager em = getEntityManager();
	String hostList = "";
        if (hostIdList.size() == 0){
            hostList = "''";
        }        
        for (String hostId : hostIdList) {
            hostList = String.format("%s,'%s'", hostList, hostId.replaceAll("''", "'")).replaceAll("^,+", "").replaceAll(",+$", "");
        }
        
        try {
            String formattedQuery = String.format("SELECT action_parameters ->> 'host_id' AS host_id, "
                    + "action_parameters ->> 'force_update' AS force_update "
                    + "FROM mw_queue WHERE queue_action = 'flavor-verify' "
                    + "AND action_parameters ->> 'host_id' IN (?) ");
            Query query = em.createNativeQuery(formattedQuery);
            query.setParameter(1, hostList);
            Map<String, Boolean> resultMap = null;
            if (query.getResultList() != null && !query.getResultList().isEmpty()) {
                List<Object[]> result = query.getResultList();
                resultMap = new HashMap();
                for (Object[] obj : result) {
                    resultMap.put(obj[0].toString(), Boolean.valueOf(obj[1].toString()));
                }
            }
            return resultMap;
        } finally {
            em.close();
        }
    }
}
