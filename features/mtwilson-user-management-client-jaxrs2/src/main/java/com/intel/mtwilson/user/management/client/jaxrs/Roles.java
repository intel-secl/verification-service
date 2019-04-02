/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.user.management.rest.v2.model.RoleCollection;
import com.intel.mtwilson.user.management.rest.v2.model.RoleFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.Role;
import com.intel.mtwilson.user.management.rest.v2.model.RoleLocator;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * These resources are used to manage roles.
 * <pre>
 * The roles created are assigned to users and the permissions associated with 
 * them are applied to the user to perform actions.
 * </pre>
 */
public class Roles extends MtWilsonClient {
    
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
     * Roles client = new Roles(properties);
     * </pre>
     * @throws Exception 
     */
    public Roles(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Creates a user role.
     * <pre>
     * The roles created are assigned to users and the permissions associated with 
     * them are applied to the user to perform actions.
     * Creating a role also requires creating RolePermissions to assign to the role.
     * This method only creates a role and requires RolePermissions to be executed which allows 
     * permissions to be assigned to the roles.
     * </pre>
     * @param item The serialized Role java model object represents the content of the request body.<br/>
     * <pre>
     *              id (optional)                         id of the role to be created.
     * 
     *              roleName (required)                   name of the role to be created.
     *              
     *              description (optional)                description of role.
     * </pre>
     * @return The serialized RoleCollection java model object that was created :
     * <pre>
     *              id
     *              roleName
     *              description
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions roles:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/roles
     * input: 
     * {
     *      "role_name":"MTW_Admin"
     * }
     * output: 
     * {
     *      "id":"17dbfd48-12a4-4328-85af-43b0d5adfee3",
     *      "role_name":"MTW_Admin"
     * }
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     * // Create the role item model and set the role name
     * Role role = new Role();
     * role.setRoleName("MTW_Admin");
     * 
     * // Create the client and call the create API 
     * Roles client = new Roles(properties);
     * RoleCollection createRole = client.create(role);
     * </pre>
     */
    public RoleCollection create(Role item) {
        log.debug("target: {}", getTarget().getUri().toString());
        RoleCollection newRole = getTarget().path("roles").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(item), RoleCollection.class);
        return newRole;
      }
    
    /**
     * Deletes a user role.
     * @param locator The content models of the RoleLocator java model object can be used as path parameter.
     * <pre>
     *              id (required)           Role ID specified as a path parameter.
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions roles:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/roles/17dbfd48-12a4-4328-85af-43b0d5adfee3
     * 
     * output: 204 No content
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the role locator model and set the role id
     * RoleLocator locator = new RoleLocator();
     * locator.id = 17dbfd48-12a4-4328-85af-43b0d5adfee3;
     * 
     * // Create the client and call the delete API
     * Roles client = new Roles(properties);
     * client.delete(locator);
     * </pre></div>
     */
    public void delete(RoleLocator locator) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", locator.id);
        Response role = getTarget().path("roles/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        if( !role.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete role failed");
        }
    }

    /**
     * Updates a user role.
     * @param item The serialized Role java model object represents the content of the request body.<br/>
     * <pre>
     *
     *              id                          ID of role.
     *
     *              name                        name of the role to be created.
     *
     *              description                 description of role.
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions roles:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/roles/17dbfd48-12a4-4328-85af-43b0d5adfee3
     * input: 
     * {
     *      "description":"MTW Admin role"
     * }
     * output: 
     * {
     *      "id":"17dbfd48-12a4-4328-85af-43b0d5adfee3",
     *      "description":"MTW Admin role"
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     *  // Create the role model and set role id and description
     *  Role role = new Role();
     *  role.setId(UUID.valueOf("17dbfd48-12a4-4328-85af-43b0d5adfee3"));
     *  role.setDescription("MTW Admin role");
     * 
     *  // Create the client and call the update API
     *  Roles client = new Roles(properties);
     *  role = client.editRole(role);
     * </pre></div>
     */
    public void store(Role item) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", item.getId().toString());
        getTarget().path("roles/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(item), Role.class);
    }
    
     /**
     * Retrieves a user role.
     * @param locator The content models of the ReportLocator java model object can be used as path parameter.
     * <pre>
     *          id (required)         Role ID specified as a path parameter. 
     * </pre>
     * @return The serialized Role java model object that was retrieved: 
     * <pre>
     *             id
     *             role_name
     *             description
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions roles:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/roles/17dbfd48-12a4-4328-85af-43b0d5adfee3
     * output: 
     * {
     *      "id":"17dbfd48-12a4-4328-85af-43b0d5adfee3",
     *      "role_name":"MTW_Admin",
     *      "description":"MTW Admin role"
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the role locator model and set the locator id
     * RoleLocator locator = new RoleLocator();
     * locator.id = UUID.valueOf("730bc182-17f5-47dd-bfbf-32e7cea0270d");
     * 
     * // Create the client and call the retrieve API
     * Roles client = new Roles(properties);
     * Role role = client.retrieve(locator);
     * </pre></div>
     */
    public Role retrieve(RoleLocator locator) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", locator.id);
        Role role = getTarget().path("roles/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(Role.class);
        return role;
    }
    
    /**
     * Searches for user role(s).
     * @param criteria The content models of the RoleFilterCriteria java model object can be used as query parameters.
     * <pre>
     * 
     *          filter                   Boolean value to indicate whether the response should be filtered to return no 
                                         results instead of listing all roles. Default value is true.
     *
     *          id                       Role ID.
     *
     *          nameEqualTo              Role name. Note that this is the name with which the role is created.
     *          
     *          nameContains             Substring of role name.
     * 
     * Only one identifying parameter can be specified. The parameters listed here are in the order of priority that will be evaluated.
     * 
     * </pre>
     * @return The serialized RoleCollection java model object that was searched with list of hosts each containing:
     * <pre>
     *              id
     *              name 
     *              description
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions roles:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/roles?filter=false
     * output: 
     * {
     *      "roles":[{
     *          "id":"0199a936-9a49-482a-8c63-cfe7a9412d7e",
     *          "role_name":"server_manager"
     *      },
     *      {
     *          "id":"177b1d3c-b0aa-4543-8509-92fde907a4a9",
     *          "role_name":"admin",
     *          "description":"user created role"
     *      }]
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     *  // Create the role filter criteria model and set the filter criteria to false
     *  RoleFilterCriteria criteria = new RoleFilterCriteria();
     *  criteria.filter = false;
     *  
     * // Create the client and call the search API
     *  Roles client = new Roles(properties);
     *  RoleCollection roles = client.search(criteria);
     * </pre></div>
     */
    public RoleCollection search(RoleFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        RoleCollection roles = getTargetPathWithQueryParams("roles", criteria).request(MediaType.APPLICATION_JSON).get(RoleCollection.class);
        return roles;
    }
}
