/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPassword;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordLocator;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * These resources are used to manage user login passwords
 * <pre>
 * Stores the details of the user's password in the system for user to login 
 * using password mechanism. The caller is expected to provide the hashed value  
 * along with the salt, algorithm and the iterations that were used
 * to calculate the hash.
 *
 * The salt can be a randomly generated 8byte value. The password hash is a sha256 
 * digest of the concatenated salt and the password values. The salt and the 
 * password_hash input to the API should be a base64 encoded string.
 * </pre>
 */
public class UserLoginPasswords extends MtWilsonClient {
    
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
     * UserLoginPasswords client = new UserLoginPasswords(properties);
     * </pre>
     * @throws Exception 
     */
    public UserLoginPasswords(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Creates a login password.
     * <pre>
     * This API is used to create a new login password for HTTP Basic authentication to be associated with the specified user UUID.
     * A single user may have multiple login passwords and login certificates, each of which may have different permissions assigned.  
     * User login passwords and certificates are created with no roles assigned and in an inactive state. An administrator must 
     * use the PUT method to assign roles and flag the password or certificate as active. 
     * Login passwords and certificates have no expiration and must be explicitly deleted by an administrator to change or remove the password.
     * </pre>
     *@param item The serialized UserLoginPassword java model object represents the content of the request body.<br/>
     * <pre>
     * 
     *                  password_hash (required)        base64 encoded string of the sha256 value from (salt + raw password).
     * 
     *                  algorithm (required)            algorithm that was used to create the hash value of password.
     * 
     *                  expires (optional)              expiration date.
     *                  
     *                  iterations (optional)           number of iterations performed to calculate the hash.
     *                  
     *                  salt (required)                 salt is value that is used to calculate hash. It should be a base64 encoded string.
     *                  
     *                  enabled (optional)              boolean value.
     *                  
     *                  status (optional)               status of approval.
     *                  
     *                  comment (optional)              comments.
     * </pre>
     * @return The serialized UserLoginPasswordCollection java model object that was created with collection of user login passwords each containing:
     * <pre>
     *              id
     *              userId
     *              passwordHash
     *              salt
     *              iterations
     *              algorithm
     *              expires
     *              enabled
     *              status
     *              comment
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions user_login_passwords:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwPreRequisite User Create API
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users/9116f3ed-5496-42b3-a9ee-4e89b1d533bc/login-passwords
     * input: 
     * {
     *      "password_hash":"RZMrrSt/PvKvdqs1OgR0id0bDE0dvF4XbPKV7sF+oDg=",
     *      "salt":"a9gDma0hUF8=",
     *      "iterations":1,
     *      "algorithm":"SHA256",
     *      "comment":"Access needed for development"
     * }
     * output: 
     * {
     *      "id":"610cc4fc-0148-4788-bc9c-633d61fbeb4e",
     *      "user_id":"9116f3ed-5496-42b3-a9ee-4e89b1d533bc",
     *      "password_hash":"RZMrrSt/PvKvdqs1OgR0id0bDE0dvF4XbPKV7sF+oDg=",
     *      "salt":"a9gDma0hUF8=",
     *      "iterations":1,
     *      "algorithm":"SHA256",
     *      "enabled":false,
     *      "comment":"Access needed for development"
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the user login password model and set userId, algorithm , iterations , salt , password hash and comment
     *  UserLoginPassword userLoginPassword = new UserLoginPassword();
     *  userLoginPassword.setUserId(UUID.valueOf("9116f3ed-5496-42b3-a9ee-4e89b1d533bc"));
     *  userLoginPassword.setAlgorithm("SHA256");
     *  userLoginPassword.setIterations(1);
     *  userLoginPassword.setSalt(salt);
     *  userLoginPassword.setPasswordHash(hashedpassword);
     *  userLoginPassword.setComment("Access needed for development");
     * 
     *  // Create the client and call the create API
     *  UserLoginPasswords client = new UserLoginPasswords(properties);
     *  UserLoginPasswordCollection createUserLoginPassword = client.create(userLoginPassword);
     * </pre></div>
     */
    public UserLoginPasswordCollection create(UserLoginPassword item) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id", item.getUserId().toString());
        UserLoginPasswordCollection userLoginPasswordCollection = getTarget().path("/users/{user_id}/login-passwords").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).post(Entity.json(item), UserLoginPasswordCollection.class);
        return userLoginPasswordCollection;
    }
    
    /**
     * Delete a user login password.
     * @param locator The content models of the UserLoginPasswordLocator java model object can be used as path parameter.
     * <pre>
     *                  id (required)              User Login Password ID specified as a path parameter.
     * 
     *                  userId (required)          User ID specified as a path parameter.
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions user_login_passwords:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwPreRequisite User Create API
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users/9116f3ed-5496-42b3-a9ee-4e89b1d533bc/login-passwords/610cc4fc-0148-4788-bc9c-633d61fbeb4e
     *
     * output: 204 No content
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     *  // Create the user login password locator model and set both id and userId
     * UserLoginPasswordLocator locator = new UserLoginPasswordLocator();
     * locator.id = UUID.valueOf("196a672c-7f00-4a59-8c30-6a1d5e5c6908");
     * locator.userId = UUID.valueOf("bb6c30b1-c8e1-473b-aa47-7bccaaf0d101");
     * 
     * // Create the client and call the delete API
     * UserLoginPasswords client = new UserLoginPasswords(properties);
     * client.delete(locator);
     * </pre></div>
     */
    public void delete(UserLoginPasswordLocator locator) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id", locator.userId);
        map.put("id", locator.id);
        Response obj = getTarget().path("/users/{user_id}/login-passwords/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete user login password failed");
        }
    }

    /**
     * Updates user login password.
     * <pre>
     * This is the API used by an administrator to actually approve/activate the user credentials and assign permissions. 
     * Without executing this step the password cannot be used. 
     * </pre>
     * @param item - The serialized UserLoginPassword java model object represents the content of the request body.<br/>
     * <pre>
     * 
     *                  password_hash (required)        hashed value of password.It should be a base64 encoded string.
     * 
     *                  algorithm (required)            algorithm that was used to create the hash value of password.
     * 
     *                  expires (optional)              expiration date.
     * 
     *                  iterations (optional)           number of iterations performed to calculate the hash.
     * 
     *                  salt (required)                 salt is value that is used to calculate hash. It should be a base64 encoded string.
     * 
     *                  enabled  (optional)             boolean value.
     * 
     *                  status   (optional)             status of approval.
     * 
     *                  comment  (optional)             comments.
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions user_login_passwords:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwPreRequisite User Create API
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users/9116f3ed-5496-42b3-a9ee-4e89b1d533bc/login-passwords/610cc4fc-0148-4788-bc9c-633d61fbeb4e
     * input: 
     * {
     *      "status":"APPROVED",
     *      "enabled":true,
     *      "roles":["security","whitelist"]
     * }
     * output: 
     * {
     *      "id":"610cc4fc-0148-4788-bc9c-633d61fbeb4e",
     *      "user_id":"9116f3ed-5496-42b3-a9ee-4e89b1d533bc",
     *      "enabled":true,
     *      "status":"APPROVED",
     *      "roles":["security","whitelist"]
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     *  // Create the user login password  model and set the id, userId , enabled ,status and roles
     *  UserLoginPassword userLoginPassword = new UserLoginPassword();
     *  loginPasswordInfo.setUserId(UUID.valueOf("9116f3ed-5496-42b3-a9ee-4e89b1d533bc"));
     *  userLoginPassword.setId(UUID.valueOf("610cc4fc-0148-4788-bc9c-633d61fbeb4e"));
     *  loginPasswordInfo.setEnabled(true);
     *  loginPasswordInfo.setStatus(Status.APPROVED);
     *  List<String> roleSet = new ArrayList<>(Arrays.asList("administrator"));
     *  loginPasswordInfo.setRoles(roleSet);
     * 
     * // Create the client and call the update API
     *  UserLoginPasswords client = new UserLoginPasswords(properties); 
     *  userLoginPassword = client.store(userLoginPassword);
     * </pre></div>
     */
    public void store(UserLoginPassword item) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id", item.getUserId());
        map.put("id", item.getId().toString());
        getTarget().path("/users/{user_id}/login-passwords/{id}").resolveTemplates(map)
                .request().accept(MediaType.APPLICATION_JSON).put(Entity.json(item), UserLoginPassword.class);
    }
    
     /**
     * Retrieves a user login password
     * @param locator The content models of the UserLoginPasswordLocator java model object can be used as path and query parameters.
     * <pre>
     *              id (required)         User login password ID specified as a path parameter.
     *              userId (required)     User ID specified as a path parameter.
     * </pre>
     * @return The serialized UserLoginPassword java model object that was retrieved:
     * <pre>
     *              id
     *              userId
     *              passwordHash
     *              salt
     *              iterations
     *              algorithm 
     *              enabled 
     *              status 
     *              comment
     *              roles
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions user_login_passwords:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwPreRequisite User Create API
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users/9116f3ed-5496-42b3-a9ee-4e89b1d533bc/login-passwords/610cc4fc-0148-4788-bc9c-633d61fbeb4e
     * output: 
     * {
     *      "id":"610cc4fc-0148-4788-bc9c-633d61fbeb4e",
     *      "user_id":"9116f3ed-5496-42b3-a9ee-4e89b1d533bc",
     *      "password_hash":"i4bjqvom3KwEwAMpMpcAZRW8R8IUbi3apS0J9zCBl6c=",
     *      "salt":"a9gDma0hUF8=",
     *      "iterations":1,
     *      "algorithm":"SHA256",
     *      "enabled":true,
     *      "status":"APPROVED",
     *      "roles":["Security","Whitelist"]
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     *  // Create the user login password locator model and set the locator id anfd userId
     * UserLoginPasswordLocator locator = new UserLoginPasswordLocator();
     * locator.id=UUID.valueOf("9116f3ed-5496-42b3-a9ee-4e89b1d533bc");
     * locator.userId=UUID.valueOf("610cc4fc-0148-4788-bc9c-633d61fbeb4e");
     * 
     * //// Create the client and call the retrieve API
     * UserLoginPasswords client = new UserLoginPasswords(properties);
     * UserLoginPassword retrieveUserLoginPassword = client.retrieve(locator);
     * </pre></div>
     */
    public UserLoginPassword retrieve(UserLoginPasswordLocator locator) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id",locator.userId );
        map.put("id", locator.id);
        UserLoginPassword userLoginPassword = getTarget().path("/users/{user_id}/login-passwords/{id}").resolveTemplates(map)
                .request(MediaType.APPLICATION_JSON).get(UserLoginPassword.class);
        return userLoginPassword;
    }
    
        
    /**
     * Search for user login password(s).
     * @param criteria The content models of the UserLoginPasswordFilterCriteria java model object can be used as query parameters. <br/>
     * <pre>
     *             id                   certificate ID.
     * 
     *             status               status of approval.
     * 
     *             enabled              boolean value.
     * 
     * Only one identifying parameter can be specified. The parameters listed here are in the order of priority that will be evaluated.
     * </pre>
     * @return The serialized UserLoginPasswordCollection java model object that was searched with list of user login passwords each containing: 
     * <pre>
     *          id
     *          userId
     *          passwordHash
     *          salt
     *          iterations
     *          algorithm
     *          roles
     *          expires
     *          enabled
     *          status 
     *          comment
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions user_login_passwords:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwPreRequisite User Create API
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users/981d5993-d380-4623-9f8b-1c6131ee8234/login-passwords?filter=false
     * output: 
     * {
     *      "user_login_passwords":[{
     *          "id":"db108831-96d7-4a3c-afd6-5521e2defcbf",
     *          "user_id":"981d5993-d380-4623-9f8b-1c6131ee8234",
     *          "password_hash":"RZMrrSt/PvKvdqs1OgR0id0bDE0dvF4XbPKV7sF+oDg=",
     *          "salt":"a9gDma0hUF8=",
     *          "iterations":1,
     *          "algorithm":"SHA256",
     *          "enabled":true,
     *          "status":"APPROVED",
     *          "comment":"Automatically created during setup.",
     *          "roles":["admin","administrator"]
     *      }]
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     *   // Create the user login password filter criteria model and set the hardware UUID
     *  UserLoginPasswordFilterCriteria criteria = new UserLoginPasswordFilterCriteria();
     *  criteria.userUuid = UUID.valueOf("981d5993-d380-4623-9f8b-1c6131ee8234");
     * 
     *  // Create the client and call the search API
     *  UserLoginPasswords client = new UserLoginPasswords(properties);
     *  UserLoginPasswordCollection userLoginPasswords = client.search(criteria);
     * </pre></div>
     */
    public UserLoginPasswordCollection search(UserLoginPasswordFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("user_id", criteria.id);
        UserLoginPasswordCollection userLoginPasswords = getTargetPathWithQueryParams("/users/{user_id}/login-passwords", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(UserLoginPasswordCollection.class);
        return userLoginPasswords;
    }
}
