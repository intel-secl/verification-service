/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.esxi.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.mtwilson.core.host.connector.vmware.VMwareClient;
import com.intel.mtwilson.core.host.connector.vmware.VMwareConnectionPool;
import com.intel.mtwilson.core.host.connector.vmware.VmwareClientFactory;
import com.intel.mtwilson.esxi.cluster.jdbi.*;
import com.intel.mtwilson.esxi.host.jdbi.*;
import com.intel.mtwilson.esxi.rest.v2.model.EsxiCluster;
import com.intel.mtwilson.esxi.rest.v2.model.EsxiClusterCollection;
import com.intel.mtwilson.esxi.rest.v2.model.EsxiClusterFilterCriteria;
import com.intel.mtwilson.esxi.rest.v2.model.EsxiClusterLocator;
import com.intel.mtwilson.flavor.rest.v2.model.HostCreateCriteria;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.intel.mtwilson.flavor.rest.v2.resource.HostResource;
import com.intel.mtwilson.flavor.rest.v2.repository.HostRepository;
import com.intel.mtwilson.flavor.rest.v2.model.Host;
import com.intel.mtwilson.flavor.rest.v2.model.HostCollection;
import com.intel.mtwilson.flavor.rest.v2.model.HostFilterCriteria;
import com.intel.mtwilson.core.common.datatypes.ConnectionString;
import com.intel.mtwilson.core.common.datatypes.Vendor;
import com.intel.mtwilson.core.common.model.HostInfo;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.tls.policy.exception.TlsPolicyAllowedException;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyFactoryUtil;
import com.intel.mtwilson.tls.policy.filter.HostTlsPolicyFilter;

/**
 *
 * @author avaguayo + hxia5
 */
public class EsxiClusterRepository implements DocumentRepository<EsxiCluster, EsxiClusterCollection, EsxiClusterFilterCriteria, EsxiClusterLocator> {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EsxiClusterRepository.class);
    
    @Override
    @RequiresPermissions("esxi_clusters:search")  
    public EsxiClusterCollection search(EsxiClusterFilterCriteria criteria) {
        log.debug("EsxiClusterCollection:Search - Got request to search for cluster info"); 
        
        EsxiClusterCollection objCollection = new EsxiClusterCollection();
            EsxiClusterDAO clusterDAO = EsxiClusterJdbiFactory.esxiClusterDAO();
            
            EsxiHostDAO hostDAO = EsxiHostJdbiFactory.esxiHostDAO();
            
            if(criteria.filter) {
                if(criteria.id != null) {
                   EsxiClusterRecord cluster = clusterDAO.findEsxiClusterById(criteria.id);
                   
                   if(cluster!=null) {
                       List<EsxiHostRecord> hosts = hostDAO.findEsxiHostsByClusterId(criteria.id);
                       EsxiCluster obj = getClusterDetails(hosts, cluster);
                       objCollection.getEsxiClusters().add(obj);
                   }
                }
                
                else if(criteria.clustername != null) {
                    
                    EsxiClusterRecord cluster = clusterDAO.findEsxiClusterByName(criteria.clustername);
                    
                     if(cluster!=null) {
                       List<EsxiHostRecord> hosts = hostDAO.findEsxiHostsByClusterId(clusterDAO.findEsxiClusterByName(criteria.clustername).getId());
                       EsxiCluster obj = getClusterDetails(hosts, cluster);
                       objCollection.getEsxiClusters().add(obj);
                     }
                }
                else {
                  
                    List<EsxiClusterRecord> clusters = clusterDAO.findAllEsxiCluster();
                    
                    for(EsxiClusterRecord record : clusters) {
                        List<EsxiHostRecord> hosts = hostDAO.findEsxiHostsByClusterId(record.getId());
                        EsxiCluster obj = getClusterDetails(hosts, record);
                        objCollection.getEsxiClusters().add(obj);
                    }
                    
                }
              }
            
            return objCollection;
            
        }
    
    @Override
    @RequiresPermissions("esxi_clusters:create")
    public void create(EsxiCluster item) {
        log.debug("EsxiCluster:Store - Got request to create cluster  with id {}.", item.getId().toString()); 
        
        try(EsxiClusterDAO clusterDAO = EsxiClusterJdbiFactory.esxiClusterDAO()) {
            
            //add the cluster only if it does not exist
            EsxiClusterRecord clusterRecord = clusterDAO.findEsxiClusterByName(item.getClusterName());
            if(clusterRecord == null) {
                
                // determine TLS policy
                ConnectionString connectString = new ConnectionString(item.getConnectionString() + ";h=na"); // the cluster connection string will not contain a particular host name. this is avoid the error where ConnectionString expects a h= options here
                Vendor vendorName = connectString.getVendor();
                if (!vendorName.name().equalsIgnoreCase("vmware")) {
                    throw new RepositoryInvalidInputException("Connection string is not vwmare");
                }
                TlsPolicyDescriptor tlsPolicyDescriptor = new HostResource().getTlsPolicy(item.getTlsPolicyId(), connectString, false);
                VMwareClient client = getVmwareClient(connectString, tlsPolicyDescriptor);
                List<String> clusterNames = getClusterNames(client); 
                
                for(String clusterName : clusterNames) {
                    //only add the requested cluster
                    if (clusterName.equals(item.getClusterName())) {
                        EsxiClusterRecord ecr = new EsxiClusterRecord();
                        ecr.setId(item.getId());
                        ecr.setConnectionStringInPlainText(item.getConnectionString());
                        ecr.setClusterName(clusterName);
                        ecr.setTlsPolicyId(item.getTlsPolicyId());

                        clusterDAO.insertEsxiCluster(ecr);
                        log.debug("New cluster {} added", clusterName);

                        // Insert also the esxi hosts associated 
                        try(EsxiHostDAO hostDAO = EsxiHostJdbiFactory.esxiHostDAO()) {

                            List<EsxiHostRecord> hosts = hostDAO.findEsxiHostsByClusterId(clusterDAO.findEsxiClusterByName(clusterName).getId());                            
                            HashMap<String, HostInfo> vcenterHosts = getVcenterHosts(client, clusterName);
                            
                            //add the hosts that are new
                            List<String> hostsToAdd = getHostsToAdd(hosts, vcenterHosts);
                            if (hostsToAdd != null && !hostsToAdd.isEmpty())
                                for(String hostname : hostsToAdd) {
                                    EsxiHostRecord ehr = new EsxiHostRecord();
                                    ehr.setId(new UUID());
                                    ehr.setClusterId(clusterDAO.findEsxiClusterByName(clusterName).getId());
                                    ehr.setHostname(hostname);
                                    // Should I run the register host process here?
                                    hostDAO.insertEsxiHost(ehr);
                                    createHostAS(ecr, hostname);
                                }
                            //remove existing hosts if they are not in the cluster anymore
                            List<String> hostsToRemove = getHostsToRemove(hosts, vcenterHosts);
                            if (hostsToRemove !=null && !hostsToRemove.isEmpty()) {
                                for(String hostname : hostsToRemove) {                                                                   
                                    hostDAO.deleteEsxiHostByHostname(hostname);
                                    deleteHostAS(clusterName, hostname);
                                } 
                            }                            
                        }
                    }
                }   
            }
            else {
                // this cluster already exist. just return the cluster with its id
                item.setId(clusterRecord.getId());
            }
        }
        catch(Exception ex) {
            log.error("Error during Esxi cluster creation", ex);
            throw new RepositoryCreateException(ex);
        }
    }
    
    @Override
    @RequiresPermissions("esxi_clusters:retrieve")  
    public EsxiCluster retrieve(EsxiClusterLocator locator) {
        if (locator == null || locator.id == null) { return null;}
        log.debug("EsxiCluster:Retrieve - Got request to retrieve the cluster details with id {}.", locator.id.toString());        
        
        try(EsxiClusterDAO clusterDAO = EsxiClusterJdbiFactory.esxiClusterDAO()) {
            EsxiClusterRecord cluster = clusterDAO.findEsxiClusterById(locator.id);
                   
            if(cluster!=null) {
                try(EsxiHostDAO hostDAO = EsxiHostJdbiFactory.esxiHostDAO()) {
                    List<EsxiHostRecord> hosts = hostDAO.findEsxiHostsByClusterId(locator.id);
                    EsxiCluster obj = getClusterDetails(hosts, cluster);
                    return obj;
                }
            }
        }
        catch (Exception ex) {
            log.error("EsxiCluster:Retrieve - Error during retrieval of cluster details from cache.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }
        return null;
    }
    
    @Override
    @RequiresPermissions("esxi_clusters:store")  
    public void store(EsxiCluster item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    @RequiresPermissions("esxi_clusters:delete")  
    public void delete(EsxiClusterLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        log.debug("EsxiCluster:Delete - Got request to delete esxi cluster with id {}.", locator.id.toString());        
        
        try(EsxiClusterDAO clusterDAO = EsxiClusterJdbiFactory.esxiClusterDAO()) {
            
            EsxiClusterRecord clusterRecord = clusterDAO.findEsxiClusterById(locator.id);
            if(clusterRecord != null) {
                
                // First we attempt to delete the associated hosts
                 try(EsxiHostDAO hostDAO = EsxiHostJdbiFactory.esxiHostDAO()) {
                    
                    List<EsxiHostRecord> hosts = hostDAO.findEsxiHostsByClusterId(locator.id);
                    if(hosts != null) {
                        for(EsxiHostRecord record : hosts) {
                            hostDAO.deleteEsxiHostById(record.getId());
                            deleteHostAS(clusterRecord.getClusterName(), record.getHostname()); //unregister the host
                        }
                    }
                    else {
                        log.info("EsxiCluster:Delete - No associated hosts to delete were found in the system.");
                    }
                 }
                 catch(Exception ex) {
                     log.error("Error on Esxi host deletion for cluster with id {}", locator.id);
                     throw new RepositoryDeleteException(ex, locator);
                 }
                 // Now we delete the cluster also
                 clusterDAO.deleteEsxiClusterById(locator.id);
            }
            else {
                log.info("EsxiCluster:Delete - Cluster does not exist in the system.");
            }
            
            
        
        }
        catch(Exception ex) {
            log.error("Error on Esxi cluster deletion with id {}", locator.id);
            throw new RepositoryDeleteException(ex, locator);
        }
        
    }
    
    @Override
    @RequiresPermissions("esxi_clusters:delete,search")  
    public void delete(EsxiClusterFilterCriteria criteria) {
        log.debug("EsxiCluster:Delete - Got request to delete esxi cluster by search criteria");
        
        try {
            EsxiClusterCollection objList = search(criteria);
            for(EsxiCluster obj : objList.getEsxiClusters()) {
                // First we attempt to delete the associated hosts
                 try(EsxiHostDAO hostDAO = EsxiHostJdbiFactory.esxiHostDAO()) {
                    
                    List<EsxiHostRecord> hosts = hostDAO.findEsxiHostsByClusterId(obj.getId());
                    if(hosts != null) {
                        for(EsxiHostRecord record : hosts) {
                            hostDAO.deleteEsxiHostById(record.getId());
                            deleteHostAS(obj.getClusterName(), record.getHostname()); //unregister the host
                        }                           
                    }
                    else {
                        log.info("EsxiCluster:Delete - No associated hosts to delete were found in the system.");
                    }
                 }
                 catch(Exception ex) {
                     log.error("Error on Esxi host deletion for cluster with id {}", obj.getId());
                     throw new RepositoryDeleteException(ex);
                 }
                EsxiClusterDAO clusterDAO = EsxiClusterJdbiFactory.esxiClusterDAO();
                clusterDAO.deleteEsxiClusterById(obj.getId());
            }
        }
        catch(Exception ex) {
            log.error("Error on Esxi cluster deletion by filter criteria");
            throw new RepositoryDeleteException(ex);
        }
        
    }
    
    private List<String> getClusterNames(VMwareClient client) {
        
        List<String> clusternames = new ArrayList<String>();
        
        try {
            List<String> longNames = client.getClusterNamesWithDC();
            for(String longname: longNames) {
                log.debug("cluster name found: " + longname.substring(longname.indexOf("]")+1).trim());
                clusternames.add(longname.substring(longname.indexOf("]")+1).trim());
                
            }
        }
        catch(Exception ex) {
            log.error("Error when getting cluster names - failed to get names", ex);
            throw new RepositoryCreateException(ex);
        }
        
        return clusternames;
    }
    
    private VMwareClient getVmwareClient(ConnectionString connectionString, TlsPolicyDescriptor tlsPolicyDescriptor) { 
        
        VMwareClient client;
        
        try {
            if (tlsPolicyDescriptor == null) {
                throw new IllegalArgumentException("Cannot determine appropriate TLS policy for the VMware cluster");
            }
            // check if the tlsPolicyDescriptor is allowed. Throw error if not allowed.
            if (!HostTlsPolicyFilter.isTlsPolicyAllowed(tlsPolicyDescriptor.getPolicyType())) {
                log.error("TLS policy {} is not allowed", tlsPolicyDescriptor.getPolicyType());
                throw new TlsPolicyAllowedException("TLS policy is not allowed");
            }
            
            TlsPolicy vCenterTlsPolicy = TlsPolicyFactoryUtil.createTlsPolicy(tlsPolicyDescriptor);            
            // get hosts directly from esxi server
            VMwareConnectionPool pool = new VMwareConnectionPool(new VmwareClientFactory());
            client = pool.getClientForConnection(new TlsConnection(connectionString.getURL(), vCenterTlsPolicy));    
        } catch(Exception ex) {
            log.error("Error at Vmware client retrieval  - failed to create client", ex);
            throw new RepositoryCreateException(ex);
        }
        
        return client;
    }
    
    private HashMap<String, HostInfo> getVcenterHosts(VMwareClient client, String clusterName) {
        HashMap<String, HostInfo> hostMap = new HashMap();
        try {
            ArrayList<HostInfo> hostList = client.getHostNamesForCluster(clusterName);
            for (HostInfo record : hostList) {
                hostMap.put(record.getHostName(), record);
            }
        } catch (Exception ex) {
            log.error("Error during Esxi hosts insertion - failed to get hosts", ex);
            throw new RepositoryCreateException(ex);
        }
        return hostMap;
    }
    
    private EsxiCluster getClusterDetails(List<EsxiHostRecord> hosts, EsxiClusterRecord cluster ) {
       List<String> hostnames = new ArrayList<String>();

       log.debug("hosts.size :" + hosts.size());
       for(EsxiHostRecord record: hosts) {
           log.debug("hostname: " + record.getHostname() + " with cluster id = " + record.getClusterId() );
            hostnames.add(record.getHostname());
        }

       EsxiCluster obj = new EsxiCluster();
       obj.setId(cluster.getId());
       obj.setConnectionString(cluster.getConnectionStringInPlainText());
       obj.setClusterName(cluster.getClusterName());
       obj.getHosts().put("hosts", hostnames);
       
       return obj;
        
    }            
    
    /*
     * Compare the two lists to find list of new hosts to be added in the host table.
     * If a host in the newHosts does not exist in the existingHosts, remove it
     */
    private List<String> getHostsToAdd(List<EsxiHostRecord> existingHosts, HashMap<String, HostInfo> newHosts) {
        List<String> hostsToBeAdded = new ArrayList();
        HashSet<String> existingHostList = new HashSet();
        
        //convert the existing hosts list to a set
        if (existingHosts != null && !existingHosts.isEmpty()) {
            for (EsxiHostRecord record : existingHosts) {
                existingHostList.add(record.getHostname());
            }
        }
        
        if (newHosts != null) {
            for (Map.Entry<String, HostInfo> entry : newHosts.entrySet()) {
                if (!existingHostList.contains(entry.getValue().getHostName())) {
                    hostsToBeAdded.add(entry.getValue().getHostName());
                }
            }
        }
        return hostsToBeAdded;
    }
    
    /*
     * Compare the two lists to find list of hosts to be removed in the host table. 
     * If a host in the newHosts does not exist in the existingHosts, remove it
     */
    private List<String> getHostsToRemove(List<EsxiHostRecord> existingHosts, HashMap<String, HostInfo> newHosts) {
        List<String> hostsTobeRemoved = new ArrayList();
        HashSet<String> refreshedHostList = new HashSet();
        
        //convert the newHosts to a set
        if (newHosts != null) {
            for (Map.Entry<String, HostInfo> entry : newHosts.entrySet()) {
                refreshedHostList.add(entry.getValue().getHostName());
            }
        }
        
        if (existingHosts != null && !existingHosts.isEmpty()) {
            for (EsxiHostRecord record : existingHosts) {
                //put an existing host to the removeList if it is not in the refreshed host list
                if (!refreshedHostList.contains(record.getHostname())) {
                    hostsTobeRemoved.add(record.getHostname());
                }
            }
        }
        return hostsTobeRemoved;
    }
    
    /*  update the the list of hosts in the VMware clusters by 
     *  1)get new list of hosts from the registered clusters 
     *  2)add newly founded host from the vcenter cluster to the database
     *  3)remove host from the database if the host does not exist from the cluster 
     */
    public void updateHostsFromClusters() {
        log.debug("EsxiCluster:update- update the host lists in ESXi clusters");
        try (EsxiClusterDAO clusterDAO = EsxiClusterJdbiFactory.esxiClusterDAO()) {
            //find the list of clusters
            List<EsxiClusterRecord> clusterList = clusterDAO.findAllEsxiCluster();
            if (clusterList != null && !clusterList.isEmpty()) {
                for (EsxiClusterRecord clusterItem : clusterList) {
                    try (EsxiHostDAO hostDAO = EsxiHostJdbiFactory.esxiHostDAO()) {
                        // find the list of hosts in the cluster in the database
                        List<EsxiHostRecord> hosts = hostDAO.findEsxiHostsByClusterId(clusterItem.getId());
                        //find the list of hosts in the cluster from VCenter
                        // determine TLS policy
                        ConnectionString connectString = new ConnectionString(clusterItem.getConnectionStringInPlainText() + ";h=na"); // the cluster connection string will not contain a particular host name. this is avoid the error where ConnectionString expects a h= options here
                        Vendor vendorName = connectString.getVendor();
                        if (!vendorName.name().equalsIgnoreCase("vmware")) {
                            throw new RepositoryInvalidInputException("Connection string is not vwmare");
                        }
                        TlsPolicyDescriptor tlsPolicyDescriptor = new HostResource().getTlsPolicy(clusterItem.getTlsPolicyId(), connectString, false);
                        VMwareClient client = getVmwareClient(connectString, tlsPolicyDescriptor);
                        HashMap<String, HostInfo> vcenterHosts = getVcenterHosts(client, clusterItem.getClusterName());
                        //find the newly added hosts
                        List<String> hostsToAdd = getHostsToAdd(hosts, vcenterHosts);
                        //add the new hosts into table
                        if (hostsToAdd != null && !hostsToAdd.isEmpty()) {
                            for (String hostname : hostsToAdd) {
                                EsxiHostRecord ehr = new EsxiHostRecord();
                                ehr.setId(new UUID());
                                ehr.setClusterId(clusterItem.getId());
                                ehr.setHostname(hostname);
                                hostDAO.insertEsxiHost(ehr);
                                createHostAS(clusterItem, hostname);
                            }
                        }
                        //remove a host in the table if the host does not belong to the cluster
                        List<String> hostsToRemove = getHostsToRemove(hosts, vcenterHosts);
                        if (hostsToRemove != null && !hostsToRemove.isEmpty()) {
                            for (String hostname : hostsToRemove) {
                                hostDAO.deleteEsxiHostByHostname(hostname);
                                deleteHostAS(clusterItem.getClusterName(), hostname);
                            }
                        }
                    }
                }
            } else {
                log.debug("EsxiCluster:updateHosts - no clusters found in the table");
            }
        } catch (Exception ex) {
            log.error("Error during Esxi cluster host update", ex);
            throw new RepositoryCreateException(ex);
        }
    }
    
    // register the host for attestation
    public void createHostAS(EsxiClusterRecord clusterRecord, String hostname) {
        HostCreateCriteria newHostCriteria = new HostCreateCriteria();
        HostResource hostOps = new HostResource();
        
        //String connStr = "vmware:"+ clusterRecord.getConnectionStringInPlainText()+";h="+hostname;
        String connStr = clusterRecord.getConnectionStringInPlainText()+";h="+hostname;
        log.debug("Added new ESXi host with connection_string: {}", connStr);
        
        newHostCriteria.setConnectionString(connStr);
        newHostCriteria.setHostName(hostname);
        newHostCriteria.setDescription(hostname+" in ESX cluster "+clusterRecord.getClusterName());
        newHostCriteria.setTlsPolicyId(clusterRecord.getTlsPolicyId());
        
        try {
            hostOps.createHost(newHostCriteria);
            log.debug("Added new ESXi host {} from cluster {}", hostname, clusterRecord.getClusterName());
        } catch (Exception ex) {
            log.error("Excpection: {}", ex);
            log.error("Error during host creation for vmware host {}", hostname);
        }
    }
    
    // remove the host from attestation
    public void deleteHostAS(String clusterName, String hostname) {
        HostRepository asHostRepository = new HostRepository();
        
        //first find the host based on its name
        HostFilterCriteria hostFilterCriteria = new HostFilterCriteria();
        hostFilterCriteria.nameEqualTo = hostname;
        HostCollection hostConnection = asHostRepository.search(hostFilterCriteria);
        List<Host> hostList = hostConnection.getHosts();
        
        //remove the host based on the hostId
        if (hostList != null) {
            for (Host aHost: hostList) {
                if (aHost != null) {
                    try {
                        asHostRepository.deleteByName(aHost.getHostName());
                        log.debug("ESXi host {} from cluster {} deleted", hostname, clusterName);
                    }
                    catch (Exception ex) {
                        log.error("Excpection: {}", ex);
                        log.error("Error during host delete for vmware host {}", hostname);
                    }
                    break;
                }
            }
        }
    }
}