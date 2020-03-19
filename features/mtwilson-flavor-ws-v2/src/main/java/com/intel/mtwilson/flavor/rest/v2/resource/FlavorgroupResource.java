/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.core.flavor.model.SignedFlavor;
import com.intel.mtwilson.flavor.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.flavor.rest.v2.model.*;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorFlavorgroupLinkRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorgroupHostLinkRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorgroupRepository;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorFlavorgroupLinkCollection;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorFlavorgroupLink;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorFlavorgroupLinkFilterCriteria;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author hmgowda
 */
@V2
@Path("/flavorgroups")
public class FlavorgroupResource {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FlavorgroupResource.class);

    private FlavorgroupRepository repository;

    public FlavorgroupResource() {
        repository = new FlavorgroupRepository();
    }

    protected FlavorgroupCollection createEmptyCollection() {
        return new FlavorgroupCollection();
    }

    protected FlavorgroupRepository getRepository() {
        return repository;
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @RequiresPermissions("flavorgroups:create")
    public Flavorgroup create(Flavorgroup flavorgroup) {
        ValidationUtil.validate(flavorgroup);
        Flavorgroup create = repository.create(flavorgroup);
        return create;
    }

    @DELETE
    @Path("/{id}")
    @RequiresPermissions("flavorgroups:delete")
    public void delete(@BeanParam FlavorgroupLocator locator) throws IOException, NonexistentEntityException {
        ValidationUtil.validate(locator);

        // First lets retrieve the Flavorgroup object using the locator
        Flavorgroup obj = repository.retrieve(locator);

        if (obj == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        // Check if the flavor group is linked to any host
        FlavorgroupHostLinkRepository flavorGroupHostRepository = new FlavorgroupHostLinkRepository();
        FlavorgroupHostLinkFilterCriteria criteria = new FlavorgroupHostLinkFilterCriteria();
        criteria.flavorgroupId = obj.getId();

        FlavorgroupHostLinkCollection objHostLinkList = flavorGroupHostRepository.search(criteria);
        if (objHostLinkList != null && !objHostLinkList.getFlavorgroupHostLinks().isEmpty()) {
            log.error("The Flavorgroup has active assocations with host(s)");
            throw new WebApplicationException("The Flavorgroup has active assocations with host(s)", Response.Status.BAD_REQUEST);
        }
        repository.delete(locator);

    }

    

    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @RequiresPermissions("flavorgroups:retrieve")
    public Flavorgroup retrieve(@BeanParam FlavorgroupLocator locator) throws IOException, NonexistentEntityException {
        ValidationUtil.validate(locator);
        Flavorgroup flavorgroupObj = new Flavorgroup();
        if (locator == null) {
            log.error("Flavorgroup retrieve : error during flavorgroup retrieve");
            throw new RepositoryInvalidInputException("Flavorgroup name or the ID must be specified");
        }
        //search the mw_flavorgroup tabel and get all the flavorgroup table values
        FlavorgroupRepository flavorgroupRepository = new FlavorgroupRepository();
        Flavorgroup flavorgroup = flavorgroupRepository.retrieve(locator);
        if (flavorgroup == null) {
            log.error("Flavorgroup retrieve : error during flavorgroup retrieve");
            throw new RepositoryInvalidInputException("Specified flavorgroup could not be found");            
        }
        flavorgroupObj.setId(flavorgroup.getId());
        flavorgroupObj.setName(flavorgroup.getName());
        flavorgroupObj.setFlavorMatchPolicyCollection(flavorgroup.getFlavorMatchPolicyCollection());
        //get the collection of flavorId's from mw_link_flavor_flavorgroup
        FlavorFlavorgroupLinkCollection flavorgroupFlavorCollection;
        FlavorFlavorgroupLinkRepository flavorgroupFlavorRepository = new FlavorFlavorgroupLinkRepository();
        FlavorFlavorgroupLinkFilterCriteria criteria = new FlavorFlavorgroupLinkFilterCriteria();
        criteria.flavorgroupId = flavorgroup.getId();
        flavorgroupFlavorCollection = flavorgroupFlavorRepository.search(criteria);
        List<Flavor> flavors = new ArrayList<>();
        List<UUID> flavorIds = new ArrayList<>();
        if (flavorgroupFlavorCollection != null && flavorgroupFlavorCollection.getFlavorFlavorgroupLinks() != null && flavorgroupFlavorCollection.getFlavorFlavorgroupLinks().size() > 0) {
            for (FlavorFlavorgroupLink flavorgroupFlavor : flavorgroupFlavorCollection.getFlavorFlavorgroupLinks()) {
                UUID flavorId = flavorgroupFlavor.getFlavorId();
                flavorIds.add(flavorId);
                if (locator.includeFlavorContent == true) {
                    FlavorRepository flavorRepository = new FlavorRepository();
                    FlavorLocator flavorLocator = new FlavorLocator();
                    flavorLocator.id = flavorId;
                    SignedFlavor signedFlavor = flavorRepository.retrieve(flavorLocator);
                    if (signedFlavor == null) {
                        throw new WebApplicationException("Could not find signed flavor for id:" + flavorId.toString(), 404);
                    }
                    flavors.add(signedFlavor.getFlavor());
                }
            }
            flavorgroup.setFlavorIds(flavorIds);
            flavorgroup.setFlavors(flavors);
        }

        return flavorgroup;
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Path("/{flavorgroupId}/flavors")
    @RequiresPermissions("flavorgroups:create")
    public void createFlavorgroupFlavorLinks(@PathParam("flavorgroupId") UUID flavorgroupId, FlavorFlavorgroupLinkCreateCriteria criteria) {
        if(criteria.getFlavorId() == null) {
            throw new WebApplicationException("Flavor ID is required to associate with flavorgroup.");
        }
        FlavorFlavorgroupLinkRepository repo = new FlavorFlavorgroupLinkRepository();
        FlavorFlavorgroupLinkLocator flavorgroupLinkLocator = new FlavorFlavorgroupLinkLocator();
        flavorgroupLinkLocator.flavorgroupId = flavorgroupId;
        flavorgroupLinkLocator.flavorId = criteria.getFlavorId();
        FlavorFlavorgroupLink flavorFlavorgroupLink = repo.retrieve(flavorgroupLinkLocator);
        if (flavorFlavorgroupLink != null) {
            throw new RepositoryInvalidInputException("Specified flavor-flavorgroup link already exists");
        }
        FlavorFlavorgroupLink link = new FlavorFlavorgroupLink();
        link.setFlavorId(criteria.getFlavorId());
        link.setFlavorgroupId(flavorgroupId);

        repo.create(link);
    }

    @DELETE
    @Path("/{flavorgroupId}/flavors/{flavorId}")
    @RequiresPermissions("flavorgroups:delete")
    public void deleteFlavorgroupFlavorLinks(@PathParam("flavorgroupId") UUID flavorgroupId, @PathParam("flavorId") UUID flavorId) {
        FlavorFlavorgroupLinkRepository repo = new FlavorFlavorgroupLinkRepository();
        FlavorFlavorgroupLinkLocator flavorgroupLinkLocator = new FlavorFlavorgroupLinkLocator();
        flavorgroupLinkLocator.flavorgroupId = flavorgroupId;
        flavorgroupLinkLocator.flavorId = flavorId;
        FlavorFlavorgroupLink flavorFlavorgroupLink = repo.retrieve(flavorgroupLinkLocator);
        if (flavorFlavorgroupLink == null) {
            log.error("Flavor-Flavorgroup link retrieve : error during flavor-flavorgroup link retrieve");
            throw new RepositoryInvalidInputException("Specified flavor-flavorgroup link does not exists");
        }
        repo.delete(flavorgroupLinkLocator);
    }

    @GET
    @Path("/{flavorgroupId}/flavors/{flavorId}")
    public FlavorFlavorgroupLink retrieveFlavorgroupFlavorLink(@PathParam("flavorgroupId") UUID flavorgroupId, @PathParam("flavorId") UUID flavorId) {

        FlavorFlavorgroupLinkRepository repo = new FlavorFlavorgroupLinkRepository();
        FlavorFlavorgroupLinkLocator flavorgroupLinkLocator = new FlavorFlavorgroupLinkLocator();
        flavorgroupLinkLocator.flavorgroupId = flavorgroupId;
        flavorgroupLinkLocator.flavorId = flavorId;
        return repo.retrieve(flavorgroupLinkLocator);
    }
    
    @GET
    @Path("/{flavorgroupId}/flavors/")
    public FlavorFlavorgroupLinkCollection seachFlavorgroupFlavorLink(@PathParam("flavorgroupId") UUID flavorgroupId, @BeanParam FlavorFlavorgroupLinkFilterCriteria criteria, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
        ValidationUtil.validate(flavorgroupId);
        ValidationUtil.validate(criteria);
        log.debug("target: {} - {}", httpServletRequest.getRequestURI(), httpServletRequest.getQueryString());
        FlavorFlavorgroupLinkRepository flavorFlavorgroupLinkRepository = new FlavorFlavorgroupLinkRepository();
        return flavorFlavorgroupLinkRepository.search(criteria);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @RequiresPermissions("flavorgroups:search")
    public FlavorgroupCollection search(@BeanParam FlavorgroupFilterCriteria flavorgroupFilterCriteria) {
        ValidationUtil.validate(flavorgroupFilterCriteria);

        //search the mw_flavorgroup tabel and get all the flavorgroup table values
        Flavorgroup flavorgroup;
        FlavorgroupCollection flavorgroupCollection = repository.search(flavorgroupFilterCriteria);
        if (flavorgroupCollection != null && flavorgroupCollection.getFlavorgroups() != null
                && flavorgroupCollection.getFlavorgroups().size() > 0) {
            for (int j = 0; j < flavorgroupCollection.getFlavorgroups().size(); j++) {
                flavorgroup = flavorgroupCollection.getFlavorgroups().get(j);
                //get the collection of flavorId's from mw_link_flavor_flavorgroup
                FlavorFlavorgroupLinkRepository flavorgroupFlavorRepository = new FlavorFlavorgroupLinkRepository();
                FlavorFlavorgroupLinkFilterCriteria criteria = new FlavorFlavorgroupLinkFilterCriteria();
                criteria.flavorgroupId = flavorgroup.getId();
                FlavorFlavorgroupLinkCollection flavorgroupFlavorCollection = flavorgroupFlavorRepository.search(criteria);
                List<Flavor> flavors = new ArrayList<>();
                List<UUID> flavorIds = new ArrayList<>();
                if (flavorgroupFlavorCollection != null && flavorgroupFlavorCollection.getFlavorFlavorgroupLinks() != null
                        && flavorgroupFlavorCollection.getFlavorFlavorgroupLinks().size() > 0) {
                    for (FlavorFlavorgroupLink flavorgroupFlavor : flavorgroupFlavorCollection.getFlavorFlavorgroupLinks()) {
                        UUID flavorId = flavorgroupFlavor.getFlavorId();
                        flavorIds.add(flavorId);
                        if (flavorgroupFilterCriteria.includeFlavorContent) {
                            FlavorRepository flavorRepository = new FlavorRepository();
                            FlavorLocator locator = new FlavorLocator();
                            locator.id = flavorId;
                            SignedFlavor signedFlavor = flavorRepository.retrieve(locator);
                            if (signedFlavor == null) {
                                throw new WebApplicationException("Could not find signed flavor for id:" + flavorId.toString(), 404);
                            }
                            flavors.add(signedFlavor.getFlavor());
                        }
                    }
                    flavorgroup.setFlavorIds(flavorIds);
                    flavorgroup.setFlavors(flavors);
                }
            }
        }
        return flavorgroupCollection;
    }
}
