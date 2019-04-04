/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.telemetry.controller;

import com.intel.mtwilson.telemetry.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.telemetry.controller.exceptions.PreexistingEntityException;
import com.intel.mtwilson.telemetry.data.MwTelemetry;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author hdxia
 */
public class MwTelemetryJpaController implements Serializable {

    public MwTelemetryJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MwTelemetry mwTelemetry) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(mwTelemetry);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findMwTelemetry(mwTelemetry.getId()) != null) {
                throw new PreexistingEntityException("MwTelemetry " + mwTelemetry + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(MwTelemetry mwTelemetry) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            mwTelemetry = em.merge(mwTelemetry);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = mwTelemetry.getId();
                if (findMwTelemetry(id) == null) {
                    throw new NonexistentEntityException("The mwTelemetry with id " + id + " no longer exists.");
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
            MwTelemetry mwTelemetry;
            try {
                mwTelemetry = em.getReference(MwTelemetry.class, id);
                mwTelemetry.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The mwTelemetry with id " + id + " no longer exists.", enfe);
            }
            em.remove(mwTelemetry);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<MwTelemetry> findMwTelemetryEntities() {
        return findMwTelemetryEntities(true, -1, -1);
    }

    public List<MwTelemetry> findMwTelemetryEntities(int maxResults, int firstResult) {
        return findMwTelemetryEntities(false, maxResults, firstResult);
    }

    private List<MwTelemetry> findMwTelemetryEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwTelemetry.class));
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

    public MwTelemetry findMwTelemetry(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwTelemetry.class, id);
        } finally {
            em.close();
        }
    }

    public int getMwTelemetryCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwTelemetry> rt = cq.from(MwTelemetry.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    public List<MwTelemetry> findMwTelemetryOldest() {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNativeQuery("SELECT * FROM mw_telemetry ORDER BY create_date LIMIT 1", MwTelemetry.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
}
