/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.controller;

import com.intel.mtwilson.flavor.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.flavor.data.MwLinkFlavorHost;
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
import org.apache.commons.lang.exception.ExceptionUtils;
import org.postgresql.util.PSQLException;

/**
 *
 * @author rksavino
 */
public class MwLinkFlavorHostJpaController implements Serializable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MwLinkFlavorHostJpaController.class);

    public MwLinkFlavorHostJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MwLinkFlavorHost mwLinkFlavorHost) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(mwLinkFlavorHost);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (PSQLException.class.equals(rootCause.getClass()) && "23505".equals(((PSQLException) rootCause).getSQLState())) {
                log.error("The link between flavor and host with id {} already exists", mwLinkFlavorHost.getId());
            } else {
                throw e;
            }
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(MwLinkFlavorHost mwLinkFlavorHost) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            mwLinkFlavorHost = em.merge(mwLinkFlavorHost);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = mwLinkFlavorHost.getId();
                if (findMwLinkFlavorHost(id) == null) {
                    throw new NonexistentEntityException("The link between flavor and host with id " + id + " no longer exists.");
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
            MwLinkFlavorHost mwLinkFlavorHost;
            try {
                mwLinkFlavorHost = em.getReference(MwLinkFlavorHost.class, id);
                mwLinkFlavorHost.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The link between flavor and host with id " + id + " no longer exists.", enfe);
            }
            em.remove(mwLinkFlavorHost);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<MwLinkFlavorHost> findMwLinkFlavorHostEntities() {
        return findMwLinkFlavorHostEntities(true, -1, -1);
    }

    public List<MwLinkFlavorHost> findMwLinkFlavorHostEntities(int maxResults, int firstResult) {
        return findMwLinkFlavorHostEntities(false, maxResults, firstResult);
    }

    private List<MwLinkFlavorHost> findMwLinkFlavorHostEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwLinkFlavorHost.class));
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

    public MwLinkFlavorHost findMwLinkFlavorHost(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwLinkFlavorHost.class, id);
        } finally {
            em.close();
        }
    }

    /* *
     *  Retrieves the list of Flavor and host link entries given a flavorID
     * */
    public List<MwLinkFlavorHost> getMwLinkFlavorHostByFlavorId(String flavorId) {
        List<MwLinkFlavorHost> mwLinkFlavorHostList = new ArrayList<MwLinkFlavorHost>();
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("MwLinkFlavorHost.findByFlavorId");
            query.setParameter("flavorId", flavorId);
            if (query.getResultList() != null && !query.getResultList().isEmpty()) {
                mwLinkFlavorHostList = query.getResultList();
            }
            return mwLinkFlavorHostList;
        }finally {
            em.close();
        }
    }

    public int getMwLinkFlavorHostCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwLinkFlavorHost> rt = cq.from(MwLinkFlavorHost.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    public List<MwLinkFlavorHost> findMwLinkFlavorHostByFlavorId(String flavorId) {
        List<MwLinkFlavorHost> mwLinkFlavorHostList = null;
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("MwLinkFlavorHost.findByFlavorId");
            query.setParameter("flavorId", flavorId);
            if (query.getResultList() != null && !query.getResultList().isEmpty()) {
                mwLinkFlavorHostList = query.getResultList();
            }
            return mwLinkFlavorHostList;
        } finally {
            em.close();
        }
    }
    
    public List<MwLinkFlavorHost> findMwLinkFlavorHostByHostId(String hostId) {
        List<MwLinkFlavorHost> mwLinkFlavorHostList = null;
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("MwLinkFlavorHost.findByHostId");
            query.setParameter("hostId", hostId);
            if (query.getResultList() != null && !query.getResultList().isEmpty()) {
                mwLinkFlavorHostList = query.getResultList();
            }
            return mwLinkFlavorHostList;
        } finally {
            em.close();
        }
    }
    
    public MwLinkFlavorHost findMwLinkFlavorHostByBothIds(String flavorId, String hostId) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("MwLinkFlavorHost.findByBothIds");
            query.setParameter("flavorId", flavorId);
            query.setParameter("hostId", hostId);
            MwLinkFlavorHost result = (MwLinkFlavorHost) query.getSingleResult();
            return result;
        } catch(NoResultException e){
            log.info( "NoResultException : No flavor [{}] host [{}] link exists", flavorId, hostId);
            return null;
        } finally {
            em.close();
        }
    }
}
