/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.client.jaxrs;

import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupHostLinkCreateCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupHostLinkLocator;
import com.intel.mtwilson.flavor.rest.v2.model.Host;
import com.intel.mtwilson.flavor.rest.v2.model.HostCollection;
import com.intel.mtwilson.flavor.rest.v2.model.HostCreateCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.HostFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.HostLocator;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * These resources are used to manage hosts.
 * <pre>
 * 
 * A host is a datacenter server. When a host is created, the connection details are specified and it is associated with 
 * a flavor group. The host will be continually monitored against the flavors in the respective flavor group, and the trust 
 * status will be updated accordingly.
 * </pre>
 * @author ssbangal
 * */
public class Hosts extends MtWilsonClient {

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
     * Hosts client = new Hosts(properties);
     * </pre>
     * @throws Exception 
     */
    public Hosts(Properties properties) throws Exception {
        super(properties);
    }

    /**
     * Creates a host.
     * <pre> 
     * A connection string and name for the host must be specified. This name is the value the Host Verification Service 
     * (HVS) uses to keep track of the host. It does not have to be the actual host name or IP address of the server.
     * 
     * If a flavor group is not specified, the host created will be assigned to the default “automatic” flavor 
     * group. If a flavor group is specified and does not already exist, it will be created with a default flavor match 
     * policy.
     * 
     * A TLS policy ID can be specified if one has previously been created (See mtwilson-tls-policy-client-jaxrs2). 
     * Alternatively, if the string text “TRUST_FIRST_CERTIFICATE” is specified, that generic policy will be used (See 
     * the product guide for further description on generic TLS policies). If no TLS policy is provided, the service will 
     * automatically use the default TLS policy specified during HVS installation (See product guide for description on 
     * default TLS policies).
     * 
     * Once the host is created, it is added to the backend queue, flavor verification process.
     * </pre>
     * @param createCriteria The serialized HostCreateCriteria java model object represents the content of the request body.
     * <pre> 
     *          host_name                     HVS name for the host.
     * 
     *          tls_policy_id                 ID of the TLS policy for connection from the HVS to the host.
     * 
     *          connection_string             The host connection string. 
     * 
     *                                        For INTEL & MICROSOFT hosts, this would have the vendor name, the IP addresses, 
     *                                        or DNS host name and credentials.
     *                                        i.e.: "intel:https://trustagent.server.com:1443;u=trustagentUsername;p=
     *                                        trustagentPassword"
     *                                        microsoft:https://trustagent.server.com:1443;u=trustagentUsername;p=
     *                                        trustagentPassword
     * 
     *                                        For VMware, this includes the vCenter and host IP address or DNS host name.
     *                                        i.e.: "vmware:https://vCenterServer.com:443/sdk;h=trustagent.server.com;u=
     *                                        vCenterUsername;p=vCenterPassword"
     * 
     *          flavorgroup_name              Flavor group name that the created host will be associated.
     * 
     *          description                   Host description.
     *</pre>
     * @return <pre>The serialized Host java model object that was created:
     *          id
     *          host_name
     *          description
     *          connection_string
     *          hardware_uuid
     *          tls_policy_id
     *          flavorgroup_name</pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions hosts:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwPreRequisite None
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/hosts
     * Input :
     *  {
     *     "host_name" : "RHEL-Host",
     *     "tls_policy_id" : "a0950923-596b-41f7-b9ad-09f525929ba1",
     *     "connection_string" : "intel:https://trustagent.server.com:1443;u=trustagentUsername;p=trustagentPassword",
     *     "flavorgroup_name" : "samplename123",
     *     "description" : "a new rhel host has been created"
     *  }
     * 
     * Output:
     * {
     *     "id": "feda6821-c41e-4a28-a8b8-df081d7784f9",
     *     "host_name": "RHEL-Host",
     *     "description": "a new rhel host has been created",
     *     "connection_string": "intel:https://trustagent.server.com:1443;u=trustagentUsername;p=trustagentPassword",
     *     "hardware_uuid": "Hardware.uuid",,
     *     "tls_policy_id": "2f83d103-a308-4130-b153-b818eb1479e7"
     *     "flavorgroup_name": "samplename123"
     * }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the host create criteria model and set a host name, connection string and description
     * HostCreateCriteria createCriteria = new HostCreateCriteria();
     * createCriteria.hostName = "RHEL-Host";
     * createCriteria.connectionString = "intel:https://trustagent.server.com:1443;u=trustagentUsername;p=trustagentPassword";
     * createCriteria.description = "a new rhel host has been created";
     * 
     * // Create the client and call the create API
     * Hosts client = new Hosts(properties);
     * Host obj = client.create(createCriteria);
     * </pre></div>
     * */
    public Host create(HostCreateCriteria createCriteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        Host newObj = getTarget().path("hosts").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(createCriteria), Host.class);
        return newObj;
    }

    /**
     * Updates a host.
     * @param host The serialized Host java model object represents the content of the request body or path parameter.
     * <pre> 
     *          id (required)                 Host ID specified as a path parameter.
     * 
     *          host_name                     Complete name of the host.
     * 
     *          hardware_uuid                 Hardware UUID of the host.
     * 
     *          tls_policy_id                 ID of the TLS policy for connection from the HVS to the host.
     * 
     *          connection_string             The host connection string. 
     * 
     *                                        For INTEL & MICROSOFT hosts, this would have the vendor name, the IP addresses, 
     *                                        or DNS host name and credentials.
     *                                        i.e.: "intel:https://trustagent.server.com:1443;u=trustagentUsername;p=
     *                                        trustagentPassword"
     *                                        microsoft:https://trustagent.server.com:1443;u=trustagentUsername;p=
     *                                        trustagentPassword
     * 
     *                                        For VMware, this includes the vCenter and host IP address or DNS host name.
     *                                        i.e.: "vmware:https://vCenterServer.com:443/sdk;h=trustagent.server.com;u=
     *                                        vCenterUsername;p=vCenterPassword"
     * 
     *          flavorgroup_name              Flavor group name that the created flavor(s) will be associated.
     * 
     *          description                   Host description.
     * </pre>  
     * @return <pre>The serialized Host java model object that was updated:
     *          id
     *          host_name
     *          hardware_uuid
     *          tls_policy_id
     *          connection_string
     *          flavorgroup_name
     *          description</pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions hosts:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/hosts/ea26890c-a9aa-4a8d-897f-f252d216e204
     * Input:
     * {
     *     "host_name" : "RHEL-Host",
     *     "hardware_uuid" : "Hardware.uuid",
     *     "connection_string" : "intel:https://trustagent.server.com:1443;u=trustagentUsername;p=trustagentPassword",
     *     "flavorgroup_name" : "samplename123",
     *     "description" : "Host updated."
     * }
     * 
     * Output:
     * {
     *     "id" : "453f8bd3-1c0b-4a6e-82fd-2eb7e8a0b0cd",
     *     "host_name" : "RHEL-Host",
     *     "hardware_uuid" : "Hardware.uuid",
     *     "tls_policy_id" : "a0950923-596b-41f7-b9ad-09f525929ba1",
     *     "connection_string" : "intel:https://trustagent.server.com:1443;u=trustagentUsername;p=trustagentPassword",
     *     "flavorgroup_name" : "samplename123",
     *     "description" : "Host updated."
     * }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the host model and set a host name, connection string and description
     * Host host = new Host();
     * host.setId("ea26890c-a9aa-4a8d-897f-f252d216e204");
     * host.setHostName("RHEL-Host");
     * host.setHardwareUuid("Hardware.uuid");
     * host.setConnectionString("intel:https://trustagent.server.com:1443;u=trustagentUsername;p=trustagentPassword");
     * host.setFlavorgroupNames("samplename123");
     * host.setDescription("Host updated.");
     * 
     * // Create the client and call the update API
     * Hosts client = new Hosts(properties);
     * Host obj = client.update(host);
     * </pre></div>
     * */
    public Host update(Host host) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", host.getId().toString());
        Host newObj = getTarget().path("hosts/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(host), Host.class);
        return newObj;
    }

    /**
     * Retrieves a host.
     * @param locator The content model of the HostLocator java model object can be used as a path parameter.
     * <pre>
     *          id (required)          Host ID specified as a path parameter.
     * </pre>
     * @return <pre>The serialized Host java model object that was retrieved:
     *          id
     *          host_name
     *          connection_string
     *          hardware_uuid
     *          tls_policy_id</pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions hosts:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/hosts/7df1dcc9-31b9-4596-9a38-0a72bb57d7c8 
     * output:
     * {
     *      "id": "52de4b05-4548-455f-b916-5dab4de00131",
     *      "host_name": "RHEL-Host",
     *      "connection_string": "intel:https://trustagent.server.com:1443;u=trustagentUsername;p=trustagentPassword",
     *      "hardware_uuid": "Hardware.uuid",
     *      "tls_policy_id": "2f83d103-a308-4130-b153-b818eb1479e7"
     * }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the host locator model and set the locator id
     * HostLocator locator = new HostLocator();
     * locator.id = UUID.valueOf("52de4b05-4548-455f-b916-5dab4de00131");
     * 
     * // Create the client and call the retrieveHost API
     * Hosts client = new Hosts(properties);
     * Host obj = client.retrieve(locator);
     * </pre></div>
     */    
    public Host retrieve(HostLocator locator) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", locator.id.toString());
        Host obj = getTarget().path("hosts/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(Host.class);
        return obj;
    }

    /**
     * Searches for hosts.
     * @param filterCriteria The content models of the HostFilterCriteria java model object can be used as query parameters.
     * <pre>
     *          filter              Boolean value to indicate whether the response should be filtered to return no 
     *                              results instead of listing all hosts. Default value is true.
     * 
     *          limit               By default this is set to 10. So, only the top 10 results would be returned back.
     * 
     *          id                  Host ID.
     * 
     *          nameEqualTo         Host name. Note that this is the name with which the host is registered.
     * 
     *          nameContains        Substring of host name.
     * 
     *          hostHardwareId      Hardware UUID of host.
     * 
     *          key & value         User needs to specify values for both key and value fields. Keys can be any field in host info 
     *                              section of host report field in host status table.
     *                              The host_name here refers to the actual name of the host
     *                              that is configured on the system, not the one that is used to register the host.
     * 
     * Only one identifying parameter can be specified. The parameters listed here are in the order of priority that will be evaluated.
     * </pre>
     * @return <pre>The serialized HostCollection java model object that was searched with list of hosts each containing:
     *          id
     *          host_name
     *          connection_string
     *          hardware_uuid
     *          tls_policy_id</pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions hosts:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/hosts?hostHardwareId=Hardware.uuid
     * output:
     * {
     *     "hosts": [
     *         {
     *             "id": "f271d192-4efe-43ad-994c-4fa46459ac57",
     *             "host_name": "RHEL-Host",
     *             "description": "",
     *             "connection_string": "intel:https://trustagent.server.com:1443;u=trustagentUsername;p=trustagentPassword",
     *             "hardware_uuid": "Hardware.uuid",
     *             "tls_policy_id": "e590cee6-fd7f-41c5-8968-3f1d4a5b22c5"
     *         }
     *     ]
     * }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the host filter criteria model and set the hardware UUID
     * HostFilterCriteria filterCriteria = new HostFilterCriteria();
     * filterCriteria.hostHardwareId = UUID.valueOf("Hardware.uuid");
     * 
     * // Create the client and call the search API
     * Hosts client = new Hosts(properties);
     * HostCollection obj = client.search(filterCriteria);
     * </pre></div>
     * */
    public HostCollection search(HostFilterCriteria filterCriteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HostCollection objCollection = getTargetPathWithQueryParams("hosts", filterCriteria).request(MediaType.APPLICATION_JSON).get(HostCollection.class);
        return objCollection;
    }

    /**
     * Deletes a host.
     * @param id ID of host
     * @since ISecL 1.0
     * @mtwRequiresPermissions hosts:delete
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/hosts/21f7d831-85b3-46bc-a499-c2d14ff136c8
     * output: 204 No content
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create a string with host id
     * String id = "52de4b05-4548-455f-b916-5dab4de00131";
     * 
     * // Create the client and call the delete API
     * Hosts client = new Hosts(properties);
     * Host obj = client.delete(id);
     * </pre></div>
     * */
    public void delete(String id) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", id);
        Response obj = getTarget().path("hosts/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }

//    public void deleteHost(HostFilterCriteria criteria) {
//        log.debug("target: {}", getTarget().getUri().toString());
//    }

    /**
     * Associates the host with the flavorgroup specified in FlavorgroupHostLinkCreateCriteria java model object
     * @param hostId ID of the host to be linked to the flavorgroup
     * @param createCriteria The serialized FlavorgroupHostLinkCreateCriteria java model object represents the content of the request body.
     * <pre>
     *          flavorgroupName            Flavor group name to be linked to host.
     * </pre>
     * @since ISecL 1.0
     * @mtwMethodType POST
     * @mtwPreRequisite hosts create API
     * @mtwSampleRestCall      
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/hosts/01b9f1bc-a17d-465d-83fc-3df5986a656c/flavorgroups
     * Input :
     *  {
     *      "flavorgroupName" : "samplegroup123"
     *  }
     * 
     * Output : 204 No content
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the flavor group host link create criteria model and set the create criteria
     * FlavorgroupHostLinkCreateCriteria createCriteria = new FlavorgroupHostLinkCreateCriteria();
     * createCriteria.setFlavorgroupName("samplegroup123");
     * 
     * // Create the client and call the create API
     * Hosts client = new Hosts(properties);
     * client.createFlavorgroupHostLink(createCriteria);
     * </pre></div>
     */
    public void createFlavorgroupHostLink(String hostId, FlavorgroupHostLinkCreateCriteria createCriteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("hostId", hostId);
        getTarget().path("hosts/{hostId}/flavorgroups").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).post(Entity.json(createCriteria));
    }

    /**
     * Deletes the link between the host and the flavorgroup
     * @param hostId ID of the host that need to be dissociated from the flavorgroup
     * @param flavorgroupId Flavor group ID that need to be dissociated from host 
     * @since ISecL 1.0
     * @mtwMethodType DELETE
     * @mtwPreRequisite Hosts create API, Flavorgroup-Host link create API
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/hosts/01b9f1bc-a17d-465d-83fc-3df5986a656c/flavorgroups/01b9f1bc-a17d-465d-83fc-1537947354963320
     * Output: 204 No content
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the flavor group host link locator model and set the locator id
     * FlavorgroupHostLinkLocator locator = new FlavorgroupHostLinkLocator();
     * locator.pathId(UUID.valueOf("01b9f1bc-a17d-465d-83fc-1537947354963320"));
     * 
     * // Create the client and call the delete API
     * Hosts client = new Hosts(properties);
     * client.deleteFlavorgroupHostAssociation("01b9f1bc-a17d-465d-83fc-3df5986a656c", locator);
     * </pre></div>
     */
    public void deleteFlavorgroupHostAssociation(String hostId, String flavorgroupId) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("hostId", hostId);
        map.put("flavorgroupId", flavorgroupId);
        getTarget().path("hosts/{hostId}/flavorgroups/{flavorgroupId}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
    }
}
