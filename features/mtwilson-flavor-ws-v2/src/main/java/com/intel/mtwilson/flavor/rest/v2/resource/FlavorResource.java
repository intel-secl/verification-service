/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.core.flavor.PlatformFlavor;
import com.intel.mtwilson.core.flavor.PlatformFlavorFactory;
import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.core.flavor.common.PlatformFlavorException;
import com.intel.mtwilson.core.flavor.common.PlatformFlavorUtil;
import com.intel.mtwilson.core.flavor.model.Flavor;
import static com.intel.mtwilson.core.flavor.common.FlavorPart.*;

import com.intel.mtwilson.core.flavor.model.Meta;
import com.intel.mtwilson.core.flavor.model.SignedFlavor;
import com.intel.mtwilson.flavor.rest.v2.model.*;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorFlavorgroupLinkRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorHostLinkRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorgroupHostLinkRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorgroupRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.HostRepository;
import com.intel.mtwilson.flavor.rest.v2.utils.FlavorGroupUtils;
import com.intel.mtwilson.flavor.rest.v2.utils.FlavorUtils;
import com.intel.mtwilson.i18n.HostState;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.core.common.datatypes.ConnectionString;
import com.intel.mtwilson.core.common.model.HostManifest;
import com.intel.mtwilson.core.common.model.SoftwareFlavorPrefix;
import com.intel.mtwilson.core.common.tag.model.X509AttributeCertificate;
import com.intel.mtwilson.core.common.tag.model.TagCertificate;
import com.intel.mtwilson.flavor.runnable.AddFlavorgroupHostsToFlavorVerifyQueue;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.tag.model.TagCertificateLocator;
import com.intel.mtwilson.tag.rest.v2.repository.TagCertificateRepository;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;

import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.intel.mtwilson.util.crypto.keystore.PrivateKeyStore;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author srege
 */
@V2
@Path("/flavors")
public class FlavorResource {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FlavorResource.class);
    private FlavorRepository repository;
    private static final String DEPRECATED_FLAVOR_PART_BIOS = "BIOS";
    private static final String FLAVOR_SIGNER_KEYSTORE_FILE = "flavor.signer.keystore.file";
    private static final String FLAVOR_SIGNER_KEYSTORE_PASSWORD = "flavor.signer.keystore.password";
    private static final String FLAVOR_SIGNING_KEY_ALIAS = "flavor.signing.key.alias";

    public FlavorResource() {
        repository = new FlavorRepository();
    }

    protected FlavorRepository getRepository() {
        return repository;
    }

    protected FlavorCollection createEmptyCollection() {
        return new FlavorCollection();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @RequiresPermissions("flavors:search")
    public SignedFlavorCollection searchFlavorJSON(@BeanParam FlavorFilterCriteria criteria, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
        ValidationUtil.validate(criteria);
        log.debug("target: {} - {}", httpServletRequest.getRequestURI(), httpServletRequest.getQueryString());
        return repository.search(criteria);
    }

    @GET
    @Produces({DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @RequiresPermissions("flavors:search")
    public FlavorCollection searchFlavor(@BeanParam FlavorFilterCriteria criteria, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
        ValidationUtil.validate(criteria);
        log.debug("target: {} - {}", httpServletRequest.getRequestURI(), httpServletRequest.getQueryString());
        FlavorCollection flavorCollection = new FlavorCollection();
        flavorCollection.setFlavors(repository.search(criteria).getFlavors());
        return flavorCollection;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML})
    @RequiresPermissions("flavors:search")
    public FlavorCollection searchFlavorXML(@BeanParam FlavorFilterCriteria criteria, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
        ValidationUtil.validate(criteria);
        log.debug("target: {} - {}", httpServletRequest.getRequestURI(), httpServletRequest.getQueryString());
        SignedFlavorCollection signedFlavorCollection =  repository.search(criteria);
        FlavorCollection flavorCollection = new FlavorCollection();
        flavorCollection.setFlavors(signedFlavorCollection.getFlavors());
        return FlavorGroupUtils.updatePathSeparatorForXML(flavorCollection);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{id}")
    @RequiresPermissions("flavors:retrieve")
    public SignedFlavor retrieveFlavorJSON(@BeanParam FlavorLocator locator, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
        ValidationUtil.validate(locator);
        return repository.retrieve(locator);
    }

    @GET
    @Produces({DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Path("/{id}")
    @RequiresPermissions("flavors:retrieve")
    public Flavor retrieveFlavor(@BeanParam FlavorLocator locator, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
        ValidationUtil.validate(locator);
        SignedFlavor signedFlavor = repository.retrieve(locator);
        if (signedFlavor == null) {
            throw new WebApplicationException("Signed Flavor not found for flavor id:" + locator.id.toString(), Response.Status.BAD_REQUEST);
        }
        return signedFlavor.getFlavor();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/{id}")
    @RequiresPermissions("flavors:retrieve")
    public Flavor retrieveFlavorXML(@BeanParam FlavorLocator locator, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
        ValidationUtil.validate(locator);
        SignedFlavor signedFlavor = repository.retrieve(locator);
        Flavor flavor = new Flavor();
        if (signedFlavor != null && signedFlavor.getFlavor() != null && signedFlavor.getFlavor().getMeta() != null &&
                signedFlavor.getFlavor().getMeta().getDescription() != null)
            if (signedFlavor.getFlavor().getMeta().getDescription().getFlavorPart().equals("SOFTWARE"))
                flavor = FlavorUtils.updatePathSeparatorForXML(signedFlavor.getFlavor());
            else
                flavor = signedFlavor.getFlavor();
        return flavor;
    }

    /**
     * Add an item to the collection. Input Content-Type is any of
     * application/json, application/xml, application/yaml, or text/yaml Output
     * Content-Type is any of application/json, application/xml,
     * application/yaml, or text/yaml
     *
     * The input must represent a single item NOT wrapped in a collection.
     *
     * @param item
     * @return
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces({MediaType.APPLICATION_JSON})
    public SignedFlavorCollection createFlavors(FlavorCreateCriteria item) throws IOException, Exception {
        ValidationUtil.validate(item);

        if (item == null) {
            throw new WebApplicationException("Flavor create criteria must be specified", Response.Status.BAD_REQUEST);
        }

        if (item.getPartialFlavorTypes() == null || item.getPartialFlavorTypes().isEmpty()) {
            return createNonHostUnique(item);
        } else {
            item.setPartialFlavorTypes(replaceBoisToPlatform(item.getPartialFlavorTypes()));
            for (String partialFlavorType : item.getPartialFlavorTypes()) {
                if (partialFlavorType.equalsIgnoreCase(HOST_UNIQUE.getValue())) {
                    return createHostUniqueFlavors(item);
                } else if (partialFlavorType.equalsIgnoreCase(ASSET_TAG.getValue())) {
                    return createAssetTagFlavors(item);
                } else if (partialFlavorType.equalsIgnoreCase(FlavorPart.SOFTWARE.getValue())) {
                    return createSoftwareFlavors(item);
                } else {
                    return createNonHostUnique(item);
                }
            }
        }
        return null;
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces({DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    public FlavorCollection createFlavorsYAML(FlavorCreateCriteria item) throws IOException, Exception {
        ValidationUtil.validate(item);
        SignedFlavorCollection signedFlavorCollection = createFlavors(item);
        if (signedFlavorCollection == null) {
            throw new WebApplicationException("Failed to create flavor for given criteria", Response.Status.INTERNAL_SERVER_ERROR);
        }
        FlavorCollection flavorCollection = new FlavorCollection();
        flavorCollection.setFlavors(signedFlavorCollection.getFlavors());
        return flavorCollection;
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces({MediaType.APPLICATION_XML})
    public FlavorCollection createFlavorsXML(FlavorCreateCriteria item) throws IOException, Exception {
        ValidationUtil.validate(item);
        FlavorCollection flavorCollection = new FlavorCollection();
        SignedFlavorCollection signedFlavorCollection = createFlavors(item);
        if (signedFlavorCollection != null) {
            flavorCollection.setFlavors(signedFlavorCollection.getFlavors());
            flavorCollection = FlavorGroupUtils.updatePathSeparatorForXML(flavorCollection);
        }
        return flavorCollection;
    }

    // TODO: Additional support for backward compatibility
    private List<String> replaceBoisToPlatform(List<String> partialFlavorTypes) {
        List<String> filteredFlavorTypes = new ArrayList<>();
        for(String flavorType :  partialFlavorTypes) {
            if(flavorType.equalsIgnoreCase("BIOS")) {
                filteredFlavorTypes.add(FlavorPart.PLATFORM.getValue());
            } else {
                filteredFlavorTypes.add(flavorType);
            }
        }
        return filteredFlavorTypes;
    }

    @RequiresPermissions("tag_flavors:create")
    private SignedFlavorCollection createAssetTagFlavors(FlavorCreateCriteria item) throws IOException, Exception {
        return createOne(item);
    }
    
    @RequiresPermissions("host_unique_flavors:create")
    private SignedFlavorCollection createHostUniqueFlavors(FlavorCreateCriteria item) throws IOException, Exception {
        return createOne(item);
    }

    @RequiresPermissions("software_flavors:create")
    private SignedFlavorCollection createSoftwareFlavors(FlavorCreateCriteria item) throws IOException, Exception {
        return createOne(item);
    }

    @RequiresPermissions("flavors:create")
    private SignedFlavorCollection createNonHostUnique(FlavorCreateCriteria item) throws IOException, Exception{
        return createOne(item);
    }

    
    private SignedFlavorCollection createOne(FlavorCreateCriteria item) throws IOException, Exception {
        X509AttributeCertificate attributeCertificate = null;
        Map<String, List<SignedFlavor>> flavorPartFlavorMap = new HashMap<>();
        List<String> partialFlavorTypes = new ArrayList();
        // get flavor from host or from input
        PlatformFlavor platformFlavor = null;
        if (item.getConnectionString() != null && !item.getConnectionString().isEmpty()) {
            ConnectionString connectionString = HostRepository.generateConnectionString(item.getConnectionString());
            
            // connect to the host and retrieve the host manifest
            TlsPolicyDescriptor tlsPolicyDescriptor = new HostResource().getTlsPolicy(
                    item.getTlsPolicyId(), connectionString, true);
            HostManifest hostManifest;
            try {
                hostManifest = new HostResource().getHostManifest(
                    tlsPolicyDescriptor, connectionString, null);
            } catch (Exception e) {
                log.debug("Flavors: Exception instance when connecting to host is {}", e.toString());
                HostState hostState = new HostStatusResource().determineHostState(e);
                throw new WebApplicationException(hostState.getHostStateText(), e, Response.Status.BAD_REQUEST);
            }
            
            TagCertificateRepository repo = new TagCertificateRepository();
            TagCertificateLocator tagCertificateLocator = new TagCertificateLocator();
            tagCertificateLocator.subjectEqualTo = hostManifest.getHostInfo().getHardwareUuid();
            TagCertificate tagCertificate = repo.retrieve(tagCertificateLocator);
            if (tagCertificate != null) {
                attributeCertificate = X509AttributeCertificate.valueOf(tagCertificate.getCertificate());
                log.debug("X509Attribute Certificate created {}", attributeCertificate);
            }
            
            // cast the host manifest to a platform flavor using the lib-flavor
            PlatformFlavorFactory factory = new PlatformFlavorFactory();
            platformFlavor = factory.getPlatformFlavor(hostManifest, attributeCertificate);
            log.debug("Platform flavor part names: {}", platformFlavor.getFlavorPartNames());
            
            // set user specified partial flavor types
            if (item.getPartialFlavorTypes() != null && !item.getPartialFlavorTypes().isEmpty()) {
                partialFlavorTypes.addAll(item.getPartialFlavorTypes());
            }
        } else if (item.getFlavorCollection() != null && item.getFlavorCollection().getFlavors() != null
                && !item.getFlavorCollection().getFlavors().isEmpty()) {
            PrivateKeyStore privateKeyStore = new PrivateKeyStore("PKCS12", new File(MSConfig.getConfiguration().getString(FLAVOR_SIGNER_KEYSTORE_FILE)), MSConfig.getConfiguration().getString(FLAVOR_SIGNER_KEYSTORE_PASSWORD).toCharArray());
            PrivateKey privateKey = privateKeyStore.getPrivateKey(MSConfig.getConfiguration().getString(FLAVOR_SIGNING_KEY_ALIAS, "flavor-signing-key"));
            for (Flavor flavor : item.getFlavorCollection().getFlavors()) {
                if (flavor != null && flavor.getMeta() != null && flavor.getMeta().getDescription() != null
                        && flavor.getMeta().getDescription().getFlavorPart() != null) {
                    // TODO: Additional support for backward compatibility
                    if(flavor.getMeta().getDescription().getFlavorPart().equalsIgnoreCase(DEPRECATED_FLAVOR_PART_BIOS)) {
                        flavor.getMeta().getDescription().setFlavorPart(FlavorPart.PLATFORM.getValue());
                    }
                    validateFlavorMetaContent(flavor.getMeta());
                    if(flavorPartFlavorMap.containsKey(flavor.getMeta().getDescription().getFlavorPart())) {
                        flavorPartFlavorMap.get(flavor.getMeta().getDescription().getFlavorPart())
                                .add(PlatformFlavorUtil.getSignedFlavor(Flavor.serialize(flavor), privateKey));
                    } else {
                        List<SignedFlavor> signedFlavorsList = new ArrayList();
                        signedFlavorsList.add(PlatformFlavorUtil.getSignedFlavor(Flavor.serialize(flavor), privateKey));
                        flavorPartFlavorMap.put(flavor.getMeta().getDescription().getFlavorPart(), signedFlavorsList);
                    }
                    partialFlavorTypes.add(flavor.getMeta().getDescription().getFlavorPart());
                }
            }
            if (flavorPartFlavorMap.isEmpty()) {
                throw new WebApplicationException("Flavor collection or host connection string must be specified", Response.Status.BAD_REQUEST);
            }
        } else {
            throw new WebApplicationException("Host connection string or flavor content must be specified", Response.Status.BAD_REQUEST);
        }
        
        // when no flavor types are specified for automatic flavor creation, set default automatic flavor types
        // need to also validate that the flavor collection input has not set any partial flavor types
        if ((item.getFlavorgroupName() == null || item.getFlavorgroupName().isEmpty())
                && (item.getPartialFlavorTypes() == null || item.getPartialFlavorTypes().isEmpty())
                && partialFlavorTypes.isEmpty()) {
            partialFlavorTypes.addAll(FlavorPart.getValues());
        }
        
        // determine flavorgroup name
        String flavorgroupName;
        if (item.getFlavorgroupName() != null && !item.getFlavorgroupName().isEmpty()) {
            flavorgroupName = item.getFlavorgroupName();
        } else {
            flavorgroupName = Flavorgroup.AUTOMATIC_FLAVORGROUP;
        }
        
        // look for flavorgroup
        Flavorgroup flavorgroup = FlavorGroupUtils.getFlavorGroupByName(flavorgroupName);
        if(flavorgroup == null) {
            flavorgroup = FlavorGroupUtils.createFlavorGroupByName(flavorgroupName);
        }
        // if host connector retrieved platform flavor, break it into the flavor part flavor map using the flavorgroup id
        if (platformFlavor != null) {
            flavorPartFlavorMap = retrieveFlavorCollection(platformFlavor, flavorgroup.getId().toString(), partialFlavorTypes);
        }
        
        // throw error if no flavors are to be created
        if (flavorPartFlavorMap == null || flavorPartFlavorMap.isEmpty()) {
            throw new WebApplicationException("Cannot create flavors", Response.Status.BAD_REQUEST);
        }
        return addFlavorToFlavorgroup(flavorPartFlavorMap, flavorgroup.getId());
    }

    private void validateFlavorMetaContent(Meta flavorMeta) {
        if(flavorMeta.getDescription().getLabel() == null || flavorMeta.getDescription().getLabel().isEmpty()) {
            throw new WebApplicationException("Flavor label missing", Response.Status.BAD_REQUEST);
        }
        try {
            FlavorPart.valueOf(flavorMeta.getDescription().getFlavorPart());
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException("Invalid flavor part specified", Response.Status.BAD_REQUEST);
        }
    }

    // the delete method is on a specific resource id and because we don't return any content it's the same whether its simple object or json api 
    // jersey automatically returns status code 204 No Content (successful) to the client because
    // we have a void return type
    @Path("/{id}")
    @DELETE
    @RequiresPermissions("flavors:delete")
    public void deleteOne(@BeanParam FlavorLocator locator, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
        ValidationUtil.validate(locator); 
        SignedFlavor item = getRepository().retrieve(locator); // subclass is responsible for validating the id in whatever manner it needs to;  most will return null if !UUID.isValid(id)  but we don't do it here because a resource might want to allow using something other than uuid as the url key, for example uuid OR hostname for hosts
        if (item == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        List<UUID> hostIds = new ArrayList();
        
        // retrieve list of hosts associated with the flavor
        FlavorHostLinkFilterCriteria flavorHostLinkFilterCriteria = new FlavorHostLinkFilterCriteria();
        flavorHostLinkFilterCriteria.flavorId = UUID.valueOf(item.getFlavor().getMeta().getId());
        FlavorHostLinkCollection flavorHostLinkCollection
                = new FlavorHostLinkRepository().search(flavorHostLinkFilterCriteria);
        if (flavorHostLinkCollection != null && flavorHostLinkCollection.getFlavorHostLinks() != null
                && !flavorHostLinkCollection.getFlavorHostLinks().isEmpty()) {
            for (FlavorHostLink flavorHostLink : flavorHostLinkCollection.getFlavorHostLinks()) {
                hostIds.add(flavorHostLink.getHostId());
                
                // Delete the flavor host link
                FlavorHostLinkLocator flavorHostLinkLocator = new FlavorHostLinkLocator(flavorHostLink.getId());
                new FlavorHostLinkRepository().delete(flavorHostLinkLocator);
            }
        }
        
        // retrieve list of flavorgroups associated with the flavor
        FlavorFlavorgroupLinkFilterCriteria flavorFlavorgroupLinkFilterCriteria
                = new FlavorFlavorgroupLinkFilterCriteria();
        flavorFlavorgroupLinkFilterCriteria.flavorId = UUID.valueOf(item.getFlavor().getMeta().getId());
        FlavorFlavorgroupLinkCollection flavorFlavorgroupLinkCollection
                = new FlavorFlavorgroupLinkRepository().search(flavorFlavorgroupLinkFilterCriteria);
        
        // if there are flavorgroups associated with the flavor
        if (flavorFlavorgroupLinkCollection != null
                && flavorFlavorgroupLinkCollection.getFlavorFlavorgroupLinks() != null
                && !flavorFlavorgroupLinkCollection.getFlavorFlavorgroupLinks().isEmpty()) {
            
            // for each flavorgroup associated with the flavor
            for (FlavorFlavorgroupLink flavorFlavorgroupLink : flavorFlavorgroupLinkCollection.getFlavorFlavorgroupLinks()) {
                
                // get hosts associated with flavorgroup
                FlavorgroupHostLinkFilterCriteria flavorgroupHostLinkFilterCriteria
                        = new FlavorgroupHostLinkFilterCriteria();
                flavorgroupHostLinkFilterCriteria.flavorgroupId = flavorFlavorgroupLink.getFlavorgroupId();
                FlavorgroupHostLinkCollection flavorgroupHostLinkCollection
                = new FlavorgroupHostLinkRepository().search(flavorgroupHostLinkFilterCriteria);
                
                // add the hosts to the list of affected host IDs
                if (flavorgroupHostLinkCollection != null && flavorgroupHostLinkCollection.getFlavorgroupHostLinks() != null
                        && !flavorgroupHostLinkCollection.getFlavorgroupHostLinks().isEmpty()) {
                    for (FlavorgroupHostLink flavorgroupHostLink : flavorgroupHostLinkCollection.getFlavorgroupHostLinks()) {
                        if (!hostIds.contains(flavorgroupHostLink.getHostId()))
                            hostIds.add(flavorgroupHostLink.getHostId());
                    }
                }
                
                // Delete the link between the flavor and flavor group
                FlavorFlavorgroupLinkLocator flavorFlavorgroupLinkLocator = new FlavorFlavorgroupLinkLocator();
                flavorFlavorgroupLinkLocator.id = flavorFlavorgroupLink.getId();
                log.debug("Flavors : About to delete the flavor-flavorgroup link with ID - {}", flavorFlavorgroupLinkLocator.id.toString());
                new FlavorFlavorgroupLinkRepository().delete(flavorFlavorgroupLinkLocator);
            }
        }
        
        // finally, delete the flavor
        getRepository().delete(locator);
        
        // add the hosts to the flavor-verify queue
        for (UUID hostId : hostIds) {
            new HostResource().addHostToFlavorVerifyQueue(hostId, false);
        }
        
        // set the response code to 204
        httpServletResponse.setStatus(Status.NO_CONTENT.getStatusCode());
    }
    
    /**
     * This method associates the flavor/flavor parts specified from the PlatformFlavor instance
     * with a particular flavor group.
     *
     * @param flavorObjCollection
     * @param flavorgroupId Flavorgroup ID to which the flavor needs to be
     * associated
     * @return createdFlavor Created flavor
     */
    public SignedFlavorCollection addFlavorToFlavorgroup(Map<String, List<SignedFlavor>> flavorObjCollection, UUID flavorgroupId) {
        SignedFlavorCollection returnFlavors = new SignedFlavorCollection();
        Collection<UUID> flavorIds = new ArrayList<>();

        for (Map.Entry<String, List<SignedFlavor>> flavorObj : flavorObjCollection.entrySet()) {
            for(SignedFlavor signedFlavor : flavorObj.getValue()) {
                SignedFlavor flavorCreated = new FlavorRepository().create(signedFlavor);
                //TODO: need to handle this more gracefully for exception in flavor creation
                if (flavorCreated != null) {
                    returnFlavors.getSignedFlavors().add(flavorCreated);
                    // If the flavor part is HOST_UNIQUE OR ASSET_TAG, then we associate it with the host_unique group name
                    if (flavorObj.getKey().equalsIgnoreCase(ASSET_TAG.getValue())) {
                        addFlavorToUniqueFlavorgroup(flavorCreated.getFlavor(), true);
                    } else if (flavorObj.getKey().equalsIgnoreCase(HOST_UNIQUE.getValue())) {
                        addFlavorToUniqueFlavorgroup(flavorCreated.getFlavor(), false);
                    } else if (flavorObj.getKey().equalsIgnoreCase(SOFTWARE.getValue()) && signedFlavor.getFlavor().getMeta().getDescription().getLabel().contains(SoftwareFlavorPrefix.DEFAULT_APPLICATION_FLAVOR_PREFIX.getValue())) {
                        addFlavorToIseclSoftwareFlavorgroup(flavorCreated.getFlavor(), Flavorgroup.PLATFORM_SOFTWARE_FLAVORGROUP);
                    } else if (flavorObj.getKey().equalsIgnoreCase(SOFTWARE.getValue()) && signedFlavor.getFlavor().getMeta().getDescription().getLabel().contains(SoftwareFlavorPrefix.DEFAULT_WORKLOAD_FLAVOR_PREFIX.getValue())) {
                        addFlavorToIseclSoftwareFlavorgroup(flavorCreated.getFlavor(), Flavorgroup.WORKLOAD_SOFTWARE_FLAVORGROUP);
                    } else {
                        // For other flavor parts, we just store all the individual flavors first and finally do the association below.
                        // Other flavor parts include OS, PLATFORM, SOFTWARE
                        flavorIds.add(UUID.valueOf(flavorCreated.getFlavor().getMeta().getId()));
                    }
                }
            }
        }

        if (flavorgroupId == null || flavorIds.isEmpty()) {
            log.trace("Flavorgroup ID or flavor IDs not specified");
            return returnFlavors;
        }

        // For flavor parts other than HOST_UNIQUE & ASSET_TAG, we associate it with the specified flavor group, which can be either 
        // automatic or a custom group name spcified by the end user.
        for (UUID flavorId : flavorIds) {
            FlavorFlavorgroupLink flavorFlavorgroupLink = new FlavorFlavorgroupLink();
            flavorFlavorgroupLink.setFlavorId(flavorId);
            flavorFlavorgroupLink.setFlavorgroupId(flavorgroupId);
            new FlavorFlavorgroupLinkRepository().create(flavorFlavorgroupLink);
        }

        // Add hosts matching the flavorgroup to flavor verify queue in background
        new Thread(new AddFlavorgroupHostsToFlavorVerifyQueue(flavorgroupId, false)).start();
        return returnFlavors;
    }
    
    /**
     * Associates the flavor with the unique flavor group created during the
     * installation.
     *
     * @param flavor Complete flavor.
     * @param forceUpdate option for flavor-verify operation to force an updated
     * report to be generated from the host directly
     */
    private void addFlavorToUniqueFlavorgroup(Flavor flavor, boolean forceUpdate) {
        // get flavor ID
        UUID flavorId = UUID.valueOf(flavor.getMeta().getId());
        // find unique flavorgroup
        FlavorgroupLocator flavorgroupLocator = new FlavorgroupLocator();
        flavorgroupLocator.name = Flavorgroup.HOST_UNIQUE_FLAVORGROUP;
        Flavorgroup uniqueFlavorgroup = new FlavorgroupRepository().retrieve(flavorgroupLocator);
        if (uniqueFlavorgroup == null) {
            Flavorgroup newFlavorgroup = new Flavorgroup();
            newFlavorgroup.setName(Flavorgroup.HOST_UNIQUE_FLAVORGROUP);
            newFlavorgroup.setFlavorMatchPolicyCollection(null);
            uniqueFlavorgroup = new FlavorgroupRepository().create(newFlavorgroup);
        }
        
        // create the flavor flavorgroup link association
        FlavorFlavorgroupLink flavorFlavorgroupLink = new FlavorFlavorgroupLink();
        flavorFlavorgroupLink.setFlavorId(flavorId);
        flavorFlavorgroupLink.setFlavorgroupId(uniqueFlavorgroup.getId());
        new FlavorFlavorgroupLinkRepository().create(flavorFlavorgroupLink);
        
        // retrieve the host name from the flavor document, if it exists
        String hostName = null;
        if (flavor.getMeta() != null && flavor.getMeta().getDescription() != null
                && flavor.getMeta().getDescription().getLabel() != null
                && !flavor.getMeta().getDescription().getLabel().isEmpty()) {
            hostName = flavor.getMeta().getDescription().getLabel();
        }
        
        // retrieve the hardware uuid from the flavor document, if it exists
        UUID hardwareUuid = null;
        if (flavor.getMeta() != null && flavor.getMeta().getDescription() != null
                && flavor.getMeta().getDescription().getHardwareUuid() != null
                && !flavor.getMeta().getDescription().getHardwareUuid().isEmpty()
                && UUID.isValid(flavor.getMeta().getDescription().getHardwareUuid())) {
            hardwareUuid = UUID.valueOf(flavor.getMeta().getDescription().getHardwareUuid());
        }
        
        if ((hostName == null || hostName.isEmpty()) && hardwareUuid == null) {
            throw new IllegalArgumentException("Host name or hardware UUID must be specified in the flavor document");
        }
        
        // get the host details from the database
        HostLocator hostLocator = new HostLocator();
        if (hostName != null)
            hostLocator.name = hostName;
        if (hardwareUuid != null)
            hostLocator.hardwareUuid =  hardwareUuid;
        Host host = new HostRepository().retrieve(hostLocator);
        if (host == null) {
            log.debug("Host [{}] is not registered, no further processing will be performed", hostName);
            return;
        }
        
        // add host to flavor-verify queue
        new HostResource().addHostToFlavorVerifyQueue(host.getId(), forceUpdate);
    }

    /**
     * Associates the flavor with the default software flavor group created during the
     * installation.
     *
     * @param flavor Complete flavor.
     * report to be generated from the host directly
     */
    private void addFlavorToIseclSoftwareFlavorgroup(Flavor flavor, String flavorgroupName) {
        // get flavor ID
        UUID flavorId = UUID.valueOf(flavor.getMeta().getId());

        // find unique flavorgroup
        Flavorgroup iseclSoftwareFlavorgroup = FlavorGroupUtils.getFlavorGroupByName(flavorgroupName);
        if (iseclSoftwareFlavorgroup == null) {
            Flavorgroup newFlavorgroup = new Flavorgroup();
            newFlavorgroup.setName(flavorgroupName);
            newFlavorgroup.setFlavorMatchPolicyCollection(Flavorgroup.getIseclSoftwareFlavorMatchPolicy());
            iseclSoftwareFlavorgroup = new FlavorgroupRepository().create(newFlavorgroup);
        }

        // create the flavor flavorgroup link association
        FlavorFlavorgroupLink flavorFlavorgroupLink = new FlavorFlavorgroupLink();
        flavorFlavorgroupLink.setFlavorId(flavorId);
        flavorFlavorgroupLink.setFlavorgroupId(iseclSoftwareFlavorgroup.getId());
        new FlavorFlavorgroupLinkRepository().create(flavorFlavorgroupLink);
        
        // Add hosts matching the default flavorgroup to flavor verify queue in background and get updated host manifest
        new Thread(new AddFlavorgroupHostsToFlavorVerifyQueue(iseclSoftwareFlavorgroup.getId(), true)).start();
    }

    /**
     * Retrieves the list of all the flavors requested and returns it back to the caller
     * @param platformFlavor 
     * @param flavorgroupId
     * @param flavorParts
     * @return 
     */
    private Map<String, List<SignedFlavor>> retrieveFlavorCollection(PlatformFlavor platformFlavor, String flavorgroupId, Collection<String> flavorParts) throws Exception {
        Map<String, List<SignedFlavor>> flavorCollection = new HashMap<>();
        PrivateKeyStore privateKeyStore = new PrivateKeyStore("PKCS12", new File(MSConfig.getConfiguration().getString(FLAVOR_SIGNER_KEYSTORE_FILE)), MSConfig.getConfiguration().getString(FLAVOR_SIGNER_KEYSTORE_PASSWORD).toCharArray());
        PrivateKey privateKey = privateKeyStore.getPrivateKey(MSConfig.getConfiguration().getString(FLAVOR_SIGNING_KEY_ALIAS, "flavor-signing-key"));

        if (platformFlavor == null || flavorgroupId == null) {
            throw new IllegalArgumentException("Platform flavor and flavorgroup ID must be specified");
        }
        
        if (flavorParts.isEmpty()) {
            flavorParts.add(SOFTWARE.name());
        }
        // User has specified the particular flavor part(s)
        for (String flavorPart : flavorParts) {
            try {
                for(SignedFlavor signedFlavor : platformFlavor.getFlavorPartWithSignature(flavorPart, (PrivateKey)privateKey)) {
                    if(flavorCollection.containsKey(flavorPart)) {
                        flavorCollection.get(flavorPart).add(signedFlavor);
                    } else {
                        List<SignedFlavor> signedFlavorList = new ArrayList();
                        signedFlavorList.add(signedFlavor);
                        flavorCollection.put(flavorPart, signedFlavorList);
                    }
                }
            } catch (PlatformFlavorException pe) {
                // This should be changed to warn , but Flavor library is throwing an exception when it does not find all flavor 
                // types in the content provided. Bug# ISECL-2210.
                log.debug("Could not build flavor [{}] from flavor library: {}", flavorPart, pe.getMessage());
            } catch (Exception ex) {
                throw new IllegalStateException("Error during creation of the flavors for the specified flavor parts", ex);
            }
        }
        return flavorCollection;
    }  
}
