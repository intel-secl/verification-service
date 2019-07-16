/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.rest.v2.utils;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.core.common.datatypes.ConnectionString;
import com.intel.mtwilson.flavor.data.MwHostCredential;
import com.intel.mtwilson.flavor.rest.v2.model.Flavorgroup;
import com.intel.mtwilson.flavor.rest.v2.model.Host;
import com.intel.mtwilson.flavor.rest.v2.model.HostLocator;
import com.intel.mtwilson.flavor.rest.v2.repository.HostRepository;
import com.intel.mtwilson.flavor.rest.v2.resource.HostResource;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.exception.TlsPolicyAllowedException;
import com.intel.mtwilson.tls.policy.filter.HostTlsPolicyFilter;

import java.io.IOException;


/**
 *
 * @author ddhawale
 */
public class HostConnectorUtils {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostConnectorUtils.class);

    public static String getFlavorgroupName(String flavorgroupName) {
        if (flavorgroupName != null && !flavorgroupName.isEmpty()) {
            return flavorgroupName;
        } else {
            return  Flavorgroup.AUTOMATIC_FLAVORGROUP;
        }
    }

    public static String getHostConnectionString(String connectionString, UUID hostId) throws IOException {
        if(connectionString != null) {
            return connectionString;
        } else {
            Host host = getHostByIdentifier(hostId);
            MwHostCredential credential = My.jpa().mwHostCredential().findByHostId(host.getId().toString());
            return String.format("%s;%s",host.getConnectionString(), credential.getCredential());
        }
    }

    public static TlsPolicyDescriptor getTlsPolicyDescriptor(String connectionString, UUID hostId) throws IOException {
        if(hostId != null) {
            return getTlsPolicyDescriptorFromHost(connectionString, hostId);
        } else if (connectionString != null){
            return getTlsPolicyDescriptorFromConnectionString(connectionString);
        }
        throw new IllegalArgumentException("Cannot determine appropriate TLS policy for host");
    }

    private static TlsPolicyDescriptor getTlsPolicyDescriptorFromHost(String connectionString, UUID hostId) throws IOException {
        Host host = getHostByIdentifier(hostId);
        String tlsPolicyId = host.getTlsPolicyId();
        //TODO: replace tls policy with actual policy
        return new HostResource().getTlsPolicy(tlsPolicyId, new ConnectionString(
                getHostConnectionString(connectionString, hostId)), true);
    }

    private static TlsPolicyDescriptor getTlsPolicyDescriptorFromConnectionString(String connectionString) throws IOException {
        ConnectionString hostConnectionString = HostRepository.generateConnectionString(connectionString);

        // connect to the host and retrieve the host manifest
        TlsPolicyDescriptor tlsPolicyDescriptor = new HostResource().getTlsPolicy(
                null, hostConnectionString, true);

        if (tlsPolicyDescriptor == null) {
            throw new IllegalArgumentException("Cannot determine appropriate TLS policy for host");
        }

        // check if the tlsPolicyDescriptor is allowed. Throw error if not allowed.
        if (!HostTlsPolicyFilter.isTlsPolicyAllowed(tlsPolicyDescriptor.getPolicyType())) {
            log.error("TLS policy {} is not allowed", tlsPolicyDescriptor.getPolicyType());
            throw new TlsPolicyAllowedException("TLS policy is not allowed");
        }
        return tlsPolicyDescriptor;
    }

    private static Host getHostByIdentifier(UUID hostId) {
        HostLocator hostLocator = new HostLocator();
        hostLocator.id = hostId;
        Host host = new HostRepository().retrieve(hostLocator);
        if (host == null) {
            log.error("The host with specified id was not found {}", hostId);
            throw new RepositoryInvalidInputException(hostLocator);
        }
        return host;
    }
}
