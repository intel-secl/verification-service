/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.user.management.rest.v2.model.UserCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.User;
import com.intel.mtwilson.user.management.rest.v2.model.UserLocator;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * These resources are used to manage user accounts of Host Verification system.
 * <pre>
 * To these users, role with permissions will be associated for user to perform
 * only approved actions and not others.
 * 
 * A user will also be associated with user login certificates and passwords.
 * For more details check UserLoginCertificates and UserLoginPasswords javadoc.
 * </pre>
 */
public class Users extends MtWilsonClient {
    
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
     * Users client = new Users(properties);
     * </pre>
     * @throws Exception 
     */
    public Users(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Creates a user.
     * <pre>
     * This method creates a new User object, which includes the username, locale, and an optional description. 
     * The user will then need to be assigned a login password (UserLoginPassword) or certificate (UserLoginCertificate). 
     * Roles and permissions are assigned at the login password and login certificate level, 
     * and each login password or certificate must be set to APPROVED and have roles assigned by an administrator before becoming active.
     * The user can specify the purpose of the request in the comments section so that the administrator can provide access to 
     * appropriate roles during approval.
     * </pre>
     * @param item The serialized User java model object represents the content of the request body.<br/>
     * <pre>
     * 
     *              username (required)              name of the user to be created.
     * 
     *              locale (optional)                location of user. By default, set to null.
     * 
     *              comment (optional)               user comments.
     * </pre>
     * @return The serialized UserCollection java model object that was created with collection of flavors each containing:
     * <pre>
     *          id 
     *          username
     *          locale
     *          comment
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions None
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users
     * input: 
     * {
     *      "username":"Developer1",
     *      "locale":"en-US",
     *      "comment":"Access needed for Project1"
     * }
     * output: 
     * {
     *      "id":"e6c9337c-e709-4b38-9f04-3b61b8a84667",
     *      "username":"Developer1",
     *      "locale":"en-us",
     *      "comment":"Access needed for Project1"
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     *  // Create the user model and set a user name, locale and comment
     *  User user = new User();
     *  user.setUsername("Developer1");
     *  user.setLocale(Locale.US);
     *  user.setComment("Access needed for Project1");
     * 
     *  // Create the client and call the create API
     *  Users client = new Users(properties);
     *  UserCollection newUser = client.create(user);
     * </pre></div>
     */
    public UserCollection create(User item) {
        log.debug("target: {}", getTarget().getUri().toString());
        UserCollection newObj = getTarget().path("users").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(item), UserCollection.class);
        return newObj;
    }
    
    /**
     * Deletes a user account.
     * @param locator The content model of the UserLocator java model object can be used as path parameter.
     * <pre>
     *          id (required)         User ID specified as a path parameter.
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions users:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users/e6c9337c-e709-4b38-9f04-3b61b8a84667
     * 
     * output: 204 No content
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the user locator model and set the locator id
     *  UserLocator locator = new UserLocator();
     *  locator.id=UUID.valueOf("e6c9337c-e709-4b38-9f04-3b61b8a84667");
     * 
     *  // Create the client and call the delete API
     *  Users client = new Users(properties);
     *  client.delete(locator);
     *  </pre></div>
     */
    public void delete(UserLocator locator) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", locator.id);
        Response user = getTarget().path("users/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        if( !user.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete user failed");
        }
    }

    /**
     * Updates a user account.
     * @param item The serialized User java model object represents the content of the request body.<br/>
     * <pre>
     *              username (required)            name of the user to be created.
     * 
     *              locale (optional)              location of user. By default, set to null.
     * 
     *              comment (optional)             user comments.
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions users:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users/e6c9337c-e709-4b38-9f04-3b61b8a84667
     * input: 
     * {
     *      "locale":"fr",
     *      "comment":"Access granted for Project1"
     * }
     * output: 
     * {
     *      "id":"e6c9337c-e709-4b38-9f04-3b61b8a84667",
     *      "locale":"fr",
     *      "comment":"Access granted for Project1"
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     *   // Create the user model and set id, locale and comment
     *  User user = new User();
     *  user.setId(UUID.valueOf("e6c9337c-e709-4b38-9f04-3b61b8a84667"));
     *  user.setLocale(Locale.FRENCH);
     *  user.setComment("Access granted for Project1");
     * 
     *  // Create the client and call the update API
     *  Users client = new Users(properties);
     *  user = client.editUser(user);
     * </pre></div>
     */
    public void store(User item) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", item.getId().toString());
        getTarget().path("users/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(item), User.class);
       
    }
    
     /**
     * Retrieves a user account.
     * @param locator The content model of the UserLocatorLocator java model object can be used as path parameter.
     * <pre>
     *          id (required)         User ID specified as a path parameter. 
     * </pre>
     * @return The serialized User java model object that was retrieved:
     *          id
     *          username 
     *          locale 
     *          comment
     * @since ISecL 1.0
     * @mtwRequiresPermissions users:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users/e6c9337c-e709-4b38-9f04-3b61b8a84667
     * output: 
     * {
     *      "id":"e6c9337c-e709-4b38-9f04-3b61b8a84667",
     *      "username":"Developer1",
     *      "locale":"en-us",
     *      "comment":"Access needed for Project1"
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     *  // Create the user locator model and set the locator id
     * UserLocator locator = new UserLocator();
     * locator.id = UUID.valueOf("e6c9337c-e709-4b38-9f04-3b61b8a84667");
     * 
     * // Create the client and call the retrieve API
     * User client = new User(properties);
     * User user = client.retrieve(locator);
     * </pre></div>
     */
    public User retrieve(UserLocator locator) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", locator.id);
        User user = getTarget().path("users/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(User.class);
        return user;
    }
    
    /**
     * Searches for user account(s).
     * @param criteria The content models of the UserFilterCriteria java model object can be used as query parameters.
     * <pre>
     *          filter                    Boolean value to indicate whether the response should be filtered to return no 
     *                                    results instead of listing all users. Default value is true.
     *
     *          id                        User ID.
     *
     *          nameEqualTo               User name.Note that this is the name with which the user was created.
     *          
     *          nameContains              Substring of user name.
     * 
     *  Only one identifying parameter can be specified. The parameters listed here are in the order of priority that will be evaluated.
     * </pre>
     * @return The serialized UserCollection java model object that was searched with list of hosts each containing: 
     *              id
     *              username 
     *              locale 
     *              comment
     * 
     * @since ISecL 1.0
     * @mtwRequiresPermissions users:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/users?filter=false
     * output: 
     * {
     *      "users":[{
     *          "id":"3f2442cd-33d5-4d2b-8897-9c79a5dee0c4",
     *          "username":"tagservice","locale":"en-us"
     *      },
     *      {
     *          "id":"e6c9337c-e709-4b38-9f04-3b61b8a84667",
     *          "username":"Developer1",
     *          "locale":"fr",
     *          "comment":"Access granted for Project1"
     *      }]
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the user filter criteria model and set the search criteria
     * UserFilterCriteria criteria = new UserFilterCriteria();
     * criteria.filter = false;
     * 
     * // Create the client and call the search API
     * Users client = new Users(properties);
     * UserCollection users = client.search(criteria);
     * </pre></div>
     */
    public UserCollection search(UserFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        UserCollection users = getTargetPathWithQueryParams("users", criteria).request(MediaType.APPLICATION_JSON).get(UserCollection.class);
        return users;
    }
}
