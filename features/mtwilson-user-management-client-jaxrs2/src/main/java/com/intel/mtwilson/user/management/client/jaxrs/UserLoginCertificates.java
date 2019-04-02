/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificate;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateLocator;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * These resources are used to manage user login certificate. 
 * <pre>
 * Stores the details of the user's certificate in the system for allowing the 
 * user to use certificate authentication. The request would be created in 
 * the disabled and pending state. Once the access is approved with the roles, 
 * users would be able to login.
 * </pre>
 */
public class UserLoginCertificates extends MtWilsonClient {
    
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
     * properties.put("mtwilson.api.username", "admin");
     * properties.put("mtwilson.api.password", "password");
     * properties.put("mtwilson.api.tls.policy.certificate.sha256", "bfc4884d748eff5304f326f34a986c0b3ff0b3b08eec281e6d08815fafdb8b02");
     * 
     * UserLoginCertificates client = new UserLoginCertificates(properties);
     * </pre>
     * @throws Exception 
     */
    public UserLoginCertificates(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Creates a user login certificate.
     * <pre>
     * This API only creates a user login certificate database entry with the certificate that is 
     * already created using the host verification service's CA certificate and the username.
     * Once created, the LoginCertificate must be approved by an admin using the PUT method 
     * and permissions need to be assigned before performing further action.
     * A user may have more than one login certificates and any combinations of
     * passwords and certificates, each with their own permissions. 
     * A sample case would be to allow more access to flavors when authenticating using a 
     * certificate, but only read access when using password and HTTP Basic.
     * For a detailed description about how certificate based authentication works please refer to product guide.
     * </pre>
     * @param item The serialized UserLoginCertificate java model object represents the content of the request body. <br/>
     * <pre>
     *                  id (optional)                 Certificate ID.
     * 
     *                  userId (required)             User ID.
     * 
     *                  certificate (required)        certificate that is required for login.
     *           
     *                  sha1Hash (optional)           sha1 hash of certificate.
     *                  
     *                  sha256Hash (optional)         sha256 hash of certificate.
     *                  
     *                  expires (optional)            expiration date of certificate.
     *                  
     *                  status (optional)             status of approval.
     *                  
     *                  enabled (optional)            boolean value.
     *                   
     * </pre>
     * @return The serialized UserLoginCertificateCollection java model object that was created with collection of login certificates each containing:
     * <pre>
     *              id
     *              userId
     *              certificate
     *              sha1Hash
     *              sha256Hash 
     *              expires
     *              enabled
     *              status 
     *              comment
     *              roles
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions user_login_certificates:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwPreRequisite User Create API
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users/cdec55c3-206d-4abb-8ba3-83b819e0b256/login-certificates
     * input: 
     * {
     *      "certificate":"MIICrzCCAZegAwIB.....LX+ukqAKQDdqfiSkV+Bw==",
     *      "comment":"Need to manage user accounts."
     * }
     * output: 
     * {
     *      "id":"574874bd-2d5c-4190-b724-d69f2b4c89b4",
     *      "user_id":"cdec55c3-206d-4abb-8ba3-83b819e0b256",
     *      "certificate":"MIICrzCCAZegAwIBAgIJAJ9cWj....LX+ukqAKQDdqfiSkV+Bw==",
     *      "enabled":false,
     *      "comment":"Need to manage user accounts."
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     *  // Create the user login certificate model and set user id , comment and certificate
     * UserLoginCertificate userLoginCertificate = new UserLoginCertificate();
     * userLoginCertificate.setUserId(UUID.valueOf("cdec55c3-206d-4abb-8ba3-83b819e0b256"));
     * userLoginCertificate.setComment("Need to manage user accounts.");
     * userLoginCertificate.setCertificate(certificate.getEncoded()); // assuming the user has created a x509Certificate
     * 
     *  // Create the client and call the create API
     * UserLoginCertificates client = new UserLoginCertificates(properties);
     * UserLoginCertificateCollection createUserLoginCertificate = client.create(userLoginCertificate);
     * </pre></div>
     */
    public UserLoginCertificateCollection create(UserLoginCertificate item) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id", item.getUserId().toString());
        UserLoginCertificateCollection userLoginCertificateCollection = getTarget().path("/users/{user_id}/login-certificates").resolveTemplates(map)
                .request().accept(MediaType.APPLICATION_JSON).post(Entity.json(item), UserLoginCertificateCollection.class);
        return userLoginCertificateCollection;
       
    }
    
    /**
     * Deletes a user login certificate.
     * @param locator The content model of the UserLoginCertificateLocator java model object can be used as path parameter.
     * <pre>
     *                      id (required)       Certificate ID specified as a path parameter.
     * 
     *                      userId (required)   User ID specified as a path parameter.
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions user_login_certificates:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwPreRequisite User Create API
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/users/cdec55c3-206d-4abb-8ba3-83b819e0b256/login-certificates/574874bd-2d5c-4190-b724-d69f2b4c89b4
     * 
     * output: 204 No content
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the user login certificate locator model and set the locator id
     * UserLoginCertificateLocator locator = new UserLoginCertificateLocator();
     * locator.id= UUID.valueOf("5579b5cb-e5de-4ad3-8470-96f36f921074");
     * locator.userId= UUID.valueOf("f78d5b3e-c3f0-481e-a0da-61932b2db2f6");
     * 
     * // Create the client and call the delete API
     * UserLoginCertificates client = new UserLoginCertificates(properties);
     * client.delete(locator);
     * 
     * </pre></div>
     */
    public void delete(UserLoginCertificateLocator locator) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id", locator.userId);
        map.put("id", locator.id);
        Response obj = getTarget().path("/users/{user_id}/login-certificates/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete user login certificate failed");
        }
    }

    /**
     * Updates a user login certificate.
     * This API is used by an administrator to actually approve / activate the login certificate. 
     * Without the admin marking the certificate as APPROVED and assigns permissions,
     * the certificate will not authenticate for any API request.
     * @param item   The serialized UserLoginCertificate java model object represents the content of the request body.<br/>
     * <pre>
     *              userId (required)             User ID.
     * 
     *              certificate (required)        certificate that is required for login.
     * 
     *              sha1Hash (optional)           sha1 hash of certificate.
     * 
     *              sha256Hash (optional)         sha256 hash of certificate.
     * 
     *              expires (optional)            expiration date of certificate.
     * 
     *              status (optional)             status of approval.
     * 
     *              enabled (optional)            boolean value.
     
     *              comment                       comment.
     
     *              roles                         roles to be associated with user certificate.
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions user_login_certificates:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwPreRequisite User Create API
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users/cdec55c3-206d-4abb-8ba3-83b819e0b256/login-certificates/574874bd-2d5c-4190-b724-d69f2b4c89b4
     * input: 
     * {
     *      "enabled":"true",
     *      "status":"APPROVED",
     *      "roles":["security","whitelist"]
     * }
     * output: 
     * {
     *      "id":"574874bd-2d5c-4190-b724-d69f2b4c89b4",
     *      "enabled":true,
     *      "status":"APPROVED",
     *      "roles":["security","whitelist"]
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     *  // Create the UserLoginCertificate model and set userId, enabled , status and rolse set
     *  UserLoginCertificate userLoginCertificate = new UserLoginCertificate();
     *  userLoginCertificate.setId(UUID.valueOf("574874bd-2d5c-4190-b724-d69f2b4c89b4");
     *  userLoginCertificate.setUserId(UUID.valueOf("cdec55c3-206d-4abb-8ba3-83b819e0b256");
     *  userLoginCertificate.setEnabled(true);
     *  userLoginCertificate.setStatus(Status.APPROVED);
     *  List<String> roleSet = new ArrayList<>(Arrays.asList("security", "whitelist"));
     *  userLoginCertificate.setRoles(roleSet);
     * 
     *  // Create the client and call the update API
     *  UserLoginCertificates client = new UserLoginCertificates(properties);
     *  client.store(userLoginPassword);
     * </pre></div>
     */
    public void store(UserLoginCertificate item) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id", item.getUserId());
        map.put("id", item.getId().toString());
        getTarget().path("/users/{user_id}/login-certificates/{id}").resolveTemplates(map)
                .request().accept(MediaType.APPLICATION_JSON).put(Entity.json(item), UserLoginCertificate.class);
        
    }
    
     /**
     * Retrieves a certificate
     * @param locator The content model of the UserLoginCertificateLocator java model object can be used as a path parameter.
     *  <pre>
     *              id (required)          Certificate ID specified as a path parameter.
     * 
     *              userId (required)      User ID specified as a path parameter.
     * </pre>
     * @return The serialized UserLoginCertificate java model object that was retrieved: 
     * <pre>
     *              id
     *              userId 
     *              certificate 
     *              sha1Hash 
     *              sha256Hash 
     *              expires
     *              enabled
     *              status 
     *              comment
     *              roles
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions user_login_certificates:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwPreRequisite User Create API
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users/cdec55c3-206d-4abb-8ba3-83b819e0b256/login-certificates/574874bd-2d5c-4190-b724-d69f2b4c89b4
     * output: 
     * {
     *      "user_login_certificates":[{
     *          "id":"574874bd-2d5c-4190-b724-d69f2b4c89b4",
     *          "user_id":"cdec55c3-206d-4abb-8ba3-83b819e0b256",
     *          "certificate":"MIICrzCCAZegAwIB....==",
     *          "sha1_hash":"5vv7fVyDVD6fGdi/AfAmoieTRfo=",
     *          "sha256_hash":"b5v2UPacu4zkDnmxXCXrbFBsmHOiUhwES5Olrd+TKC4=",
     *          "expires":1432106266000,
     *          "enabled":false,
     *          "status":"PENDING",
     *          "comment":"Need to manage user accounts."
     *      }]
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the user login certificate locator model and set the locator id and userId
     * UserLoginCertificateLocator locator = new UserLoginCertificateLocator();
     * locator.id= UUID.valueOf("f86f7959-567e-4ff2-b83e-b09c289c2b02");
     * locator.userId= UUID.valueOf("3458ddb0-3508-482b-8581-1fb2ec558d92");
     * 
     * // Create the client and call the retrieve API   
     * UserLoginCertificates client = new UserLoginCertificates(properties);
     * UserLoginCertificate userLoginCertificate = client.retrieve(locator);
     * </pre></div>
     */
    public UserLoginCertificate retrieve(UserLoginCertificateLocator locator) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id", locator.userId);
        map.put("id", locator.id);
        UserLoginCertificate userLoginPassword = getTarget().path("/users/{user_id}/login-certificates/{id}").resolveTemplates(map)
                .request(MediaType.APPLICATION_JSON).get(UserLoginCertificate.class);
        return userLoginPassword;
    }
    
    /**
     * Search for certificate(s).
     * @param criteria criteria - The content models of the UserLoginCertificateFilterCriteria java model object can be used as query parameters. <br/>
     * <pre>
     *             filter           Boolean value to indicate whether the response should be filtered to return no 
                                    results instead of listing all flavors. Default value is true.
     *             
     *             id               certificate ID.
     *            
     *             status           status of approval.
     *             
     *             enabled          boolean value.
     *             
     *             sha1             sha1 hash of certificate.
     *             
     *             sha256           sha256 hash of certificate.
     * 
     * Only one identifying parameter can be specified. The parameters listed here are in the order of priority that will be evaluated.
     * </pre>
     * @return The serialized UserLoginCertificateCollection java model object that was searched with list of certificates each containing:
     * <pre>
     *              id
     *              userId
     *              certificate
     *              sha1Hash
     *              sha256Hash 
     *              expires
     *              enabled 
     *              status 
     *              comment
     *              roles
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions user_login_certificates:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwPreRequisite User Create API
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users/cdec55c3-206d-4abb-8ba3-83b819e0b256/login-certificates?filter=false
     * output: 
     * {
     *      "user_login_certificates":[{
     *          "id":"574874bd-2d5c-4190-b724-d69f2b4c89b4",
     *          "user_id":"cdec55c3-206d-4abb-8ba3-83b819e0b256",
     *          "certificate":"MIICrzCCAZegAwIBAgIJAJ9cWj/....LX+ukqAKQDdqfiSkV+Bw==",
     *          "sha1_hash":"5vv7fVyDVD6fGdi/AfAmoieTRfo=",
     *          "sha256_hash":"b5v2UPacu4zkDnmxXCXrbFBsmHOiUhwES5Olrd+TKC4=",
     *          "expires":1432106266000,"enabled":true,
     *          "status":"APPROVED",
     *          "comment":"Need to manage user accounts.",
     *          "roles":["Security","Whitelist"]
     *      }]
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     *  // Create the user login certificate criteria model and set 
     *  UserLoginCertificateFilterCriteria criteria = new UserLoginCertificateFilterCriteria();
     *  criteria.userUuid=UUID.valueOf("3458ddb0-3508-482b-8581-1fb2ec558d92");
     * 
     *  // Create the client and call the search API
     *  UserLoginCertificates client = new UserLoginCertificates(properties);
     *  UserLoginCertificateCollection userLoginCertificates = client.search(criteria);
     * </pre></div>
     */
    public UserLoginCertificateCollection search(UserLoginCertificateFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id", criteria.userUuid);
        UserLoginCertificateCollection userLoginCertificateCollection = getTargetPathWithQueryParams("/users/{user_id}/login-certificates", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(UserLoginCertificateCollection.class);
        return userLoginCertificateCollection;
    }
}
