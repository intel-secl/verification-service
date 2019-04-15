/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.user.management.rest.v2.model.RolePermissionCollection;
import com.intel.mtwilson.user.management.rest.v2.model.RolePermissionFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.RolePermission;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * These resources are used to manage permissions of a role.
 * <pre> Permissions have 3 parts. Domain, Action and Selection. 
 * 
 * Domains are basically resources on which the permissions would apply 
 * (Ex: Flavors, Hosts, etc). 
 * 
 * Action is to create, store, retrieve, search and delete. There can be 
 * special actions based on the resources like import & export in case 
 * of certificates. Multiple actions for a single domain can be separated by comma. 
 * 
 * Selection is currently not being used. By default it would be set to "*". 
 * This is for future purpose where users can specify certain conditions which if
 * evaluates to true would get the required permissions. 
 * 
 * User can provide "*" as the option for any combination of domain, action and 
 * selection. * indicates everything. Example: An administrator would have * for 
 * all the 3 options. 
 * </pre>
 */
public class RolePermissions extends MtWilsonClient {
    
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
     * RolePermissions client = new RolePermissions(properties);
     * </pre>
     * @throws Exception 
     */
    public RolePermissions(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Create a user role permission(s).
     * @param item The serialized RolePermission java model object represents the content of the request body.<br/>
     * <pre>
     * 
     *              permit_domain (optional)             resources on which permissions apply (eg. hosts, flavors).
     *              
     *              permit_action (optional)             actions on which the permission is required (eg. add, delete).
     *              
     *              permit_selection (optional)          user can specify a condition and if that evaluates to true, it will get the required permissions. By default, it is set to *
     * </pre>
     * @return The serialized RolePermissionCollection java model object that was created with collection of flavors each containing:
     * <pre>
     *              roleId
     *              permitDomain
     *              permitAction 
     *              permitSelection
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions role_permissions:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwPreRequisite Role Create API
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/roles/9e5c1b6b-44d6-40ea-90f3-4a8c32c140c2/permissions
     * input: 
     * {
     *      "permit_domain":"user_mgmt",
     *      "permit_action":"add,delete",
     *      "permit_selection":"*"
     * }
     * output: 
     * {
     *      "id":"9b35b89c-c5f0-4ffb-8f94-a7f73eef8f76",
     *      "role_id":"05f80052-2642-480a-8504-880e27ce8b57",
     *      "permit_domain":"user_mgmt",
     *      "permit_action":"add,delete",
     *      "permit_selection":"*"
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     *  // Create the role permission model 
     *  RolePermission rolePermission = new RolePermission();
     *  rolePermission.setRoleId("05f80052-2642-480a-8504-880e27ce8b57");
     *  rolePermission.setPermitDomain("user_mgmt");
     *  rolePermission.setPermitAction("add,delete");
     *  rolePermission.setPermitSelection("*");
     * 
     *  // Create the client and call the create API
     *  RolePermissions client = new RolePermissions(properties);
     *  RolePermissionCollection rolePermissionCollection = client.create(rolePermission);
     * </pre></div>
     */
    public RolePermissionCollection create(RolePermission item) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("role_id", item.getRoleId().toString());
        RolePermissionCollection newRolePermission = getTarget().path("roles/{role_id}/permissions").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).post(Entity.json(item), RolePermissionCollection.class);
        return newRolePermission;
    }
    
    /**
     * Search for user role permission(s).
     * @param criteria The content models of the RolePermissionFilterCriteria java model object can be used as query parameters.
     * <pre>
     *          filter                  Boolean value to indicate whether the response should be filtered to return no 
                                        results instead of listing all flavors. Default value is true.
     *          
     *          roleId                  Role ID.
     *  
     *          actionEqualTo           permision action regex pattern.
     *          
     *          domainEqualTo           resource name regex pattern.
     * 
     *  Only one of the above parameters can be specified. The parameters listed here are in the order of priority that will be evaluated.
     * 
     * </pre>
     * @return The serialized RolePermissionCollection java model object that was searched with list of role permissions each containing: 
     * <pre>
     *              roldeId
     *              permitDomain
     *              permitAction 
     *              permitSelection
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions role_permissions:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwPreRequisite Role Create API
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/roles/05f80052-2642-480a-8504-880e27ce8b57/permissions?actionEqualTo=*
     * output: 
     * {
     *      "role_permissions":[{
     *              "role_id":"05f80052-2642-480a-8504-880e27ce8b57",
     *              "permit_domain":"user_mgmt",
     *              "permit_action":"*","permit_selection":"*"
     *      }]
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the role permission filter criteria model and set the role id and filter 
     * RolePermissionFilterCriteria criteria = new RolePermissionFilterCriteria();
     * criteria.roleId = UUID.valueOf("05f80052-2642-480a-8504-880e27ce8b57");
     * criteria.filter = false;
     * 
     * // Create the client and call the search API
     * RolePermissions client = new RolePermissions(properties);
     * RolePermissionCollection rolePermissions = client.search(criteria);
     * </pre></div>
     */
    public RolePermissionCollection search(RolePermissionFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("role_id", criteria.roleId);
        RolePermissionCollection rolePermissions = getTargetPathWithQueryParams("roles/{role_id}/permissions", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(RolePermissionCollection.class);
        return rolePermissions;
    }
}
