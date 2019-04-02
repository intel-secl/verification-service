/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.controller;

import com.intel.mtwilson.flavor.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.flavor.controller.exceptions.PreexistingEntityException;
import com.intel.mtwilson.flavor.data.MwLinkFlavorFlavorgroup;
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
public class MwLinkFlavorFlavorgroupJpaController implements Serializable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MwLinkFlavorFlavorgroupJpaController.class);

    public MwLinkFlavorFlavorgroupJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MwLinkFlavorFlavorgroup mwLinkFlavorFlavorgroup) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(mwLinkFlavorFlavorgroup);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findMwLinkFlavorFlavorgroup(mwLinkFlavorFlavorgroup.getId()) != null) {
                throw new PreexistingEntityException("The link between flavor and flavor group " + mwLinkFlavorFlavorgroup + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(MwLinkFlavorFlavorgroup mwLinkFlavorFlavorgroup) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            mwLinkFlavorFlavorgroup = em.merge(mwLinkFlavorFlavorgroup);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = mwLinkFlavorFlavorgroup.getId();
                if (findMwLinkFlavorFlavorgroup(id) == null) {
                    throw new NonexistentEntityException("The link between flavor and flavor group with id " + id + " no longer exists.");
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
            MwLinkFlavorFlavorgroup mwLinkFlavorFlavorgroup;
            try {
                mwLinkFlavorFlavorgroup = em.getReference(MwLinkFlavorFlavorgroup.class, id);
                mwLinkFlavorFlavorgroup.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The link between flavor and flavor group with id " + id + " no longer exists.", enfe);
            }
            em.remove(mwLinkFlavorFlavorgroup);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<MwLinkFlavorFlavorgroup> findMwLinkFlavorFlavorgroupEntities() {
        return findMwLinkFlavorFlavorgroupEntities(true, -1, -1);
    }

    public List<MwLinkFlavorFlavorgroup> findMwLinkFlavorFlavorgroupEntities(int maxResults, int firstResult) {
        return findMwLinkFlavorFlavorgroupEntities(false, maxResults, firstResult);
    }

    private List<MwLinkFlavorFlavorgroup> findMwLinkFlavorFlavorgroupEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwLinkFlavorFlavorgroup.class));
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


    /* *
     *  Retrieves the list of FlavorFlavorgroup link entries given a flavorID
     * */
    public List<MwLinkFlavorFlavorgroup> getMwLinkFlavorFlavorgroupByFlavorId(String flavorId) {
        List<MwLinkFlavorFlavorgroup> mwLinkFlavorFlavorgroupList = new ArrayList<MwLinkFlavorFlavorgroup>() ;
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("MwLinkFlavorFlavorgroup.findByFlavorId");
            query.setParameter("flavorId", flavorId);
            if (query.getResultList() != null && !query.getResultList().isEmpty()) {
                mwLinkFlavorFlavorgroupList = query.getResultList();
            }
            return mwLinkFlavorFlavorgroupList;
        }finally {
            em.close();
        }
    }

    public MwLinkFlavorFlavorgroup findMwLinkFlavorFlavorgroup(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwLinkFlavorFlavorgroup.class, id);
        } finally {
            em.close();
        }
    }
    
    public List<MwLinkFlavorFlavorgroup> findMwLinkFlavorFlavorgroupByFlavorId(String flavorId) {
        List<MwLinkFlavorFlavorgroup> mwLinkFlavorFlavorgroupList = null;
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("MwLinkFlavorFlavorgroup.findByFlavorId");
            query.setParameter("flavorId", flavorId);
            if (query.getResultList() != null && !query.getResultList().isEmpty()) {
                mwLinkFlavorFlavorgroupList = query.getResultList();
            }
            return mwLinkFlavorFlavorgroupList;
        } finally {
            em.close();
        }
    }
    
    public List<MwLinkFlavorFlavorgroup> findMwLinkFlavorFlavorgroupByFlavorgroupId(String flavorgroupId) {
        List<MwLinkFlavorFlavorgroup> mwLinkFlavorFlavorgroupList = null;
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("MwLinkFlavorFlavorgroup.findByFlavorgroupId");
            query.setParameter("flavorgroupId", flavorgroupId);
            if (query.getResultList() != null && !query.getResultList().isEmpty()) {
                mwLinkFlavorFlavorgroupList = query.getResultList();
            }
            return mwLinkFlavorFlavorgroupList;
        } finally {
            em.close();
        }
    }
    

    public int getMwLinkFlavorFlavorgroupCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwLinkFlavorFlavorgroup> rt = cq.from(MwLinkFlavorFlavorgroup.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    public MwLinkFlavorFlavorgroup findMwLinkFlavorFlavorgroupByBothIds(String flavorId, String flavorgroupId) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("MwLinkFlavorFlavorgroup.findByBothIds");
            query.setParameter("flavorId", flavorId);
            query.setParameter("flavorgroupId", flavorgroupId);
            MwLinkFlavorFlavorgroup result = (MwLinkFlavorFlavorgroup) query.getSingleResult();
            return result;
        } catch(NoResultException e){
            log.info( "No flavor [{}] flavorgroup [{}] link exists", flavorId, flavorgroupId);
            return null;
        } finally {
            em.close();
        }
    }
}
