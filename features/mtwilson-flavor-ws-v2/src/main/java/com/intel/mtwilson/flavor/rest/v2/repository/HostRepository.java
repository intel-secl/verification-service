/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.flavor.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.flavor.controller.MwHostJpaController;
import com.intel.mtwilson.flavor.controller.MwHostStatusJpaController;
import com.intel.mtwilson.flavor.data.MwHost;
import com.intel.mtwilson.flavor.data.MwHostCredential;
import com.intel.mtwilson.flavor.rest.v2.model.Host;
import com.intel.mtwilson.flavor.rest.v2.model.HostCollection;
import com.intel.mtwilson.flavor.rest.v2.model.HostFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.HostLocator;
import com.intel.mtwilson.flavor.rest.v2.model.HostStatus;
import com.intel.mtwilson.flavor.rest.v2.model.HostStatusLocator;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.core.common.datatypes.ConnectionString;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositoryStoreException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author hmgowda
 * @author purvades
 */
public class HostRepository {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostRepository.class);

    public HostCollection search(HostFilterCriteria criteria) {
        log.debug("host:search - got request to search for hosts");
        HostCollection hostCollection = new HostCollection();
        try {
            MwHostJpaController mwHostJpaController = My.jpa().mwHost();
            if (criteria.filter == false) {
                List<MwHost> mwHostList = mwHostJpaController.findMwHostEntities();
                if (mwHostList != null && !mwHostList.isEmpty()) {
                    for (MwHost mwHost : mwHostList) {
                        hostCollection.getHosts().add(convert(mwHost));
                    }
                }
            } else if (criteria.id != null) {
                MwHost mwHost = mwHostJpaController.findMwHost(criteria.id.toString());
                if (mwHost != null) {
                    hostCollection.getHosts().add(convert(mwHost));
                }
            } else if (criteria.tlsPolicyId != null) {
                MwHost mwHost = mwHostJpaController.findMwHostByTlsPolicyId(criteria.tlsPolicyId);
                if (mwHost != null) {
                    hostCollection.getHosts().add(convert(mwHost));
                }
            } else if (criteria.nameEqualTo != null && !criteria.nameEqualTo.isEmpty()) {
                // re-arranged slightly to look more like the nameContains case below
                MwHost mwHost = mwHostJpaController.findMwHostByName(criteria.nameEqualTo);
                if (mwHost != null) {
                    hostCollection.getHosts().add(convert(mwHost));
                }
            } else if (criteria.nameContains != null && !criteria.nameContains.isEmpty()) {
                List<MwHost> mwHostList = mwHostJpaController.findMwHostByNameLike(criteria.nameContains);
                if (mwHostList != null && !mwHostList.isEmpty()) {
                    for (MwHost mwHost : mwHostList) {
                        hostCollection.getHosts().add(convert(mwHost));
                    }
                }
            } else if (criteria.hostHardwareId != null) {
                MwHost mwHost = mwHostJpaController.findMwHostByHardwareUuid(criteria.hostHardwareId.toString());
                if (mwHost != null) {
                    hostCollection.getHosts().add(convert(mwHost));
                }
            } else if (criteria.key != null && !criteria.key.isEmpty() && criteria.value != null && !criteria.value.isEmpty()) {
                MwHostStatusJpaController hostStatuspaController = My.jpa().mwHostStatus();
                List<String> mwHostEntries = hostStatuspaController.findMwHostListByKeyValue(criteria.key, criteria.value);
                if (mwHostEntries != null && !mwHostEntries.isEmpty()) {
                    for (String hostId : mwHostEntries) {
                        MwHost mwHost = mwHostJpaController.findMwHost(hostId);
                        if (mwHost != null) {
                            hostCollection.getHosts().add(convert(mwHost));
                        }
                    }
                }
            } else {
                // Invalid search criteria specified. Just log the error and return back empty collection.
                log.error("host:search - invalid search criteria specified");
            }
        } catch (Exception ex) {
            log.error("host:search - error during host search", ex);
            throw new RepositorySearchException(ex, criteria);
        }         
        log.debug("host:search - returning back {} results", hostCollection.getHosts().size());
        return hostCollection;
    }

    public Host retrieve(HostLocator locator) {
        log.debug("host:retrieve - got request to retrieve host");
        if (locator == null || ( locator.id == null && locator.pathId == null && locator.hardwareUuid == null
                && (locator.name == null || locator.name.isEmpty())
                && (locator.aikCertificate == null || locator.aikCertificate.isEmpty()) )) {
            log.debug("host:retrieve - host ID, hardware UUID, host name, or AIK certificate must be specified");
            return null;
        }
        
        try {
            // if AIK criteria specified, retrieve host ID from host status table
            if (locator.aikCertificate != null && !locator.aikCertificate.isEmpty()) {
                HostStatusLocator hostStatusLocator = new HostStatusLocator();
                hostStatusLocator.aikCertificate = locator.aikCertificate;
                HostStatus hostStatus = new HostStatusRepository().retrieve(hostStatusLocator);
                
                if (hostStatus != null) {
                    locator.id = hostStatus.getHostId();
                }
            }
            
            MwHostJpaController mwHostJpaController = My.jpa().mwHost();
            if (locator.pathId != null) {
                MwHost mwHost = mwHostJpaController.findMwHost(locator.pathId.toString());
                if (mwHost != null) {
                    return convert(mwHost);
                }
            } else if (locator.id != null) {
                MwHost mwHost = mwHostJpaController.findMwHost(locator.id.toString());
                if (mwHost != null) {
                    return convert(mwHost);
                }
            } else if (locator.hardwareUuid != null) {
                MwHost mwHost = mwHostJpaController.findMwHostByHardwareUuid(locator.hardwareUuid.toString());
                if (mwHost != null) {
                    return convert(mwHost);
                }
            } else if (locator.name != null) {
                MwHost mwHost = mwHostJpaController.findMwHostByName(locator.name);
                if (mwHost != null) {
                    return convert(mwHost);
                }
            }
        } catch (Exception ex) {
            log.error("host:retrieve - error during retrieval of host", ex);
            throw new RepositoryRetrieveException(ex);
        }
        return null;
    }

    public Host store(Host item) {
        log.debug("Host:Store - Got request to update host");
        if (item == null || item.getId() == null) {
            log.error("Host:store - Host ID must be specified");
            throw new RepositoryInvalidInputException();
        }
        
        HostLocator locator = new HostLocator();
        locator.id = item.getId();
        
        try {
            MwHostJpaController hostJpaController = My.jpa().mwHost();
            MwHost mwHost = hostJpaController.findMwHost(locator.id.toString());
            if (mwHost == null) {
                log.error("Host:Store - Host does not exist");
                throw new RepositoryInvalidInputException(locator);
            }
            
            if (item.getHostName() != null && !item.getHostName().isEmpty())
                mwHost.setName(item.getHostName());
            if (item.getHardwareUuid() != null)
                mwHost.setHardwareUuid(item.getHardwareUuid().toString());
            if (item.getTlsPolicyId() != null && !item.getTlsPolicyId().isEmpty()){
                if (!UUID.isValid(item.getTlsPolicyId())) {
                    if (!item.getTlsPolicyId().equalsIgnoreCase("TRUST_FIRST_CERTIFICATE") &&
                            !item.getTlsPolicyId().equalsIgnoreCase("INSECURE"))
                        throw new RepositoryInvalidInputException(String.format(
                        "TLS policy specified [%s] not supported", item.getTlsPolicyId()));
                }
                mwHost.setTlsPolicyId(item.getTlsPolicyId());
            }
            if (item.getConnectionString() != null && !item.getConnectionString().isEmpty()) {
                // remove credentials from connection string for host table storage
                ConnectionString connectionString = generateConnectionString(item.getConnectionString());
                String csWithoutCredentials = getConnectionStringWithoutCredentials(connectionString.getConnectionString());
                                
                mwHost.setConnectionString(csWithoutCredentials);
                
                MwHostCredential mwHostCredential = My.jpa().mwHostCredential().findByHostId(item.getId().toString());
                
                // create credentail
                if (mwHostCredential != null) {
                    mwHostCredential.setCredential(String.format("u=%s;p=%s", connectionString.getUserName(), connectionString.getPassword()));
                    mwHostCredential.setCreatedTs(Calendar.getInstance().getTime());
                    My.jpa().mwHostCredential().edit(mwHostCredential);
                }
            
            }
            if (item.getDescription() != null && !item.getDescription().isEmpty())
                mwHost.setDescription(item.getDescription());
            hostJpaController.edit(mwHost);
            
            log.debug("Host:Store - Updated the Host with id {} successfully.", item.getId().toString());
            return retrieve(locator);
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Host:Store - Error during Host update.", ex);
            throw new RepositoryStoreException(ex, locator);
        }
    }
    
    public Host create(Host item) {
        log.debug("host:create - got request to create a new host");
        UUID hostId;
        if (item.getId() != null) {
            hostId = item.getId();
        } else {
            hostId = new UUID();
        }
        
        HostLocator locator = new HostLocator();
        locator.id = hostId;
        
        if (item == null || item.getConnectionString() == null || item.getHostName() == null) {
            log.error("host:create - host name and connection string must be specified");
            throw new RepositoryInvalidInputException(locator);
        }
        
        
        try {
            MwHostJpaController mwHostJpaController = My.jpa().mwHost();
            MwHost mwHost = mwHostJpaController.findMwHostByName(item.getHostName());
           
            if (mwHost != null) {
                log.error("host:create - host specified with host name{} already exists", item.getHostName());
                throw new RepositoryInvalidInputException(locator);
            }

            // remove credentials from connection string for host table storage
            ConnectionString connectionString = generateConnectionString(item.getConnectionString());
            String csWithoutCredentials = getConnectionStringWithoutCredentials(connectionString.getConnectionString());
        
            mwHost = new MwHost();
            log.debug("host:create - the host name {} does not exist in mw_host. Creating a new host table entry in the database", item.getHostName());
            mwHost.setId(hostId.toString());
            mwHost.setConnectionString(csWithoutCredentials);
            mwHost.setDescription(item.getDescription());
            mwHost.setName(item.getHostName());
            mwHost.setTlsPolicyId(item.getTlsPolicyId());
            if(item.getHardwareUuid()!=null){
                mwHost.setHardwareUuid(item.getHardwareUuid().toString());
            }
            else{
                mwHost.setHardwareUuid(null);
            }
            mwHostJpaController.create(mwHost);

            MwHostCredential mwHostCredential = My.jpa().mwHostCredential().findByHostName(item.getHostName());
            // create credentail
            if (mwHostCredential != null) {
                mwHostCredential.setHostId(hostId.toString());
                mwHostCredential.setCredential(String.format("u=%s;p=%s", connectionString.getUserName(), connectionString.getPassword()));
                mwHostCredential.setCreatedTs(Calendar.getInstance().getTime());
                if (item.getHardwareUuid() != null) {
                    mwHostCredential.setHardwareUuid(item.getHardwareUuid().toString());
                }
                My.jpa().mwHostCredential().edit(mwHostCredential);
            } else {
                mwHostCredential = new MwHostCredential();
                mwHostCredential.setId(new UUID().toString());
                mwHostCredential.setHostName(item.getHostName());
                mwHostCredential.setHostId(hostId.toString());
                mwHostCredential.setCredential(String.format("u=%s;p=%s", connectionString.getUserName(), connectionString.getPassword()));
                mwHostCredential.setCreatedTs(Calendar.getInstance().getTime());
                if (item.getHardwareUuid() != null) {
                    mwHostCredential.setHardwareUuid(item.getHardwareUuid().toString());
                }
                My.jpa().mwHostCredential().create(mwHostCredential);
            }
             log.info("Created host with ID {} ", item.getId().toString());
            return item;

        } catch (IOException ex) {
            log.error("host:create - error during host creation", ex);
            throw new RepositoryCreateException(ex);
        } catch (Exception Ex) {
            log.error("host:create - error during host creation", Ex);
            throw new RepositoryCreateException(Ex);
        }
    }

    public void delete(HostLocator locator) {

    }

    public void delete(String hostId) {
        log.debug("Deleting host with Host ID: {}", hostId);
        try {
            MwHostJpaController hostJpa = My.jpa().mwHost();
            hostJpa.destroy(hostId);
            log.debug("Successfully deleted the host: {}", hostId);
        } catch (NonexistentEntityException ex) {
            log.error("Bad Input. Host ID is incorrect. {}", ex);
            throw new ASException(ErrorCode.AS_HOST_NOT_FOUND, hostId);
        } catch (IOException ex) {
            log.error("Got exception while deleting the host. {}", ex);
            throw new ASException(ErrorCode.AS_DELETE_HOST_ERROR, hostId);
        }
    }
    
    public void deleteByName(String hostName) {
        log.debug("Deleting host with Host Name: {}", hostName);
        try {
            MwHostJpaController hostJpa = My.jpa().mwHost();
            MwHost mwHost = hostJpa.findMwHostByName(hostName);
            if (mwHost != null) {
                hostJpa.destroy(mwHost.getId());
                log.debug("Successfully deleted the host: {}", hostName);
            }
        } catch (NonexistentEntityException ex) {
            log.error("Bad Input. Host ID is incorrect. {}", ex);
            throw new ASException(ErrorCode.AS_HOST_NOT_FOUND, hostName);
        } catch (IOException ex) {
            log.error("Got exception while deleting the host. {}", ex);
            throw new ASException(ErrorCode.AS_DELETE_HOST_ERROR, hostName);
        }
    }
    
    private Host convert(MwHost mwHost) {
        Host host = new Host();
        if (mwHost != null) {
            host.setId(UUID.valueOf(mwHost.getId()));
            host.setHostName(mwHost.getName());
            host.setDescription(mwHost.getDescription());
            host.setTlsPolicyId(mwHost.getTlsPolicyId());
            host.setConnectionString(mwHost.getConnectionString());
            if(mwHost.getHardwareUuid() != null){
                host.setHardwareUuid(UUID.valueOf(mwHost.getHardwareUuid()));
            }
            else
                host.setHardwareUuid(null);
        }
        
        return host;
    }
    
    public HostLocator convert(Host item){
        HostLocator hostLocator = new HostLocator();
        if (item.getId() != null)
            hostLocator.id = item.getId();
        if (item.getHostName() != null && !item.getHostName().isEmpty())
            hostLocator.name = item.getHostName();
        if (item.getHardwareUuid() != null)
            hostLocator.hardwareUuid = item.getHardwareUuid();
        return hostLocator;
    }
    
    /**
     * Creates a connection string object. If the username and password are not specified, then it would retrieve it
     * from the credential table and forms the complete connection string.
     * 
     * @param connectionString
     * @return Formated connection string.
     * @throws MalformedURLException
     * @throws IOException 
     */
    public static ConnectionString generateConnectionString(String connectionString) throws MalformedURLException, IOException {
        String credential;
        String username;
        String password;
        
        MwHostCredential mwHostCredential;
        
        //if credentials not specified in connection string, retrieve from credential table
        if(!connectionString.contains("u=") || !connectionString.contains("p=")) {
            String hostName  = null;
            // If the connection string is for VMware, we would have this substring from which we need to extract
            // the host name. Otherwise we can extract the host name after the https:// in the connection string.
            if (connectionString.contains("h=")) {
                String params = connectionString.substring(connectionString.indexOf(';') + 1); // get everything after the first semicolon 
                String[] parts = params.split(";");
                for (String partInfo : parts){
                    if (partInfo.startsWith("h=")) {
                        hostName = partInfo.substring(2); 
                        break;
                    }
                }
            } else {
                hostName = connectionString.split("//")[1].split(":")[0];
            }
            if (hostName == null || hostName.isEmpty()) {
                throw new IllegalArgumentException("Host connection string is formatted incorrectly, cannot retrieve host name");
            }
            mwHostCredential = My.jpa().mwHostCredential().findByHostName(hostName);

            if (mwHostCredential == null || mwHostCredential.getCredential() == null || mwHostCredential.getCredential().isEmpty()) {
                throw new IllegalArgumentException("Credentials must be provided for the host connection string");
            }
            
            credential = mwHostCredential.getCredential();
            username = credential.split(";", 2)[0];
            password = credential.split(";", 2)[1];
            connectionString = String.format("%s;%s", connectionString, credential);
        } else {
            ConnectionString cs = new ConnectionString(connectionString);
            username = cs.getUserName();
            password = cs.getPassword();
            credential = String.format("%s;%s", username, password);
        }
        
        // validate credential information values are not null or empty
        if (credential == null || credential.isEmpty()) {
            throw new IllegalArgumentException("Credentials must be provided for the host connection string");
        }
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username must be provided in the host connection string");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password must be provided in the host connection string");
        }
        return new ConnectionString(connectionString);
    }
    
    /**
     * This function remove the username and password from the connection string and returns it back. This
     * would be stored in the host table and the credentials would be stored in the separate table.
     * @param connectionString
     * @return 
     */
    public static String getConnectionStringWithoutCredentials(String connectionString) {
        // remove credentials from connection string for host table storage
        String connStringParts[] = connectionString.split(";");
        for (String part: connStringParts){
            if (part.startsWith("u=") || part.startsWith("p=")){
                connStringParts = ArrayUtils.removeElement(connStringParts, part);
            }
        }
        return StringUtils.join(connStringParts, ";");        
    }

    //This method is used to get the list of hosts not in queue
    public List<String> filterHostsAlreadyInQueue(List<String> hostIdList, boolean forceUpdate) {
        try {
            MwHostJpaController mwHostJpaController = My.jpa().mwHost();
            Map<String, Boolean> hostsInQueue = mwHostJpaController.filterHostIdFromMwQueue(hostIdList);
            if (hostsInQueue == null || hostsInQueue.isEmpty()) {
                return hostIdList;
            }
            
            List<String> hostToAddInQueue = new ArrayList();
            for (String hostId : hostIdList) {
                // two conditions to add to queue
                // 1. if host is not currently in queue at all
                if (!hostsInQueue.containsKey(hostId)) {
                    hostToAddInQueue.add(hostId);
                // 2. if host is in queue, user is force updating, and the queue 
                // entry force update is set to false
                } else if (forceUpdate && !hostsInQueue.get(hostId)) {
                    hostToAddInQueue.add(hostId);
                }
            }
            return hostToAddInQueue;
        } catch (Exception ex) {
            log.error("Error while retieving list of hosts to add to flavor verify queue", ex);
        }
        return null;
    }
}
