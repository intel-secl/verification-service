/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.flavor.controller.MwLinkFlavorHostJpaController;
import com.intel.mtwilson.flavor.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.flavor.data.MwLinkFlavorHost;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorHostLink;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorHostLinkCollection;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorHostLinkFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorHostLinkLocator;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorLocator;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author rksavino
 */
public class FlavorHostLinkRepository {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FlavorHostLinkRepository.class);
    
    public FlavorHostLinkCollection search(FlavorHostLinkFilterCriteria criteria) {
        log.debug("FlavorHostLinkRepository:search - got request to search for flavor host links");
        FlavorHostLinkCollection flavorHostLinkCollection = new FlavorHostLinkCollection();
        try {
            MwLinkFlavorHostJpaController mwLinkFlavorHostJpaController = My.jpa().mwLinkFlavorHost();
            if (criteria.filter == false) {
                List<MwLinkFlavorHost> mwLinkFlavorHostList = mwLinkFlavorHostJpaController.findMwLinkFlavorHostEntities();
                if (mwLinkFlavorHostList != null && !mwLinkFlavorHostList.isEmpty()) {
                    for (MwLinkFlavorHost mwLinkFlavorHost : mwLinkFlavorHostList) {
                        flavorHostLinkCollection.getFlavorHostLinks().add(convert(mwLinkFlavorHost));
                    }
                }
            } else if (criteria.id != null) {
                FlavorHostLink flavorHostLink = retrieve(new FlavorHostLinkLocator(criteria.id));
                if (flavorHostLink != null) {
                    flavorHostLinkCollection.getFlavorHostLinks().add(flavorHostLink);
                }
            } else if (criteria.flavorId != null && criteria.hostId != null) {
                FlavorHostLink flavorHostLink = retrieve(new FlavorHostLinkLocator(criteria.flavorId, criteria.hostId));
                if (flavorHostLink != null) {
                    flavorHostLinkCollection.getFlavorHostLinks().add(flavorHostLink);
                }
            } else if (criteria.flavorId != null) {
                List<MwLinkFlavorHost> mwLinkFlavorHostList =
                        mwLinkFlavorHostJpaController.findMwLinkFlavorHostByFlavorId(criteria.flavorId.toString());
                if (mwLinkFlavorHostList != null && !mwLinkFlavorHostList.isEmpty()) {
                    for (MwLinkFlavorHost mwLinkFlavorHost : mwLinkFlavorHostList) {
                        flavorHostLinkCollection.getFlavorHostLinks().add(convert(mwLinkFlavorHost));
                    }
                }
            } else if (criteria.hostId != null && criteria.flavorgroupId != null) {
                List<MwLinkFlavorHost> mwLinkFlavorHostList =
                        mwLinkFlavorHostJpaController.findMwLinkFlavorHostByHostIdAndFlavorGroupId(criteria.hostId.toString(),
                                criteria.flavorgroupId.toString());
                if (mwLinkFlavorHostList != null && !mwLinkFlavorHostList.isEmpty()) {
                    for (MwLinkFlavorHost mwLinkFlavorHost : mwLinkFlavorHostList) {
                        flavorHostLinkCollection.getFlavorHostLinks().add(convert(mwLinkFlavorHost));
                    }
                }
            } else if (criteria.hostId != null) {
                List<MwLinkFlavorHost> mwLinkFlavorHostList =
                        mwLinkFlavorHostJpaController.findMwLinkFlavorHostByHostId(criteria.hostId.toString());
                if (mwLinkFlavorHostList != null && !mwLinkFlavorHostList.isEmpty()) {
                    for (MwLinkFlavorHost mwLinkFlavorHost : mwLinkFlavorHostList) {
                        flavorHostLinkCollection.getFlavorHostLinks().add(convert(mwLinkFlavorHost));
                    }
                }
            } else {
                // Invalid search criteria specified. Just log the error and return back empty collection.
                log.error("FlavorHostLinkRepository:search - invalid flavor host link search criteria specified");
            }
        } catch (Exception ex) {
            log.error("FlavorHostLinkRepository:search - error during search for flavor host links", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("FlavorHostLinkRepository:search - returning back {} flavor host link results", flavorHostLinkCollection.getFlavorHostLinks().size());
        return flavorHostLinkCollection;
    }
    
    public FlavorHostLink retrieve(FlavorHostLinkLocator locator) {
        log.debug("FlavorHostLinkRepository:retrieve - got request to retrieve flavor host link");
        if (locator == null || (locator.id == null && locator.pathId == null && locator.flavorId == null && locator.hostId == null)) { return null; }
        
        try {
            MwLinkFlavorHostJpaController mwLinkFlavorHostJpaController = My.jpa().mwLinkFlavorHost();
            if (locator.pathId != null) {
                MwLinkFlavorHost mwLinkFlavorHost = mwLinkFlavorHostJpaController.findMwLinkFlavorHost(locator.pathId.toString());
                if (mwLinkFlavorHost != null) {
                    return convert(mwLinkFlavorHost);
                }
            } else if (locator.id != null) {
                MwLinkFlavorHost mwLinkFlavorHost = mwLinkFlavorHostJpaController.findMwLinkFlavorHost(locator.id.toString());
                if (mwLinkFlavorHost != null) {
                    return convert(mwLinkFlavorHost);
                }
            } else if (locator.flavorId != null && locator.hostId != null) {
                MwLinkFlavorHost mwLinkFlavorHost = mwLinkFlavorHostJpaController.findMwLinkFlavorHostByBothIds(
                        locator.flavorId.toString(), locator.hostId.toString());
                if (mwLinkFlavorHost != null) {
                    return convert(mwLinkFlavorHost);
                }
            }
        } catch (Exception ex) {
            log.error("FlavorHostLinkRepository:retrieve - error during retrieve for flavor host link", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }
        return null;
    }
    
    public void store(FlavorHostLink item) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public FlavorHostLink create(FlavorHostLink item) {
        log.debug("FlavorHostLinkRepository:create - got request to create a new flavor host link");
        UUID flavorHostLinkId;
        if (item.getId() != null) {
            flavorHostLinkId = item.getId();
        } else {
            flavorHostLinkId = new UUID();
        }
        
        FlavorLocator locator = new FlavorLocator();
        locator.id = flavorHostLinkId;
        
        if (item.getFlavorId() == null || item.getHostId() == null) {
            log.error("FlavorHostLinkRepository:create - both flavor ID and host ID must be specified");
            throw new RepositoryInvalidInputException(locator);
        }
        String flavorId = item.getFlavorId().toString();
        String hostId = item.getHostId().toString();
        
        try {
            MwLinkFlavorHostJpaController mwLinkFlavorHostJpaController = My.jpa().mwLinkFlavorHost();            
            // create the flavor
            MwLinkFlavorHost newMwLinkFlavorHost = new MwLinkFlavorHost(flavorHostLinkId.toString(), flavorId, hostId);
            mwLinkFlavorHostJpaController.create(newMwLinkFlavorHost);
            log.debug("FlavorHostLinkRepository:create - Created the flavor [{}] host [{}] link [{}] successfully",
                    flavorId, hostId, flavorHostLinkId.toString());
            
            // return back the flavor host link created
            return convert(newMwLinkFlavorHost);
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("FlavorHostLinkRepository:create - error during the storage of the flavor host link in the DB", ex);
            throw new RepositoryCreateException(ex, locator);
        }
    }
    
    public void delete(FlavorHostLinkLocator locator) {
        log.debug("FlavorHostLinkRepository: Received request to delete flavor host link");
        if (locator == null || (locator.id == null && locator.pathId == null)) { return; }
        
        FlavorHostLink flavorHostLink = retrieve(locator);
        if (flavorHostLink != null) {
            try {
                My.jpa().mwLinkFlavorHost().destroy(flavorHostLink.getId().toString());
            } catch (IOException | NonexistentEntityException ex) {
                log.error("FlavorHostLinkRepository: Error during deletion of flavor host link", ex);
                throw new RepositoryDeleteException(ex);
            }
        }
    }
    
    public void delete(FlavorHostLinkFilterCriteria criteria) {
        log.debug("FlavorHostLinkRepository:Delete - Got request to delete FlavorHostLink by search criteria.");        
        FlavorHostLinkCollection objCollection = search(criteria);
        try { 
            for (FlavorHostLink obj : objCollection.getFlavorHostLinks()) {
                FlavorHostLinkLocator locator = new FlavorHostLinkLocator(obj.getId());
                delete(locator);
            }
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("FlavorHostLinkRepository:Delete - Error during FlavorHostLink deletion.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }
    
    private FlavorHostLink convert(MwLinkFlavorHost mwLinkFlavorHost) {
        FlavorHostLink flavorHostLink = new FlavorHostLink();
        flavorHostLink.setId(UUID.valueOf(mwLinkFlavorHost.getId()));
        flavorHostLink.setFlavorId(UUID.valueOf(mwLinkFlavorHost.getFlavorId()));
        flavorHostLink.setHostId(UUID.valueOf(mwLinkFlavorHost.getHostId()));
        return flavorHostLink;
    }
}
