/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.flavor.controller.MwLinkFlavorgroupHostJpaController;
import com.intel.mtwilson.flavor.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.flavor.data.MwLinkFlavorgroupHost;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupHostLink;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupHostLinkCollection;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupHostLinkFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupHostLinkLocator;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author hmgowda
 */
public class FlavorgroupHostLinkRepository {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FlavorgroupHostLinkRepository.class);
    
    public FlavorgroupHostLinkCollection search(FlavorgroupHostLinkFilterCriteria criteria) {
        log.debug("Received request to search for flavorgroup host link associations");
        FlavorgroupHostLinkCollection flavorgroupHostLinkCollection = new FlavorgroupHostLinkCollection();
        try {
            MwLinkFlavorgroupHostJpaController mwLinkFlavorgroupHostJpaController = My.jpa().mwLinkFlavorgroupHost();
            if (criteria.filter == false) {
                List<MwLinkFlavorgroupHost> mwLinkFlavorgroupHostList = mwLinkFlavorgroupHostJpaController.findMwLinkFlavorgroupHostEntities();
                if (mwLinkFlavorgroupHostList != null && !mwLinkFlavorgroupHostList.isEmpty()) {
                    for (MwLinkFlavorgroupHost mwLinkFlavorgroupHost : mwLinkFlavorgroupHostList) {
                        flavorgroupHostLinkCollection.getFlavorgroupHostLinks().add(convert(mwLinkFlavorgroupHost));
                    }
                }
            } else if (criteria.id != null) {
                FlavorgroupHostLink flavorgroupHostLink = retrieve(new FlavorgroupHostLinkLocator(criteria.id));
                if (flavorgroupHostLink != null) {
                    flavorgroupHostLinkCollection.getFlavorgroupHostLinks().add(flavorgroupHostLink);
                }
            } else if (criteria.flavorgroupId != null && criteria.hostId != null) {
                FlavorgroupHostLink flavorgroupHostLink = retrieve(new FlavorgroupHostLinkLocator(criteria.flavorgroupId, criteria.hostId));
                if (flavorgroupHostLink != null) {
                    flavorgroupHostLinkCollection.getFlavorgroupHostLinks().add(flavorgroupHostLink);
                }
            } else if (criteria.flavorgroupId != null) {
                List<MwLinkFlavorgroupHost> mwLinkFlavorgroupHostList =
                        mwLinkFlavorgroupHostJpaController.findMwLinkFlavorgroupHostByFlavorgroupId(criteria.flavorgroupId.toString());
                if (mwLinkFlavorgroupHostList != null && !mwLinkFlavorgroupHostList.isEmpty()) {
                    for (MwLinkFlavorgroupHost mwLinkFlavorgroupHost : mwLinkFlavorgroupHostList) {
                        flavorgroupHostLinkCollection.getFlavorgroupHostLinks().add(convert(mwLinkFlavorgroupHost));
                    }
                }
            } else if (criteria.hostId != null) {
                List<MwLinkFlavorgroupHost> mwLinkFlavorgroupHostList =
                        mwLinkFlavorgroupHostJpaController.findMwLinkFlavorgroupHostByHostId(criteria.hostId.toString());
                if (mwLinkFlavorgroupHostList != null && !mwLinkFlavorgroupHostList.isEmpty()) {
                    for (MwLinkFlavorgroupHost mwLinkFlavorgroupHost : mwLinkFlavorgroupHostList) {
                        flavorgroupHostLinkCollection.getFlavorgroupHostLinks().add(convert(mwLinkFlavorgroupHost));
                    }
                }
            } else {
                // Invalid search criteria specified. Just log the error and return back empty collection.
                log.error("Invalid flavorgroup host link search criteria specified");
            }
        } catch (Exception ex) {
            log.error("Error during search for flavorgroup host link associations", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("Returning back {} flavorgroup host link results", flavorgroupHostLinkCollection.getFlavorgroupHostLinks().size());
        return flavorgroupHostLinkCollection;
    }
    
    public FlavorgroupHostLink retrieve(FlavorgroupHostLinkLocator locator) {
        log.debug("Received request to retrieve flavorgroup host link");
        if (locator == null || (locator.id == null
                && locator.pathId == null
                && (locator.flavorgroupId == null || locator.hostId == null))) {
            return null;
        }
        
        try {
            MwLinkFlavorgroupHostJpaController mwLinkFlavorgroupHostJpaController = My.jpa().mwLinkFlavorgroupHost();
            if (locator.pathId != null) {
                MwLinkFlavorgroupHost mwLinkFlavorgroupHost
                        = mwLinkFlavorgroupHostJpaController.findMwLinkFlavorgroupHost(locator.pathId.toString());
                if (mwLinkFlavorgroupHost != null) {
                    return convert(mwLinkFlavorgroupHost);
                }
            } else if (locator.id != null) {
                MwLinkFlavorgroupHost mwLinkFlavorgroupHost
                        = mwLinkFlavorgroupHostJpaController.findMwLinkFlavorgroupHost(locator.id.toString());
                if (mwLinkFlavorgroupHost != null) {
                    return convert(mwLinkFlavorgroupHost);
                }
            } else if (locator.flavorgroupId != null && locator.hostId != null) {
                MwLinkFlavorgroupHost mwLinkFlavorgroupHost
                        = mwLinkFlavorgroupHostJpaController.findMwLinkFlavorgroupHostByBothIds(
                        locator.flavorgroupId.toString(), locator.hostId.toString());
                if (mwLinkFlavorgroupHost != null) {
                    return convert(mwLinkFlavorgroupHost);
                }
            }
        } catch (Exception ex) {
            log.error("Error during retrieve for flavorgroup host link", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }
        return null;
    }
    
    public void store(FlavorgroupHostLink item) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public FlavorgroupHostLink create(FlavorgroupHostLink item) {
        log.debug("Got request to create a new flavorgroup host link");
        UUID flavorgroupHostLinkId;
        if (item.getId() != null) {
            flavorgroupHostLinkId = item.getId();
        } else {
            flavorgroupHostLinkId = new UUID();
        }
        
        FlavorgroupHostLinkLocator locator = new FlavorgroupHostLinkLocator();
        locator.id = flavorgroupHostLinkId;
        
        if (item == null || item.getFlavorgroupId() == null || item.getHostId() == null) {
            log.error("Flavorgroup ID and host ID must be specified");
            throw new RepositoryInvalidInputException(locator);
        }
        
        try {
            MwLinkFlavorgroupHostJpaController flavorgroupHostJpa = My.jpa().mwLinkFlavorgroupHost();
            List<MwLinkFlavorgroupHost> mwLinkFlavorgroupHostList = flavorgroupHostJpa.findMwLinkFlavorgroupHostByHostId(item.getHostId().toString());
            if (mwLinkFlavorgroupHostList != null) {                
                for (MwLinkFlavorgroupHost links : mwLinkFlavorgroupHostList) {
                    if (links.getFlavorgroupId().equals(item.getFlavorgroupId().toString())) {
                        log.error("Flavorgroup [{}] host [{}] link already exists", item.getFlavorgroupId().toString(), item.getHostId().toString());
                        throw new RepositoryInvalidInputException(locator);
                    }
                }
            }
            
            MwLinkFlavorgroupHost mwLinkFlavorgroupHost = new MwLinkFlavorgroupHost();
            mwLinkFlavorgroupHost.setId(flavorgroupHostLinkId.toString());
            mwLinkFlavorgroupHost.setFlavorgroupId(item.getFlavorgroupId().toString());
            mwLinkFlavorgroupHost.setHostId(item.getHostId().toString());
            flavorgroupHostJpa.create(mwLinkFlavorgroupHost);
            return item;
        } catch (IOException Ex) {
            log.error("Error while associating the host [{}] with the flavorgroup [{}]", item.getHostId(), item.getFlavorgroupId());
            throw new RepositoryCreateException(Ex);
        } catch (Exception ex) {
            log.error("Error while associating the host [{}] with the flavor group [{}]", item.getHostId(), item.getFlavorgroupId());
            throw new RepositoryCreateException(ex);
        }
    }

    public void delete(FlavorgroupHostLinkLocator locator) {
        log.debug("Received request to delete flavorgroup host link");
        
        if (locator == null || locator.id == null && (locator.flavorgroupId == null && locator.hostId == null)) {
            throw new RepositoryInvalidInputException("Invalid flavorgroup host link ID");
        }
        MwLinkFlavorgroupHost linkFlavorgroupTbl = null;
        try {
            MwLinkFlavorgroupHostJpaController flavorgroupHostJpa = My.jpa().mwLinkFlavorgroupHost();

            if (locator.flavorgroupId != null && locator.hostId != null){
                linkFlavorgroupTbl = flavorgroupHostJpa.findMwLinkFlavorgroupHostByBothIds(locator.flavorgroupId.toString(), locator.hostId.toString());
                if (linkFlavorgroupTbl == null) {
                    log.error("No flavorgroup host link found with flavorgroupid: {} and hostId: {}", locator.flavorgroupId.toString(), locator.hostId.toString());
                    throw new RepositoryInvalidInputException("Invalid flavorgroup host link");
                }
            }
            else {
                linkFlavorgroupTbl = flavorgroupHostJpa.findMwLinkFlavorgroupHost(locator.id.toString());
                if (linkFlavorgroupTbl == null) {
                    log.error("No flavorgroup host link found with id: {}", locator.id.toString());
                    throw new RepositoryInvalidInputException("Invalid flavorgroup host link");
                }
            }
            flavorgroupHostJpa.destroy(linkFlavorgroupTbl.getId());
        } catch (IOException ex) {
            log.error("Error while deleting the flavorgroup host link");
            throw new RepositoryDeleteException(ex, locator);
        } catch (NonexistentEntityException Ex) {
            log.error("Error while deleting the flavorgroup host link with id: {}", linkFlavorgroupTbl.getId());
            throw new RepositoryDeleteException(Ex, locator);
        }
    }

    public void delete(FlavorgroupHostLinkFilterCriteria criteria) {
        log.debug("FlavorgroupHostLinkRepository:Delete - Got request to delete FlavorgroupHostLink by search criteria.");        
        FlavorgroupHostLinkCollection objCollection = search(criteria);
        try { 
            for (FlavorgroupHostLink obj : objCollection.getFlavorgroupHostLinks()) {
                FlavorgroupHostLinkLocator locator = new FlavorgroupHostLinkLocator(obj.getId());
                delete(locator);
            }
        } catch (RepositoryDeleteException re) {
            throw re;
        } catch (Exception ex) {
            log.error("FlavorgroupHostLinkRepository:Delete - Error during FlavorgroupHostLink deletion.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }
    
    private FlavorgroupHostLink convert(MwLinkFlavorgroupHost mwLinkFlavorgroupHost) {
        FlavorgroupHostLink flavorgroupHostLink = new FlavorgroupHostLink();
        flavorgroupHostLink.setId(UUID.valueOf(mwLinkFlavorgroupHost.getId()));
        flavorgroupHostLink.setFlavorgroupId(UUID.valueOf(mwLinkFlavorgroupHost.getFlavorgroupId()));
        flavorgroupHostLink.setHostId(UUID.valueOf(mwLinkFlavorgroupHost.getHostId()));
        return flavorgroupHostLink;
    }
}
