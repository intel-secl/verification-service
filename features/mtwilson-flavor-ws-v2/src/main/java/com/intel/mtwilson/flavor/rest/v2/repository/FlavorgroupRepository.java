/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.flavor.controller.MwFlavorgroupJpaController;
import com.intel.mtwilson.flavor.controller.MwLinkFlavorgroupHostJpaController;
import com.intel.mtwilson.flavor.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.flavor.data.MwFlavorgroup;
import com.intel.mtwilson.flavor.data.MwLinkFlavorgroupHost;
import com.intel.mtwilson.flavor.rest.v2.model.Flavorgroup;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupCollection;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupLocator;
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
public class FlavorgroupRepository {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FlavorgroupRepository.class);

    public FlavorgroupCollection search(FlavorgroupFilterCriteria criteria) {
        log.debug("flavorgroup:search - got request to search for flavorgroups");
        FlavorgroupCollection flavorgroupCollection = new FlavorgroupCollection();
        try {
            MwFlavorgroupJpaController mwFlavorgroupJpaController = My.jpa().mwFlavorgroup();
            if (criteria.filter == false) {
                List<MwFlavorgroup> mwFlavorgroupList = mwFlavorgroupJpaController.findMwFlavorgroupEntities();
                if (mwFlavorgroupList != null && !mwFlavorgroupList.isEmpty()) {
                    for (MwFlavorgroup mwFlavorgroup : mwFlavorgroupList) {
                        flavorgroupCollection.getFlavorgroups().add(convert(mwFlavorgroup));
                    }
                }
            } else if (criteria.id != null) {
                MwFlavorgroup mwFlavorgroup = mwFlavorgroupJpaController.findMwFlavorgroup(criteria.id.toString());
                if (mwFlavorgroup != null) {
                    flavorgroupCollection.getFlavorgroups().add(convert(mwFlavorgroup));
                }
            } else if (criteria.nameEqualTo != null && !criteria.nameEqualTo.isEmpty()) {
                // re-arranged slightly to look more like the nameContains case below
                MwFlavorgroup mwFlavorgroup = mwFlavorgroupJpaController.findMwFlavorgroupByName(criteria.nameEqualTo);
                if (mwFlavorgroup != null) {
                    flavorgroupCollection.getFlavorgroups().add(convert(mwFlavorgroup));
                }
            } else if (criteria.nameContains != null && !criteria.nameContains.isEmpty()) {
                List<MwFlavorgroup> mwFlavorgroupList = mwFlavorgroupJpaController.findMwFlavorgroupByNameLike(criteria.nameContains);
                if (mwFlavorgroupList != null && !mwFlavorgroupList.isEmpty()) {
                    for (MwFlavorgroup mwFlavorgroup : mwFlavorgroupList) {
                        flavorgroupCollection.getFlavorgroups().add(convert(mwFlavorgroup));
                    }
                }
            } else if (criteria.hostId != null) {
                MwLinkFlavorgroupHostJpaController mwLinkFlavorgroupHostJpaController = My.jpa().mwLinkFlavorgroupHost();
                List<MwLinkFlavorgroupHost> mwLinkFlavorgroupHostList
                        = mwLinkFlavorgroupHostJpaController.findMwLinkFlavorgroupHostByHostId(criteria.hostId.toString());
                if (mwLinkFlavorgroupHostList != null && !mwLinkFlavorgroupHostList.isEmpty()) {
                    for (MwLinkFlavorgroupHost mwLinkFlavorgroupHost : mwLinkFlavorgroupHostList) {
                        MwFlavorgroup mwFlavorgroup
                                = mwFlavorgroupJpaController.findMwFlavorgroup(mwLinkFlavorgroupHost.getFlavorgroupId());
                        if (mwFlavorgroup != null) {
                            flavorgroupCollection.getFlavorgroups().add(convert(mwFlavorgroup));
                        }
                    }
                }
            } else {
                // Invalid search criteria specified. Just log the error and return back empty collection.
                log.error("flavorgroup:search - invalid flavorgroup search criteria specified");
            }
        } catch (Exception ex) {
            log.error("flavorgroup:search - error during flavorgroup search", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("flavorgroup:search - returning back {} results", flavorgroupCollection.getFlavorgroups().size());
        return flavorgroupCollection;
    }

    public Flavorgroup retrieve(FlavorgroupLocator locator) {
        if (locator == null) {
            return null;
        }

        try {
            MwFlavorgroupJpaController mwFlavorgroupJpaController = My.jpa().mwFlavorgroup();
            if (locator.pathId != null) {
                MwFlavorgroup mwFlavorgroup = mwFlavorgroupJpaController.findMwFlavorgroup(locator.pathId.toString());
                if (mwFlavorgroup != null) {
                    Flavorgroup flavorgroup = convert(mwFlavorgroup);
                    return flavorgroup;
                }
            } else if (locator.id != null) {
                MwFlavorgroup mwFlavorgroup = mwFlavorgroupJpaController.findMwFlavorgroup(locator.id.toString());
                if (mwFlavorgroup != null) {
                    Flavorgroup flavorgroup = convert(mwFlavorgroup);
                    return flavorgroup;
                }
            } else if (locator.name != null && !locator.name.isEmpty()) {
                MwFlavorgroup mwFlavorgroup = mwFlavorgroupJpaController.findMwFlavorgroupByName(locator.name);
                if (mwFlavorgroup != null) {
                    Flavorgroup flavorgroup = convert(mwFlavorgroup);
                    return flavorgroup;
                }
            } else {
                log.error("Invalid flavorgroup retrieve criteria");
            }

        } catch (Exception ex) {
            log.error("Error during the retreival of flavorgroup");
            throw new RepositoryRetrieveException(ex, locator);
        }
        return null;
    }

    public void store(Flavorgroup item) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Flavorgroup create(Flavorgroup item) {
        log.debug("flavorgroup:create - got request to create a new flavorgroup");
        UUID flavorgroupId;
        if (item.getId() != null) {
            flavorgroupId = item.getId();
        } else {
            flavorgroupId = new UUID();
        }

        FlavorgroupLocator locator = new FlavorgroupLocator();
        locator.id = flavorgroupId;

        if (item.getName() == null || (item.getFlavorMatchPolicyCollection() == null && !item.getName().equals("mtwilson_unique"))) {
            log.error("flavorgroup:create - flavorgroup name and flavor match policy must be specified");
            throw new RepositoryInvalidInputException(locator);
        }

        try {
            MwFlavorgroupJpaController mwFlavorgroupJpaController = My.jpa().mwFlavorgroup();
            MwFlavorgroup mwFlavorgroup = mwFlavorgroupJpaController.findMwFlavorgroupByName(item.getName());
            if (mwFlavorgroup != null) {
                log.error("flavorgroup:create - flavorgroup specified {} already exists", item.getName());
                throw new RepositoryInvalidInputException(locator);
            }

            // create the flavor
            MwFlavorgroup newFlavorgroup = new MwFlavorgroup(flavorgroupId.toString(), item.getName(), item.getFlavorMatchPolicyCollection());
            mwFlavorgroupJpaController.create(newFlavorgroup);
            log.debug("flavorgroup:create - Created the flavorgroup {} successfully", flavorgroupId.toString());

            // Return back the flavor id created.
            return convert(newFlavorgroup);
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("flavorgroup:create - error during the storage of the flavorgroup in the DB", ex);
            throw new RepositoryCreateException(ex, locator);
        }
    }

    public void delete(FlavorgroupLocator locator) {
        log.debug("Flavorgroup : delete - Request to delete a flavorgroup.");
        if ((locator == null) || (locator.id == null && locator.pathId == null && locator.name == null)) {
            throw new RepositoryInvalidInputException("Invalid flavorgroup ID specified.");
        }

        try {
            Flavorgroup obj = retrieve(locator);
            if (obj != null) {
                if (obj.getName().equalsIgnoreCase("mtwilson_automatic") || obj.getName().equalsIgnoreCase("mtwilson_unique")) {
                    log.error("The flavorgroup is either mtwilson_automatic or mtwilson_unique, which cannot be deleted");
                    throw new RepositoryDeleteException();
                }

                MwFlavorgroupJpaController mwFlavorgroupJpa = My.jpa().mwFlavorgroup();

                mwFlavorgroupJpa.destroy(obj.getId().toString());
            }
        } catch (IOException ex) {
            log.error("flavorgroup:delete - error during the deletion of the flavorgroup in the DB", ex);
        } catch (NonexistentEntityException Ex) {
            log.error("flavorgroup:delete - error during the deletion of the flavorgroup in the DB", Ex);
            throw new RepositoryDeleteException(Ex, locator);
        }

    }

    public void delete(FlavorgroupFilterCriteria criteria) {
        log.debug("flavorgroup:delete - got request to delete a flavorgroup by filtercriteria");
        try {
            MwFlavorgroupJpaController mwFlavorgroupJpaController = My.jpa().mwFlavorgroup();
            if (criteria.id != null) {
                MwFlavorgroup mwFlavorgroup = mwFlavorgroupJpaController.findMwFlavorgroup(criteria.id.toString());
                if (mwFlavorgroup != null) {
                    mwFlavorgroupJpaController.destroy(criteria.id.toString());
                }
            } else if (criteria.nameEqualTo != null && !criteria.nameEqualTo.isEmpty()) {
                MwFlavorgroup mwFlavorgroup = mwFlavorgroupJpaController.findMwFlavorgroupByName(criteria.nameEqualTo);
                if (mwFlavorgroup != null) {
                    mwFlavorgroupJpaController.destroy(mwFlavorgroup.getId());
                }
            } else if (criteria.nameContains != null && !criteria.nameContains.isEmpty()) {
                List<MwFlavorgroup> mwFlavorgroupList = mwFlavorgroupJpaController.findMwFlavorgroupByNameLike(criteria.nameContains);
                if (mwFlavorgroupList != null && !mwFlavorgroupList.isEmpty()) {
                    for (MwFlavorgroup mwFlavorgroup : mwFlavorgroupList) {
                        mwFlavorgroupJpaController.destroy(mwFlavorgroup.getId());
                    }
                }
            } else if (criteria.hostId != null) {
                MwLinkFlavorgroupHostJpaController mwLinkFlavorgroupHostJpaController = My.jpa().mwLinkFlavorgroupHost();
                List<MwLinkFlavorgroupHost> mwLinkFlavorgroupHostList
                        = mwLinkFlavorgroupHostJpaController.findMwLinkFlavorgroupHostByHostId(criteria.hostId.toString());
                if (mwLinkFlavorgroupHostList != null && !mwLinkFlavorgroupHostList.isEmpty()) {
                    for (MwLinkFlavorgroupHost mwLinkFlavorgroupHost : mwLinkFlavorgroupHostList) {
                        MwFlavorgroup mwFlavorgroup
                                = mwFlavorgroupJpaController.findMwFlavorgroup(mwLinkFlavorgroupHost.getFlavorgroupId());
                        if (mwFlavorgroup != null) {
                            mwFlavorgroupJpaController.destroy(mwFlavorgroup.getId());
                        }
                    }
                }
            }
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("flavorgroup:delete - error during the deletion of the flavorgroup in the DB", ex);
            throw new RepositoryDeleteException(ex);
        }
    }

    private Flavorgroup convert(MwFlavorgroup mwFlavorgroup) {
        Flavorgroup flavorgroup = new Flavorgroup();
        flavorgroup.setId(UUID.valueOf(mwFlavorgroup.getId()));
        flavorgroup.setName(mwFlavorgroup.getName());
        flavorgroup.setFlavorMatchPolicyCollection(mwFlavorgroup.getFlavorTypeMatchPolicy());
        return flavorgroup;
    }
}
