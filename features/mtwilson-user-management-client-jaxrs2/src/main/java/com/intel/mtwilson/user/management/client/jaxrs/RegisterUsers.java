/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.user.management.rest.v2.model.RegisterUserWithCertificate;
import java.util.LinkedHashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This resource is used to create and register a user for Host Verification System.
 * <pre>
 * The class provides one API to create and register users with certificate.
 * This class is basically a combination of createUser and createUserLoginCertificate functions.
 * </pre>
 */
public class RegisterUsers extends MtWilsonClient {
    
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
     * RegisterUsers client = new RegisterUsers(properties);
     * </pre>
     * @throws Exception 
     */
    public RegisterUsers(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Registers new users with certificate based authentication for logging into the system. 
     * @param registerUserWithCertificate The serialized RegisterUserWithCertificate java model object represents the content of the request body.
     * <pre>
     *              user                        user details that include following fields
     *                                          username            username for login
     *                                          locale              location where user resides
     *                                          comment             comment
     * 
     *              user_login_certificate      certificate details that includes following fields
     *                                          certificate         certificate that is required for login.
     *                                                              Must explicitly specify PEM encoded certificate which contains ASCII without the prefix "--BEGIN.." and "---END".
     *                                          sha1Hash            sha1 hash of certificate
     *                                          sha256Hash          sha256 hash of certificate
     *                                          expires             expiration date of certificate
     * </pre>
     * @return boolean indicating whether the request was successful or not.
     * @since ISecL 1.0
     * @mtwRequiresPermissions users:create,user_login_certificates:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/rpc/register-user-with-certificate
     * input: 
     * {
     *      "user":
     *      {
     *          "username":"superadmin99",
     *          "locale":"en_US",
     *          "comment":"Need to manage user accounts."
     *      },
     *      "user_login_certificate":
     *      {
     *          "certificate":"MIICrjCCAZagAwIBAgIIEGqMm0g6T4YwDQYJKoZI....."
     *      }
     * }
     * output: 
     * {
     *      "user":
     *      {
     *          "username":"superadmin99",
     *          "locale":"en_US",
     *          "comment":"Need to manage user accounts."
     *      },
     *      "user_login_certificate":
     *      {
     *          "certificate":"MIICrjCCAZa.....yqXLtyU8JHQkKT",
     *          "enabled":false
     *      },
     *      "result":true
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create user model and set user name , locale and comment
     * User user = new User();
     * user.setUsername("superadmin99");
     * user.setLocale(Locale.US);
     * user.setComment("Need to manage user accounts."); 
     * 
     * //Generate the certificate with the username
     * KeyPair keyPair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
     * X509Certificate certificate = X509Builder.factory().selfSigned(String.format
     *                  ("CN=%s", userName), keyPair).expires(365, TimeUnit.DAYS).build();
     * 
     * // Create user login certificate model and set the certificate
     * UserLoginCertificate userLoginCertificate = new UserLoginCertificate();
     * userLoginCertificate.setCertificate(certificate.getEncoded());
     * 
     * // Create register user with certificate model and set the user and user login certificate
     * RegisterUserWithCertificate rpcUserWithCert = new RegisterUserWithCertificate(); 
     * rpcUserWithCert.setUser(user);
     * rpcUserWithCert.setUserLoginCertificate(userLoginCertificate);
     * 
     * // Create the client and call the create API
     * RegisterUsers client = new RegisterUsers(properties);
     * boolean registerUserWithCertificate = client.registerUserWithCertificate(rpcUserWithCert);
     * </pre></div>
     */
    public boolean registerUserWithCertificate(RegisterUserWithCertificate registerUserWithCertificate) {
        boolean isUserRegistered = false;
        System.out.println(getTarget().getUri().toString());
        log.debug("target: {}", getTarget().getUri().toString());
        Object result = getTarget().path("rpc/register-user-with-certificate").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(registerUserWithCertificate), Object.class);
        if (result.getClass().equals(LinkedHashMap.class)) {
            LinkedHashMap resultMap = (LinkedHashMap)result;
            if (resultMap.containsKey("result")) {
                isUserRegistered = Boolean.parseBoolean(resultMap.get("result").toString().trim());
                log.debug("Result of user registration with certificate is {}.", isUserRegistered);
            }
        }
        return isUserRegistered;
    }
}
