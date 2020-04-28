/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.core.flavor.model.SignedFlavor;
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
import com.intel.mtwilson.flavor.rest.v2.model.SignedFlavorCollection;
import com.intel.mtwilson.repository.*;
import com.intel.mtwilson.flavor.controller.exceptions.NonexistentEntityException;
import static com.intel.mtwilson.core.flavor.common.FlavorPart.*;
import static com.intel.mtwilson.flavor.model.MatchPolicy.MatchType.LATEST;
import com.intel.mtwilson.repository.RepositoryDeleteException;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.WebApplicationException;

import java.io.IOException;
import java.util.*;

/**
 * @author srege
 * @author nkgadepa
 */
public class FlavorRepository {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FlavorRepository.class);

    public SignedFlavorCollection search(FlavorFilterCriteria criteria) {
        log.debug("flavor:search - got request to search for flavors");
        SignedFlavorCollection signedFlavorCollection = new SignedFlavorCollection();
        try {
            MwFlavorJpaController mwFlavorJpaController = My.jpa().mwFlavor();
            if (criteria.filter == false) {
                List<MwFlavor> mwFlavorList = mwFlavorJpaController.findMwFlavorEntities();
                if (mwFlavorList != null && !mwFlavorList.isEmpty()) {
                    for (MwFlavor mwFlavor : mwFlavorList) {
                        signedFlavorCollection.getSignedFlavors().add(new SignedFlavor(mwFlavor.getContent(), mwFlavor.getSignature()));
                    }
                }
            } else if (criteria.id != null) {
                MwFlavor mwFlavor = mwFlavorJpaController.findMwFlavor(criteria.id.toString());
                if (mwFlavor != null) {
                    signedFlavorCollection.getSignedFlavors().add(new SignedFlavor(mwFlavor.getContent(), mwFlavor.getSignature()));
                }
            } else if (criteria.key != null && !criteria.key.isEmpty() && criteria.value != null && !criteria.value.isEmpty()) {
                List<MwFlavor> mwFlavorList = mwFlavorJpaController.findMwFlavorByKeyValue(criteria.key, criteria.value);
                if (mwFlavorList != null && !mwFlavorList.isEmpty()) {
                    for (MwFlavor mwFlavor : mwFlavorList) {
                        signedFlavorCollection.getSignedFlavors().add(new SignedFlavor(mwFlavor.getContent(), mwFlavor.getSignature()));
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
                        signedFlavorCollection.getSignedFlavors().add(new SignedFlavor(mwFlavor.getContent(), mwFlavor.getSignature()));
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
        log.debug("flavor:search - returning back {} flavor results", signedFlavorCollection.getSignedFlavors().size());
        return signedFlavorCollection;
    }

    public SignedFlavor retrieve(FlavorLocator locator) {
        log.debug("flavor:retrieve - got request to retrieve flavor");
        if (locator == null || (locator.id == null && locator.pathId == null && (locator.label == null || locator.label.trim().equals("")))) {
            return null;
        }

        try {
            MwFlavorJpaController mwFlavorJpaController = My.jpa().mwFlavor();
            MwFlavor mwFlavor = null;
            if (locator.pathId != null) {
                mwFlavor = mwFlavorJpaController.findMwFlavor(locator.pathId.toString());
            } else if (locator.id != null) {
                mwFlavor = mwFlavorJpaController.findMwFlavor(locator.id.toString());
            } else if (locator.label != null && !(locator.label.trim().equals(""))) {
                mwFlavor = mwFlavorJpaController.findMwFlavorByName(locator.label);
            }

            if (mwFlavor != null) {
                return new SignedFlavor(mwFlavor.getContent(), mwFlavor.getSignature());
            }
        } catch (Exception ex) {
            log.error("flavor:retrieve - error during retrieval of flavor", ex);
            throw new RepositoryRetrieveException(ex);
        }
        return null;
    }

    public List<MwFlavor> retrieveUnsignedMwFlavorList() {
        log.debug("flavor:retrieve - got request to retrieve unsigned flavors");
        try {
            MwFlavorJpaController mwFlavorJpaController = My.jpa().mwFlavor();
            List<MwFlavor> mwFlavorList = mwFlavorJpaController.getUnsignedMwFlavorList();
            return mwFlavorList;
        } catch (Exception ex) {
            log.error("flavor:retrieve - error during retrieval of unsigned flavors", ex);
            throw new RepositoryRetrieveException(ex);
        }
    }

    public void storeEntity(MwFlavor item) throws Exception {
        log.debug("Got request to update a flavor");
        if (item == null || item.getSignature() == null) {
            throw new RepositoryInvalidInputException("Flavor signature does not exist");
        }
	MwFlavorJpaController mwFlavorJpaController = My.jpa().mwFlavor();
        MwFlavor mwFlavor = mwFlavorJpaController.findMwFlavor(item.getId());
        String errorMessage;
        if (mwFlavor == null) {
            errorMessage = "A flavor with UUID " + item.getId() + " does not exist.";
            log.error(errorMessage);
            throw new EntityNotFoundException(errorMessage);
        }
        try {
            mwFlavorJpaController.edit(item);
            log.debug("Updated the flavor {} successfully", item.getId());
        } catch (Exception ex) {
            log.error("Error during the storage of the flavor in the DB", ex);
            throw new RepositoryStoreException(ex);
        }
    }

    public SignedFlavor create(SignedFlavor item) {
        log.debug("Got request to create a new flavor");
        if (item == null || item.getFlavor().getMeta() == null) {
            throw new RepositoryInvalidInputException("Flavor meta must be specified");
        }

        UUID flavorId;
        String flavorLabel = null;
        if (item.getFlavor().getMeta().getId() != null) {
            flavorId = UUID.valueOf(item.getFlavor().getMeta().getId());
        } else {
            flavorId = new UUID();
        }
        if (item.getFlavor().getMeta().getDescription().getLabel() != null){
            flavorLabel = item.getFlavor().getMeta().getDescription().getLabel();
        }
        FlavorLocator locator = new FlavorLocator();
        locator.id = flavorId;
        if (flavorLabel != null) {
            locator.label = flavorLabel;
        }
        MwFlavor mwFlavor;
        String errorMessage;
        try {
            MwFlavorJpaController mwFlavorJpaController = My.jpa().mwFlavor();
            mwFlavor = mwFlavorJpaController.findMwFlavor(flavorId.toString());
            if (mwFlavor != null) {
                errorMessage = "A flavor with UUID " + flavorId.toString() + " already exists.";
                log.error(errorMessage);
                throw new PreexistingEntityException(errorMessage);
            }
            mwFlavor = mwFlavorJpaController.findMwFlavorByName(flavorLabel);
            if (mwFlavor != null) {
                errorMessage = "A flavor with Label " + mwFlavor.getLabel() + " already exists.";
                log.error(errorMessage);
                throw new PreexistingEntityException(errorMessage);
            }

            // create the flavor
            MwFlavor newMwFlavor = new MwFlavor(flavorId.toString(), item.getFlavor(), item.getSignature());
            mwFlavorJpaController.create(newMwFlavor);
            log.debug("Created the flavor {} successfully", flavorId);

            // return back the flavor created
            return new SignedFlavor(newMwFlavor.getContent(), newMwFlavor.getSignature());
        } catch (PreexistingEntityException ex) {
            //returning null so that it will not create new flavor flavorgroup link for existing flavor
            return null;
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

        SignedFlavor signedFlavor = retrieve(locator);
        if (signedFlavor != null) {
            try {
                My.jpa().mwFlavor().destroy(signedFlavor.getFlavor().getMeta().getId());
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
