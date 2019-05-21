/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.controller;

import com.intel.mtwilson.flavor.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.flavor.controller.exceptions.PreexistingEntityException;
import com.intel.mtwilson.flavor.data.MwLinkFlavorgroupHost;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
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
public class MwLinkFlavorgroupHostJpaController implements Serializable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MwLinkFlavorgroupHostJpaController.class);

    public MwLinkFlavorgroupHostJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MwLinkFlavorgroupHost mwLinkFlavorgroupHost) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(mwLinkFlavorgroupHost);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findMwLinkFlavorgroupHost(mwLinkFlavorgroupHost.getId()) != null) {
                throw new PreexistingEntityException("The link between flavor group and host " + mwLinkFlavorgroupHost + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(MwLinkFlavorgroupHost mwLinkFlavorgroupHost) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            mwLinkFlavorgroupHost = em.merge(mwLinkFlavorgroupHost);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = mwLinkFlavorgroupHost.getId();
                if (findMwLinkFlavorgroupHost(id) == null) {
                    throw new NonexistentEntityException("The link between flavor group and host with id " + id + " no longer exists.");
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
            MwLinkFlavorgroupHost mwLinkFlavorgroupHost;
            try {
                mwLinkFlavorgroupHost = em.getReference(MwLinkFlavorgroupHost.class, id);
                mwLinkFlavorgroupHost.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The link between flavor group and host with id " + id + " no longer exists.", enfe);
            }
            em.remove(mwLinkFlavorgroupHost);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<MwLinkFlavorgroupHost> findMwLinkFlavorgroupHostEntities() {
        return findMwLinkFlavorgroupHostEntities(true, -1, -1);
    }

    public List<MwLinkFlavorgroupHost> findMwLinkFlavorgroupHostEntities(int maxResults, int firstResult) {
        return findMwLinkFlavorgroupHostEntities(false, maxResults, firstResult);
    }

    private List<MwLinkFlavorgroupHost> findMwLinkFlavorgroupHostEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwLinkFlavorgroupHost.class));
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

    public MwLinkFlavorgroupHost findMwLinkFlavorgroupHost(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwLinkFlavorgroupHost.class, id);
        } finally {
            em.close();
        }
    }

    public int getMwLinkFlavorgroupHostCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwLinkFlavorgroupHost> rt = cq.from(MwLinkFlavorgroupHost.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    public List<MwLinkFlavorgroupHost> findMwLinkFlavorgroupHostByFlavorgroupId(String flavorgroupId) {
        List<MwLinkFlavorgroupHost> mwLinkFlavorgroupHostList = null;
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("MwLinkFlavorgroupHost.findByFlavorgroupId");
            query.setParameter("flavorgroupId", flavorgroupId);
            if (query.getResultList() != null && !query.getResultList().isEmpty()) {
                mwLinkFlavorgroupHostList = query.getResultList();
            }
            return mwLinkFlavorgroupHostList;
        } finally {
            em.close();
        }
    }
    
    public List<MwLinkFlavorgroupHost> findMwLinkFlavorgroupHostByHostId(String hostId) {
        List<MwLinkFlavorgroupHost> mwLinkFlavorgroupHostList = null;
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("MwLinkFlavorgroupHost.findByHostId");
            query.setParameter("hostId", hostId);
            if (query.getResultList() != null && !query.getResultList().isEmpty()) {
                mwLinkFlavorgroupHostList = query.getResultList();
            }
            return mwLinkFlavorgroupHostList;
        } finally {
            em.close();
        }
    }
    
    public MwLinkFlavorgroupHost findMwLinkFlavorgroupHostByBothIds(String flavorgroupId, String hostId) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("MwLinkFlavorgroupHost.findByBothIds");
            query.setParameter("flavorgroupId", flavorgroupId);
            query.setParameter("hostId", hostId);
            MwLinkFlavorgroupHost result = (MwLinkFlavorgroupHost) query.getSingleResult();
            return result;
        } catch(NoResultException e){
            log.info( "No flavorgroup [{}] host [{}] link exists", flavorgroupId, hostId);
            return null;
        } finally {
            em.close();
        }
    }
}
