/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.controller;

import com.intel.mtwilson.flavor.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.flavor.controller.exceptions.PreexistingEntityException;
import com.intel.mtwilson.flavor.data.MwHostCredential;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;


public class MwHostCredentialJpaController implements Serializable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MwHostCredentialJpaController.class);

    public MwHostCredentialJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MwHostCredential mwHostPreRegistrationDetails) throws PreexistingEntityException, Exception {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(mwHostPreRegistrationDetails);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findMwHostCredential(mwHostPreRegistrationDetails.getId()) != null) {
                throw new PreexistingEntityException("MwHostCredential " + mwHostPreRegistrationDetails + " already exists.", ex);
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public void edit(MwHostCredential mwHostPreRegistrationDetails) throws NonexistentEntityException, Exception {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(mwHostPreRegistrationDetails);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = mwHostPreRegistrationDetails.getId();
                if (findMwHostCredential(id) == null) {
                    throw new NonexistentEntityException("The MwHostCredential with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public void destroy(String id) throws NonexistentEntityException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            MwHostCredential mwHostPreRegistrationDetails;
            try {
                mwHostPreRegistrationDetails = em.getReference(MwHostCredential.class, id);
                mwHostPreRegistrationDetails.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The MwHostCredential with id " + id + " no longer exists.", enfe);
            }
            em.remove(mwHostPreRegistrationDetails);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<MwHostCredential> findMwHostCredentialEntities() {
        return findMwHostCredentialEntities(true, -1, -1);
    }

    public List<MwHostCredential> findMwHostCredentialEntities(int maxResults, int firstResult) {
        return findMwHostCredentialEntities(false, maxResults, firstResult);
    }

    private List<MwHostCredential> findMwHostCredentialEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwHostCredential.class));
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

    public MwHostCredential findMwHostCredential(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwHostCredential.class, id);
        } finally {
            em.close();
        }
    }
    
    public MwHostCredential findByHostId(String id) {

        MwHostCredential host = null;
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("MwHostCredential.findByHostId");
            query.setParameter("hostId", id);

            List<MwHostCredential> list = query.getResultList();
            if (list != null && list.size() > 0) {
                host = list.get(0);

            }
        } finally {
            em.close();
        }
        return host;
    }
    
    public MwHostCredential findByHostName(String hostName) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("MwHostCredential.findByHostName");
            query.setParameter("hostName", hostName);
            query.setMaxResults(1);
            MwHostCredential result = (MwHostCredential) query.getSingleResult();
            return result;
        } catch(NoResultException e){
            log.debug( "No host credential exists for host with the specified name [{}]: {}", hostName, e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }
    
    public MwHostCredential findByHardwareUuid(String id) {

        MwHostCredential host = null;
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("MwHostCredential.findByHardwareUuid");
            query.setParameter("hardwareUuid", id);

            List<MwHostCredential> list = query.getResultList();
            if (list != null && list.size() > 0) {
                host = list.get(0);

            }
        } finally {
            em.close();
        }
        return host;
    }
        
    public int getMwHostCredentialCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwHostCredential> rt = cq.from(MwHostCredential.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    
}
