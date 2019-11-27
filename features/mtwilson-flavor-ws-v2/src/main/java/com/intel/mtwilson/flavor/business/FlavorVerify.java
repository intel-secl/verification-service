/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.business;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.core.common.model.HardwareFeature;
import com.intel.mtwilson.core.common.model.HardwareFeatureDetails;
import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.core.flavor.model.SignedFlavor;
import com.intel.mtwilson.core.verifier.Verifier;
import com.intel.mtwilson.core.verifier.policy.Fault;
import com.intel.mtwilson.core.verifier.policy.RuleResult;
import com.intel.mtwilson.core.verifier.policy.TrustMarker;
import com.intel.mtwilson.core.verifier.policy.TrustReport;
import com.intel.mtwilson.features.queue.QueueOperation;
import com.intel.mtwilson.features.queue.model.QueueState;
import com.intel.mtwilson.flavor.business.policy.rule.RequiredFlavorTypeExists;
import com.intel.mtwilson.flavor.business.policy.rule.RuleAllOfFlavors;
import com.intel.mtwilson.flavor.model.*;

import static com.intel.mtwilson.flavor.model.MatchPolicy.MatchType.ALL_OF;
import static com.intel.mtwilson.flavor.model.MatchPolicy.Required.REQUIRED;
import static com.intel.mtwilson.flavor.model.MatchPolicy.Required.REQUIRED_IF_DEFINED;

import com.intel.mtwilson.flavor.rest.v2.model.*;
import com.intel.mtwilson.flavor.model.MatchPolicy.MatchType;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorHostLinkRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorgroupRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.HostRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.HostStatusRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.ReportRepository;
import com.intel.mtwilson.flavor.rest.v2.resource.HostResource;
import com.intel.mtwilson.flavor.rest.v2.utils.HostConnectorUtils;
import com.intel.mtwilson.flavor.saml.IssuerConfigurationFactory;
import com.intel.mtwilson.i18n.HostState;
import static com.intel.mtwilson.i18n.HostState.CONNECTED;
import static com.intel.mtwilson.i18n.HostState.QUEUE;
import com.intel.mtwilson.core.common.model.HostComponents;
import com.intel.mtwilson.core.common.model.HostManifest;
import static com.intel.mtwilson.features.queue.model.QueueState.COMPLETED;
import static com.intel.mtwilson.features.queue.model.QueueState.TIMEOUT;
import static com.intel.mtwilson.features.queue.model.QueueState.ERROR;
import static com.intel.mtwilson.i18n.HostState.*;

import com.intel.mtwilson.flavor.rest.v2.resource.HostStatusResource;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import com.intel.mtwilson.ms.common.MSConfig;
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
import org.apache.shiro.util.CollectionUtils;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.xml.io.MarshallingException;
/**
 *
 * @author rksavino
 * @author dtiwari
 */
public class FlavorVerify extends QueueOperation {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FlavorVerify.class);
    private final String flavorSigningCertPath = My.configuration().getDirectoryPath() + File.separator + "flavor-signer.crt.pem";
    private final String caCertPath = My.configuration().getDirectoryPath() + File.separator + "cms-ca-cert.pem";

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

            // retrieve the flavorgroups
            List<Flavorgroup> flavorGroupsToVerify = getFlavorgroupsToVerify(hostId);
            UUID hardwareUuid = UUID.valueOf(hostManifest.getHostInfo().getHardwareUuid());
            boolean isCollectiveTrustReportValid = true;
            // create collective trust report from hostgroups individual trustreport
            TrustReport collectiveTrustReport = null;
            for(Flavorgroup flavorgroup: flavorGroupsToVerify) {
                HostTrustRequirements trustRequirementsForFlavorGroup = getHostTrustRequirementsForFlavorgroup(hostId, hardwareUuid, flavorgroup);
                List<SignedFlavor> cachedFlavorsForFlavorgroup = retrieveCachedFlavorsForFlavorgroupToMatch(hostId, flavorgroup);
                if(cachedFlavorsForFlavorgroup != null) {
                    HostTrustCache hostTrustCacheForFlavorgroup = validateCachedFlavorsAgainstHostManifest(hostId, hostManifest, cachedFlavorsForFlavorgroup);
                    TrustReport hostTrustReportForFlavorgroup = hostTrustCacheForFlavorgroup.getTrustReport();
                    if (!isHostTrustReportValidForFlavorgroup(trustRequirementsForFlavorGroup, hostTrustCacheForFlavorgroup)) {
                        isCollectiveTrustReportValid = false;
                        // Generate Host Trust Report for Flavor Group by verifying flavors against Host Manifest
                        hostTrustReportForFlavorgroup = createHostTrustReportForFlavorgroup(hostManifest, trustRequirementsForFlavorGroup, hostTrustCacheForFlavorgroup);
                    }

                    log.debug("Trust status for host {} for flavor group {} is {}", hostId.toString(), flavorgroup.getName(), hostTrustReportForFlavorgroup.isTrusted());
                    if (collectiveTrustReport == null) {
                        collectiveTrustReport = hostTrustReportForFlavorgroup;
                    } else {
                        addRuleResults(collectiveTrustReport, hostTrustReportForFlavorgroup.getResults());
                    }
                }
            }
            if (collectiveTrustReport != null && (!isCollectiveTrustReportValid || forceUpdate)) {
                log.debug("Trust cache update called, generating new SAML and saving new report for host: {}", hostId.toString());
                storeTrustReport(hostId, collectiveTrustReport);
            }
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

    private List<Flavorgroup> getFlavorgroupsToVerify(UUID hostId) {
        FlavorgroupFilterCriteria flavorgroupFilterCriteria = new FlavorgroupFilterCriteria();
        flavorgroupFilterCriteria.hostId = hostId;
        FlavorgroupCollection flavorgroupCollection = new FlavorgroupRepository().search(flavorgroupFilterCriteria);
        return flavorgroupCollection.getFlavorgroups();
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
                hostManifest = new HostResource().getHostManifest(host,
                        HostConnectorUtils.getConnectionStringWithCredentials(host.getConnectionString(), hostId));
            } catch (Exception e) {
                // detect the host state from the error response
                hostState = new HostStatusResource().determineHostState(e);
                if(hostState.equals(CONNECTION_TIMEOUT))
                    this.setQueueState(TIMEOUT);
                else if (hostState.equals(CONNECTION_FAILURE))
                    this.setQueueState(QueueState.CONNECTION_FAILURE);
                else
                    this.setQueueState(ERROR);
            }
            
            // update host record with hardware UUID and default software flavorgroups
            if (hostManifest != null && hostManifest.getHostInfo() != null
                    && hostManifest.getHostInfo().getHardwareUuid() != null
                    && !hostManifest.getHostInfo().getHardwareUuid().isEmpty()
                    && hostManifest.getHostInfo().getInstalledComponents() != null
                    && !hostManifest.getHostInfo().getInstalledComponents().isEmpty()) {
                List<String> flavorgroupNames = new ArrayList();
                if (new HostResource().validateIseclSoftwareFlavor(hostManifest.getHostInfo())){
                    if(hostManifest.getHostInfo().getInstalledComponents().contains(HostComponents.TAGENT.getValue()))
                        flavorgroupNames.add(Flavorgroup.PLATFORM_SOFTWARE_FLAVORGROUP);
                    if(hostManifest.getHostInfo().getInstalledComponents().contains(HostComponents.WLAGENT.getValue()))
                        flavorgroupNames.add(Flavorgroup.WORKLOAD_SOFTWARE_FLAVORGROUP);
                }
                host.setFlavorgroupNames(flavorgroupNames);
                host.setHardwareUuid(UUID.valueOf(hostManifest.getHostInfo().getHardwareUuid()));
                new HostRepository().store(host);
                new HostResource().linkFlavorgroupsToHost(flavorgroupNames, host.getId());
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
    
    private HostTrustRequirements getHostTrustRequirementsForFlavorgroup(UUID hostId, UUID hardwareUuid, Flavorgroup flavorgroup) {
        HostTrustRequirements hostTrustRequirements = new HostTrustRequirements();
        try {
            hostTrustRequirements.setFlavorgroupId(flavorgroup.getId());
            
            // check for ALL_OF flavors for the hosts flavorgroup
            FlavorMatchPolicyCollection flavorMatchPolicy = flavorgroup.getFlavorMatchPolicyCollection();
            hostTrustRequirements.setFlavorMatchPolicy(flavorMatchPolicy);
            if (!flavorMatchPolicy.getFlavorPartsByMatchType(ALL_OF).isEmpty()) {
                FlavorFilterCriteria flavorFilterCriteria = new FlavorFilterCriteria();
                flavorFilterCriteria.flavorgroupId = flavorgroup.getId();
                List<FlavorPart> allOfFlavorParts = flavorMatchPolicy.getFlavorPartsByMatchType(ALL_OF);
                flavorFilterCriteria.flavorParts = allOfFlavorParts;
                SignedFlavorCollection allOfFlavors = new FlavorRepository().search(flavorFilterCriteria);
                hostTrustRequirements.setAllOfFlavorsTypes(allOfFlavorParts);
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

    private List<SignedFlavor> retrieveCachedFlavorsForFlavorgroupToMatch(UUID hostId, Flavorgroup flavorgroup) {
        List<SignedFlavor> flavorsToMatch = new ArrayList<>();
        // retrieve the trusted cached flavors for the host
        FlavorHostLinkFilterCriteria flavorHostLinkFilterCriteria = new FlavorHostLinkFilterCriteria();
        flavorHostLinkFilterCriteria.hostId = hostId;
        flavorHostLinkFilterCriteria.flavorgroupId = flavorgroup.getId();
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
            SignedFlavor cachedFlavor = new FlavorRepository().retrieveSignedFlavor(flavorLocator);
            if (cachedFlavor !=  null) {
                flavorsToMatch.add(new SignedFlavor(cachedFlavor.getFlavor(), cachedFlavor.getSignature()));
            }
        }

        return flavorsToMatch;
    }

    private HostTrustCache validateCachedFlavorsAgainstHostManifest(UUID hostId, HostManifest hostManifest, List<SignedFlavor> cachedFlavors) {
        HostTrustCache hostTrustCache = new HostTrustCache();
        hostTrustCache.setHostId(hostId);
        TrustReport collectiveTrustReport = null;
        try {
            for (SignedFlavor cachedFlavor : cachedFlavors) {
                // call verifier
                String privacyCaCert = My.configuration().getPrivacyCaIdentityCacertsFile().getAbsolutePath();
                String tagCaCert = My.configuration().getAssetTagCaCertificateFile().getAbsolutePath();
                Verifier verifier = new Verifier(privacyCaCert, tagCaCert, flavorSigningCertPath, caCertPath);
                TrustReport individualTrustReport = verifier.verify(hostManifest, cachedFlavor, MSConfig.getConfiguration().getBoolean("skip.flavor.signature.verification"));

                // if the flavor is trusted, add it to the collective trust report and to the return object
                // else, delete it from the trust cache
                if (individualTrustReport.isTrusted()) {
                    hostTrustCache.getTrustedFlavors().getFlavors().add(cachedFlavor.getFlavor());
                    if (collectiveTrustReport == null) {
                        collectiveTrustReport = individualTrustReport;
                    } else {
                        collectiveTrustReport = addRuleResults(collectiveTrustReport, individualTrustReport.getResults());
                    }
                } else {
                    FlavorHostLinkFilterCriteria flavorHostLinkFilterCriteria = new FlavorHostLinkFilterCriteria();
                    flavorHostLinkFilterCriteria.hostId = hostId;
                    flavorHostLinkFilterCriteria.flavorId = UUID.valueOf(cachedFlavor.getFlavor().getMeta().getId());
                    new FlavorHostLinkRepository().delete(flavorHostLinkFilterCriteria);
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

    private TrustReport verify(UUID hostId, SignedFlavorCollection flavors, HostManifest hostManifest, HostTrustRequirements hostTrustRequirements) {
        TrustReport collectiveTrustReport = null;

        // return null if no flavors were found
        if (flavors == null || flavors.getSignedFlavors() == null || flavors.getSignedFlavors().isEmpty()) {
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
            for (SignedFlavor signedFlavor : flavors.getSignedFlavors()) {
                UUID flavorId = UUID.valueOf(signedFlavor.getFlavor().getMeta().getId());
                log.debug("Found flavor with ID: {}", flavorId.toString());
                // call verifier
                String privacyCaCert = My.configuration().getPrivacyCaIdentityCacertsFile().getAbsolutePath();
                String tagCaCert = My.configuration().getAssetTagCaCertificateFile().getAbsolutePath();
                Verifier verifier = new Verifier(privacyCaCert, tagCaCert, flavorSigningCertPath, caCertPath);
                List<FlavorMatchPolicy> flavorMatchPolicies= hostTrustRequirements.getFlavorMatchPolicy().getFlavorMatchPolicies();
                for(FlavorMatchPolicy flavorMatchPolicy : flavorMatchPolicies) {
                    if (flavorMatchPolicy.getFlavorPart().getValue().equals(signedFlavor.getFlavor().getMeta().getDescription().getFlavorPart())) {
                        TrustReport individualTrustReport = verifier.verify(hostManifest, signedFlavor, MSConfig.getConfiguration().getBoolean("skip.flavor.signature.verification"));

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
                                    FlavorPart.valueOf(signedFlavor.getFlavor().getMeta().getDescription().getFlavorPart()),
                                    flavorId,
                                    individualTrustReport));
                            for (RuleResult result : individualTrustReport.getResults()) {
                                for (Fault fault : result.getFaults()) {
                                    log.debug("Flavor [{}] did not match host [{}] due to fault: {}", flavorId.toString(), hostId.toString(), fault.toString());
                                }
                            }
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

    private TrustReport createTrustReport(HostManifest hostManifest, HostTrustRequirements hostTrustRequirements,
                                          HostTrustCache trustCache, HashMap<String, Boolean> latestReqAndDefFlavorTypes) {
        // Fetch flavors needed to verify
        SignedFlavorCollection flavorsToVerify = findFlavors(hostTrustRequirements.getFlavorgroupId(), hostManifest, latestReqAndDefFlavorTypes);
        // Verify flavor collection against host report
        TrustReport trustReport = verify(hostId, flavorsToVerify, hostManifest, hostTrustRequirements);

        // add results found in trust cache
        if (!isTrustCacheEmpty(trustCache)) {
            for (RuleResult rule : trustCache.getTrustReport().getResults()) {
                trustReport.addResult(rule);
            }
        }

        // add required and defined flavor check rules to the trust report
        List<FlavorPart> reqAndDefFlavorTypes = hostTrustRequirements.getDefinedAndRequiredFlavorTypes();
        for (FlavorPart flavorPart : reqAndDefFlavorTypes) {
            RequiredFlavorTypeExists rule = new RequiredFlavorTypeExists(flavorPart);
            trustReport = rule.apply(trustReport);
        }

        // add all of flavors check rule to the trust report
        SignedFlavorCollection allOfFlavors = hostTrustRequirements.getAllOfFlavors();
        RuleAllOfFlavors ruleAllOfFlavors = new RuleAllOfFlavors(allOfFlavors,
                My.configuration().getPrivacyCaIdentityCacertsFile().getAbsolutePath(),
                My.configuration().getAssetTagCaCertificateFile().getAbsolutePath(),
                flavorSigningCertPath,
                caCertPath,
                MSConfig.getConfiguration().getBoolean("skip.flavor.signature.verification"));
        ruleAllOfFlavors.setMarkers(getAllOfMarkers(hostTrustRequirements));
        trustReport = ruleAllOfFlavors.addFaults(trustReport);  // Add faults if every 'All of' flavors are not present
        
        return trustReport;
    }

    private String[] getAllOfMarkers(HostTrustRequirements hostTrustRequirements) {
        String markers[] = new String[hostTrustRequirements.getAllOfFlavorsTypes().size()];
        for(int i=0 ; i <hostTrustRequirements.getAllOfFlavorsTypes().size(); i++) {
            markers[i] = hostTrustRequirements.getAllOfFlavorsTypes().get(i).name();
        }
        return markers;
    }

    private boolean isHostTrustReportValidForFlavorgroup(HostTrustRequirements hostTrustRequirements, HostTrustCache trustCache) {
        // No results found in Trust Cache
        if (isTrustCacheEmpty(trustCache)) {
            log.debug("No results found in trust cache for host: {}", hostId.toString());
            return false;
        }

        // Missing Required and Defined Flavors
        TrustReport cachedTrustReport = trustCache.getTrustReport();
        List<FlavorPart> reqAndDefFlavorTypes = hostTrustRequirements.getDefinedAndRequiredFlavorTypes();
        HashMap<String, Boolean> missingRequiredFlavorPartsWithLatest = getMissingRequiredFlavorPartsWithLatest(hostTrustRequirements, reqAndDefFlavorTypes, cachedTrustReport);
        if (!missingRequiredFlavorPartsWithLatest.isEmpty()) {
            log.debug("Host [{}] has missing required and defined flavor parts: {}", hostId.toString(), missingRequiredFlavorPartsWithLatest.keySet());
            return false;
        }

        // All Of Flavors present
        SignedFlavorCollection allOfFlavors = hostTrustRequirements.getAllOfFlavors();
        RuleAllOfFlavors ruleAllOfFlavors = new RuleAllOfFlavors(allOfFlavors,
                My.configuration().getPrivacyCaIdentityCacertsFile().getAbsolutePath(),
                My.configuration().getAssetTagCaCertificateFile().getAbsolutePath(),
                flavorSigningCertPath,
                caCertPath,
                MSConfig.getConfiguration().getBoolean("skip.flavor.signature.verification"));
        ruleAllOfFlavors.setMarkers(getAllOfMarkers(hostTrustRequirements));
        if (areAllOfFlavorsMissingInCachedTrustReport(cachedTrustReport, ruleAllOfFlavors)) {
            log.debug("All of flavors exist in policy for host: {}", hostId.toString());
            log.debug("Some all of flavors do not match what is in the trust cache for host: {}", hostId.toString());
            return false;
        }
        log.debug("Trust cache valid for host: {}", hostId.toString());
        return true;
    }

    private TrustReport createHostTrustReportForFlavorgroup(HostManifest hostManifest, HostTrustRequirements hostTrustRequirements, HostTrustCache trustCache) {
        //create a hashMap with latest match policy
        List<FlavorPart> reqAndDefFlavorTypes = hostTrustRequirements.getDefinedAndRequiredFlavorTypes();
        HashMap<String, Boolean> latestReqAndDefFlavorTypes = getLatestFlavorTypeMap(hostTrustRequirements, reqAndDefFlavorTypes);

        // No results found in Trust Cache
        if (isTrustCacheEmpty(trustCache)) {
            return createTrustReport(hostManifest, hostTrustRequirements, trustCache, latestReqAndDefFlavorTypes);
        }

        // Missing Required and Defined Flavors
        TrustReport cachedTrustReport = trustCache.getTrustReport();
        HashMap<String, Boolean> missingRequiredFlavorPartsWithLatest = getMissingRequiredFlavorPartsWithLatest(hostTrustRequirements, reqAndDefFlavorTypes, cachedTrustReport);
        if (!missingRequiredFlavorPartsWithLatest.isEmpty()) {
            return createTrustReport(hostManifest, hostTrustRequirements, trustCache, missingRequiredFlavorPartsWithLatest);
        }
        
        // All Of Flavors present
        SignedFlavorCollection allOfFlavors = hostTrustRequirements.getAllOfFlavors();
        RuleAllOfFlavors ruleAllOfFlavors = new RuleAllOfFlavors(allOfFlavors,
                My.configuration().getPrivacyCaIdentityCacertsFile().getAbsolutePath(),
                My.configuration().getAssetTagCaCertificateFile().getAbsolutePath(),
                flavorSigningCertPath,
                caCertPath,
                MSConfig.getConfiguration().getBoolean("skip.flavor.signature.verification"));
        ruleAllOfFlavors.setMarkers(getAllOfMarkers(hostTrustRequirements));
        if (areAllOfFlavorsMissingInCachedTrustReport(cachedTrustReport, ruleAllOfFlavors)) {
            return createTrustReport(hostManifest, hostTrustRequirements, trustCache, latestReqAndDefFlavorTypes);
        }
        return cachedTrustReport;
    }

    private HashMap<String, Boolean> getLatestFlavorTypeMap(HostTrustRequirements hostTrustRequirements, List<FlavorPart> reqAndDefFlavorTypes) {
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
        return latestMap;
    }

    private boolean areAllOfFlavorsMissingInCachedTrustReport(TrustReport cachedTrustReport, RuleAllOfFlavors ruleAllOfFlavors) {
        return !ruleAllOfFlavors.allOfFlavorsEmpty() && !ruleAllOfFlavors.checkAllOfFlavorsExist(cachedTrustReport);
    }

    private HashMap<String, Boolean> getMissingRequiredFlavorPartsWithLatest(HostTrustRequirements hostTrustRequirements, List<FlavorPart> reqAndDefFlavorTypes, TrustReport cachedTrustReport) {
        HashMap<String, Boolean> missingRequiredFlavorPartsWithLatest = new HashMap<>();
        for (FlavorPart requiredFlavorType : reqAndDefFlavorTypes) {
            log.debug("Checking if required flavor type [{}] for host [{}] is missing", requiredFlavorType.name(), hostId.toString());
            if (areRequiredFlavorsMissing(cachedTrustReport, requiredFlavorType)) {
                log.debug("Required flavor type [{}] for host [{}] is missing", requiredFlavorType.name(), hostId.toString());
                MatchPolicy matchPolicyMissing = hostTrustRequirements.getFlavorMatchPolicy().getmatchPolicy(requiredFlavorType);
                if (matchPolicyMissing != null && matchPolicyMissing.getMatchType() == MatchType.LATEST) {
                    missingRequiredFlavorPartsWithLatest.put(requiredFlavorType.name(), true);
                } else {
                    missingRequiredFlavorPartsWithLatest.put(requiredFlavorType.name(), false);
                }
            }
        }
        return missingRequiredFlavorPartsWithLatest;
    }

    private boolean areRequiredFlavorsMissing(TrustReport cachedTrustReport, FlavorPart requiredFlavorType) {
        return cachedTrustReport.getResultsForMarker(requiredFlavorType.name()) == null
                || cachedTrustReport.getResultsForMarker(requiredFlavorType.name()).isEmpty();
    }

    private boolean isTrustCacheEmpty(HostTrustCache trustCache) {
        return trustCache == null || trustCache.getTrustedFlavors() == null || trustCache.getTrustedFlavors().getFlavors() == null
                || trustCache.getTrustedFlavors().getFlavors().isEmpty();
    }

    private SignedFlavorCollection findFlavors(UUID flavorgroupId, HostManifest hostManifest, HashMap<String, Boolean> latestFlavorMap) {
        FlavorFilterCriteria flavorFilterCriteria = new FlavorFilterCriteria();
        flavorFilterCriteria.flavorgroupId = flavorgroupId;
        flavorFilterCriteria.hostManifest = hostManifest;        
        flavorFilterCriteria.flavorPartsWithLatest = latestFlavorMap;
        
        FlavorRepository flavorRepository = new FlavorRepository();
        return flavorRepository.search(flavorFilterCriteria);
    }

    private String generateSamlReport(TrustReport trustReport) {
        SamlAssertion mapSamlAssertion;
        try {
            SAML saml = new SAML(new IssuerConfigurationFactory().loadIssuerConfiguration());
            BeanMap map = new BeanMap(trustReport.getHostManifest().getHostInfo());
            Map<String, String> samlMap = new LinkedHashMap();
            Iterator<String> it = map.keyIterator();
            X509Certificate aikCertificate = trustReport.getHostManifest().getAikCertificate();
            X509Certificate bindingKeyCertificate = trustReport.getHostManifest().getBindingKeyCertificate();
            while (it.hasNext()) {
                String key = it.next();
                if (map.get(key) != null && !key.equals("class") && !key.equals("hardwareFeatures")) {
                    String value = map.get(key).toString();
                    samlMap.put(key, value);
                }
            }
            Map<HardwareFeature, HardwareFeatureDetails> hardwareFeatures = trustReport.getHostManifest().getHostInfo().getHardwareFeatures();
            if(!CollectionUtils.isEmpty(hardwareFeatures)) {
                for (HardwareFeature feature : HardwareFeature.values()) {
                    HardwareFeatureDetails details = hardwareFeatures.get(feature);
                    if(details != null) {
                        String key = "FEATURE_" + feature.getValue();
                        String value = String.valueOf(details.getEnabled());
                        samlMap.put(key, value);
                        if(HardwareFeature.CBNT.equals(feature)) {
                            samlMap.put("FEATURE_cbntProfile", details.getMeta().get("profile"));
                        }
                    }
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
            
            if (bindingKeyCertificate != null){
                samlMap.put("Binding_Key_Certificate", X509Util.encodePemCertificate(bindingKeyCertificate));
            }
            if (aikCertificate != null){
                samlMap.put("AIK_Certificate", X509Util.encodePemCertificate(aikCertificate));
            }

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
    
    private void storeTrustReport(UUID hostId, TrustReport trustReport) {
        String samlReport = generateSamlReport(trustReport);
        Map<String, Date> dates = parseDatesFromSaml(samlReport);
        log.debug("flavorverify: {}", samlReport); 
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
