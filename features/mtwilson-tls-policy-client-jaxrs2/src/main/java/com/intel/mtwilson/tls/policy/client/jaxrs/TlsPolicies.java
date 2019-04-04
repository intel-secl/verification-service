/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.tls.policy.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicy;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicyCollection;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicyFilterCriteria;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicyLocator;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * These resources are used to manage TLS policies.
 * <pre>
 * When there is a connection between any two services in ISecL, the authenticity 
 * of connections is validated through the use of various TLS verification policies.
 * 
 * Following are policy_types used in ISecL: certificate, certificate-digest, 
 * public-key, public-key-digest, TRUST_FIRST_CERTIFICATE and 
 * INSECURE. For details on each of these TLS policies please refer to product guide.
 * 
 * TLS policies can be per-host or shared across multiple hosts. 
 * 
 * A per-host TLS policy is an individual, per-host TLS policy. When the host is 
 * deleted, its per-host TLS policy is automatically deleted as well. 
 * 
 * A shared TLS policy may be referenced by multiple host records. When a host 
 * that references a shared TLS policy is deleted, the shared policy continues 
 * to exist regardless of if there are any remaining hosts that are referencing 
 * to it. Shared policies must be explicitly deleted. 
 * 
 * It is also possible to configure the default policy which means if TLS policy
 * is not explicitly specified in REST call, the default TLS policy will be used.
 * 
 * TLS connections that do not match the requirements defined in a TLS policy, 
 * will be rejected.
 * </pre>
 */
public class TlsPolicies extends MtWilsonClient {
    
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
     * TlsPolicies client = new TlsPolicies(properties);
     * </pre>
     * @throws java.lang.Exception
     */
    public TlsPolicies(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Creates a TLS policy.
     * @param hostTlsPolicy The serialized HostTlsPolicy java model object represents the content of the request body.
     * <pre> 
     *              name (required)        TLS policy name.
     * 
     *              private (optional)     boolean variable to indicates if the policy is per-host or shared.
     *                                     If the value is true, the policy is per-host and if it is false,
     *                                     it is shared between hosts.
     *                                     By default, the value is set to false.
     * 
     *              comment (optional)     comments.
     * 
     *              descriptor (required)  details of TLS policy with data, protocols etc.
     * 
     *                                     policy_type (required) types of policies mentioned in description of the class.
     * 
     *                                     ciphers (optional)     comma separated algorithms for encryption, decryption.
     *                                                            ! prefix means exclude  for example !DES  means exclude 
     *                                                            ciphers with DES.
     *                                                            + prefix means "at least" for example +AES128  means 
     *                                                            "AES128 or greater".
     *                                                            no prefix means exact match for example "AES" would match 
     *                                                            both AES128 and AES256.
     * 
     *                                     protocols (optional)   comma separated cryptographic protocols for secure 
     *                                                            communications.
     *                                                            - prefix means don't include but allow if added later for 
     *                                                            example -ssl means don't 
     *                                                            include ssl but if one is added later it's ok.
     *                                                            ! prefix means exclude for example !SSLv2 means don't allow 
     *                                                            ssl2.
     *                                                            unlike - prefix, this prefix would not allow even if protocol 
     *                                                            is added later.
     * 
     *                                     protection (optional)  boolean value for which one to be used encryption, integrity, 
     *                                                            authentication, forwardSecrecy.
     * 
     *                                     meta (required)        digest algorithm (MD5, SHA-1, SHA-256, etc), encoding (base64 
     *                                                            or hex).
     * 
     *                                     data (required)        certificates, certificate-digests, public-keys, or public-key- 
     *                                                            digests.
     *                                                            Must explicitly specify PEM encoded certificate which contains 
     *                                                            ASCII without the prefix "--BEGIN.." and "---END" lines.
     *                                                            The digest data should be in byte array format.
     *                                                            
     * </pre>
     * @return <pre>The serialized HostTlsPolicy java model object that was created:
     *          id
     *          name
     *          descriptor
     *          private</pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions host_tls_policies:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/tls-policies
     * Input: 
     * {
     *      "name":"vcenter1_shared_policy",
     *      "descriptor":{
     *          "policy_type":"certificate-digest",
     *          "data":["d0 8f 07 b0 5c 6d 78 62 b9 27 48 ff 35 da 27 bf f2 03 b3 c1"],
     *          "meta":{"digest_algorithm":"SHA-1"}
     *      },
     *      "private":false
     * }
     * 
     * Output: 
     * {
     *      "id":"3e75091f-4657-496c-a721-8a77931ee9da",
     *      "name":"vcenter1_shared_policy",
     *      "descriptor":{
     *          "policy_type":"certificate-digest",
     *          "data":["d0 8f 07 b0 5c 6d 78 62 b9 27 48 ff 35 da 27 bf f2 03 b3 c1"],
     *          "meta":{"digest_algorithm":"SHA-1"}
     *      },
     *      "private":false
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the host TLS policy model and set the create parameters
     * HostTlsPolicy tlsPolicy = new HostTlsPolicy();
     * tlsPolicy.setName("vcenter1_shared_policy");
     * tlsPolicy.setPrivate(false);
     * TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
     * tlsPolicyDescriptor.setPolicyType("certificate-digest");
     * tlsPolicyDescriptor.setData(Arrays.asList("d0 8f 07 b0 5c 6d 78 62 b9 27 48 ff 35 da 27 bf f2 03 b3 c1"));
     * Map<String, String> metaData = new HashMap<>();
     * metaData.put("digest_algorithm","SHA-1");
     * tlsPolicyDescriptor.setMeta(metaData);
     * tlsPolicy.setDescriptor(tlsPolicyDescriptor);
     * 
     * // Create the client and call the create API
     * TlsPolicies client = new TlsPolicies(properties);
     * HostTlsPolicy tlsPolicy = client.create(tlsPolicy);
     * </pre></div>
     */
    public HostTlsPolicy create(HostTlsPolicy hostTlsPolicy) {
        log.debug("target: {}", getTarget().getUri().toString());
        HostTlsPolicy newObj = getTarget().path("tls-policies").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(hostTlsPolicy), HostTlsPolicy.class);
        return newObj;
    }
    
    /**
     * Deletes a TLS policy.
     * <pre>
     * TLS policy cannot be deleted if it is associated with any registered host.
     * </pre>
     * @param locator The content models of the HostTlsPolicyLocator java model object can be used as path parameter.
     * <pre>
     *              id (required)         TLS Policy ID specified as a path parameter.
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions host_tls_policies:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/tls-policies/3e75091f-4657-496c-a721-8a77931ee9da
     * Output: 204 No content
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the host TLS policy locator model and set the locator id
     * HostTlsPolicyLocator locator = new HostTlsPolicyLocator();
     * locator.pathId = UUID.valueOf("21f7d831-85b3-46bc-a499-c2d14ff136c8");
     * 
     * // Create the client and call the delete API
     * TlsPolicies client = new TlsPolicies(properties);
     * client.delete(locator);
     * </pre></div>
     */
    public void delete(HostTlsPolicyLocator locator) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", locator.pathId.toString());
        Response obj = getTarget().path("tls-policies/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete TlsPolicy failed");
        }
    }

    /**
     * Updates a TLS policy.
     * @param tlsPolicy The serialized HostTlsPolicy java model object represents the content of the request body.
     * <pre> 
     *              name                TLS policy name.
     * 
     *              private             boolean variable to indicates if the policy is per-host or shared.
     *                                  If the value is true, the policy is per-host and if it is false,
     *                                  it is shared between hosts.
     * 
     *              comment             comments.
     * 
     *              descriptor          details of TLS policy with data, protocols etc.
     * 
     *                                  policy_type     types of policies mentioned in description of the class.
     *                                                  This is a required parameter for update.
     * 
     *                                  ciphers         comma separated algorithms for encryption, decryption.
     *                                                  ! prefix means exclude  for example !DES  means exclude 
     *                                                  ciphers with DES.
     *                                                  + prefix means "at least" for example +AES128  means 
     *                                                  "AES128 or greater".
     *                                                  no prefix means exact match for example "AES" would match 
     *                                                  both AES128 and AES256.
     * 
     *                                  protocols       comma separated cryptographic protocols for secure 
     *                                                  communications.
     *                                                  - prefix means don't include but allow if added later for 
     *                                                  example -ssl means don't 
     *                                                  include ssl but if one is added later it's ok.
     *                                                  ! prefix means exclude for example !SSLv2 means don't allow 
     *                                                  ssl2.
     *                                                  unlike - prefix, this prefix would not allow even if protocol 
     *                                                  is added later.
     * 
     *                                  protection      boolean value for which one to be used encryption, integrity, 
     *                                                  authentication, forwardSecrecy.
     * 
     *                                  meta            digest algorithm (MD5, SHA-1, SHA-256, etc), encoding (base64 
     *                                                  or hex).
     * 
     *                                  data            certificates, certificate digests, public keys, or public key 
     *                                                  digests.
     *                                                  Must explicitly specify PEM encoded certificate which contains 
     *                                                  ASCII without the prefix "--BEGIN.." and "---END" lines.
     *                                                  The digest data should be in byte array format.
     * </pre>
     * @return <pre>The serialized HostTlsPolicy java model object that was updated:
     *          id
     *          name
     *          descriptor
     *          private</pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions host_tls_policies:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * <pre> 
     * https://server.com:8443/mtwilson/v2/tls-policies/3e75091f-4657-496c-a721-8a77931ee9da
     * Input: 
     * {
     *      "name":"vcenter1_shared_policy",
     *      "descriptor":{
     *          "policy_type":"certificate-digest",
     *          "data":["d0 8f 07 b0 5c 6d 78 62 b9 27 48 ff 35 da 27 bf f2 03 b3 c1"],
     *          "meta":{"digest_algorithm":"SHA-1"}
     *      },
     *      "comment":"Updated with comments",
     *      "private":false
     * }
     * 
     * Output: 
     * {
     *      "id":"3e75091f-4657-496c-a721-8a77931ee9da",
     *      "name":"vcenter1_shared_policy",
     *      "descriptor":{
     *          "policy_type":"certificate-digest",
     *          "data":["d0 8f 07 b0 5c 6d 78 62 b9 27 48 ff 35 da 27 bf f2 03 b3 c1"],
     *          "meta":{"digest_algorithm":"SHA-1"}
     *      },
     *      "comment":"Updated with comments",
     *      "private":false
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the host TLS policy model and set the update parameters
     * HostTlsPolicy tlsPolicy = new HostTlsPolicy();
     * tlsPolicy.setId("3e75091f-4657-496c-a721-8a77931ee9da");
     * tlsPolicy.setComment("Updated with comments");
     * 
     * // Create the client and call the update API
     * TlsPolicies client = new TlsPolicies(properties);
     * HostTlsPolicy tlsPolicy = client.update(tlsPolicy);
     * </pre></div>
     */
    public HostTlsPolicy update(HostTlsPolicy tlsPolicy) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", tlsPolicy.getId().toString());
        HostTlsPolicy updatedObj = getTarget().path("tls-policies/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(tlsPolicy), HostTlsPolicy.class);
        return updatedObj;
    }
    
     /**
     * Retrieves a TLS policy.
     * @param locator The content models of the HostTlsPolicyLocator java model object can be used as path parameter.
     * <pre>
     *              id (required)         TLS Policy ID specified as a path parameter.
     * </pre>
     * @return <pre>The serialized HostTlsPolicy java model object that was retrieved:
     *          id
     *          name
     *          descriptor
     *          private</pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions host_tls_policies:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/tls-policies/3e75091f-4657-496c-a721-8a77931ee9da
     * Output: 
     * {
     *      "id":"3e75091f-4657-496c-a721-8a77931ee9da",
     *      "name":"vcenter1_shared_policy",
     *      "descriptor":{
     *          "policy_type":"certificate-digest",
     *          "data":["d0 8f 07 b0 5c 6d 78 62 b9 27 48 ff 35 da 27 bf f2 03 b3 c1"],
     *          "meta":{"digest_algorithm":"SHA-1"}
     *      },
     *      "comment":"Updated with comments",
     *      "private":false
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the host TLS policy locator model and set the locator id
     * HostTlsPolicyLocator locator = new HostTlsPolicyLocator();
     * locator.pathId = UUID.valueOf("21f7d831-85b3-46bc-a499-c2d14ff136c8");
     * 
     * // Create the client and call the retrieve API
     * TlsPolicies client = new TlsPolicies(properties);
     * HostTlsPolicy tlsPolicy = client.retrieve(locator);
     * </pre></div>
     */
    public HostTlsPolicy retrieve(HostTlsPolicyLocator locator) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", locator.pathId.toString());
        HostTlsPolicy obj = getTarget().path("tls-policies/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(HostTlsPolicy.class);
        return obj;
    }
    
    /**
     * Searches for TLS policies. 
     * <pre>
     * The meta data in the output indicates the allowed TLS policies that
     * can be configured. This can be updated in the mtwilson.properties file.
     * </pre>
     * @param filterCriteria The content models of the HostTlsPolicyFilterCriteria java model object can be used as query parameters.
     * <pre> 
     *          filter              Boolean value to indicate whether the response should be filtered to return no 
     *                              results instead of listing all TLS policies. Default value is true.
     * 
     *          id                  UUID of TLS policy.
     * 
     *          hostId              Host ID.
     * 
     *          nameEqualTo         Name of the TLS policy.
     * 
     *          nameContains        Partial name of the TLS policy.
     * 
     *          privateEqualTo      Search per-host vs shared TLS policies.
     * 
     *          commentEqualTo      Complete comment string.
     * 
     *          commentContains     Partial comment.
     * 
     * Only one identifying parameter can be specified. The parameters listed here are in the order of priority that will be evaluated. 
     * Identifying parameters include id, nameEqualTo, nameContains, hostId, privateEqualTo, commentEqualTo, commentContains.
     * </pre>
     * @return <pre>The serialized HostTlsPolicyCollection java model object that was searched 
     * containing a meta section and collection of TLS policies each containing:
     *          id
     *          name
     *          descriptor
     *          private</pre>
     * <pre>
     * A meta section contains default policy, allowed policies and global policy.
     * A TLS polcies section contains collection od TLS policies that matches the 
     * seach criteria with id, name, descriptor and private scope boolean variable.
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions host_tls_policies:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/tls-policies?filter=false
     * Output: 
     * {
     *      "meta":{
     *          "default":null,
     *          "allow":["certificate","certificate-digest"],
     *          "global":null
     *      },
     *      "tls_policies":[{
     *          "id":"3e75091f-4657-496c-a721-8a77931ee9da",
     *          "name":"vcenter1_shared_policy",
     *          "descriptor":{
     *              "policy_type":"certificate-digest",
     *              "data":["d0 8f 07 b0 5c 6d 78 62 b9 27 48 ff 35 da 27 bf f2 03 b3 c1"],
     *              "meta":{"digest_algorithm":"SHA-1"}
     *          },
     *          "private":false
     *      }]
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the host TLS policy filter criteria model and set the search criteria
     * HostTlsPolicyFilterCriteria criteria = new HostTlsPolicyFilterCriteria();
     * criteria.privateEqualTo = false;
     * 
     * // Create the client and call the search API
     * TlsPolicies client = new TlsPolicies(properties);
     * HostTlsPolicyCollection tlsPolicyCollection = client.search(criteria);
     * </pre></div>
     */
    public HostTlsPolicyCollection search(HostTlsPolicyFilterCriteria filterCriteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HostTlsPolicyCollection objList = getTargetPathWithQueryParams("tls-policies", filterCriteria).request(MediaType.APPLICATION_JSON).get(HostTlsPolicyCollection.class);
        return objList;
    }
}
