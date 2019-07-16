/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.client.jaxrs;

import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.mtwilson.flavor.rest.v2.model.Flavorgroup;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupCollection;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupLocator;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * These resources are used to manage flavor groups.
 * <pre>
 * 
 * A flavor group represents a collection of flavors that has a specified policy for how those flavors are verified against a host. 
 * Flavors can be added to a flavor group, and hosts can be associated with a flavor group.
 * 
 * The flavor group policy lists the individual flavor parts and the match policy rules associated with each one.
 * 
 * <b>Flavor Part</b>: The type or classification of the flavor. For more 
 * information on flavor parts, see the product guide.
 *      PLATFORM
 *      OS
 *      ASSET_TAG
 *      HOST_UNIQUE
 * 
 * 
 * <b>Match Policy</b>: The policy which defines how the host is verified against the flavors in the flavor group for the specified 
 * flavor part.
 * 
 *      <u>Match Type</u>: An enum whose value identifies how the policy is evaluated 
 *      for the specified flavor part.
 * 
 *      ANY_OF              The host can match any of the flavors of this type (flavor part) in the flavor group, but it must match 
 *                          at least one.
 *      ALL_OF              The host must match each and every one of the flavors of this type (flavor part) in the flavor group.
 * 
 * 
 *      <u>Required</u>: An enum whose value determines whether the flavor part needs to be evaluated.
 * 
 *      REQUIRED             A flavor of this type (flavor part) must exist in the flavor group in order for the host to be trusted.
 *      REQUIRED_IF_DEFINED  If a flavor of this type (flavor part) does NOT exist in the flavor group, the host can still be trusted.
 * 
 * 
 * <b>Default Flavor Groups</b>: Two flavor groups exist by default.
 * 
 *      automatic   Default flavor group for flavor verification.
 *      host_unique      Default flavor group for host unique flavor parts. All host unique flavor parts are associated with this 
 *                           flavor group regardless of user settings. This flavor group’s policy is null, and the match policy for its 
 *                           flavor parts are defined in each individual separate flavor group. This separation is required for backend
 *                           processing and handling of the host unique flavors. Host Unique Flavor Parts: ASSET_TAG, HOST_UNIQUE
 * </pre>
 * @author ssbangal
 */
public class Flavorgroups extends MtWilsonClient {

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
     * Flavorgroups client = new Flavorgroups(properties);
     * </pre>
     * @throws Exception 
     */
    public Flavorgroups(Properties properties) throws Exception {
        super(properties);
    }

    /**
     * Creates a flavor group.
     *
     * @param flavorGroup The serialized Flavorgroup java model object represents the content of the request body.
     * <pre>
     *          name (required)                             Name of the flavorgroup to be created.
     * 
     *          flavor_match_policy_collection (required)   Collection of flavor match policies. Each flavor 
     *                                                      match policy contains two parts:
     * 
     *                                                      flavor_part    The type or classification of the flavor.
     * 
     *                                                      match_policy   The policy which defines how the host is verified against the
     *                                                                     flavors in the flavor group for the specified flavor part.
     * </pre>
     * @return <pre>The serialized Flavorgroup java model object that was created:
     *          id
     *          name
     *          flavor_match_policy_collection</pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions flavorgroups:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/flavorgroups
     * Input :
     * {
     *   "name":"CustomerX",
     *   "flavor_match_policy_collection":{
     *          "flavor_match_policies": [
     *             {
     *                 "flavor_part": "PLATFORM",
     *                 "match_policy": {
     *                     "match_type": "ANY_OF",
     *                     "required": "REQUIRED"
     *                 }
     *             },
     *             {
     *                 "flavor_part": "OS",
     *                 "match_policy": {
     *                     "match_type": "ANY_OF",
     *                     "required": "REQUIRED"
     *                 }
     *             },
     *             {
     *                 "flavor_part": "ASSET_TAG",
     *                 "match_policy": {
     *                      "match_type": "ANY_OF",
     *                      "required": "REQUIRED_IF_DEFINED"
     *                  }
     *             },
     *             {
     *                 "flavor_part": "HOST_UNIQUE",
     *                 "match_policy": {
     *                     "match_type": "ANY_OF",
     *                     "required": "REQUIRED_IF_DEFINED"
     *                 }
     *             }
     *         ]
     *      }
     *  }
     * 
     * Output:
     * {  
     *    "id": "a0950923-596b-41f7-b9ad-09f525929ba1",
     *    "name":"CustomerX",
     *    "flavor_match_policy_collection":{
     *          "flavor_match_policies": [
     *             {
     *                 "flavor_part": "PLATFORM",
     *                 "match_policy": {
     *                     "match_type": "ANY_OF",
     *                     "required": "REQUIRED"
     *                 }
     *             },
     *             {
     *                 "flavor_part": "OS",
     *                 "match_policy": {
     *                     "match_type": "ANY_OF",
     *                     "required": "REQUIRED"
     *                 }
     *             },
     *             {
     *                 "flavor_part": "ASSET_TAG",
     *                 "match_policy": {
     *                      "match_type": "ANY_OF",
     *                      "required": "REQUIRED_IF_DEFINED"
     *                  }
     *             },
     *             {
     *                 "flavor_part": "HOST_UNIQUE",
     *                 "match_policy": {
     *                     "match_type": "ANY_OF",
     *                     "required": "REQUIRED_IF_DEFINED"
     *                 }
     *             }
     *         ]
     *      }
     *  }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create an ANY_OF and REQUIRED match policy
     * MatchPolicy anyOfRequired = new MatchPolicy();
     * anyOfRequired.setMatchType(MatchPolicy.MatchType.ANY_OF);
     * anyOfRequired.setRequired(MatchPolicy.Required.REQUIRED);
     * 
     * // Add each flavor match policy to the collection
     * FlavorMatchPolicyCollection flavorMatchPolicyCollection = new FlavorMatchPolicyCollection();
     * flavorMatchPolicyCollection.addFlavorMatchPolicy(combinedPolicy);
     * 
     * // Create the flavor group model and set the name and flavor match policy collection
     * Flavorgroup flavorgroup = new Flavorgroup();
     * flavorgroup.name = "ThirdGenerationServers";
     * flavorgroup.setFlavorMatchPolicyCollection(flavorMatchPolicyCollection);
     * 
     * // Create the client and call the create API
     * Flavorgroups client = new Flavorgroups(properties);
     * Flavorgroup obj = client.create(flavorgroup);
     * </pre></div>
     */
    public Flavorgroup create(Flavorgroup flavorGroup) {
        log.debug("target: {}", getTarget().getUri().toString());
        Flavorgroup newObj = getTarget().path("flavorgroups").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(flavorGroup), Flavorgroup.class);
        return newObj;
    }

    
    /**
     * Retrieves a flavor group.
     * @param locator The content models of the FlavorgroupLocator java model object can be used as path and query parameters.
     * <pre>
     *              id (required)         Flavor group ID specified as a path parameter.
     * 
     *              includeFlavorContent  Boolean value to indicate whether the content of the flavors
     *                                    contained within the specified flavor group should be included
     *                                    in the response body. Default value is false.
     * </pre>
     * @return 
     * <pre>
     * The serialized Flavorgroup java model object that was retrieved and a list of associated flavor IDs
     * (with or without the flavor content):
     *          id
     *          name
     *          flavor_match_policy_collection
     *          flavor_ids
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions flavorgroups:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px">
     * <pre>
     * https://server.com:8443/mtwilson/v2/flavorgroups/826501bd-3c75-4839-a08f-db5f744f8498
     * Output: 
     * {
     *     "id": "826501bd-3c75-4839-a08f-db5f744f8498",
     *     "name": "automatic",
     *     "flavor_match_policy_collection": {
     *         "flavor_match_policies": [
     *             {
     *                 "flavor_part": "PLATFORM",
     *                 "match_policy": {
     *                     "match_type": "ANY_OF",
     *                     "required": "REQUIRED"
     *                 }
     *             },
     *             {
     *                 "flavor_part": "OS",
     *                 "match_policy": {
     *                     "match_type": "ANY_OF",
     *                     "required": "REQUIRED"
     *                 }
     *             },
     *             {
     *                 "flavor_part": "ASSET_TAG",
     *                 "match_policy": {
     *                     "match_type": "ANY_OF",
     *                     "required": "REQUIRED_IF_DEFINED"
     *                 }
     *             },
     *             {
     *                 "flavor_part": "HOST_UNIQUE",
     *                 "match_policy": {
     *                     "match_type": "ANY_OF",
     *                     "required": "REQUIRED_IF_DEFINED"
     *                 }
     *             }
     *         ]
     *     },
     *     "flavor_ids": [
     *         "b37580dd-f300-4229-8358-2640936c3841"
     *     ]
     * }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the flavor group locator model and set the locator id
     * FlavorgroupLocator locator = new FlavorgroupLocator();
     * locator.id = "826501bd-3c75-4839-a08f-db5f744f8498";
     * 
     * // Create the client and call the retrieve API
     * Flavorgroups client = new Flavorgroups(properties);
     * Flavorgroup obj = client.retrieve(locator);
     * </pre></div>
     * */
    public Flavorgroup retrieve(FlavorgroupLocator locator) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", locator.pathId);
        Flavorgroup obj = getTarget().path("flavorgroups/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(Flavorgroup.class);
        return obj;
    }

     /**
     * Searches for flavor groups.
     * @param filterCriteria The content models of the FlavorgroupFilterCriteria java model object can be used as query parameters.
     * <pre>
     *          filter                    Boolean value to indicate whether the response should be filtered to return no 
     *                                    results instead of listing all flavor groups. Default value is true.
     * 
     *          id                        Flavor group ID.
     * 
     *          nameEqualTo               Flavor group name.
     * 
     *          nameContains              Substring of flavor group name.
     * 
     *          hostId                    Host ID.
     * 
     *          includeFlavorContent      Boolean value to indicate whether the content of the flavors
     *                                    contained within the specified flavor group should be included
     *                                    in the response body. Default value is false.
     * 
     * Only one identifying parameter can be specified. The parameters listed here are in the order of priority that will be evaluated. 
     * Identifying parameters include id, nameEqualTo, nameContains, hostId.
     * </pre>
     * @return <pre>
     * The serialized FlavorgroupCollection java model object that was searched and a list of associated flavor IDs
     * (with or without the flavor content):
     *          id
     *          name
     *          flavor_match_policy_collection
     *          flavor_ids
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions flavorgroups:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/flavorgroups?nameEqualTo=automatic
     * Output:
     * {
     *     "flavorgroups": [
     *         {
     *             "id": "826501bd-3c75-4839-a08f-db5f744f8498",
     *             "name": "automatic",
     *             "flavor_match_policy_collection": {
     *                 "flavor_match_policies": [
     *                     {
     *                         "flavor_part": "PLATFORM",
     *                         "match_policy": {
     *                             "match_type": "ANY_OF",
     *                             "required": "REQUIRED"
     *                         }
     *                     },
     *                     {
     *                         "flavor_part": "OS",
     *                         "match_policy": {
     *                             "match_type": "ANY_OF",
     *                             "required": "REQUIRED"
     *                         }
     *                     },
     *                     {
     *                         "flavor_part": "ASSET_TAG",
     *                         "match_policy": {
     *                             "match_type": "ANY_OF",
     *                             "required": "REQUIRED_IF_DEFINED"
     *                         }
     *                     },
     *                     {
     *                         "flavor_part": "HOST_UNIQUE",
     *                         "match_policy": {
     *                             "match_type": "ANY_OF",
     *                             "required": "REQUIRED_IF_DEFINED"
     *                         }
     *                     }
     *                 ]
     *             },
     *             "flavor_ids": [
     *                 "b37580dd-f300-4229-8358-2640936c3841"
     *             ]
     *         }
     *     ]
     * }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the flavor group filter criteria model and set a flavor group name
     * FlavorgroupFilterCriteria filterCriteria = new FlavorgroupFilterCriteria();
     * filterCriteria.nameEqualTo = "automatic";
     * 
     * // Create the client and call the search API
     * Flavorgroups client = new Flavorgroups(properties);
     * FlavorgroupCollection obj = client.search(filterCriteria);
     * </pre></div>
     * */
    public FlavorgroupCollection search(FlavorgroupFilterCriteria filterCriteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        FlavorgroupCollection newObj = getTargetPathWithQueryParams("flavorgroups", filterCriteria).request(MediaType.APPLICATION_JSON).get(FlavorgroupCollection.class);
        return newObj;
    }

    /**
     * Deletes a flavor group. If the flavor group is still associated with any hosts, an error will be thrown. 
     * @param locator The content models of the FlavorgroupLocator java model object can be used as path parameter.
     * <pre>
     *              id (required)         Flavor group ID specified as a path parameter.
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions flavorgroups:delete
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/flavorgroups/a0950923-596b-41f7-b9ad-09f525929ba1
     * Output: 204 No content
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the flavor group locator model and set the locator id
     * FlavorgroupLocator locator = new FlavorgroupLocator();
     * locator.id = "826501bd-3c75-4839-a08f-db5f744f8498";
     * 
     * // Create the client and call the delete API
     * Flavorgroups client = new Flavorgroups(properties);
     * client.delete(locator);
     * </pre></div>
     */
    public void delete(FlavorgroupLocator locator) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", locator.pathId);
        Response obj = getTarget().path("flavorgroups/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }
}