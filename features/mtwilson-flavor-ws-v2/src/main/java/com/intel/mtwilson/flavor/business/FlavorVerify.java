/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.business;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.core.verifier.Verifier;
import com.intel.mtwilson.core.verifier.policy.Fault;
import com.intel.mtwilson.core.verifier.policy.RuleResult;
import com.intel.mtwilson.core.verifier.policy.TrustMarker;
import com.intel.mtwilson.core.verifier.policy.TrustReport;
import com.intel.mtwilson.features.queue.QueueOperation;
import com.intel.mtwilson.flavor.business.policy.rule.RequiredFlavorTypeExists;
import com.intel.mtwilson.flavor.business.policy.rule.RuleAllOfFlavors;
import com.intel.mtwilson.flavor.data.MwHostCredential;
import com.intel.mtwilson.flavor.model.FlavorMatchPolicyCollection;
import com.intel.mtwilson.flavor.model.HostStatusInformation;
import com.intel.mtwilson.flavor.model.HostTrustCache;
import com.intel.mtwilson.flavor.model.HostTrustRequirements;
import static com.intel.mtwilson.flavor.model.MatchPolicy.MatchType.ALL_OF;
import static com.intel.mtwilson.flavor.model.MatchPolicy.Required.REQUIRED;
import static com.intel.mtwilson.flavor.model.MatchPolicy.Required.REQUIRED_IF_DEFINED;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorCollection;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorHostLink;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorHostLinkCollection;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorHostLinkFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorHostLinkLocator;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorLocator;
import com.intel.mtwilson.flavor.rest.v2.model.Flavorgroup;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupCollection;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.Host;
import com.intel.mtwilson.flavor.rest.v2.model.HostCollection;
import com.intel.mtwilson.flavor.rest.v2.model.HostFilterCriteria;
import com.intel.mtwilson.flavor.model.FlavorTrustReport;
import com.intel.mtwilson.flavor.model.FlavorTrustReportCollection;
import com.intel.mtwilson.flavor.model.MatchPolicy;
import com.intel.mtwilson.flavor.model.MatchPolicy.MatchType;
import com.intel.mtwilson.flavor.rest.v2.model.HostStatus;
import com.intel.mtwilson.flavor.rest.v2.model.HostStatusLocator;
import com.intel.mtwilson.flavor.rest.v2.model.Report;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorHostLinkRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorgroupRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.HostRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.HostStatusRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.ReportRepository;
import com.intel.mtwilson.flavor.rest.v2.resource.HostResource;
import com.intel.mtwilson.flavor.saml.IssuerConfigurationFactory;
import com.intel.mtwilson.i18n.HostState;
import static com.intel.mtwilson.i18n.HostState.CONNECTED;
import static com.intel.mtwilson.i18n.HostState.QUEUE;
import com.intel.mtwilson.core.common.datatypes.ConnectionString;
import com.intel.mtwilson.core.common.model.HostManifest;
import static com.intel.mtwilson.features.queue.model.QueueState.COMPLETED;
import static com.intel.mtwilson.features.queue.model.QueueState.TIMEOUT;
import static com.intel.mtwilson.features.queue.model.QueueState.ERROR;
import com.intel.mtwilson.flavor.rest.v2.resource.HostStatusResource;
import static com.intel.mtwilson.i18n.HostState.CONNECTION_TIMEOUT;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import com.intel.mtwilson.supplemental.saml.SAML;
import com.intel.mtwilson.supplemental.saml.MapFormatter;
import com.intel.mtwilson.supplemental.saml.SamlAssertion;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang.WordUtils;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.xml.io.MarshallingException;

/**
 *
 * @author rksavino
 * @author dtiwari
 */
public class FlavorVerify extends QueueOperation {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FlavorVerify.class);
    
    private UUID hostId;
    private boolean forceUpdate;
    
    public FlavorVerify() { }
    
    public FlavorVerify(UUID hostId) {
        this.hostId = hostId;
        this.forceUpdate = false;
    }
    
    public FlavorVerify(UUID hostId, boolean forceUpdate) {
        this.hostId = hostId;
        this.forceUpdate = forceUpdate;
    }
    
    @Override
    public Boolean call() {
        try {
            // verify host ID is specified as input
            if (this.hostId == null) {
                String hostIdString = this.getParameter("host_id");
                if (hostIdString == null || hostIdString.isEmpty()) {
                    this.setQueueState(ERROR);
                    throw new FlavorVerifyException("Host ID must be specified in parameters");
                }
                this.hostId = UUID.valueOf(hostIdString);
            }

            // verify force update flag is specified as input
            String forceUpdateString = this.getParameter("force_update");
            if (forceUpdateString != null && !forceUpdateString.isEmpty() && Boolean.valueOf(forceUpdateString)) {
                this.forceUpdate = true;
            }

            HostManifest hostManifest = retrieveHostManifest(hostId, forceUpdate);

            log.debug("FlavorVerify: Hostmanifest retrieval for host {} with forceUpdate flag set to {} is {}", hostId, forceUpdate, hostManifest == null ? "Failure" : "Success");

            if (hostManifest == null || hostManifest.getHostInfo() == null
                    || hostManifest.getHostInfo().getHardwareUuid() == null
                    || hostManifest.getHostInfo().getHardwareUuid().isEmpty()
                    || !UUID.isValid(hostManifest.getHostInfo().getHardwareUuid())) {
                log.warn("Error communicating with host, cannot retrieve host manifest");
                return false;
            }
            UUID hardwareUuid = UUID.valueOf(hostManifest.getHostInfo().getHardwareUuid());

            HostTrustRequirements trustRequirements = getTrustRequirementsForHost(hostId, hardwareUuid);
            HostTrustCache hostTrustCache = retrieveAndValidateFlavorsInTrustCache(hostId, hostManifest);
            createTrustReportFromTrustCache(hostManifest, trustRequirements, hostTrustCache);
            // update host_status so not in QUEUE state
            new HostResource().updateHostStatus(hostId, CONNECTED, hostManifest);
            this.setQueueState(COMPLETED);
            log.info("Flavor verification completed succesfully for host with ID {}",hostId.toString());
            return true;
        } catch (Exception e) {
            this.setQueueState(ERROR);
            log.error("Error while running flavor verification for host [{}]: {}", this.hostId, e.getMessage());
            log.debug("Error while running flavor verification for host [{}]", this.hostId, e);
            return false;
        }
    }
    
    private HostManifest retrieveHostManifest(UUID hostId, boolean forceUpdate) {
        try {
            // retrieve the host
            HostFilterCriteria hostFilterCriteria = new HostFilterCriteria();
            hostFilterCriteria.id = hostId;
            HostCollection hostList = new HostRepository().search(hostFilterCriteria);
            if (hostList == null || hostList.getHosts() == null || hostList.getHosts().isEmpty()) {
                log.trace("Host record for host [{}] does not exist in the database. Skipping retrieval of host manifest.", hostId.toString());
                return null;
            }
            Host host = hostList.getHosts().get(0);
            if (host == null) {
                log.trace("Cannot determine host record from database: {}", hostId.toString());
                return null;
            }
            
            // if force update is false, return the latest host status record from the database
            if (!forceUpdate) {
                HostStatusLocator hostStatusLocator = new HostStatusLocator();
                hostStatusLocator.hostId = hostId;
                HostStatus hostStatus = new HostStatusRepository().retrieve(hostStatusLocator);
                // If in case the hostManifest is NULL, even though force update was false, get 
                // connect to the host and get the latest manifest.
                if (hostStatus != null && hostStatus.getHostManifest() != null) {
                    return hostStatus.getHostManifest();
                }
            }
            
            // get the host manifest
            HostState hostState = QUEUE;
            HostManifest hostManifest = null;
            try {
                MwHostCredential credential = My.jpa().mwHostCredential().findByHostId(hostId.toString());
                hostManifest = new HostResource().getHostManifest(host,
                        new ConnectionString(String.format("%s;%s", host.getConnectionString(), credential.getCredential())));
            } catch (Exception e) {
                // detect the host state from the error response
                hostState = new HostStatusResource().determineHostState(e);
                if(hostState.equals(CONNECTION_TIMEOUT))
                    this.setQueueState(TIMEOUT);
                else
                    this.setQueueState(ERROR);
            }
            
            // update host record with hardware UUID
            if (hostManifest != null && hostManifest.getHostInfo() != null
                    && hostManifest.getHostInfo().getHardwareUuid() != null
                    && !hostManifest.getHostInfo().getHardwareUuid().isEmpty()) {
                host.setHardwareUuid(UUID.valueOf(hostManifest.getHostInfo().getHardwareUuid()));
                new HostRepository().store(host);
            }
            
            // build host status info model for database insertion
            HostStatusInformation hostStatusInfo = new HostStatusInformation();
            hostStatusInfo.setLastTimeConnected(Calendar.getInstance().getTime());
            hostStatusInfo.setHostState(hostState);

            // store host status
            HostStatus hostStatus = new HostStatus();
            hostStatus.setHostId(hostId);
            hostStatus.setStatus(hostStatusInfo);
            if (hostManifest != null) {
                hostStatus.setHostManifest(hostManifest);
            }
            new HostStatusRepository().store(hostStatus);
            return hostManifest;
        } catch (Exception ex) {
            log.error("Error while retrieving the host manifest for host: {}", hostId.toString());
            throw new FlavorVerifyException(String.format(
                    "Error while retrieving the host manifest for host [%s]", hostId.toString()), ex);
        }
    }
    
    private HostTrustRequirements getTrustRequirementsForHost(UUID hostId, UUID hardwareUuid) {
        HostTrustRequirements hostTrustRequirements = new HostTrustRequirements();
        try {
            // retrieve the flavorgroup
            FlavorgroupFilterCriteria flavorgroupFilterCriteria = new FlavorgroupFilterCriteria();
            flavorgroupFilterCriteria.hostId = hostId;
            FlavorgroupCollection flavorgroupCollection = new FlavorgroupRepository().search(flavorgroupFilterCriteria);
            Flavorgroup flavorgroup = flavorgroupCollection.getFlavorgroups().get(0);
            hostTrustRequirements.setFlavorgroupId(flavorgroup.getId());
            
            // check for ALL_OF flavors for the hosts flavorgroup
            FlavorMatchPolicyCollection flavorMatchPolicy = flavorgroup.getFlavorMatchPolicyCollection();
            hostTrustRequirements.setFlavorMatchPolicy(flavorMatchPolicy);
            if (!flavorMatchPolicy.getFlavorPartsByMatchType(ALL_OF).isEmpty()) {
                FlavorFilterCriteria flavorFilterCriteria = new FlavorFilterCriteria();
                flavorFilterCriteria.flavorgroupId = flavorgroup.getId();
                flavorFilterCriteria.flavorParts = flavorMatchPolicy.getFlavorPartsByMatchType(ALL_OF);
                FlavorCollection allOfFlavors = new FlavorRepository().search(flavorFilterCriteria);
                hostTrustRequirements.setAllOfFlavors(allOfFlavors);
            }
            
            // get REQUIRED and REQUIRED_IF_DEFINED flavor parts
            List<FlavorPart> reqFlavorParts = flavorMatchPolicy.getFlavorPartsByRequired(REQUIRED);
            List<FlavorPart> ridFlavorParts = flavorMatchPolicy.getFlavorPartsByRequired(REQUIRED_IF_DEFINED);
            
            // determine if unique flavor parts exist for host
            List<FlavorPart> definedUniqueFlavorParts
                    = new FlavorRepository().getUniqueFlavorTypesThatExistForHost(hardwareUuid);
            
            if (definedUniqueFlavorParts != null) {
                for (Iterator<FlavorPart> uniqueFlavorPart = definedUniqueFlavorParts.iterator(); uniqueFlavorPart.hasNext();) {
                    FlavorPart flavorPart = uniqueFlavorPart.next();
                    if (!ridFlavorParts.contains(flavorPart) && !reqFlavorParts.contains(flavorPart)) {
                        uniqueFlavorPart.remove();
                    }
                }
            }
            // determine if required if defined flavor parts exist in flavorgroup
            List<FlavorPart> definedAutomaticFlavorParts
                    = new FlavorRepository().getFlavorTypesInFlavorgroup(flavorgroup.getId(), ridFlavorParts);
            
            // combine required and defined flavor parts
            List<FlavorPart> definedAndRequiredFlavorParts = new ArrayList<>();
            addAllIfNotNull(definedAndRequiredFlavorParts, reqFlavorParts);
            addAllIfNotNull(definedAndRequiredFlavorParts, definedAutomaticFlavorParts);
            addAllIfNotNull(definedAndRequiredFlavorParts, definedUniqueFlavorParts);
            
            // add defined and required flavor parts to return object
            hostTrustRequirements.setDefinedAndRequiredFlavorTypes(definedAndRequiredFlavorParts);
        } catch (Exception ex) {
            log.error("Error while retrieving the trust requirements for host [{}|{}]",
                    hostId.toString(), hardwareUuid.toString());
            throw new FlavorVerifyException(String.format(
                    "Error while retrieving the trust requirements for host [%s|%s]",
                    hostId.toString(), hardwareUuid.toString()), ex);
        }
        return hostTrustRequirements;
    }
    
    private <E> void addAllIfNotNull(List<E> list, Collection<? extends E> c) {
        if (c != null) {
            for (E e : c) {
                if (!list.contains(e)) // no duplicates
                    list.add(e);
            }
        }
    }
    
    private HostTrustCache retrieveAndValidateFlavorsInTrustCache(UUID hostId, HostManifest hostManifest) {
        HostTrustCache hostTrustCache = new HostTrustCache();
        hostTrustCache.setHostId(hostId);
        TrustReport collectiveTrustReport = null;
        try {
            // retrieve the trusted cached flavors for the host
            FlavorHostLinkFilterCriteria flavorHostLinkFilterCriteria = new FlavorHostLinkFilterCriteria();
            flavorHostLinkFilterCriteria.hostId = hostId;
            FlavorHostLinkCollection flavorHostLinkCollection = new FlavorHostLinkRepository().search(flavorHostLinkFilterCriteria);

            // return null if no trusted flavors were found
            if (flavorHostLinkCollection == null || flavorHostLinkCollection.getFlavorHostLinks() == null) {
                log.debug("No cached flavors exist for host: {}", hostId.toString());
                return null;
            }

            for (FlavorHostLink flavorHostLink : flavorHostLinkCollection.getFlavorHostLinks()) {
                // retrieve the trusted flavor
                FlavorLocator flavorLocator = new FlavorLocator();
                flavorLocator.id = flavorHostLink.getFlavorId();
                Flavor cachedFlavor = new FlavorRepository().retrieve(flavorLocator);

                // call verifier
                String privacyCaCert = My.configuration().getPrivacyCaIdentityCacertsFile().getAbsolutePath();
                String tagCaCert = My.configuration().getAssetTagCaCertificateFile().getAbsolutePath();
                Verifier verifier = new Verifier(privacyCaCert, tagCaCert);
                TrustReport individualTrustReport = verifier.verify(hostManifest, cachedFlavor);

                // if the flavor is trusted, add it to the collective trust report and to the return object
                // else, delete it from the trust cache
                if (individualTrustReport.isTrusted()) {
                    hostTrustCache.getTrustedFlavors();
                    hostTrustCache.getTrustedFlavors().getFlavors();
                    hostTrustCache.getTrustedFlavors().getFlavors().add(cachedFlavor);
                    if (collectiveTrustReport == null) {
                        collectiveTrustReport = individualTrustReport;
                    } else {
                        collectiveTrustReport = addRuleResults(collectiveTrustReport, individualTrustReport.getResults());
                    }
                } else {
                    FlavorHostLinkLocator flavorHostLinkLocator = new FlavorHostLinkLocator(flavorHostLink.getId());
                    new FlavorHostLinkRepository().delete(flavorHostLinkLocator);
                }
            }
            hostTrustCache.setTrustReport(collectiveTrustReport);
        } catch (Exception ex) {
            log.error("Error while retrieving and validating the flavors in the trust cache for host: {}",
                    hostId.toString());
            throw new FlavorVerifyException(String.format(
                    "Error while retrieving and validating the flavors in the trust cache for host: [%s]",
                    hostId.toString()), ex);
        }
        return hostTrustCache;
    }

    private TrustReport verify(UUID hostId, FlavorCollection flavors, HostManifest hostManifest, HostTrustRequirements hostTrustRequirements) {
        TrustReport collectiveTrustReport = null;

        // return null if no flavors were found
        if (flavors == null || flavors.getFlavors() == null || flavors.getFlavors().isEmpty()) {
            log.debug("No flavors found to verify for host with ID {}", hostId.toString());
            return new TrustReport(hostManifest, null);
        }

        // raise error if host manifest is null
        if (hostManifest == null) {
            log.error("Host manifest must be specified in order to verify");
            throw new FlavorVerifyException("Host manifest must be specified in order to verify");
        }

        try {
            FlavorTrustReportCollection untrustedReports = new FlavorTrustReportCollection();
            for (Flavor flavor : flavors.getFlavors()) {
                UUID flavorId = UUID.valueOf(flavor.getMeta().getId());
                log.debug("Found flavor with ID: {}", flavorId.toString());
                // call verifier
                String privacyCaCert = My.configuration().getPrivacyCaIdentityCacertsFile().getAbsolutePath();
                String tagCaCert = My.configuration().getAssetTagCaCertificateFile().getAbsolutePath();
                Verifier verifier = new Verifier(privacyCaCert, tagCaCert);
                TrustReport individualTrustReport = verifier.verify(hostManifest, flavor);

                // if the flavor is trusted, add it to the collective trust report
                // and store the flavor host link in the trust cache
                if (individualTrustReport.isTrusted()) {
                    log.debug("Flavor [{}] is trusted for host [{}]", flavorId.toString(), hostId.toString());
                    if (collectiveTrustReport == null) {
                        collectiveTrustReport = individualTrustReport;
                    } else {
                        collectiveTrustReport = addRuleResults(collectiveTrustReport, individualTrustReport.getResults());
                    }
                    
                    // create a new flavor host link (trust cache record), only if it doesn't already exist
                    createFlavorHostLink(flavorId, hostId);
                } else {
                    untrustedReports.getFlavorTrustReportList().add(new FlavorTrustReport(
                            FlavorPart.valueOf(flavor.getMeta().getDescription().getFlavorPart()),
                            flavorId,
                            individualTrustReport));
                    for (RuleResult result : individualTrustReport.getResults()) {
                        for (Fault fault : result.getFaults()) {
                            log.debug("Flavor [{}] did not match host [{}] due to fault: {}", flavorId.toString(), hostId.toString(), fault.toString());
                        }
                    }
                }
            }
            
            // associate untrusted flavors with host
            for (FlavorPart flavorPart : untrustedReports.getFlavorParts()) {
                log.debug("Processing untrusted trust report for flavor part: {}", flavorPart.name());
                // if the flavor part is defined and required, and the trust report is untrusted
                if ((hostTrustRequirements.getDefinedAndRequiredFlavorTypes().contains(flavorPart))
                    && (collectiveTrustReport == null || !collectiveTrustReport.isTrustedForMarker(flavorPart.name()))) {
                    log.debug("Flavor part [{}] is required, and collective trust report is untrusted for marker", flavorPart.name());
                    MatchPolicy matchPolicy = hostTrustRequirements.getFlavorMatchPolicy().getmatchPolicy(flavorPart);
                    // add each ALL_OF trust report to the collective
                    if (matchPolicy != null && matchPolicy.getMatchType() == MatchType.ALL_OF) {
                        log.debug("Flavor part [{}] requires ALL_OF policy, each untrusted flavor report must be added to the collective report", flavorPart.name());
                        for (FlavorTrustReport untrustedReport : untrustedReports.getFlavorTrustReports(flavorPart)) {
                            log.debug("Adding untrusted trust report to collective report for ALL_OF flavor part [{}] with flavor ID [{}]",
                                    untrustedReport.getFlavorPart(), untrustedReport.getFlavorId());
                            if (collectiveTrustReport == null)
                                collectiveTrustReport = untrustedReport.getTrustReport();
                            else
                                collectiveTrustReport = addRuleResults(collectiveTrustReport, untrustedReport.getTrustReport().getResults());
                            createFlavorHostLink(untrustedReport.getFlavorId(), hostId);
                        }
                    // add the ANY_OF trust report with least faults to the collective
                    } else if (matchPolicy != null && (matchPolicy.getMatchType() == MatchType.ANY_OF 
                            || matchPolicy.getMatchType() == MatchType.LATEST)) {
                        log.debug("Flavor part [{}] requires ANY_OF policy, untrusted flavor report with least faults must be added to the collective report", flavorPart.name());
                        FlavorTrustReport leastFaultsReport = null;
                        for (FlavorTrustReport untrustedReport : untrustedReports.getFlavorTrustReports(flavorPart)) {
                            if (untrustedReport != null && untrustedReport.getTrustReport() != null) {
                                if (leastFaultsReport == null || untrustedReport.getTrustReport().getFaultsCount()
                                        < leastFaultsReport.getTrustReport().getFaultsCount()) {
                                    leastFaultsReport = untrustedReport;
                                }
                            }
                        }
                        if (leastFaultsReport != null) {
                            log.debug("Adding untrusted trust report to collective report for ANY_OF flavor part [{}] with flavor ID [{}]",
                                    leastFaultsReport.getFlavorPart(), leastFaultsReport.getFlavorId());
                            if (collectiveTrustReport == null)
                                collectiveTrustReport = leastFaultsReport.getTrustReport();
                            else
                                collectiveTrustReport = addRuleResults(collectiveTrustReport, leastFaultsReport.getTrustReport().getResults());
                            createFlavorHostLink(leastFaultsReport.getFlavorId(), hostId);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error while verifying flavors");
            throw new FlavorVerifyException("Error while verifying flavors", ex);
        }
        if (collectiveTrustReport == null) {
            return new TrustReport(hostManifest, null);
        }
        return collectiveTrustReport;
    }
    
    private Boolean createFlavorHostLink(UUID flavorId, UUID hostId) {
        // create a new flavor host link (trust cache record), only if it doesn't already exist
        FlavorHostLinkLocator flavorHostLinkLocator = new FlavorHostLinkLocator(flavorId, hostId);
        FlavorHostLink existingFlavorHostLink = new FlavorHostLinkRepository().retrieve(flavorHostLinkLocator);
        if (existingFlavorHostLink == null) {
            FlavorHostLink flavorHostLink = new FlavorHostLink();
            flavorHostLink.setFlavorId(flavorId);
            flavorHostLink.setHostId(hostId);
            new FlavorHostLinkRepository().create(flavorHostLink);
            return true;
        }
        return false;
    }

    private void createTrustReport(HostTrustRequirements hostTrustRequirements, TrustReport trustReport, HostTrustCache trustCache) {
        List<FlavorPart> reqAndDefFlavorTypes = hostTrustRequirements.getDefinedAndRequiredFlavorTypes();
        FlavorCollection allOfFlavors = hostTrustRequirements.getAllOfFlavors();
        
        if (trustCache != null && trustCache.getTrustedFlavors() != null && trustCache.getTrustedFlavors().getFlavors() != null
                && !trustCache.getTrustedFlavors().getFlavors().isEmpty()) {
            for (RuleResult rule : trustCache.getTrustReport().getResults()) {
                trustReport.addResult(rule);
            }
        }
        
        // add required and defined flavor check rules to the trust report
        for (FlavorPart flavorPart : reqAndDefFlavorTypes) {
            RequiredFlavorTypeExists rule = new RequiredFlavorTypeExists(flavorPart);
            trustReport = rule.apply(trustReport);
        }
        
        // add all of flavors check rule to the trust report
        RuleAllOfFlavors ruleAllOfFlavors = new RuleAllOfFlavors(allOfFlavors,
                My.configuration().getPrivacyCaIdentityCacertsFile().getAbsolutePath(),
                My.configuration().getAssetTagCaCertificateFile().getAbsolutePath());
        trustReport = ruleAllOfFlavors.addFaults(trustReport);  // Add faults if every 'All of' flavors are not present
        
        createReport(hostId, trustReport);
    }

    private void createTrustReportFromTrustCache(HostManifest hostManifest, HostTrustRequirements hostTrustRequirements, HostTrustCache trustCache) {
       
        List<FlavorPart> reqAndDefFlavorTypes = hostTrustRequirements.getDefinedAndRequiredFlavorTypes();

        //create a hashMap with latest match policy 
        HashMap<String, Boolean> latestMap = new HashMap<>();

        if (!reqAndDefFlavorTypes.isEmpty()) {
            for (FlavorPart flavorPart : reqAndDefFlavorTypes) {
                MatchPolicy matchPolicy = hostTrustRequirements.getFlavorMatchPolicy().getmatchPolicy(flavorPart);
                if (matchPolicy != null && matchPolicy.getMatchType() == MatchType.LATEST) {
                    latestMap.put(flavorPart.name(), true);
                } else {
                    latestMap.put(flavorPart.name(), false);
                }
            }
        }
        FlavorCollection allOfFlavors = hostTrustRequirements.getAllOfFlavors();
        UUID flavorgroupId = hostTrustRequirements.getFlavorgroupId();
        
        // No results found in Trust Cache
        if (trustCache == null || trustCache.getTrustedFlavors() == null || trustCache.getTrustedFlavors().getFlavors() == null
                || trustCache.getTrustedFlavors().getFlavors().isEmpty()) {
            log.debug("No results found in trust cache for host: {}", hostId.toString());
            FlavorCollection flavorsToVerify = findFlavors(flavorgroupId, hostManifest, latestMap);
            TrustReport cachedTrustReport = verify(hostId, flavorsToVerify, hostManifest, hostTrustRequirements);
            createTrustReport(hostTrustRequirements, cachedTrustReport, trustCache);
            return;
        }
        
        TrustReport cachedTrustReport = trustCache.getTrustReport();
        // Missing Required and Defined Flavors
        HashMap<String, Boolean> missingRequiredFlavorPartsWithLatest = new HashMap<>();
        List<FlavorPart> missingRequiredFlavorParts = new ArrayList();
        for (FlavorPart requiredFlavorType : reqAndDefFlavorTypes) {
            log.debug("Checking if required flavor type [{}] for host [{}] is missing", requiredFlavorType.name(), hostId.toString());
            if (cachedTrustReport.getResultsForMarker(requiredFlavorType.name()) == null
                    || cachedTrustReport.getResultsForMarker(requiredFlavorType.name()).isEmpty()) {
                log.debug("Required flavor type [{}] for host [{}] is missing", requiredFlavorType.name(), hostId.toString());
                missingRequiredFlavorParts.add(requiredFlavorType);
                MatchPolicy matchPolicyMissing = hostTrustRequirements.getFlavorMatchPolicy().getmatchPolicy(requiredFlavorType);
                if (matchPolicyMissing != null && matchPolicyMissing.getMatchType() == MatchType.LATEST) {
                    missingRequiredFlavorPartsWithLatest.put(requiredFlavorType.name(), true);
                } else {
                    missingRequiredFlavorPartsWithLatest.put(requiredFlavorType.name(), false);
                }
            }
        }
        if (!missingRequiredFlavorParts.isEmpty()) {
            log.debug("Host [{}] has missing required and defined flavor parts: {}", hostId.toString(), missingRequiredFlavorParts);
            FlavorCollection flavorsToVerify = findFlavors(flavorgroupId, hostManifest, missingRequiredFlavorPartsWithLatest);
            cachedTrustReport = verify(hostId, flavorsToVerify, hostManifest, hostTrustRequirements);
            createTrustReport(hostTrustRequirements, cachedTrustReport, trustCache);
            return;
        }
        
        // All Of Flavors present
        RuleAllOfFlavors ruleAllOfFlavors = new RuleAllOfFlavors(allOfFlavors,
                My.configuration().getPrivacyCaIdentityCacertsFile().getAbsolutePath(),
                My.configuration().getAssetTagCaCertificateFile().getAbsolutePath());
        if (!ruleAllOfFlavors.allOfFlavorsEmpty()) {
            log.debug("All of flavors exist in policy for host: {}", hostId.toString());
            if (!ruleAllOfFlavors.checkAllOfFlavorsExist(cachedTrustReport)) {
                log.debug("Some all of flavors do not match what is in the trust cache for host: {}", hostId.toString());
                FlavorCollection flavorsToVerify = findFlavors(flavorgroupId, hostManifest, latestMap);
                cachedTrustReport = verify(hostId, flavorsToVerify, hostManifest, hostTrustRequirements);
                createTrustReport(hostTrustRequirements, cachedTrustReport, trustCache);
                return;
            }
        }
        log.debug("Trust cache valid for host: {}", hostId.toString());
        
        if (forceUpdate) {
            log.debug("Force update called, generating new SAML and saving new report for host: {}", hostId.toString());
            createReport(hostId, cachedTrustReport);
        }       
    }

    private FlavorCollection findFlavors(UUID flavorgroupId, HostManifest hostManifest, HashMap<String, Boolean> latestFlavorMap) {
        FlavorFilterCriteria flavorFilterCriteria = new FlavorFilterCriteria();
        flavorFilterCriteria.flavorgroupId = flavorgroupId;
        flavorFilterCriteria.hostManifest = hostManifest;        
        flavorFilterCriteria.flavorPartsWithLatest = latestFlavorMap;
        
        FlavorRepository flavorRepository = new FlavorRepository();
        FlavorCollection flavors = flavorRepository.search(flavorFilterCriteria);
        return flavors;
    }

    private String generateSamlReport(TrustReport trustReport) {
        SamlAssertion mapSamlAssertion;
        try {
            SAML saml = new SAML(new IssuerConfigurationFactory().loadIssuerConfiguration());
            BeanMap map = new BeanMap(trustReport.getHostManifest().getHostInfo());
            Map<String, String> samlMap = new LinkedHashMap();
            Iterator<String> it = map.keyIterator();
            while (it.hasNext()) {
                String key = it.next();
                if (map.get(key) != null && !key.equals("class")) {
                    String value = map.get(key).toString();
                    samlMap.put(key, value);
                }
            }
            for (TrustMarker marker : TrustMarker.values()) {
                String markerName = marker.name();
                if (!trustReport.getResultsForMarker(markerName).isEmpty()) {
                    samlMap.put("TRUST_" + WordUtils.capitalize(markerName), String.valueOf(trustReport.isTrustedForMarker(markerName)));
                } else {
                    samlMap.put("TRUST_" + WordUtils.capitalize(markerName), "NA");
                }
            }
            samlMap.put("TRUST_OVERALL", String.valueOf(trustReport.isTrusted()));
            for (Map.Entry<String, String> tag : trustReport.getTags().entrySet()) {
                samlMap.put("TAG_" + WordUtils.capitalize(tag.getKey()), WordUtils.capitalize(tag.getValue()));
            }
            MapFormatter mapAssertion = new MapFormatter(samlMap);
            mapSamlAssertion = saml.generateSamlAssertion(mapAssertion);
        } catch (InitializationException | MarshallingException | GeneralSecurityException | XMLSignatureException | MarshalException e) {
            throw new FlavorVerifyException("Failed to generate SAML report", e);
        }
        return mapSamlAssertion.assertion;
    }
    
    private void createReport(UUID hostId, TrustReport trustReport) {
        String samlReport = generateSamlReport(trustReport);
        Map<String, Date> dates = parseDatesFromSaml(samlReport);
        
        // Save Report in DB
        Report report = new Report();
        report.setHostId(hostId);
        report.setTrustInformation(new ReportRepository().buildTrustInformation(trustReport));
        report.setTrustReport(trustReport);
        report.setSaml(samlReport);
        report.setCreated(dates.get("created"));
        report.setExpiration(dates.get("expiration"));
        new ReportRepository().create(report);       
    }

    private TrustReport addRuleResults(TrustReport trustReport, List<RuleResult> ruleResults) {
        for (RuleResult ruleResult : ruleResults) {
            trustReport.addResult(ruleResult);
        }
        return trustReport;
    }
    
    private Map<String, Date> parseDatesFromSaml(String saml) {
        String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        Map<String, Date> dates = new HashMap();
        
        try (StringReader sr = new StringReader(saml)) {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(sr);
            while (reader.hasNext()) {
                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getLocalName().equalsIgnoreCase("SubjectConfirmationData")) {
                        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PATTERN);
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                        dates.put("created", sdf.parse(reader.getAttributeValue("", "NotBefore")));
                        dates.put("expiration", sdf.parse(reader.getAttributeValue("", "NotOnOrAfter")));
                    }
                }
                reader.next();
            }
        } catch (Exception ex) {
            log.error("Error while parsing dates from SAML XML string for host: {}", hostId.toString());
            throw new FlavorVerifyException(String.format(
                    "Error while parsing dates from SAML XML string for host: [%s]", hostId.toString()), ex);
        }
        return dates;
    }
}
