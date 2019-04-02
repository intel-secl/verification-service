/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.esxiclusters.client.jaxrs2;

import com.intel.mtwilson.esxi.rest.v2.model.EsxiCluster;
import com.intel.mtwilson.esxi.rest.v2.model.EsxiClusterCollection;
import com.intel.mtwilson.esxi.rest.v2.model.EsxiClusterFilterCriteria;
import com.intel.mtwilson.esxi.rest.v2.model.EsxiClusterLocator;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * These resources are used to register and manage ESXi clusters.
 * <pre>
 * This API resource allows entire Clusters of VMWare  ESXi hosts to be managed 
 * as a group, using the vCenter Cluster object. When a Cluster is registered to 
 * the Host Verification Service, the Host Verification Service will automatically mirror 
 * the Cluster object in vCenter, automatically registering any ESXi hosts currently 
 * in the Cluster in vCenter.  As additional ESXi hosts are added or removed from the 
 * Cluster object in vCenter, the Host Verification Service will also register or remove 
 * the ESXi hosts from its own database.
 * 
 * This API is only used for registering VMware clusters.
 * </pre>
 * @author hxia5
 * */
public class EsxiClusters extends MtWilsonClient {

    Logger log = LoggerFactory.getLogger(getClass().getName());
    
    /**
     * Constructor.
     * 
     * @param properties This java properties model must include server connection details for the API client initialization.
     * <pre>
     * mtwilson.api.url - Host Verification Service (HVS) base URL for accessing REST APIs
     * 
     * // basic authentication
     * mtwilson.api.username - Username for API basic authentication with the HVS
     * mtwilson.api.password - Password for API basic authentication with the HVS
     * 
     * <b>Example:</b>
     * Properties properties = new Properties();
     * properties.put(“mtwilson.api.url”, “https://server.com:port/mtwilson/v2”);
     * 
     * // basic authentication
     * properties.put(“mtwilson.api.username”, “user”);
     * properties.put(“mtwilson.api.password”, “*****”);
     * properties.put("mtwilson.api.tls.policy.certificate.sha256", "bfc4884d748eff5304f326f34a986c0b3ff0b3b08eec281e6d08815fafdb8b02");
     * EsxiClusters client = new EsxiClusters(properties);
     * </pre>
     * @throws Exception 
     */
    public EsxiClusters(Properties properties) throws Exception {
        super(properties);
    }

    /**
     * Creates ESXi Clusters.
     * <pre>
     * The method will register specified ESXi cluster as well as all the hosts 
     * associated with the cluster in vCenter.
     * </pre>
     * @param esxiClusterCollection The serialized EsxiClusterCollection java model object represents the content of the request body.
     * <pre> 
     *          cluster_name (required)         Name of the vCenter cluster. The name needs to be exactly as it appears in vCenter.
     * 
     *          tls_policy_id (optional)        Tls Policy ID created in hvs for specified vCenter. 
     *                                          (TRUST_FIRST_CERTIFICATE policy is accepted if it's allowed.)
     * 
     *          connection_string (required)    Connection string in the following format: 
     *                                          "vmware:https://vCenterServer.com:443/sdk;u=vCenterUsername;p=vCenterPassword"
     * </pre>
     * @return <pre>The serialized EsxiClusterCollection java model object that was searched:
     *          id
     *          connection_string
     *          cluster_name
     *          tls_policy_id
     *          hosts</pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions esxi_clusters:create
     * @mtwContentType application/vnd.api+json
     * @mtwContentTypeReturned application/vnd.api+json
     * @mtwMethodType POST
     * @mtwPreRequisite None
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/esxi-cluster
     * Input :
     * {
     *    "connection_string": "vmware:https://vCenterServer.com:443/sdk;h=trustagent.
     *                          server.com;u=vCenterUsername;p=vCenterPassword",
     *    "cluster_name": "vCenter.cluster.name",
     *    "tls_policy_id": "TRUST_FIRST_CERTIFICATE"
     * }
     *
     * Output :
     * {
     *    "id": "3513b52d-21cd-4227-86c9-1d5fdda18533",
     *    "connection_string": "vmware:https://vCenterServer.com:443/sdk;h=trustagent.
     *                          server.com;u=vCenterUsername;p=vCenterPassword",
     *    "cluster_name": "vCenter.cluster.name",
     *    "tls_policy_id": "TRUST_FIRST_CERTIFICATE"
     * }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the ESXi cluster collection model and set the create parameters
     * EsxiClusterCollection esxiClusterCollection = new EsxiClusterCollection();
     * esxiClusterCollection.connection_string = "vmware:https://vCenterServer.com:443/
     *                   sdk;h=trustagent.server.com;u=vCenterUsername;p=vCenterPassword";
     * esxiClusterCollection.cluster_name = "vCenter.cluster.name";
     * esxiClusterCollection.tls_policy_id = "TRUST_FIRST_CERTIFICATE";
     * 
     * // Create the client and call the create API
     * EsxiClusters client = new EsxiClusters(properties);
     * EsxiClusterCollection esxiClusterCollection = client.create(esxiClusterCollection);
     * </pre></div>
     * */
    public EsxiClusterCollection create(EsxiClusterCollection esxiClusterCollection) {
        log.debug("target: {}", getTarget().getUri().toString());
        EsxiClusterCollection newObj = getTarget().path("esxi-cluster").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(esxiClusterCollection), EsxiClusterCollection.class);
        return newObj;
    }
    
    /**
     * Retrieves ESXi clusters.
     * @param locator The content models of the EsxiClusterLocator java model object can be used as path parameter.
     * <pre>
     *              id (required)         ESXi cluster ID specified as a path parameter.
     * </pre>
     * @return <pre>The serialized EsxiCluster java model object that was retrieved:
     *          id
     *          connection_string
     *          cluster_name
     *          tls_policy_id
     *          hosts</pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions esxi_clusters:retrieve
     * @mtwContentTypeReturned application/vnd.api+json
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/esxi-cluster/2d026d64-ec08-4406-8a2d-3f90f2addd5e
     * 
     * Output:
     * {
     *       "id": "e110cf4d-6bd0-4b6f-88d1-e8d326ff1a87",
     *       "connection_string": "mUYVj3BTwl+kmXWsd4....",
     *       "cluster_name": "vCenter.cluster.name",
     *       "tls_policy_id": "TRUST_FIRST_CERTIFICATE",
     *       "hosts": {
     *           "hosts": [
     *               "host.ip"
     *           ]
     *       }
     *   }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the host ESXi cluster locator model and set the locator id
     * EsxiClusterLocator locator = new EsxiClusterLocator();
     * locator.id = UUID.valueOf("21f7d831-85b3-46bc-a499-c2d14ff136c8");
     * 
     * // Create the client and call the retrieve API
     * EsxiClusters clusters = new EsxiClusters(properties);
     * EsxiCluster cluster = clusters.retrieve(locator);
     * </pre></div>
    */
    public EsxiCluster retrieve(EsxiClusterLocator locator) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", locator.id.toString());
        EsxiCluster obj = getTarget().path("esxi-cluster/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(EsxiCluster.class);
        return obj;
    }    
    /**
     * Searches for ESXi clusters.
     * @param filterCriteria The content models of the EsxiClusterFilterCriteria java model object can be used as query parameters.
     * <pre> 
     *          id                  UUID of the ESXi cluster
     * 
     *          cluster_name        vCenter cluster name
     * 
     * If no query parameter is provided, the query will retrieve all registered esxi clusters.
     * 
     * Only one identifying parameter can be specified. The parameters listed here are in the order of priority that will be evaluated. 
     * Identifying parameters include id, cluster_name.
     * </pre>
     * @return <pre>The serialized EsxiClusterCollection java model object that was searched:
     *          id
     *          connection_string
     *          cluster_name
     *          tls_policy_id
     *          hosts</pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions esxi_clusters:search
     * @mtwContentTypeReturned application/vnd.api+json
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/esxi-cluster?cluster_name=vCenter.cluster.name
     * 
     * Output :
     *   {
     *       "esxi_clusters": [
     *           {
     *               "id": "e110cf4d-6bd0-4b6f-88d1-e8d326ff1a87",
     *               "connection_string": "mUYVj3BTwl+kmXWsd4I/I3TRFeXH88ZF8PEf/B....",
     *               "cluster_name": "vCenter.cluster.name",
     *               "tls_policy_id": "TRUST_FIRST_CERTIFICATE",
     *               "hosts": {
     *                   "hosts": [
     *                       "host.ip"
     *                   ]
     *               }
     *           }
     *       ]
     *   }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the ESXi cluster filter criteria model and set the search criteria
     * EsxiClusterFilterCriteria filterCriteria = new EsxiClusterFilterCriteria();
     * filterCriteria.cluster_name = "css_attestation";
     * 
     * // Create the client and call the search API
     * EsxiClusters client = new EsxiClusters(properties);
     * EsxiClusterCollection esxiClusterCollection = client.search(filterCriteria);
     * </pre></div>
     */
    public EsxiClusterCollection search(EsxiClusterFilterCriteria filterCriteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        EsxiClusterCollection objCollection = getTargetPathWithQueryParams("esxi-cluster", filterCriteria).request(MediaType.APPLICATION_JSON).get(EsxiClusterCollection.class);
        return objCollection;
    }
     
    /**
     * Deletes a ESXi cluster.
     * <pre>
     * Deleting cluster would delete all the hosts associated with that cluster in vCenter.
     * </pre>
     * @param locator The content models of the EsxiClusterLocator java model object can be used as path parameter.
     * <pre>
     *              id (required)         ESXi cluster ID specified as a path parameter.
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions esxi_clusters:delete
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/esxi-cluster/e43424ca-9e00-4cb9-b038-9259d0307888
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the host ESXi cluster locator model and set the locator id
     * EsxiClusterLocator locator = new EsxiClusterLocator();
     * locator.id = UUID.valueOf("21f7d831-85b3-46bc-a499-c2d14ff136c8");
     * 
     * // Create the client and call the delete API
     * EsxiClusters client = new EsxiClusters(properties);  
     * client.delete(locator);
     * </pre></div>
     */
    public void delete(EsxiClusterLocator locator) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", locator.id.toString());
        Response obj = getTarget().path("esxi-cluster/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    } 
}
