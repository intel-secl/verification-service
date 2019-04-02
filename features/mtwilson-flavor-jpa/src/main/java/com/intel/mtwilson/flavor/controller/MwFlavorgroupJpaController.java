/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.controller;

import com.intel.mtwilson.flavor.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.flavor.controller.exceptions.PreexistingEntityException;
import com.intel.mtwilson.flavor.data.MwFlavorgroup;
import java.io.Serializable;
import java.util.List;
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
public class MwFlavorgroupJpaController implements Serializable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MwFlavorgroupJpaController.class);
    
    public MwFlavorgroupJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MwFlavorgroup mwFlavorgroup) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(mwFlavorgroup);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findMwFlavorgroup(mwFlavorgroup.getId()) != null) {
                throw new PreexistingEntityException("The flavor group " + mwFlavorgroup + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(MwFlavorgroup mwFlavorgroup) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            mwFlavorgroup = em.merge(mwFlavorgroup);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = mwFlavorgroup.getId();
                if (findMwFlavorgroup(id) == null) {
                    throw new NonexistentEntityException("The flavor group with id " + id + " no longer exists.");
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
            MwFlavorgroup mwFlavorgroup;
            try {
                mwFlavorgroup = em.getReference(MwFlavorgroup.class, id);
                mwFlavorgroup.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The flavor group with id " + id + " no longer exists.", enfe);
            }
            em.remove(mwFlavorgroup);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<MwFlavorgroup> findMwFlavorgroupEntities() {
        return findMwFlavorgroupEntities(true, -1, -1);
    }

    public List<MwFlavorgroup> findMwFlavorgroupEntities(int maxResults, int firstResult) {
        return findMwFlavorgroupEntities(false, maxResults, firstResult);
    }

    private List<MwFlavorgroup> findMwFlavorgroupEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwFlavorgroup.class));
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

    public MwFlavorgroup findMwFlavorgroup(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwFlavorgroup.class, id);
        } finally {
            em.close();
        }
    }

    public int getMwFlavorgroupCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwFlavorgroup> rt = cq.from(MwFlavorgroup.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    public MwFlavorgroup findMwFlavorgroupByName(String name) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("MwFlavorgroup.findByName");
            query.setParameter("name", name);
            MwFlavorgroup result = (MwFlavorgroup) query.getSingleResult();
            return result;
        } catch(NoResultException e){
            log.debug( "NoResultException : No flavor group exists with the specified name: {}", name);
            return null;
        } finally {
            em.close();
        }
    }
    
    public List<MwFlavorgroup> findMwFlavorgroupByNameLike(String name) {
        List<MwFlavorgroup> mwFlavorgroupList = null;
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("MwFlavorgroup.findByNameLike");
            query.setParameter("name", "%" + name + "%");
            if (query.getResultList() != null && !query.getResultList().isEmpty()) {
                mwFlavorgroupList = query.getResultList();
            }
            return mwFlavorgroupList;
        } finally {
            em.close();
        }
    }
}
