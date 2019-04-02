/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.flavor.controller.MwLinkFlavorFlavorgroupJpaController;
import com.intel.mtwilson.flavor.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.flavor.data.MwLinkFlavorFlavorgroup;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorFlavorgroupLink;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorFlavorgroupLinkCollection;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorFlavorgroupLinkFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorFlavorgroupLinkLocator;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import java.io.IOException;
import java.util.List;

public class FlavorFlavorgroupLinkRepository {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FlavorFlavorgroupLinkRepository.class);
    
    public FlavorFlavorgroupLinkCollection search(FlavorFlavorgroupLinkFilterCriteria criteria) {
        log.debug("flavor_flavorgroup_link:search - got request to search for flavor flavorgroup link associations");
        FlavorFlavorgroupLinkCollection flavorFlavorgroupLinkCollection = new FlavorFlavorgroupLinkCollection();
        try {
            MwLinkFlavorFlavorgroupJpaController mwlinkFlavorFlavorgroupJpaController = My.jpa().mwLinkFlavorFlavorgroup();
            if (criteria.filter == false) {
                List<MwLinkFlavorFlavorgroup> mwLinkFlavorFlavorgroupList
                        = mwlinkFlavorFlavorgroupJpaController.findMwLinkFlavorFlavorgroupEntities();
                if (mwLinkFlavorFlavorgroupList != null && !mwLinkFlavorFlavorgroupList.isEmpty()) {
                    for (MwLinkFlavorFlavorgroup mwLinkFlavorFlavorgroup : mwLinkFlavorFlavorgroupList) {
                        flavorFlavorgroupLinkCollection.getFlavorFlavorgroupLinks().add(convert(mwLinkFlavorFlavorgroup));
                    }
                }
            } else if (criteria.id != null) {
                MwLinkFlavorFlavorgroup mwLinkFlavorFlavorgroup
                        = mwlinkFlavorFlavorgroupJpaController.findMwLinkFlavorFlavorgroup(criteria.id.toString());
                if (mwLinkFlavorFlavorgroup != null) {
                    flavorFlavorgroupLinkCollection.getFlavorFlavorgroupLinks().add(convert(mwLinkFlavorFlavorgroup));
                }
            } else if (criteria.flavorId != null) {
                List<MwLinkFlavorFlavorgroup> mwLinkFlavorFlavorgroupList
                        = mwlinkFlavorFlavorgroupJpaController.findMwLinkFlavorFlavorgroupByFlavorId(criteria.flavorId.toString());
                if (mwLinkFlavorFlavorgroupList != null && !mwLinkFlavorFlavorgroupList.isEmpty()) {
                    for (MwLinkFlavorFlavorgroup mwLinkFlavorFlavorgroup : mwLinkFlavorFlavorgroupList) {
                        flavorFlavorgroupLinkCollection.getFlavorFlavorgroupLinks().add(convert(mwLinkFlavorFlavorgroup));
                    }
                }
            } else if (criteria.flavorgroupId != null) {
                List<MwLinkFlavorFlavorgroup> mwLinkFlavorFlavorgroupList
                        = mwlinkFlavorFlavorgroupJpaController.findMwLinkFlavorFlavorgroupByFlavorgroupId(criteria.flavorgroupId.toString());
                if (mwLinkFlavorFlavorgroupList != null && !mwLinkFlavorFlavorgroupList.isEmpty()) {
                    for (MwLinkFlavorFlavorgroup mwLinkFlavorFlavorgroup : mwLinkFlavorFlavorgroupList) {
                        flavorFlavorgroupLinkCollection.getFlavorFlavorgroupLinks().add(convert(mwLinkFlavorFlavorgroup));
                    }
                }
            } else {
                // Invalid search criteria specified. Just log the error and return back empty collection.
                log.error("flavor_flavorgroup_link:search - invalid flavor flavorgroup link search criteria specified");
            }
        } catch (Exception ex) {
            log.error("flavor_flavorgroup_link:search - error during search for flavor flavorgroup link associations", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("flavor_flavorgroup_link:search - returning back {} flavor flavorgroup link results",
                flavorFlavorgroupLinkCollection.getFlavorFlavorgroupLinks().size());
        return flavorFlavorgroupLinkCollection;
    }

    public FlavorFlavorgroupLink retrieve(FlavorFlavorgroupLinkLocator locator) {
        log.debug("flavor_flavorgroup_link:retrieve - got request to retrieve flavor flavorgroup link");
        if (locator == null
                || ((locator.id == null && locator.pathId == null) && (locator.flavorId == null || locator.flavorgroupId == null)))
        { return null; }
        
        try {
            MwLinkFlavorFlavorgroupJpaController mwLinkFlavorFlavorgroupJpaController = My.jpa().mwLinkFlavorFlavorgroup();
            if (locator.pathId != null) {
                MwLinkFlavorFlavorgroup mwLinkFlavorFlavorgroup = mwLinkFlavorFlavorgroupJpaController.findMwLinkFlavorFlavorgroup(locator.pathId.toString());
                if (mwLinkFlavorFlavorgroup != null) {
                    return convert(mwLinkFlavorFlavorgroup);
                }
            } else if (locator.id != null) {
                MwLinkFlavorFlavorgroup mwLinkFlavorFlavorgroup = mwLinkFlavorFlavorgroupJpaController.findMwLinkFlavorFlavorgroup(locator.id.toString());
                if (mwLinkFlavorFlavorgroup != null) {
                    return convert(mwLinkFlavorFlavorgroup);
                }
            } else if (locator.flavorId != null && locator.flavorgroupId != null) {
                MwLinkFlavorFlavorgroup mwLinkFlavorFlavorgroup = mwLinkFlavorFlavorgroupJpaController.findMwLinkFlavorFlavorgroupByBothIds(
                        locator.flavorId.toString(), locator.flavorgroupId.toString());
                if (mwLinkFlavorFlavorgroup != null) {
                    return convert(mwLinkFlavorFlavorgroup);
                }
            }
        } catch (Exception ex) {
            log.error("flavor_flavorgroup_link:retrieve - error during retrieve for flavor host link", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }
        return null;
    }
    
    public void store(FlavorFlavorgroupLink item) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public FlavorFlavorgroupLink create(FlavorFlavorgroupLink item) {
        log.debug("flavor_flavorgroup_link:create - got request to create a new flavor flavorgroup link");
        UUID flavorFlavorgroupLinkId;
        if (item.getId() != null) {
            flavorFlavorgroupLinkId = item.getId();
        } else {
            flavorFlavorgroupLinkId = new UUID();
        }
        
        FlavorFlavorgroupLinkLocator locator = new FlavorFlavorgroupLinkLocator();
        locator.id = flavorFlavorgroupLinkId;
        
        if (item.getFlavorId() == null || item.getFlavorgroupId() == null) {
            log.error("flavor_flavorgroup_link:create - both flavor ID and flavorgroup ID must be specified");
            throw new RepositoryInvalidInputException(locator);
        }
        UUID flavorId = item.getFlavorId();
        UUID flavorgroupId = item.getFlavorgroupId();
        
        try {
            MwLinkFlavorFlavorgroupJpaController mwLinkFlavorFlavorgroupJpaController = My.jpa().mwLinkFlavorFlavorgroup();
            MwLinkFlavorFlavorgroup mwLinkFlavorFlavorgroup = mwLinkFlavorFlavorgroupJpaController.findMwLinkFlavorFlavorgroup(flavorFlavorgroupLinkId.toString());
            if (mwLinkFlavorFlavorgroup != null) {
                log.error("flavor_flavorgroup_link:create - flavor flavorgroup link specified {} already exists", flavorFlavorgroupLinkId.toString());
                throw new RepositoryInvalidInputException(locator);                                        
            }
            
            // create the flavor
            MwLinkFlavorFlavorgroup newMwLinkFlavorFlavorgroup
                    = new MwLinkFlavorFlavorgroup(
                            flavorFlavorgroupLinkId.toString(),
                            flavorId.toString(),
                            flavorgroupId.toString());
            mwLinkFlavorFlavorgroupJpaController.create(newMwLinkFlavorFlavorgroup);
            log.debug("flavor_flavorgroup_link:create - created the flavor [{}] flavorgroup [{}] link [{}] successfully",
                    flavorId.toString(), flavorgroupId.toString(), flavorFlavorgroupLinkId.toString());
            
            // return back the flavor host link created
            return convert(newMwLinkFlavorFlavorgroup);
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("flavor_flavorgroup_link:create - error during the storage of the flavor flavorgroup link in the DB", ex);
            throw new RepositoryCreateException(ex, locator);
        }
    }
    
    public void delete(FlavorFlavorgroupLinkLocator locator) {
        log.debug("FlavorFlavorgroupLinkRepository: Received request to delete link between flavor and flavorgroup");
        if (locator == null
                || ((locator.id == null && locator.pathId == null) && (locator.flavorId == null || locator.flavorgroupId == null)))
        { return; }
        
        FlavorFlavorgroupLink flavorFlavorgroupLink = retrieve(locator);        
        if (flavorFlavorgroupLink != null) {
            try {
                log.debug("FlavorFlavorgroupLinkRepository: About to delete flavor-flavorgroup link with id - {}", flavorFlavorgroupLink.getId());
                My.jpa().mwLinkFlavorFlavorgroup().destroy(flavorFlavorgroupLink.getId().toString());
            } catch (IOException | NonexistentEntityException ex) {
                log.error("Error during deletion of flavor", ex);
                throw new RepositoryDeleteException(ex);
            }
        }
    }
    
    public void delete(FlavorFlavorgroupLinkFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private FlavorFlavorgroupLink convert(MwLinkFlavorFlavorgroup mwLinkFlavorFlavorgroup) {
        FlavorFlavorgroupLink flavorFlavorgroupLink = new FlavorFlavorgroupLink();
        flavorFlavorgroupLink.setId(UUID.valueOf(mwLinkFlavorFlavorgroup.getId()));
        flavorFlavorgroupLink.setFlavorId(UUID.valueOf(mwLinkFlavorFlavorgroup.getFlavorId()));
        flavorFlavorgroupLink.setFlavorgroupId(UUID.valueOf(mwLinkFlavorFlavorgroup.getFlavorgroupId()));
        return flavorFlavorgroupLink;
    }
}
