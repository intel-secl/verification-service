/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.client.jaxrs;

import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.flavor.rest.v2.model.*;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.intel.wml.manifest.xml.Manifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * These resources are used to manage flavors.
 * <pre>
 * 
 * A flavor is a set of measurements and metadata organized in a flexible format that allows for ease of further extension. The 
 * measurements included in the flavor pertain to various hardware, software and feature categories, and their respective metadata 
 * sections provide descriptive information.
 * 
 * The four current flavor categories: (BIOS is deprecated)
 * PLATFORM, OS, ASSET_TAG, HOST_UNIQUE (See the product guide for a detailed explanation)
 *
 * When a flavor is created, it is associated with a flavor group. This means that the measurements for that flavor type are deemed 
 * acceptable to obtain a trusted status. If a host, associated with the same flavor group, matches the measurements contained within 
 * that flavor, the host is trusted for that particular flavor category (dependent on the flavor group policy).
 * </pre>
 */
public class Flavors extends MtWilsonClient {

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
     * Flavors client = new Flavors(properties);
     * </pre>
     * @throws Exception 
     */
    public Flavors(Properties properties) throws Exception {
        super(properties);
    }

    /**
     * Creates a flavor(s).
     * <pre>
     * Flavors can be created by directly providing the flavor content in the request body, or they can be imported from a host. 
     * If the flavor content is provided, the flavor parameter must be set in the request. If the flavor is being imported from a 
     * host, the host connection string must be specified.
     * 
     * If a flavor group is not specified, the flavor(s) created will be assigned to the default “automatic” flavor group, 
     * with the exception of the host unique flavors, which are associated with the “host_unique” flavor group. If a flavor group 
     * is specified and does not already exist, it will be created with a default flavor match policy.
     * 
     * Partial flavor types can be specified as an array input. In this fashion, the user can choose which flavor types to import from 
     * a host. Only flavor types that are defined in the flavor group flavor match policy can be specified. If no partial flavor types 
     * are provided, the default action is to attempt retrieval of all flavor types. The response will contain all flavor types that 
     * it was able to create.
     * 
     * A TLS policy ID can be specified if one has previously been created (See mtwilson-tls-policy-client-jaxrs2). Alternatively, if the 
     * string text “TRUST_FIRST_CERTIFICATE” is specified, that generic policy will be used (See the product guide for further description 
     * on generic TLS policies). If no TLS policy is provided, the service will automatically use the default TLS policy specified during 
     * HVS installation (See product guide for description on default TLS policies).
     * 
     * If generic flavors are created, all hosts in the flavor group will be added to the backend queue, flavor verification process to 
     * re-evaluate their trust status. If host unique flavors are created, the individual affected hosts are added to the flavor 
     * verification process.
     * </pre>
     * @param createCriteria The serialized FlavorCreateCriteria java model object represents the content of the request body.
     * <pre> 
     *          connection_string               The host connection string. flavorgroup_name, partial_flavor_types, tls_policy_id
     *                                          can be provided as optional parameters along with the host connection string. 
     * 
     *                                          For INTEL & MICROSOFT hosts, this would have the vendor name, the IP addresses, 
     *                                          or DNS host name and credentials.
     *                                          i.e.: "intel:https://trustagent.server.com:1443;u=trustagentUsername;p=
     *                                          trustagentPassword"
     *                                          microsoft:https://trustagent.server.com:1443;u=trustagentUsername;p=
     *                                          trustagentPassword
     * 
     *                                          For VMware, this includes the vCenter and host IP address or DNS host name.
     *                                          i.e.: "vmware:https://vCenterServer.com:443/sdk;h=trustagent.server.com;u=
     *                                          vCenterUsername;p=vCenterPassword"
     * 
     *          flavor_collection               A collection of flavors in the defined flavor format. No other parameters are
     *                                          needed in this case.
     * 
     *          flavorgroup_name(optional)      Flavor group name that the created flavor(s) will be associated with. If not provided, 
     *                                          created flavor will be associated with automatic flavor group.
     * 
     *          partial_flavor_types(optional)  List array input of flavor types to be imported from a host. Partial flavor type can be 
     *                                          any of the following: PLATFORM, OS, ASSET_TAG, HOST_UNIQUE, SOFTWARE (BIOS is deprecated). Can be provided
     *                                          with the host connection string. See the product guide for more details on how flavor 
     *                                          types are broken down for each host type.
     * 
     *          tls_policy_id(optional)         ID of the TLS policy for connection from the HVS to the host. Can be provided along with
     *                                          host connection string.
     * 
     * Only one of the above parameters can be specified. The parameters listed here are in the order of priority that will be evaluated.
     * </pre>
     * @return <pre>For XML/YAML : The serialized FlavorCollection java model object that was created with collection of flavors each containing:
     *          meta (descriptive information)
     *          pcrs (measurements)
     *
     *          For JSON : The serialized SignedFlavorCollection java model object that was created with collection of signed flavors each containing:
     *          flavor(containing meta and pcr)
     *          signature (signature of respective flavor in JSON)</pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions flavors:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/flavors
     *
     * <b>Example 1:</b>
     *
     * Input:
     * { 
     *    "connection_string": "intel:https://trustagent.server.com:1443;u=trustagentUsername;p=trustagentPassword"
     * }
     * 
     * Output:
     * {
     *     "signed_flavors": [
     *         {
     *             "flavor": {
     *                 "meta": {
     *                     "id": "f171eca1-fd75-475f-8117-bea28dd61f3f",
     *                     "description": {
     *                         "flavor_part": "HOST_UNIQUE",
     *                         "source": "source1",
     *                         "label": "VMWARE_0019204C-C4B7-E811-906E-00163566263E_11-27-2019_00-59-36",
     *                         "bios_name": "Intel Corporation",
     *                         "bios_version": "SE5C620.86B.00.01.0014.070920180847",
     *                         "os_name": "VMware ESXi",
     *                         "os_version": "6.7.0",
     *                         "tpm_version": "2.0",
     *                         "hardware_uuid": "0019204C-C4B7-E811-906E-00163566263E"
     *                     },
     *                     "vendor": "VMWARE"
     *                 },
     *                 "bios": {
     *                     "bios_name": "Intel Corporation",
     *                     "bios_version": "SE5C620.86B.00.01.0014.070920180847"
     *                 },
     *                 "pcrs": {
     *                     "SHA1": {},
     *                     "SHA256": {
     *                         "pcr_20": {
     *                             "value": "dc8bebc261348dde924500bab34389890350f936337bcd15de059523985cff69",
     *                             "event": []
     *                         },
     *                         "pcr_21": {
     *                             "value": "1769a4dd84d7919dc3408a93499367caaded24eeba42be06a19edd12303b2920",
     *                             "event": [
     *                                 {
     *                                     "digest_type": "com.intel.mtwilson.core.common.model.MeasurementSha256",
     *                                     "value": "f180c6bc92cfe0b585cdaaa8c28f52a6b97d325cfebae44f99ee66f13deac357",
     *                                     "label": "",
     *                                     "info": {
     *                                         "ComponentName": "commandLine.",
     *                                         "EventName": "Vim25Api.HostTpmCommandEventDetails",
     *                                         "EventType": "HostTpmCommandEvent"
     *                                     }
     *                                 }
     *                             ]
     *                         }
     *                     }
     *                 }
     *             },
     *             "signature": "mB+UXkrTvCYsW0qNNmqx9Kqki0SLVOFoN3JNoboYRbhPf9Z0v42tYSxDMXESPkcTqRVr4jSzd+x6rMOmuYEDCJ
     *             0xjiahD2womLW+HjGraaCkLwbiZN1AgW/BOzcWbtJrqhvejIGPRZ04OsiVUBI5tDg6PVVvthVKidHEdMyRcCaUHNxXb594zxAvq/
     *             hBmmeT9DzmOnNe1LQF2h+WQ5Nux8TMlu5za4O0hw1RR7I+6cloFZnK5rcuvahldxdoxhZIMsQv40Q2zLyiWGwBcJWkyZjEmEuHHd
     *             YIB+hUqxanFNeu1o9Yj7PHRban7+C/EJ0CNrk92WeMJXCI8o4tbafZsZdgJb6CuOAJha+gXPRR6XdBCH+brCBf7YImpxuNUqbIbk
     *             k/zrXue0d1TWJ12N9ip46cmzSndEwvnXA/+0l2dtwoCmyZfiPCWN5nKC3CYkb8HtHBf7j+QZiuecQLF5n6YazOlWZRUm/v36T/Fk
     *             RfFcML+izIzCRubaBaFL0OdQmy"
     *         }
     *     ]
     * }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the flavor filter criteria model and set the criteria to be searched
     * FlavorCreateCriteria createCriteria = new FlavorCreateCriteria();
     * createCriteria.setConnectionString("intel:https://trustagent.server.com:1443;u=trustagentUsername;
     *              p=trustagentPassword");
     * 
     * // Create the client and call the create API
     * Flavors client = new Flavors(properties);
     * FlavorCollection obj = client.create(createCriteria);
     * </pre></div>
     */
    public SignedFlavorCollection create(FlavorCreateCriteria createCriteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        SignedFlavorCollection newObj = getTarget().path("flavors").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(createCriteria), SignedFlavorCollection.class);
        return newObj;
    }
    
    /**
     * Retrieves a flavor.
     * @param locator The content model of the FlavorLocator java model object can be used as a path parameter.
     * <pre>
     *          id (required)          Flavor ID specified as a path parameter.
     * </pre>
     * @return <pre>For XML/YAML : The serialized Flavor java model object that was retrieved:
     *          meta (descriptive information)
     *          pcrs (measurements)
     *
     *          For JSON : The serialized SignedFlavorCollection java model object that was created with collection of signed flavors each containing:
     *          flavor(containing meta and pcr)
     *          signature (signature of respective flavor in JSON)</pre>
     *
     * @since ISecL 1.0
     * @mtwRequiresPermissions flavors:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/flavors/21f7d831-85b3-46bc-a499-c2d14ff136c8
     * output:
     * {
     *     "flavor": {
     *         "meta": {
     *             "id": "f171eca1-fd75-475f-8117-bea28dd61f3f",
     *             "description": {
     *                 "flavor_part": "HOST_UNIQUE",
     *                 "source": "source1",
     *                 "label": "VMWARE_0019204C-C4B7-E811-906E-00163566263E_11-27-2019_00-59-36",
     *                 "bios_name": "Intel Corporation",
     *                 "bios_version": "SE5C620.86B.00.01.0014.070920180847",
     *                 "os_name": "VMware ESXi",
     *                 "os_version": "6.7.0",
     *                 "tpm_version": "2.0",
     *                 "hardware_uuid": "0019204C-C4B7-E811-906E-00163566263E"
     *             },
     *             "vendor": "VMWARE"
     *         },
     *         "bios": {
     *             "bios_name": "Intel Corporation",
     *             "bios_version": "SE5C620.86B.00.01.0014.070920180847"
     *         },
     *         "pcrs": {
     *             "SHA1": {},
     *             "SHA256": {
     *                 "pcr_20": {
     *                     "value": "dc8bebc261348dde924500bab34389890350f936337bcd15de059523985cff69",
     *                     "event": []
     *                 },
     *                 "pcr_21": {
     *                     "value": "1769a4dd84d7919dc3408a93499367caaded24eeba42be06a19edd12303b2920",
     *                     "event": [
     *                         {
     *                             "digest_type": "com.intel.mtwilson.core.common.model.MeasurementSha256",
     *                             "value": "f180c6bc92cfe0b585cdaaa8c28f52a6b97d325cfebae44f99ee66f13deac357",
     *                             "label": "",
     *                             "info": {
     *                                 "ComponentName": "commandLine.",
     *                                 "EventName": "Vim25Api.HostTpmCommandEventDetails",
     *                                 "EventType": "HostTpmCommandEvent"
     *                             }
     *                         }
     *                     ]
     *                 }
     *             }
     *         }
     *     },
     *     "signature": "mB+UXkrTvCYsW0qNNmqx9Kqki0SLVOFoN3JNoboYRbhPf9Z0v42tYSxDMXESPkcTqRVr4jSzd+x6rMOmuYEDCJ0xjiahD2
     *     womLW+HjGraaCkLwbiZN1AgW/BOzcWbtJrqhvejIGPRZ04OsiVUBI5tDg6PVVvthVKidHEdMyRcCaUHNxXb594zxAvq/hBmmeT9DzmOnNe1L
     *     QF2h+WQ5Nux8TMlu5za4O0hw1RR7I+6cloFZnK5rcuvahldxdoxhZIMsQv40Q2zLyiWGwBcJWkyZjEmEuHHdYIB+hUqxanFNeu1o9Yj7PHRb
     *     an7+C/EJ0CNrk92WeMJXCI8o4tbafZsZdgJb6CuOAJha+gXPRR6XdBCH+brCBf7YImpxuNUqbIbkk/zrXue0d1TWJ12N9ip46cmzSndEwvnX
     *     A/+0l2dtwoCmyZfiPCWN5nKC3CYkb8HtHBf7j+QZiuecQLF5n6YazOlWZRUm/v36T/FkRfFcML+izIzCRubaBaFL0OdQmy"
     * }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the flavor locator model and set the locator id
     * FlavorLocator locator = new FlavorLocator();
     * locator.pathId = UUID.valueOf("21f7d831-85b3-46bc-a499-c2d14ff136c8");
     * 
     * // Create the client and call the retrieve API
     * Flavors client = new Flavors(properties);
     * Flavor obj = client.retrieve(locator);
     * </pre></div>
     */
    public Flavor retrieve(FlavorLocator locator) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", locator.pathId.toString());
        Flavor obj = getTarget().path("flavors/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(Flavor.class);
        return obj;
    }
    
    /**
     * Searches for flavors.
     * @param filterCriteria The content models of the FlavorFilterCriteria java model object can be used as query parameters.
     * <pre>
     *          filter                  Boolean value to indicate whether the response should be filtered to return no 
     *                                  results instead of listing all flavors. Default value is true.
     * 
     *          id                      Flavor ID.
     * 
     *          key and value           The key can be any “key” field from the meta description section of a flavor. The
     *                                  value can be any “value” of the specified key field in the flavor meta description
     *                                  section. Both key and value query parameters need to be specified.
     * 
     *          flavorgroupId           Flavor group ID.
     * 
     *          flavorParts             List array input of flavor types. See the product guide for more details on flavor 
     *                                  types.
     * 
     * Only one of the above parameters can be specified. The parameters listed here are in the order of priority that will be evaluated.
     * </pre>
     * @return <pre>For XML/YAML : The serialized FlavorCollection java model object that was searched with collection of flavors each containing:
     *          meta (descriptive information)
     *          pcrs (measurements)
     *
     *          For JSON : The serialized SignedFlavorCollection java model object that was created with collection of signed flavors each containing:
     *          flavor(containing meta and pcr)
     *          signature (signature of respective flavor in JSON)</pre>
     *
     * @since ISecL 1.0
     * @mtwRequiresPermissions flavors:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/flavors?key=os_name&amp;value=RedHatEnterpriseServer
     * output:
     * {
     *     "signed_flavors": [
     *         {
     *             "flavor": {
     *                 "meta": {
     *                     "id": "8c397fca-552e-4e60-a706-9eeedd68cbb4",
     *                     "description": {
     *                         "flavor_part": "HOST_UNIQUE",
     *                         "source": "source1",
     *                         "label": "VMWARE_0019204C-C4B7-E811-906E-00163566263E_11-26-2019_23-01-35",
     *                         "bios_name": "Intel Corporation",
     *                         "bios_version": "SE5C620.86B.00.01.0014.070920180847",
     *                         "os_name": "VMware ESXi",
     *                         "os_version": "6.7.0",
     *                         "tpm_version": "2.0",
     *                         "hardware_uuid": "0019204C-C4B7-E811-906E-00163566263E"
     *                     },
     *                     "vendor": "VMWARE"
     *                 },
     *                 "bios": {
     *                     "bios_name": "Intel Corporation",
     *                     "bios_version": "SE5C620.86B.00.01.0014.070920180847"
     *                 },
     *                 "pcrs": {
     *                     "SHA1": {},
     *                     "SHA256": {
     *                         "pcr_20": {
     *                             "value": "dc8bebc261348dde924500bab34389890350f936337bcd15de059523985cff69",
     *                             "event": []
     *                         },
     *                         "pcr_21": {
     *                             "value": "1769a4dd84d7919dc3408a93499367caaded24eeba42be06a19edd12303b2920",
     *                             "event": [
     *                                 {
     *                                     "digest_type": "com.intel.mtwilson.core.common.model.MeasurementSha256",
     *                                     "value": "f180c6bc92cfe0b585cdaaa8c28f52a6b97d325cfebae44f99ee66f13deac357",
     *                                     "label": "",
     *                                     "info": {
     *                                         "ComponentName": "commandLine.",
     *                                         "EventName": "Vim25Api.HostTpmCommandEventDetails",
     *                                         "EventType": "HostTpmCommandEvent"
     *                                     }
     *                                 }
     *                             ]
     *                         }
     *                     }
     *                 }
     *             },
     *             "signature": "hJB4THE5Goef78ys4e4YJqlpwYE/BCPz8MUdq8RKsxiaG6W/QGUwa1PBXiRALm9yQKKDe8xsGluVvB1spAWy2S
     *             2Igj9HbkOhakS3AqPo4MngEI7xikog0vYgJpflj51e1zv18Z6/3Rtp3Bwo+iC9C1slVL3PfoiNAptuzzqmy4XEIdMRZrmTnzSeiE
     *             wWHsDyqKFdLh7LsJr1lrX6V8S2EO3D/lyosvDdlmIwtS7r9nlAJF/rO9SNkXwO+BueYZsOZH+d8+1chCiYWMlxVOXGsjCzneNsW
     *             +/NSp7QdjkRujJ+XRBa/7CM+1EG+vnpCLjfziGHRjb/ytXj1YXaLZoQJ9piBHPeiDHo11pA7ToUS965AHWbEQkd8n31y+F4KdvbL
     *             7NJmsgKJgt4qo2b4dwfyu2k61K+4E1bYI2IzSYE/xvz8h4tH1WT8ewNApDMAjkkJOR3ZihL37jV+w0hj/ARs4idAQSEtmJ3x/PkV
     *             5lNaJzD5VC0e+oUAeti3DQEmtWx"
     *         },
     *         {
     *             "flavor": {
     *                 "meta": {
     *                     "id": "13453bc6-3e83-4a6a-9cad-d726f57ee189",
     *                     "description": {
     *                         "flavor_part": "HOST_UNIQUE",
     *                         "source": "source1",
     *                         "label": "VMWARE_0019204C-C4B7-E811-906E-00163566263E_11-26-2019_23-02-02",
     *                         "bios_name": "Intel Corporation",
     *                         "bios_version": "SE5C620.86B.00.01.0014.070920180847",
     *                         "os_name": "VMware ESXi",
     *                         "os_version": "6.7.0",
     *                         "tpm_version": "2.0",
     *                         "hardware_uuid": "0019204C-C4B7-E811-906E-00163566263E"
     *                     },
     *                     "vendor": "VMWARE"
     *                 },
     *                 "bios": {
     *                     "bios_name": "Intel Corporation",
     *                     "bios_version": "SE5C620.86B.00.01.0014.070920180847"
     *                 },
     *                 "pcrs": {
     *                     "SHA1": {},
     *                     "SHA256": {
     *                         "pcr_20": {
     *                             "value": "dc8bebc261348dde924500bab34389890350f936337bcd15de059523985cff69",
     *                             "event": []
     *                         },
     *                         "pcr_21": {
     *                             "value": "1769a4dd84d7919dc3408a93499367caaded24eeba42be06a19edd12303b2920",
     *                             "event": [
     *                                 {
     *                                     "digest_type": "com.intel.mtwilson.core.common.model.MeasurementSha256",
     *                                     "value": "f180c6bc92cfe0b585cdaaa8c28f52a6b97d325cfebae44f99ee66f13deac357",
     *                                     "label": "",
     *                                     "info": {
     *                                         "ComponentName": "commandLine.",
     *                                         "EventName": "Vim25Api.HostTpmCommandEventDetails",
     *                                         "EventType": "HostTpmCommandEvent"
     *                                     }
     *                                 }
     *                             ]
     *                         }
     *                     }
     *                 }
     *             },
     *             "signature": "WWX95jewBdmV0Cmk1D4wVtBvYUFXf1ESVW+EMgB5hwXfXM+5CAkZbXJ1L3ZAqJwJZ6cwKTb17T5D3+azdXEiai
     *             KV1h5o8tmqY5cQP+O4gTbZGFxGU6bFp1eO8wQJKecpSI86Pr9GdnEBOFa5LSdKGoSJSqvEu9X7GgU0eFyE21KQfNQsEk8XDVW2R0
     *             EGrtCB4Mo4Z5jgwJz0BFm1CEaU1QztWkSx67twMN7LoiStYanBQn23tuM7+c3b9QIG3jnDvgmXKL3Q/OkRFdUAGOaPTFBraidqDw
     *             l6WUSmdanffDgg4D+1zpxzH3d9KGt2rjM2XDGZPMqEC62+sDalyXDXswpBrmY7ebO4dF3JNkNvhRnl12vWeBnT4OJzOZd1mUZ6x
     *             KWXUl2x+Pag65XRcufxR3BSvztEuzOk9o5oQdU8vqnfv3qw8qFLt8db3eDbhspHB3mu/qTMoAFZSkgB5f8X4W1Wc0w+Wgpk/tjNZ
     *             E93ALgzLSHbXhGW+7xeANB823ga"
     *         }
     *     ]
     * }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the flavor filter criteria model and set a key and value
     * FlavorFilterCriteria filterCriteria = new FlavorFilterCriteria();
     * filterCriteria.key = "os_name";
     * filterCriteria.value = "RedHatEnterpriseServer";
     * 
     * // Create the client and call the search API
     * Flavors client = new Flavors(properties);
     * FlavorCollection obj = client.search(filterCriteria);
     * </pre></div>
     */
    public FlavorCollection search(FlavorFilterCriteria filterCriteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        FlavorCollection newObj = getTargetPathWithQueryParams("flavors", filterCriteria).request(MediaType.APPLICATION_JSON).get(FlavorCollection.class);
        return newObj;        
    }
         
    /**
     * Deletes a flavor.
     * <pre>
     * All host associations with the specified flavor will be deleted. These 
     * associations are used for caching the trust status for performance reasons.
     * 
     * The flavor group associations with the specified flavor will be deleted. 
     * All hosts in affected flavor groups will be added to the backend queue, 
     * flavor verification process.
     * </pre>
     * @param locator The content model of the FlavorLocator java model object can be used as path parameter.
     * <pre>
     *          id (required)         Flavor ID specified as a path parameter.
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions flavors:delete
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/flavors/21f7d831-85b3-46bc-a499-c2d14ff136c8
     * output: 204 No content
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the flavor locator model and set the locator id
     * FlavorLocator locator = new FlavorLocator();
     * locator.pathId = UUID.valueOf("21f7d831-85b3-46bc-a499-c2d14ff136c8");
     * 
     * // Create the client and call the delete API
     * Flavors client = new Flavors(properties);
     * client.delete(locator);
     * </pre></div>
     */
    public void delete(FlavorLocator locator) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", locator.pathId.toString());
        Response obj = getTarget().path("flavors/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }

    /**
     * Generates manifest from software flavor.
     * @param filterCriteria - The content models of the FlavorFilterCriteria java model object can be used as query parameters.
     * <pre>
     *          uuid                            flavor id which needs to be converted to manifest
     *
     *          label                           flavor label which needs to be converted to manifest
     *
     * Only one of the above parameters can be specified.
     * </pre>
     *
     * @return <pre>Manifest in XML format is retrieved</pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions flavors:search
     * @mtwContentTypeReturned XML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/manifests?id=834076cd-f733-4cca-a417-113fac90adc7
     * output:
     *{@code 
     * <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
     * <Manifest xmlns="lib:wml:manifests:1.0" DigestAlg="SHA256" Label="ISecL_Default_Applicaton_Flavor_v2.0" Uuid="834076cd-f733-4cca-a417-113fac90adc7">
     *     <Dir Include=".*" Exclude="" Path="/opt/trustagent/hypertext/WEB-INF"></Dir>
     *     <Symlink Path="/opt/trustagent/bin/tpm_nvinfo"></Symlink>
     *     <File Path="/opt/trustagent/bin/module_analysis_da.sh"></File>
     * </Manifest>
     *}
    */
    public Manifest getManifestFromFlavor(FlavorFilterCriteria filterCriteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        Manifest manifest = getTargetPathWithQueryParams("manifests", filterCriteria).request(MediaType.APPLICATION_XML).get(Manifest.class);
        return manifest;
    }

}
