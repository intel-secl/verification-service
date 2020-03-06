/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.rest.v2.utils;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.configuration.ConfigurationProvider;
import com.intel.mtwilson.core.common.datatypes.ConnectionString;
import com.intel.mtwilson.core.common.datatypes.Vendor;
import com.intel.mtwilson.core.common.utils.AASConstants;
import com.intel.mtwilson.flavor.rest.v2.model.Flavorgroup;
import com.intel.mtwilson.flavor.rest.v2.model.Host;
import com.intel.mtwilson.flavor.rest.v2.model.HostLocator;
import com.intel.mtwilson.flavor.rest.v2.repository.HostRepository;
import com.intel.mtwilson.flavor.rest.v2.resource.HostResource;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.exception.TlsPolicyAllowedException;
import com.intel.mtwilson.tls.policy.filter.HostTlsPolicyFilter;

import javax.ws.rs.WebApplicationException;
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

    public static ConnectionString getConnectionStringWithCredentials(String connectionString, UUID hostId) throws IOException {
        String credential = getCredentialsForHost(connectionString, hostId);
        return new ConnectionString(String.format("%s;%s", connectionString, credential));
    }

    public static String getHostConnectionString(String connectionString, UUID hostId) throws IOException {
        if(connectionString == null) {
            if (hostId == null){
                throw new WebApplicationException("Both hostId and connectionString are null, either hostId or connectionString must be specified", 400);
            }
            connectionString = getHostByIdentifier(hostId).getConnectionString();
        }

        if(!connectionString.contains("u=") || !connectionString.contains("p=")) {
            return String.format("%s;%s", connectionString, getCredentialsForHost(connectionString, hostId));
        } else {
            return connectionString;
        }
    }

    public static String getCredentialsForHost(String connectionString, UUID id) throws IOException {
        String credential;
        ConnectionString cs = new ConnectionString(connectionString);
        if (!Vendor.VMWARE.equals(cs.getVendor())) { //Not using
            ConfigurationProvider configurationProvider = ConfigurationFactory.getConfigurationProvider();
            Configuration configuration = configurationProvider.load();
            String username = "u=" + configuration.get(AASConstants.MC_FIRST_USERNAME);
            String password = "p=" + configuration.get(AASConstants.MC_FIRST_PASSWORD);
            credential = String.format("%s;%s", username, password);
        } else {
            credential = My.jpa().mwHostCredential().findByHostId(id.toString()).getCredential();
        }
        return credential;
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
