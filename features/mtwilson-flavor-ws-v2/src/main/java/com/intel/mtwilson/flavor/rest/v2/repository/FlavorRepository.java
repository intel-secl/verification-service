/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.flavor.controller.MwFlavorJpaController;
import com.intel.mtwilson.flavor.controller.exceptions.PreexistingEntityException;
import com.intel.mtwilson.flavor.data.MwFlavor;
import com.intel.mtwilson.flavor.model.FlavorMatchPolicy;

import com.intel.mtwilson.flavor.model.FlavorMatchPolicyCollection;
import com.intel.mtwilson.flavor.model.MatchPolicy;
import static com.intel.mtwilson.flavor.model.MatchPolicy.MatchType.ANY_OF;
import static com.intel.mtwilson.flavor.model.MatchPolicy.Required.REQUIRED;
import static com.intel.mtwilson.flavor.model.MatchPolicy.Required.REQUIRED_IF_DEFINED;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorCollection;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorLocator;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.flavor.controller.exceptions.NonexistentEntityException;
import static com.intel.mtwilson.core.flavor.common.FlavorPart.*;
import static com.intel.mtwilson.flavor.model.MatchPolicy.MatchType.LATEST;
import com.intel.mtwilson.repository.RepositoryDeleteException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author srege
 * @author nkgadepa
 *
 */
public class FlavorRepository {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FlavorRepository.class);

    public FlavorCollection search(FlavorFilterCriteria criteria) {
        log.debug("flavor:search - got request to search for flavors");
        FlavorCollection flavorCollection = new FlavorCollection();
        try {
            MwFlavorJpaController mwFlavorJpaController = My.jpa().mwFlavor();
            if (criteria.filter == false) {
                List<MwFlavor> mwFlavorList = mwFlavorJpaController.findMwFlavorEntities();
                if (mwFlavorList != null && !mwFlavorList.isEmpty()) {
                    for (MwFlavor mwFlavor : mwFlavorList) {
                        flavorCollection.getFlavors().add(mwFlavor.getContent());
                    }
                }
            } else if (criteria.id != null) {
                MwFlavor mwFlavor = mwFlavorJpaController.findMwFlavor(criteria.id.toString());
                if (mwFlavor != null) {
                    flavorCollection.getFlavors().add(mwFlavor.getContent());
                }
            } else if (criteria.key != null && !criteria.key.isEmpty() && criteria.value != null && !criteria.value.isEmpty()) {
                List<MwFlavor> mwFlavorList = mwFlavorJpaController.findMwFlavorByKeyValue(criteria.key, criteria.value);
                if (mwFlavorList != null && !mwFlavorList.isEmpty()) {
                    for (MwFlavor mwFlavor : mwFlavorList) {
                        flavorCollection.getFlavors().add(mwFlavor.getContent());
                    }
                }
            } else if (criteria.flavorgroupId != null || criteria.hostManifest != null
                    || (criteria.flavorParts != null && !criteria.flavorParts.isEmpty())
                    || (criteria.flavorPartsWithLatest != null && !criteria.flavorPartsWithLatest.isEmpty())) {
                if (criteria.flavorParts != null && !criteria.flavorParts.isEmpty()
                        && criteria.flavorPartsWithLatest == null) {
                    HashMap<String, Boolean> flavorParts = new HashMap<>();
                    for (FlavorPart flavorPart : criteria.flavorParts) {
                        flavorParts.put(flavorPart.name(), false);
                    }
                    criteria.flavorPartsWithLatest = flavorParts;
                }

                List<MwFlavor> mwFlavorList = mwFlavorJpaController.findMwFlavorEntities(
                        criteria.flavorgroupId, criteria.hostManifest, criteria.flavorPartsWithLatest);
                if (mwFlavorList != null && !mwFlavorList.isEmpty()) {
                    for (MwFlavor mwFlavor : mwFlavorList) {
                        flavorCollection.getFlavors().add(mwFlavor.getContent());
                    }
                }
            } else {
                // Invalid search criteria specified. Just log the error and return back empty collection.
                log.error("flavor:search - invalid flavor search criteria specified");
            }
        } catch (Exception ex) {
            log.error("flavor:search - error during search for flavors", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("flavor:search - returning back {} flavor results", flavorCollection.getFlavors().size());
        return flavorCollection;
    }

    public Flavor retrieve(FlavorLocator locator) {
        log.debug("flavor:retrieve - got request to retrieve flavor");
        if (locator == null || (locator.id == null && locator.pathId == null)) {
            return null;
        }

        try {
            MwFlavorJpaController mwFlavorJpaController = My.jpa().mwFlavor();
            if (locator.pathId != null) {
                MwFlavor mwFlavor = mwFlavorJpaController.findMwFlavor(locator.pathId.toString());
                if (mwFlavor != null) {
                    return mwFlavor.getContent();
                }
            } else if (locator.id != null) {
                MwFlavor mwFlavor = mwFlavorJpaController.findMwFlavor(locator.id.toString());
                if (mwFlavor != null) {
                    return mwFlavor.getContent();
                }
            }
        } catch (Exception ex) {
            log.error("flavor:retrieve - error during retrieval of flavor", ex);
            throw new RepositoryRetrieveException(ex);
        }
        return null;
    }

    public void store(Flavor item) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Flavor create(Flavor item) {
        log.debug("Got request to create a new flavor");
        if (item == null || item.getMeta() == null) {
            throw new RepositoryInvalidInputException("Flavor meta must be specified");
        }
        
        UUID flavorId;
        String flavorLabel = null;
        if (item.getMeta().getId() != null) {
            flavorId = UUID.valueOf(item.getMeta().getId());
        } else {
            flavorId = new UUID();
        }
        if (item.getMeta().getDescription().getLabel() != null){
            flavorLabel = item.getMeta().getDescription().getLabel();
        }
        
        FlavorLocator locator = new FlavorLocator();
        locator.id = flavorId;
        if (flavorLabel != null) {
            locator.label = flavorLabel;
        }
        MwFlavor mwFlavor;
        try {
            MwFlavorJpaController mwFlavorJpaController = My.jpa().mwFlavor();
            mwFlavor = mwFlavorJpaController.findMwFlavor(flavorId.toString());
            if (mwFlavor != null) {
                log.error("Flavor specified {} already exists", flavorId.toString());
                throw new RepositoryInvalidInputException(locator);
            }
            mwFlavor = mwFlavorJpaController.findMwFlavorByName(flavorLabel);
            if (mwFlavor != null) {
                throw new PreexistingEntityException("The flavor with label " + mwFlavor.getLabel() + " already exists.");
            }
            
            // create the flavor
            MwFlavor newMwFlavor = new MwFlavor(flavorId.toString(), item);
            mwFlavorJpaController.create(newMwFlavor);
            log.debug("Created the flavor {} successfully", flavorId);
            
            // return back the flavor created
            return newMwFlavor.getContent();
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Error during the storage of the flavor in the DB", ex);
            throw new RepositoryCreateException(ex, locator);
        }
    }


    public void delete(FlavorLocator locator) {
        log.debug("Received request to delete flavor");
        if (locator == null || (locator.id == null && locator.pathId == null)) {
            return;
        }

        Flavor flavor = retrieve(locator);
        if (flavor != null) {
            try {
                My.jpa().mwFlavor().destroy(flavor.getMeta().getId());
            } catch (IOException | NonexistentEntityException ex) {
                log.error("Error during deletion of flavor", ex);
                throw new RepositoryDeleteException(ex);
            }
        }
    }

    public void delete(FlavorFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<FlavorPart> getUniqueFlavorTypesThatExistForHost(UUID hardwareUuid) {
        List<FlavorPart> uniqueFlavorTypesForHost = new ArrayList<>();
        try {
            MwFlavorJpaController mwFlavorJpaController = My.jpa().mwFlavor();

            // check for HOST_UNIQUE flavor part
            boolean hostHasHostUniqueFlavor
                    = mwFlavorJpaController.hostHasUniqueFlavor(hardwareUuid.toString(), FlavorPart.HOST_UNIQUE.name());
            if (hostHasHostUniqueFlavor) {
                log.debug("Host [{}] has HOST_UNIQUE flavor", hardwareUuid.toString());
                uniqueFlavorTypesForHost.add(FlavorPart.HOST_UNIQUE);
            }

            // check for ASSET_TAG flavor part
            boolean hostHasTagFlavor
                    = mwFlavorJpaController.hostHasUniqueFlavor(hardwareUuid.toString(), FlavorPart.ASSET_TAG.name());
            if (hostHasTagFlavor) {
                log.debug("Host [{}] has ASSET_TAG flavor", hardwareUuid.toString());
                uniqueFlavorTypesForHost.add(FlavorPart.ASSET_TAG);
            }

            // return null if no unique flavor parts were found
            if (uniqueFlavorTypesForHost.isEmpty()) {
                log.debug("Host [{}] does NOT have any unique flavor types", hardwareUuid.toString());
                return null;
            }
            return uniqueFlavorTypesForHost;
        } catch (Exception ex) {
            log.error("Error during search for unique flavors for host [{}]", hardwareUuid.toString());
            throw new RepositorySearchException(String.format(
                    "Error during search for unique flavors for host [%s]", hardwareUuid.toString()), ex);
        }
    }

    public List<FlavorPart> getFlavorTypesInFlavorgroup(UUID flavorgroupId, List<FlavorPart> flavorParts) {
        try {
            MwFlavorJpaController mwFlavorJpaController = My.jpa().mwFlavor();
            List<FlavorPart> flavorTypesInFlavorGroup = new ArrayList<>();
            if (flavorParts == null || flavorParts.isEmpty()) {
                flavorParts = Arrays.asList(FlavorPart.values());
            }
            for (FlavorPart flavorPart : flavorParts) {
                boolean flavorgroupContainsFlavorType
                        = mwFlavorJpaController.flavorgroupContainsFlavorType(flavorgroupId, flavorPart.name());
                if (flavorgroupContainsFlavorType) {
                    log.debug("Flavorgroup [{}] contains flavor type [{}]",
                            flavorgroupId.toString(), flavorPart.name());
                    flavorTypesInFlavorGroup.add(flavorPart);
                }
            }

            if (flavorTypesInFlavorGroup.isEmpty()) {
                log.debug("Flavorgroup [{}] does NOT contain flavor types [{}]",
                        flavorgroupId.toString(), flavorParts.toString());
                return null;
            }
            return flavorTypesInFlavorGroup;
        } catch (Exception ex) {
            log.error("Error during search flavor types [{}] in flavorgroup [{}]",
                    flavorParts.toString(), flavorgroupId.toString());
            throw new RepositorySearchException(String.format(
                    "Error during search flavor types [{}] in flavorgroup [{}]",
                    flavorParts.toString(), flavorgroupId.toString()), ex);
        }
    }

    public FlavorMatchPolicyCollection createAutomaticFlavorMatchPolicy() {
        FlavorMatchPolicyCollection policy = new FlavorMatchPolicyCollection();
        policy.addFlavorMatchPolicy(new FlavorMatchPolicy(PLATFORM, new MatchPolicy(ANY_OF, REQUIRED)));
        policy.addFlavorMatchPolicy(new FlavorMatchPolicy(OS, new MatchPolicy(ANY_OF, REQUIRED)));
        policy.addFlavorMatchPolicy(new FlavorMatchPolicy(ASSET_TAG, new MatchPolicy(LATEST, REQUIRED_IF_DEFINED)));
        policy.addFlavorMatchPolicy(new FlavorMatchPolicy(HOST_UNIQUE, new MatchPolicy(LATEST, REQUIRED_IF_DEFINED)));
        return policy;
    }
}
