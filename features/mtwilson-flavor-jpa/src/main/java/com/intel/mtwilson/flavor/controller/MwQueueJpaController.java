/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.controller;

import com.intel.mtwilson.flavor.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.flavor.controller.exceptions.PreexistingEntityException;
import com.intel.mtwilson.flavor.data.MwQueue;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author rksavino
 */
public class MwQueueJpaController implements Serializable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MwQueueJpaController.class);
    
    public MwQueueJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MwQueue mwQueue) throws PreexistingEntityException {
        EntityManager em = null;
        try {
            Date createdDate = Calendar.getInstance().getTime();
            mwQueue.setCreated(createdDate);
            mwQueue.setUpdated(createdDate);
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(mwQueue);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findMwQueue(mwQueue.getId()) != null) {
                throw new PreexistingEntityException("The queue entry " + mwQueue + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    
    public void createQueueList(List<MwQueue> mwQueueList) {
       int batchSize = 25;
        EntityManager em = getEntityManager();
        EntityTransaction entityTransaction = em.getTransaction();
        try {
            entityTransaction.begin();
            for (int i = 0; i < mwQueueList.size(); i++) {
                if (i > 0 && i % batchSize == 0) {
                    entityTransaction.commit();
                    entityTransaction.begin();
                    em.clear();
                }
                MwQueue mwQueue = mwQueueList.get(i);
                Date createdDate = Calendar.getInstance().getTime();
                mwQueue.setCreated(createdDate);
                mwQueue.setUpdated(createdDate);
                em.persist(mwQueue);
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

    public void edit(MwQueue mwQueue) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            mwQueue.setUpdated(Calendar.getInstance().getTime());
            em = getEntityManager();
            em.getTransaction().begin();
            em.merge(mwQueue);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = mwQueue.getId();
                if (findMwQueue(id) == null) {
                    throw new NonexistentEntityException("The queue entry with id " + id + " no longer exists.");
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
            MwQueue mwQueue;
            try {
                mwQueue = em.getReference(MwQueue.class, id);
                mwQueue.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The queue entry with id " + id + " no longer exists.", enfe);
            }
            em.remove(mwQueue);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<MwQueue> findMwQueueEntities() {
        return findMwQueueEntities(true, -1, -1);
    }

    public List<MwQueue> findMwQueueEntities(int maxResults, int firstResult) {
        return findMwQueueEntities(false, maxResults, firstResult);
    }

    private List<MwQueue> findMwQueueEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwQueue.class));
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

    public MwQueue findMwQueue(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwQueue.class, id);
        } finally {
            em.close();
        }
    }

    public int getMwQueueCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwQueue> rt = cq.from(MwQueue.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    public List<MwQueue> findMwQueueByActionParameter(String action, String actionParameter, String value) {
        List<MwQueue> mwQueueList = null;
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNativeQuery("SELECT * FROM mw_queue WHERE queue_action = ? AND action_parameters ->> ? = ?", MwQueue.class);
            query.setParameter(1, action);
            query.setParameter(2, actionParameter);
            query.setParameter(3, value);
            if (query.getResultList() != null && !query.getResultList().isEmpty()) {
                mwQueueList = query.getResultList();
            }
            return mwQueueList;
        } finally {
            em.close();
        }
    }
    
    public List<MwQueue> findMwQueueByQueueStates(List<String> queueStates) {
        List<MwQueue> mwQueueList = null;
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("MwQueue.findByQueueStates");
            query.setParameter("queueStates", queueStates);
            if (query.getResultList() != null && !query.getResultList().isEmpty()) {
                mwQueueList = query.getResultList();
            }
            return mwQueueList;
        } finally {
            em.close();
        }
    }
}
