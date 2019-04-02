/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.PublicKeyTlsPolicy;
import com.intel.mtwilson.core.common.model.HostInfo;
import com.intel.mtwilson.flavor.rest.v2.model.Host;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupHostLinkCreateCriteria;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.dcsg.cpg.x509.repository.PublicKeyRepository;
import com.intel.mtwilson.My;
import com.intel.mtwilson.core.host.connector.HostConnector;
import com.intel.mtwilson.core.host.connector.HostConnectorFactory;
import com.intel.mtwilson.flavor.model.HostStatusInformation;
import com.intel.mtwilson.flavor.rest.v2.model.Flavorgroup;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupCollection;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupHostLink;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupHostLinkLocator;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupLocator;
import com.intel.mtwilson.flavor.rest.v2.model.HostCollection;
import com.intel.mtwilson.flavor.rest.v2.model.HostCreateCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.HostFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.HostLocator;
import com.intel.mtwilson.flavor.rest.v2.model.HostStatus;
import com.intel.mtwilson.flavor.rest.v2.model.HostStatusLocator;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorgroupHostLinkRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorgroupRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.HostRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.HostStatusRepository;
import com.intel.mtwilson.flavor.rest.v2.utils.FlavorGroupUtils;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.core.common.model.HostManifest;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.intel.mtwilson.features.queue.model.Queue;
import com.intel.mtwilson.features.queue.model.QueueCollection;
import com.intel.mtwilson.features.queue.model.QueueFilterCriteria;
import com.intel.mtwilson.features.queue.repository.QueueRepository;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorHostLinkFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupHostLinkFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorHostLinkRepository;
import com.intel.mtwilson.i18n.HostState;

import static com.intel.mtwilson.i18n.HostState.QUEUE;

import com.intel.mtwilson.core.common.datatypes.ConnectionString;

import static com.intel.mtwilson.features.queue.model.QueueState.NEW;

import com.intel.mtwilson.flavor.controller.MwHostStatusJpaController;
import com.intel.mtwilson.flavor.controller.MwQueueJpaController;
import com.intel.mtwilson.flavor.data.MwHostStatus;
import com.intel.mtwilson.flavor.data.MwQueue;
import com.intel.mtwilson.flavor.rest.v2.model.ReportLocator;
import com.intel.mtwilson.flavor.rest.v2.repository.HostTlsPolicyRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.ReportRepository;
import com.intel.mtwilson.tls.policy.exception.TlsPolicyAllowedException;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyFactoryUtil;
import com.intel.mtwilson.tls.policy.filter.HostTlsPolicyFilter;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicy;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicyLocator;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.TlsProtection;

import java.net.MalformedURLException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 * @author hmgowda
 * @author purvades
 */
@V2
@Path("/hosts")
public class HostResource {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostResource.class);
    private static final String INSECURE = "INSECURE";
    private static final String TRUST_FIRST_CERTIFICATE = "TRUST_FIRST_CERTIFICATE";
    private final HostRepository repository;

    public HostResource() {
        repository = new HostRepository();
    }

    protected HostCollection createEmptyCollection() {
        return new HostCollection();
    }

    protected HostRepository getRepository() {
        return repository;
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @RequiresPermissions("hosts:create")
    public Host createHost(HostCreateCriteria hostCreateCriteria) throws Exception {
        ValidationUtil.validate(hostCreateCriteria);
        UUID hostId = new UUID();
        UUID hardwareUuid = null;
        HostInfo hostInfo = null;
        TlsPolicyDescriptor tlsPolicyDescriptor;

        try {
            if (hostCreateCriteria == null
                    || (hostCreateCriteria.getConnectionString() == null || hostCreateCriteria.getConnectionString().trim().isEmpty())
                    || (hostCreateCriteria.getHostName() == null || hostCreateCriteria.getHostName().trim().isEmpty())) {
                throw new WebApplicationException("Host connection string and host name must be specified", 400);
            }
            HostLocator locator = new HostLocator();
            locator.name = hostCreateCriteria.getHostName();
            Host existingHost = repository.retrieve(locator);
            if (existingHost != null) {
                throw new WebApplicationException("Host with this name already exists", Response.Status.BAD_REQUEST);
            }
            // set the tls policy id to default if it is not specified
            if (hostCreateCriteria.getTlsPolicyId() == null || hostCreateCriteria.getTlsPolicyId().isEmpty()) {
                hostCreateCriteria.setTlsPolicyId(HostTlsPolicyFilter.getDefaultTlsPolicyType());
            }

            ConnectionString connectionString = HostRepository.generateConnectionString(hostCreateCriteria.getConnectionString());

            // determine TLS policy
            tlsPolicyDescriptor = getTlsPolicy(hostCreateCriteria.getTlsPolicyId(), connectionString, false);
            // get the host manifest, if host is connected
            try {
                log.debug("Connecting to host to get the host manifest and the hardware UUID of the host : {}",
                        hostCreateCriteria.getHostName());
                // connect to the host and retrieve the host manifest
                hostInfo = getHostInfo(tlsPolicyDescriptor, connectionString, null);
                if (hostInfo != null
                        && hostInfo.getHardwareUuid() != null
                        && !hostInfo.getHardwareUuid().isEmpty()
                        && UUID.isValid(hostInfo.getHardwareUuid())) {
                    hardwareUuid = UUID.valueOf(hostInfo.getHardwareUuid());
                }
            } catch (TlsPolicyAllowedException e) {
                throw new WebApplicationException("TLS policy type is not allowed", e, 400);
            } catch (Exception e) {
                HostState hostState = new HostStatusResource().determineHostState(e);
                log.warn("Could not connect to host, hardware UUID and host manifest will not be set: {}", hostState.getHostStateText());
            }

            List<String> flavorgroupNames = new ArrayList<>();
            List<UUID> flavorgroupIds = new ArrayList<>();

            if ((hostCreateCriteria.getFlavorgroupName() == null || hostCreateCriteria.getFlavorgroupName().isEmpty())) {
                flavorgroupNames.add(Flavorgroup.AUTOMATIC_FLAVORGROUP);
            } else {
                if(hostCreateCriteria.getFlavorgroupName() != null && !hostCreateCriteria.getFlavorgroupName().isEmpty()) {
                    flavorgroupNames.add(hostCreateCriteria.getFlavorgroupName());
                }
            }

            // Link to default software group if host is linux
            if (hostInfo != null && validateIseclSoftwareFlavor(hostInfo.getOsName()))
                flavorgroupNames.add(Flavorgroup.DEFAULT_SOFTWARE_FLAVORGROUP);

            FlavorgroupLocator flavorgroupLocator = new FlavorgroupLocator();
            for (String flavorgroupName : flavorgroupNames) {
                flavorgroupLocator.name = flavorgroupName;
                Flavorgroup flavorgroup = new FlavorgroupRepository().retrieve(flavorgroupLocator);

                if (flavorgroup != null) {
                    flavorgroupIds.add(flavorgroup.getId());
                } else {
                    flavorgroup = createNewFlavorGroup(flavorgroupName);
                    flavorgroupIds.add(flavorgroup.getId());
                }
            }

            // set all host parameters and create the host
            log.debug("Setting all the host obj parameters");
            Host host = new Host();
            host.setConnectionString(hostCreateCriteria.getConnectionString());
            host.setDescription(hostCreateCriteria.getDescription());
            host.setFlavorgroupNames(flavorgroupNames);
            host.setHostName(hostCreateCriteria.getHostName());
            host.setTlsPolicyId(hostCreateCriteria.getTlsPolicyId());
            host.setHardwareUuid(hardwareUuid);
            host.setId(hostId);
            log.debug("Creating a new host");
            host = repository.create(host);

            // for user specified TRUST_FIRST_CERTIFICATE or INSECURE, create the TLS policy
            if (tlsPolicyDescriptor.getPolicyType().equalsIgnoreCase(TRUST_FIRST_CERTIFICATE)
                    || tlsPolicyDescriptor.getPolicyType().equalsIgnoreCase(INSECURE)) {
                createTlsPolicy(tlsPolicyDescriptor.getPolicyType(), hostId);
            }

            for (UUID flavorgroupId : flavorgroupIds) {
                log.debug("Linking host {} with flavorgroup {}", host.getHostName(), flavorgroupId);
                linkFlavorGroupToHost(flavorgroupId, hostId);
            }

            log.debug("Adding host to flavor-verify queue");
            // Since we are adding a new host, the forceUpdate flag should be set to true so that
            // we connect to the host and get the latest host manifest to verify against.
            addHostToFlavorVerifyQueue(host.getId(), true);
            host.setConnectionString(HostRepository.getConnectionStringWithoutCredentials(connectionString.getConnectionString()));
            return host;

        } catch (RepositoryInvalidInputException e) {
            throw new WebApplicationException(String.format("Invalid input: %s", e.getMessage()), e, 400);
        } catch (MalformedURLException e) {
            throw new WebApplicationException("Connection string is incorrectly formatted", e, 400);
        }
    }

    @POST
    @Path("/{hostId}/flavorgroups")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    public FlavorgroupHostLink createFlavorgroupHostAssociation(@PathParam("hostId") String hostId, FlavorgroupHostLinkCreateCriteria flavorgroupHostCreateCriteria) {
        ValidationUtil.validate(hostId);
        ValidationUtil.validate(flavorgroupHostCreateCriteria);
    
        HostRepository hostRepository = new HostRepository();
        HostLocator locator = new HostLocator();
        locator.id = UUID.valueOf(hostId);
        Host host = hostRepository.retrieve(locator);
        if (host == null) {
            log.error("The host ID {} does not exist");
            throw new RepositoryInvalidInputException(locator);
        }

        String flavorgroupName;
        UUID flavorgroupId;

        if (flavorgroupHostCreateCriteria.getFlavorgroupName() == null || flavorgroupHostCreateCriteria.getFlavorgroupName().isEmpty()) {
            log.error("The flavorgroup name is empty");
            throw new RepositoryInvalidInputException(locator); 
        } else {
            flavorgroupName = flavorgroupHostCreateCriteria.getFlavorgroupName();
        }

        FlavorgroupLocator flavorgroupLocator = new FlavorgroupLocator();
        flavorgroupLocator.name = flavorgroupName;
        Flavorgroup flavorgroup = new FlavorgroupRepository().retrieve(flavorgroupLocator);

        if (flavorgroup != null) {
            flavorgroupId = flavorgroup.getId();
        } else {
            log.error("The flavorgroup with name {} does not exist", flavorgroupName);
            throw new RepositoryInvalidInputException(locator);
        }

        log.debug("Linking host {} with flavorgroup {}", host.getHostName(), flavorgroupId);
        FlavorgroupHostLinkRepository flavorgroupHostLinkRepository = new FlavorgroupHostLinkRepository();
        FlavorgroupHostLink flavorgroupHostLink = new FlavorgroupHostLink();
        flavorgroupHostLink.setFlavorgroupId(flavorgroupId);
        flavorgroupHostLink.setHostId(UUID.valueOf(hostId));
        flavorgroupHostLink.setId(new UUID());
        flavorgroupHostLinkRepository.create(flavorgroupHostLink);

        log.debug("Adding host to flavor-verify queue");
        addHostToFlavorVerifyQueue(host.getId(), false);

        return flavorgroupHostLink;
    }

    @DELETE
    @Path("/{hostId}/flavorgroups/{flavorgroupId}")
    public void deleteFlavorgroupHostAssociation(@PathParam("hostId") UUID hostId, @PathParam("flavorgroupId") UUID flavorgroupId) {

        ValidationUtil.validate(hostId);
        FlavorgroupHostLinkLocator flavorgroupHostLocator = new FlavorgroupHostLinkLocator(flavorgroupId, hostId);
        ValidationUtil.validate(flavorgroupHostLocator);

        FlavorgroupHostLinkRepository flavorgroupHostLinkRepository = new FlavorgroupHostLinkRepository();
        //delete the flavorgroup-host link
        log.debug("HostFlavorgroupLink : delete - deleting the host flavorgroup link with flavorgroupid {} and hostId {}", flavorgroupHostLocator.flavorgroupId, flavorgroupHostLocator.hostId);
        flavorgroupHostLinkRepository.delete(flavorgroupHostLocator);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @RequiresPermissions("hosts:search")
    public HostCollection searchHost(@BeanParam HostFilterCriteria hostFilterCriteria, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
        ValidationUtil.validate(hostFilterCriteria);
        log.debug("target: {} - {}", httpServletRequest.getRequestURI(), httpServletRequest.getQueryString());
        return repository.search(hostFilterCriteria);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Path("/{id}")
    @RequiresPermissions("hosts:retrieve")
    public Host retrieveHost(@BeanParam HostLocator locator, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
        ValidationUtil.validate(locator);
        return repository.retrieve(locator);
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Path("/{id}")
    @RequiresPermissions("hosts:store")
    public Host updateHost(@BeanParam HostLocator locator, Host item) throws Exception {
        ValidationUtil.validate(item);
        ValidationUtil.validate(locator);

        locator.copyTo(item);

        item = getRepository().store(item);

        if (item != null && item.getFlavorgroupName() != null && !item.getFlavorgroupName().isEmpty()) {
            createFlavorgroupHostLink(item, item.getFlavorgroupName());
        } else if (item != null && item.getFlavorgroupNames() != null && !item.getFlavorgroupNames().isEmpty()) {
            for (String flavorGroupName : item.getFlavorgroupNames()) {
                createFlavorgroupHostLink(item, flavorGroupName);
            }
        }

        if (item != null) {
            // Since the host has been updated, add it to the verify queue
            addHostToFlavorVerifyQueue(item.getId(), true);
            item.setConnectionString(HostRepository.getConnectionStringWithoutCredentials(item.getConnectionString()));
        }

        return item;
    }

    private void createFlavorgroupHostLink(Host item, String flavorGroupName) {
        // if flavorgroup is specified, retrieve it
        FlavorgroupLocator flavorgroupLocator = new FlavorgroupLocator();
        flavorgroupLocator.name = flavorGroupName;
        Flavorgroup flavorgroup = new FlavorgroupRepository().retrieve(flavorgroupLocator);

        // if the flavorgroup doesn't exist, create it with a host based policy
        if (flavorgroup == null) {
            flavorgroup = FlavorGroupUtils.createFlavorGroupByName(flavorGroupName);
        }
        // create flavorgroup host link
        FlavorgroupHostLinkRepository flavorgroupHostLinkRepository = new FlavorgroupHostLinkRepository();
        FlavorgroupHostLink flavorgroupHostLink = new FlavorgroupHostLink();
        flavorgroupHostLink.setFlavorgroupId(flavorgroup.getId());
        flavorgroupHostLink.setHostId(item.getId());
        flavorgroupHostLinkRepository.create(flavorgroupHostLink);
    }

    @DELETE
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Path("/{hostId}")
    @RequiresPermissions("hosts:delete")
    public void deleteHost(@PathParam("hostId") String hostId) {
        ValidationUtil.validate(hostId);

        // retrieve host
        HostLocator locator = new HostLocator();
        locator.id = UUID.valueOf(hostId);
        Host host = repository.retrieve(locator);

        if (host == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        // delete private ONLY TLS policy associated with host
        HostTlsPolicyLocator hostTlsPolicyLocator = new HostTlsPolicyLocator();
        hostTlsPolicyLocator.id = UUID.valueOf(host.getTlsPolicyId());
        HostTlsPolicy hostTlsPolicy = new HostTlsPolicyRepository().retrieve(hostTlsPolicyLocator);
        if (hostTlsPolicy != null && hostTlsPolicy.isPrivate()) {
            new HostTlsPolicyRepository().delete(hostTlsPolicyLocator);
        }

        // delete host reports for the host
        ReportLocator reportLocator = new ReportLocator();
        reportLocator.hostId = host.getId();
        new ReportRepository().delete(reportLocator);

        // delete host status record for the host
        HostStatusLocator hostStatusLocator = new HostStatusLocator();
        hostStatusLocator.hostId = host.getId();
        new HostStatusRepository().delete(hostStatusLocator);

        // Delete all the links between the flavor and the hosts
        FlavorHostLinkFilterCriteria flavorHostLinkCriteria = new FlavorHostLinkFilterCriteria();
        flavorHostLinkCriteria.hostId = UUID.valueOf(hostId);
        FlavorHostLinkRepository flavorHostLinkRepository = new FlavorHostLinkRepository();
        flavorHostLinkRepository.delete(flavorHostLinkCriteria);

        // Delete all the links between the flavor group and the hosts
        FlavorgroupHostLinkFilterCriteria flavorgroupHostLinkCriteria = new FlavorgroupHostLinkFilterCriteria();
        flavorgroupHostLinkCriteria.hostId = UUID.valueOf(hostId);
        FlavorgroupHostLinkRepository flavorgroupHostLinkRepository = new FlavorgroupHostLinkRepository();
        flavorgroupHostLinkRepository.delete(flavorgroupHostLinkCriteria);

        repository.delete(hostId);

    }

//    private Host convert(HostFilterCriteria item) {
//        Host host = new Host();
//        if (item.id != null) {
//            host.setId(item.id);
//        }
//        if (item.nameEqualTo != null) {
//            host.setHostName(item.nameEqualTo);
//        }
//        if (item.hostHardwareId != null) {
//            host.setHardwareUuid(item.hostHardwareId);
//        }
//        if (item.tlsPolicyId != null) {
//            host.setTlsPolicyId(item.tlsPolicyId);
//        }
//        if (item.connectionString != null) {
//            host.setConnectionString(item.connectionString);
//        }
//        if (item.description != null) {
//            host.setDescription(item.description);
//        }
//        return host;
//    }

    public void addHostToFlavorVerifyQueue(UUID hostId, boolean forceUpdate) {
        //check if the host exists in the queue
        if (checkHostAlreadyInQueue(hostId.toString(), forceUpdate)) {
            return;
        }
        // set host status to queue
        updateHostStatus(hostId, QUEUE, null);

        // add host to queue with force update parameter
        Map<String, String> actionParameters = new HashMap<>();
        actionParameters.put("host_id", hostId.toString());
        actionParameters.put("force_update", String.valueOf(forceUpdate));
        Queue queue = new Queue();
        queue.setActionParameters(actionParameters);
        queue.setQueueAction("flavor-verify");
        new QueueRepository().create(queue);
    }

    //returns true if host already in queue else returns false
    public boolean checkHostAlreadyInQueue(String hostId, boolean forceUpdate) {
        // get flavor-verify queue entries for host ID
        QueueFilterCriteria queueFilterCriteria = new QueueFilterCriteria();
        queueFilterCriteria.action = "flavor-verify";
        queueFilterCriteria.parameter = "host_id";
        queueFilterCriteria.value = hostId;
        QueueCollection queueCollection = new QueueRepository().search(queueFilterCriteria);

        // two condition to skip adding to queue
        if (queueCollection != null && queueCollection.getQueueEntries() != null
                && !queueCollection.getQueueEntries().isEmpty()) {
            // 1: host is in queue, and force update is set to FALSE
            if (!forceUpdate) {
                return true;
            }

            // 2: host is in queue, and at least one existing queue entry force update parameter is TRUE
            for (Queue queue : queueCollection.getQueueEntries()) {
                if (queue.getActionParameters() != null && !queue.getActionParameters().isEmpty()) {
                    String queueEntryForceUpdate = queue.getActionParameters().get("force_update");
                    if (queueEntryForceUpdate != null && !queueEntryForceUpdate.isEmpty()
                            && Boolean.valueOf(queueEntryForceUpdate)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public void addHostsToFlavorVerifyQueue(List<String> hostIds, boolean forceUpdate) {
        try {
            MwQueueJpaController queueJpa = My.jpa().mwQueue();
            List<MwQueue> mwQueueList = new ArrayList();
            for (String hostId : hostIds) {
                Map<String, String> actionParameters = new HashMap<>();
                actionParameters.put("host_id", hostId);
                actionParameters.put("force_update", String.valueOf(forceUpdate));
                Queue queue = new Queue();
                queue.setId(new UUID());
                queue.setActionParameters(actionParameters);
                queue.setQueueAction("flavor-verify");
                queue.setStatus(NEW);
                MwQueue mwQueue = new QueueRepository().convertToMwQueue(queue);
                mwQueueList.add(mwQueue);
            }
            queueJpa.createQueueList(mwQueueList);
        } catch (Exception ex) {
            log.error("Error adding host to flavor verify queue", ex);
        }
    }

    public void updateHostStatus(UUID hostId, HostState hostState, HostManifest hostManifest) {
        // retrieve current host status for host ID
        HostStatusLocator hostStatusLocator = new HostStatusLocator();
        hostStatusLocator.hostId = hostId;

        // retrieve and set details from previous host status, if available from database
        HostStatusInformation hostStatusInformation = new HostStatusInformation();
        hostStatusInformation.setHostState(hostState);

        // create the new host status record
        HostStatus hostStatus = new HostStatus();
        hostStatus.setCreated(Calendar.getInstance().getTime());
        hostStatus.setHostId(hostId);
        hostStatus.setStatus(hostStatusInformation);
        hostStatus.setHostManifest(hostManifest);
        new HostStatusRepository().store(hostStatus);
    }

    public void updateHostStatusList(List<String> hostIds, HostState hostState, HostManifest hostManifest) {
        try {
            MwHostStatusJpaController hostStatusJpa = My.jpa().mwHostStatus();
            List<MwHostStatus> mwHostStatusList = new ArrayList();
            for (String hostId : hostIds) {
                // retrieve current host status for host ID
                HostStatusLocator hostStatusLocator = new HostStatusLocator();
                hostStatusLocator.hostId = UUID.valueOf(hostId);
                // retrieve and set details from host status
                HostStatusInformation hostStatusInformation = new HostStatusInformation();
                hostStatusInformation.setHostState(hostState);

                // create the new host status record
                HostStatus hostStatus = new HostStatus();
                hostStatus.setHostId(UUID.valueOf(hostId));
                hostStatus.setStatus(hostStatusInformation);
                hostStatus.setHostManifest(hostManifest);
                MwHostStatus mwHostStatus = new HostStatusRepository().convertToMwHostStatus(hostStatus);
                mwHostStatusList.add(mwHostStatus);
            }
            hostStatusJpa.editHostStatusList(mwHostStatusList);
        } catch (Exception ex) {
            log.error("Error updating host status during adding host to flavor verify queue", ex);
        }
    }

    private TlsPolicyDescriptor createTlsPolicy(String tlsPolicyIdentifier, UUID hostId) {
        if (tlsPolicyIdentifier == null || tlsPolicyIdentifier.isEmpty()) {
            tlsPolicyIdentifier = HostTlsPolicyFilter.getDefaultTlsPolicyType();
        }

        //HostTlsPolicyFilter.isTlsPolicyAllowed(tlsPolicyIdentifier);
        // build TLS policy descriptor for TLS policy creation
        TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
        switch (tlsPolicyIdentifier) {
            case TRUST_FIRST_CERTIFICATE:
                tlsPolicyDescriptor.setPolicyType(TRUST_FIRST_CERTIFICATE);
                break;
            case INSECURE:
                tlsPolicyDescriptor.setPolicyType(INSECURE);
                tlsPolicyDescriptor.setProtection(new TlsProtection());
                tlsPolicyDescriptor.getProtection().encryption = false;
                tlsPolicyDescriptor.getProtection().integrity = false;
                tlsPolicyDescriptor.getProtection().authentication = false;
                break;
            default:
                log.error("TLS policy specified [{}] not supported", tlsPolicyIdentifier);
                throw new RepositoryInvalidInputException(String.format(
                        "TLS policy specified [%s] not supported", tlsPolicyIdentifier));
        }

        if (hostId != null) {
            // store the TLS policy
            UUID tlsPolicyId = new UUID();
            HostTlsPolicy hostTlsPolicy = new HostTlsPolicy();
            hostTlsPolicy.setId(tlsPolicyId);
            hostTlsPolicy.setName(hostId.toString());
            hostTlsPolicy.setPrivate(true);
            hostTlsPolicy.setComment("Insecure TLS Policy");
            hostTlsPolicy.setDescriptor(tlsPolicyDescriptor);
            new HostTlsPolicyRepository().create(hostTlsPolicy);

            // update the host record with the TLS policy ID
            Host host = new Host();
            host.setId(hostId);
            host.setTlsPolicyId(tlsPolicyId.toString());
            new HostRepository().store(host);
        }
        return tlsPolicyDescriptor;
    }

    public TlsPolicyDescriptor getTlsPolicy(String tlsPolicyIdentifier, ConnectionString connectionString, Boolean forceExisting) {
        // try to find host based on connection string
        HostLocator hostLocator = new HostLocator();
        hostLocator.name = connectionString.getManagementServerName();
        Host existingHost = new HostRepository().retrieve(hostLocator);

        // set host ID if host found
        UUID hostId = null;
        if (existingHost != null && existingHost.getId() != null) {
            hostId = existingHost.getId();
        }

        // set host TLS policy ID if host found
        String hostTlsPolicyId = null;
        if (existingHost != null && existingHost.getTlsPolicyId() != null) {
            hostTlsPolicyId = existingHost.getTlsPolicyId();
        }

        // if TLS policy identifier string isn't specified, set it equal to host TLS policy ID
        // if force existing flag set, use the existing TLS policy found in the database
        if ((tlsPolicyIdentifier == null || tlsPolicyIdentifier.isEmpty())
                || (forceExisting && hostTlsPolicyId != null && !hostTlsPolicyId.isEmpty())) {
            tlsPolicyIdentifier = hostTlsPolicyId;
        }

        if (tlsPolicyIdentifier == null || tlsPolicyIdentifier.isEmpty()) {
            tlsPolicyIdentifier = HostTlsPolicyFilter.getDefaultTlsPolicyType();
        }

        if (UUID.isValid(tlsPolicyIdentifier)) {
            // retrieve the TLS policy from the database
            HostTlsPolicyLocator hostTlsPolicyLocator = new HostTlsPolicyLocator();
            hostTlsPolicyLocator.id = UUID.valueOf(tlsPolicyIdentifier);
            HostTlsPolicy hostTlsPolicy = new HostTlsPolicyRepository().retrieve(hostTlsPolicyLocator);

            if (hostTlsPolicy == null) {
                throw new IllegalArgumentException("TLS policy specified does not exist");
            } else {
                return hostTlsPolicy.getDescriptor();
            }
        } else {
            return createTlsPolicy(tlsPolicyIdentifier, hostId);
        }
    }

    public HostManifest getHostManifest(Host host, ConnectionString connectionString) throws IOException {
        // set host TLS policy ID if host found
        UUID tlsPolicyId = null;
        if (host != null && host.getTlsPolicyId() != null) {
            if (!UUID.isValid(host.getTlsPolicyId())) {
                // Since the host table sometimes stores the TLS Policy Identifier (TRUST_FIRST_CERTIFICATE etc) in case
                // of connection issues with the host, we need to retrive the actual ID from the TLS Policy table.
                HostTlsPolicyLocator hostTlsPolicyLocator = new HostTlsPolicyLocator();
                hostTlsPolicyLocator.name = host.getId().toString();
                HostTlsPolicy hostTlsPolicy = new HostTlsPolicyRepository().retrieve(hostTlsPolicyLocator);
                if (hostTlsPolicy != null) {
                    log.debug("FlavorVerify: Retrieved the TLS policy for the host {}", hostTlsPolicy.getId().toHexString());
                    tlsPolicyId = hostTlsPolicy.getId();
                }
            } else {
                tlsPolicyId = UUID.valueOf(host.getTlsPolicyId());
            }
        }
        return getHostManifest(connectionString, tlsPolicyId);
    }

    public HostManifest getHostManifest(ConnectionString connectionString, UUID tlsPolicyId) throws IOException {
        return getHostManifest(null, connectionString, tlsPolicyId);
    }

    public HostManifest getHostManifest(TlsPolicyDescriptor tlsPolicyDescriptor, ConnectionString connectionString, UUID tlsPolicyId) throws IOException {
        HostTlsPolicy hostTlsPolicy = getHostTlsPolicy(tlsPolicyId);
        tlsPolicyDescriptor = getTlsPolicyDescriptor(tlsPolicyDescriptor, hostTlsPolicy);
        // check if the tlsPolicyDescriptor is allowed. Throw error if not allowed.
        validateTlsPolicyDescriptor(tlsPolicyDescriptor);

        // get the host manifest
        TlsPolicy tlsPolicy = TlsPolicyFactoryUtil.createTlsPolicy(tlsPolicyDescriptor);
        HostConnector hostConnector = new HostConnectorFactory().getHostConnector(connectionString, tlsPolicy);
        HostManifest hostManifest = hostConnector.getHostManifest();

        storeTlsPolicyDescriptor(hostTlsPolicy, tlsPolicy);
        return hostManifest;
    }

    private void storeTlsPolicyDescriptor(HostTlsPolicy hostTlsPolicy, TlsPolicy tlsPolicy) {
        if (hostTlsPolicy != null && hostTlsPolicy.getDescriptor() != null
                && hostTlsPolicy.getDescriptor().getPolicyType() != null
                && hostTlsPolicy.getDescriptor().getPolicyType().equalsIgnoreCase(TRUST_FIRST_CERTIFICATE)) {
            // cast the TLS policy to a public key TLS policy
            // and update the TLS policy database record
            PublicKeyTlsPolicy publicKeyTlsPolicy = (PublicKeyTlsPolicy) tlsPolicy;
            HostTlsPolicy updatedHostTlsPolicy = new HostTlsPolicy();
            updatedHostTlsPolicy.setId(hostTlsPolicy.getId());
            updatedHostTlsPolicy.setComment("Public Key TLS Policy");
            updatedHostTlsPolicy.setDescriptor(convert(publicKeyTlsPolicy.getRepository()));
            new HostTlsPolicyRepository().store(updatedHostTlsPolicy);
        }
    }

    private TlsPolicyDescriptor getTlsPolicyDescriptor(TlsPolicyDescriptor tlsPolicyDescriptor, HostTlsPolicy hostTlsPolicy) {
        if (tlsPolicyDescriptor == null) {
            if (hostTlsPolicy == null || hostTlsPolicy.getDescriptor() == null) {
                throw new IllegalArgumentException("Cannot determine appropriate TLS policy for host");
            }
            tlsPolicyDescriptor = hostTlsPolicy.getDescriptor();
        }
        return tlsPolicyDescriptor;
    }

    private HostInfo getHostInfo(TlsPolicyDescriptor tlsPolicyDescriptor, ConnectionString connectionString, UUID tlsPolicyId) throws IOException {
        HostTlsPolicy hostTlsPolicy = getHostTlsPolicy(tlsPolicyId);
        tlsPolicyDescriptor = getTlsPolicyDescriptor(tlsPolicyDescriptor, hostTlsPolicy);

        // check if the tlsPolicyDescriptor is allowed. Throw error if not allowed.
        validateTlsPolicyDescriptor(tlsPolicyDescriptor);

        // get the host info
        TlsPolicy tlsPolicy = TlsPolicyFactoryUtil.createTlsPolicy(tlsPolicyDescriptor);
        HostConnector hostConnector = new HostConnectorFactory().getHostConnector(connectionString, tlsPolicy);
        HostInfo hostInfo = hostConnector.getHostDetails();

        storeTlsPolicyDescriptor(hostTlsPolicy, tlsPolicy);
        return hostInfo;
    }

    private void validateTlsPolicyDescriptor(TlsPolicyDescriptor tlsPolicyDescriptor) {
        if (!HostTlsPolicyFilter.isTlsPolicyAllowed(tlsPolicyDescriptor.getPolicyType())) {
            log.error("TLS policy {} is not allowed", tlsPolicyDescriptor.getPolicyType());
            throw new TlsPolicyAllowedException("TLS policy is not allowed");
        }
    }

    private HostTlsPolicy getHostTlsPolicy(UUID tlsPolicyId) {
        HostTlsPolicy hostTlsPolicy = null;
        if (tlsPolicyId != null) {
            // get the TLS policy record from the database and assign the descriptor
            HostTlsPolicyLocator hostTlsPolicyLocator = new HostTlsPolicyLocator();
            hostTlsPolicyLocator.id = tlsPolicyId;
            hostTlsPolicy = new HostTlsPolicyRepository().retrieve(hostTlsPolicyLocator);
        }
        return hostTlsPolicy;
    }

    private boolean validateIseclSoftwareFlavor(String osName) {
        String formattedOsName = osName.trim().toUpperCase();
        return (formattedOsName.equals("RHEL") || formattedOsName.equals("REDHATENTERPRISESERVER"));
    }

    private Flavorgroup createNewFlavorGroup(String flavorgroupName) {
        return FlavorGroupUtils.createFlavorGroupByName(flavorgroupName);
    }

    private void linkFlavorGroupToHost(UUID flavorgroupId, UUID hostId) {
        FlavorgroupHostLinkRepository flavorgroupHostLinkRepository = new FlavorgroupHostLinkRepository();
        FlavorgroupHostLink flavorgroupHostLink = new FlavorgroupHostLink();
        flavorgroupHostLink.setFlavorgroupId(flavorgroupId);
        flavorgroupHostLink.setHostId(hostId);
        flavorgroupHostLinkRepository.create(flavorgroupHostLink);
    }

    private TlsPolicyDescriptor convert(PublicKeyRepository repository) {
        TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
        tlsPolicyDescriptor.setPolicyType("public-key");
        tlsPolicyDescriptor.setProtection(TlsPolicyFactoryUtil.getAllTlsProtection());
        tlsPolicyDescriptor.setMeta(new HashMap<String, String>());
        tlsPolicyDescriptor.getMeta().put("encoding", "base64");
        tlsPolicyDescriptor.setData(new HashSet<String>());
        for (PublicKey publicKey : repository.getPublicKeys()) {
            String publicKeyString = Base64.encodeBase64String(publicKey.getEncoded());
            tlsPolicyDescriptor.getData().add(publicKeyString);
            log.debug("Added public key to policy: {}", publicKeyString);
        }
        return tlsPolicyDescriptor;
    }

}
