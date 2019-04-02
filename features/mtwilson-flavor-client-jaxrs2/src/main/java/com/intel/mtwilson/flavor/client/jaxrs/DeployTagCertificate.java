/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import java.util.Properties;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

/**
 * This resource is used to deploy asset tag certificate to a host.
 * <pre>
 * 
 * An asset tag is a key value pair attribute that can be assigned to an individual host for identification and trust purposes. 
 * The Host Verification Service exposes a REST API to create an asset tag certificate which contains the asset tag key value 
 * pair attributes (See mtwilson-tag-client-jaxrs2).
 * 
 * In order for an asset tag certificate to be associated with a given host, the SHA256 digest bytes of the asset tag certificate 
 * are stored in a specific TPM NVRAM index of the host. For hosts which have the Intel Trust Agent running (Linux and Windows), 
 * the asset tag certificate digest value can be deployed or pushed directly to the host using the REST API described here.
 * 
 * VMWare ESXi hosts do not run the Intel Trust Agent, and therefore, a different method must be used to provision the asset tag. 
 * See the product guide for details on how to provision asset tags for VMWare ESXi hosts.
 * </pre>   
 * @author ssbangal
 */
public class DeployTagCertificate extends MtWilsonClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DeployTagCertificate.class);

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
     * DeployTagCertificate client = new DeployTagCertificate(properties);
     * </pre>
     * @throws Exception 
     */
    public DeployTagCertificate(Properties properties) throws Exception {
        super(properties);
    }    
        
    /**
     * Retrieves the specified asset tag certificate, verifies it, 
     * and deploys it to the host specified in the asset tag certificate subject. 
     * If the asset tag certificate is successfully deployed to the host, an 
     * ASSET_TAG flavor is created for the host and the host is queued for flavor 
     * verification. The TagCertificates REST API is used to manage the certificates
     * that are deployed on the host.  
     * @param certificateId UUID of the certificate that needs to be deployed on the host.
     * This UUID is obtained when a TagCertificate is created using the TagCertificates API. 
     * @since ISecL 1.0
     * @mtwRequiresPermissions tag_certificates:deploy
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwPreRequisite Tag Certificate Create API
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:port/mtwilson/v2/rpc/deploy-tag-certificate
     * Input: {
     *      "certificate_id":"a6544ff4-6dc7-4c74-82be-578592e7e3ba"
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the client and call the deployTagCertificate API with certificate id as an input
     * DeployTagCertificate client = new DeployTagCertificate(properties);
     * client.deployTagCertificate(UUID.valueOf("a6544ff4-6dc7-4c74-82be-578592e7e3ba"));
     * </pre></div>
     */
    public void deployTagCertificate(UUID certificateId) {
        log.debug("target: {}", getTarget().getUri().toString());
        Response obj = getTarget().path("rpc/deploy-tag-certificate").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(certificateId));
        if( !obj.getStatusInfo().getFamily().equals(Family.SUCCESSFUL)) {
            throw new WebApplicationException("Deploy tag certificate failed");
        }
    }
        
}
