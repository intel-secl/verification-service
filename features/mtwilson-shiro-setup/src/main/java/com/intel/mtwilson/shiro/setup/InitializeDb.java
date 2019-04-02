/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.setup;

import com.intel.mtwilson.user.management.rest.v2.model.RolePermission;
import com.intel.mtwilson.user.management.rest.v2.model.Role;
import com.intel.mtwilson.setup.DatabaseSetupTask;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * we do not store the admin username or password in configuration - the application
 * must display them to the administrator
 * 
 * @author jbuhacoff
 */
public class InitializeDb extends DatabaseSetupTask {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InitializeDb.class);
    private static final String ADMINISTRATOR_ROLE = "administrator";
    private static final String AUDITOR_ROLE = "auditor";
    private static final String ASSET_TAG_MANAGER_ROLE = "asset_tag_manager";
    private static final String FLAVOR_MANAGER_ROLE = "flavor_manager";
    private static final String HOST_MANAGER_ROLE = "host_manager";
    private static final String REPORTS_MANAGER_ROLE = "reports_manager";
    private static final String HOST_BASED_FLAVOR_CREATOR_ROLE = "host_based_flavor_creator";
    private static final String HOST_UNIQUE_FLAVOR_CREATOR_ROLE = "host_unique_flavor_creator";
    private static final String TRUSTAGENT_PROVISIONER_ROLE = "trustagent_provisioner";
    private HashMap<String, HashMap<String, String>> roleDomainActions = null;
    
    @Override
    protected void configure() throws Exception {
        roleDomainActions = createDomainActionListForRole();
        try (Connection c = My.jdbc().connection()) {
            // data migrated from these mtwilson 1.2 tables:
            requireTable(c, "mw_role");
            requireTable(c, "mw_role_permission");
        }
    }

    @Override
    protected void execute() throws Exception {
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            createRoleAndAssociatedPermissions(loginDAO, ADMINISTRATOR_ROLE, "", roleDomainActions.get(ADMINISTRATOR_ROLE));
            createRoleAndAssociatedPermissions(loginDAO, AUDITOR_ROLE, "", roleDomainActions.get(AUDITOR_ROLE));
            createRoleAndAssociatedPermissions(loginDAO, ASSET_TAG_MANAGER_ROLE, "", roleDomainActions.get(ASSET_TAG_MANAGER_ROLE));
            createRoleAndAssociatedPermissions(loginDAO, FLAVOR_MANAGER_ROLE, "", roleDomainActions.get(FLAVOR_MANAGER_ROLE));
            createRoleAndAssociatedPermissions(loginDAO, HOST_MANAGER_ROLE, "", roleDomainActions.get(HOST_MANAGER_ROLE));
            createRoleAndAssociatedPermissions(loginDAO, REPORTS_MANAGER_ROLE, "", roleDomainActions.get(REPORTS_MANAGER_ROLE));
            createRoleAndAssociatedPermissions(loginDAO, HOST_BASED_FLAVOR_CREATOR_ROLE, "", roleDomainActions.get(HOST_BASED_FLAVOR_CREATOR_ROLE));
            createRoleAndAssociatedPermissions(loginDAO, HOST_UNIQUE_FLAVOR_CREATOR_ROLE, "", roleDomainActions.get(HOST_UNIQUE_FLAVOR_CREATOR_ROLE));
            createRoleAndAssociatedPermissions(loginDAO, TRUSTAGENT_PROVISIONER_ROLE, "", roleDomainActions.get(TRUSTAGENT_PROVISIONER_ROLE));
        }
    }

    /**
     * Provided the role name and its associated permissions, this function will initialize the database with those records.
     * @param loginDAO
     * @param roleName
     * @param roleDesc
     * @param domainActions
     */
    private void createRoleAndAssociatedPermissions(LoginDAO loginDAO, String roleName, String roleDesc, HashMap<String, String> domainActions) {
        Role role = loginDAO.findRoleByName(roleName);
        if (role == null) {
            role = new Role();
            role.setId(new UUID());
            role.setRoleName(roleName);
            role.setDescription(roleDesc);
            loginDAO.insertRole(role.getId(), role.getRoleName(), role.getDescription());
        }

        // Create the associated permissions in the mw_role_permission table
        for(Map.Entry<String, String> entry : domainActions.entrySet()) {
            String domain = entry.getKey();
            String actions = entry.getValue();

            RolePermission rolePerm = loginDAO.findAllRolePermissionsForRoleIdDomainActionAndSelection(role.getId(), domain, actions, "*");
            if (rolePerm == null) {
                rolePerm = new RolePermission();
                rolePerm.setRoleId(role.getId());
                rolePerm.setPermitDomain(domain);
                rolePerm.setPermitAction(actions);
                rolePerm.setPermitSelection("*"); // Since we are currently not using this, we will set it to *
                loginDAO.insertRolePermission(rolePerm.getRoleId(), rolePerm.getPermitDomain(), rolePerm.getPermitAction(), rolePerm.getPermitSelection());
            }
        }
    }

    @Override
    protected void validate() throws Exception {
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            validateRoleAndAssociatedPermissions(loginDAO, ADMINISTRATOR_ROLE, roleDomainActions.get(ADMINISTRATOR_ROLE));
            validateRoleAndAssociatedPermissions(loginDAO, AUDITOR_ROLE, roleDomainActions.get(AUDITOR_ROLE));
            validateRoleAndAssociatedPermissions(loginDAO, ASSET_TAG_MANAGER_ROLE, roleDomainActions.get(ASSET_TAG_MANAGER_ROLE));
            validateRoleAndAssociatedPermissions(loginDAO, FLAVOR_MANAGER_ROLE, roleDomainActions.get(FLAVOR_MANAGER_ROLE));
            validateRoleAndAssociatedPermissions(loginDAO, HOST_MANAGER_ROLE, roleDomainActions.get(HOST_MANAGER_ROLE));
            validateRoleAndAssociatedPermissions(loginDAO, REPORTS_MANAGER_ROLE, roleDomainActions.get(REPORTS_MANAGER_ROLE));
            validateRoleAndAssociatedPermissions(loginDAO, HOST_BASED_FLAVOR_CREATOR_ROLE, roleDomainActions.get(HOST_BASED_FLAVOR_CREATOR_ROLE));
            validateRoleAndAssociatedPermissions(loginDAO, HOST_UNIQUE_FLAVOR_CREATOR_ROLE, roleDomainActions.get(HOST_UNIQUE_FLAVOR_CREATOR_ROLE));
            validateRoleAndAssociatedPermissions(loginDAO, TRUSTAGENT_PROVISIONER_ROLE, roleDomainActions.get(TRUSTAGENT_PROVISIONER_ROLE));
        }
    }

    /**
     * Provided the role name and its associated permissions, this function will validate the database with those records.
     * @param loginDAO
     * @param roleName
     * @param domainActions
     */
    private void validateRoleAndAssociatedPermissions(LoginDAO loginDAO, String roleName, HashMap<String, String> domainActions) {
        Role role = loginDAO.findRoleByName(roleName);
        if (role == null) {
            validation("Role '" + roleName + "' not present in database");
            return;
        }

        for(Map.Entry<String, String> entry : domainActions.entrySet()) {
            String domain = entry.getKey();
            String actions = entry.getValue();

            RolePermission rolePerm = loginDAO.findAllRolePermissionsForRoleIdDomainActionAndSelection(role.getId(), domain, actions, "*");
            if (rolePerm == null) {
                validation("Permissions associated with role '" + roleName + "' not present in database");
            }
        }
    }

    /**
     * This function returns the list of default permissions associated with role.
     * @return
     */
    private HashMap<String, HashMap<String, String>> createDomainActionListForRole() {
        HashMap<String, HashMap<String, String>> roleDomainActions = new HashMap<>();
        HashMap<String, String> domainActions = new HashMap<>();
        
        domainActions.put("*", "*");
        roleDomainActions.put(ADMINISTRATOR_ROLE, domainActions);

        domainActions = new HashMap<>();
        domainActions.put("tag_certificates", "*");
        domainActions.put("tag_certificate_requests", "*");
        domainActions.put("tag_flavors", "create");
        roleDomainActions.put(ASSET_TAG_MANAGER_ROLE, domainActions);

        domainActions = new HashMap<>();
        domainActions.put("hosts", "*");
        roleDomainActions.put(HOST_MANAGER_ROLE, domainActions);

        domainActions = new HashMap<>();
        domainActions.put("host_aiks", "certify");
        domainActions.put("tpm_endorsements", "create,search");
        domainActions.put("tpm_passwords", "*");
        domainActions.put("tpms", "endorse");
        domainActions.put("host_signing_key_certificates", "create");
        domainActions.put("store_host_pre_registration_details", "create");
        roleDomainActions.put(TRUSTAGENT_PROVISIONER_ROLE, domainActions);

        domainActions = new HashMap<>();
        domainActions.put("*", "search,retrieve");
        roleDomainActions.put(AUDITOR_ROLE, domainActions);

        domainActions = new HashMap<>();
        domainActions.put("reports", "*");
        roleDomainActions.put(REPORTS_MANAGER_ROLE, domainActions);

        domainActions = new HashMap<>();
        domainActions.put("flavors", "*");
        domainActions.put("flavorgroups", "*");
        roleDomainActions.put(FLAVOR_MANAGER_ROLE, domainActions);

        domainActions = new HashMap<>();
        domainActions.put("host_based_flavors", "create");
        roleDomainActions.put(HOST_BASED_FLAVOR_CREATOR_ROLE, domainActions);

        domainActions = new HashMap<>();
        domainActions.put("host_unique_flavors", "create");
        roleDomainActions.put(HOST_UNIQUE_FLAVOR_CREATOR_ROLE, domainActions);
        return roleDomainActions;
    }
}
