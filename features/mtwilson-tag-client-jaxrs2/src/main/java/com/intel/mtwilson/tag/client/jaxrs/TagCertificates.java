/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.core.common.tag.model.TagCertificate;
import com.intel.mtwilson.tag.model.TagCertificateCollection;
import com.intel.mtwilson.tag.model.TagCertificateCreateCriteria;
import com.intel.mtwilson.tag.model.TagCertificateFilterCriteria;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

/**
 * These resources are used to manage the tag certificates.
 * <pre>
 * An "Asset Tag" is a host-specific certificate created with a set of
 * user-defined key/value pairs that can be used to "tag" a server at 
 * the hardware level. Asset Tags are included in host attestation, verifying 
 * that, if an Asset Tag has been deployed to a host, that the correct
 * Tag is included in the host's TPM Quote.  Attestation Reports will display 
 * the key/value pairs associated with the Asset Tag, and can be used by 
 * scheduler services or compliance audit reporting.  One typicall use case for
 * Asset Tags is "geolocation tagging," tagging each host with key/value pairs 
 * matching the physical location of the host. 
 * </pre>
 * @author ssbangal
 */
public class TagCertificates extends MtWilsonClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TagCertificates.class);

    /**
     * Constructor.
     * @param properties This java properties model must include server connection details for the API client initialization.
     * <pre>
     * mtwilson.api.url - Host Verification Service (HVS) base URL for accessing REST APIs
     * 
     * // basic authentication
     * mtwilson.api.username - Username for API basic authentication with the HVS
     * mtwilson.api.password - Password for API basic authentication with the HVS
     * mtwilson.api.tls.policy.certificate.sha256 - sha256 vlaue of the TLS Certificate for API basic authentication with the HVS
     * 
     *
     * <b>Example:</b>
     * Properties properties = new Properties();
     * properties.put(“mtwilson.api.url”, “https://server.com:port/mtwilson/v2”);
     * 
     * // basic authentication
     * properties.put(“mtwilson.api.username”, “user”);
     * properties.put(“mtwilson.api.password”, “*****”);
     * properties.put("mtwilson.api.tls.policy.certificate.sha256", "ae8b50d9a45d1941d5486df204b9e05a433e3a5bc13445f48774af686d18dcfc");
     * TagCertificates client = new TagCertificates(properties);
     * @throws Exception
    */
       
    public TagCertificates(Properties properties) throws Exception {
        super(properties);
    }

    /**
     * Creates a new tag certificate.
     * <pre>
     * Creates a new X509 certificate entry into the database table that can 
     * be used to provision an asset tag on a desired host. 
     * The hardware uuid of the host must be provided to create the tag certificate, it can be obtained 
     * using the host search API with any filter criteria. The tag certificate create API returns a tag certificate
     * object which is a model class with all the certificate attributes.
     * </pre>
     * @param obj The serialized TagCertificateCreateCriteria java model object represents the content of the request body.
     * <pre>
     *    hardwareUuid (required)           The hardware UUID of the host to which the tag certificate is associated.
     *
     * 
     *    selectionContent (required)       The selection content is an array of one or more key-value pairs with the tag
     *                                      selection attributes. The "name" attribute is used to store the keys and "value"
     *                                      attribute is used to store the values of the tag.
     *</pre>
     * @return <pre>The serialized TagCertificate java model object that was created:
     *          id                Id of tag certificate.
     *          certificate       The asset tag certificate in byte array format.
     *          subject           The hardware UUID of the host to which the tag certificate is associated.
     *          issuer            The issuer of the tag certificate.
     *          not_after         The time stamp after which the certificate is not valid.
     *          not_before        The timestamp when the certificate was created.
     *          hardware_uuid     The hardware UUID of the host to which the tag certificate is associated.</pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions tag_certificates:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall      
     * <pre>
     * https://server.com:8443/mtwilson/v2/tag-certificates
     * Input:
     * 
     * {
     *     "hardware_uuid"      : "216d20b1-ea6a-e211-bc9e-001e677cxxxx",
     *     "selection_content"  : [
     *         {
     *             "name"  : "Location",
     *             "value" : "SantaClara"
     *         },
     *         {
     *             "name"  : "Company",
     *             "value" : "IntelCorporation"
     *         }
     *      ]
     * }
     *
     * Output: 
     * {
     *      "id"             :  "94455527-4a6a-42b7-b03d-a3e325b94d9f",
     *      "certificate"    :  "MIIB1zCBwAIBATAfoR2kGzAZMRcwFQYBaQQQIW0gsepq4hG8ngAeZ3z4e
     *                          aAgMB6kHDAaMRgwFgYDVQQDDA9hc3NldFRhZ1NlcnZpY2UwDQYJKoZIhvcNAQELBQACBgFfzL
     *                          NwJjAiGA8yMDE3MTExODAxMTcxMloYDzIwMTgxMTE4MDExNzEyWjA9MB4GBVUEhhUCMRUwEww
     *                          JRGV2ZWxvcGVyMAYMBFJ5YW4wGwYFVQSGFQIxEjAQDARDaXR5MAgMBkZvbHNvbTANBgkqhkiG
     *                          9w0BAQsFAAOCAQEAbCp28tijYhQOTek2/s+E6CGMb2mCDQOHpGNDFuxHox8DRaSe/qN+4miAy
     *                          ZZuN4/lCeLokBZ4ZQDmCBuACXu6nHeP+s1G3egznfLuqXv7LKp4aFoeY6gMPDhNbm4QzOXY7V
     *                          A4PdT28ewgzTY5tM3x6mIXzT95LgUwYNrzDTa9c4lwLM9tXLbZ4R+SRan7/QpIwmid17XJu+a
     *                          Orna5rKmqCNu0bdKz+eTz6qv3fxh+D2gL6FZAxRFroem9UGoQ8Vpp3RGlk3Zf6ipPM+tym01z
     *                          a73uJKQIEhHw5rm01gb5/H7bZPYqVb278uxSUBrDRXjRPYjlwmacEcxpM0IU7+hIBQ==",
     *      "subject"        :  "216d20b1-ea6a-e211-bc9e-001e677cf879",
     *      "issuer"         :  "CN=assetTagService",
     *      "not_before"     :  "2018-03-08T13:51:38-0800",
     *      "not_after"      :  "2019-03-08T13:51:38-0800",
     *      "hardware_uuid"  :  "216d20b1-ea6a-e211-bc9e-001e677xxxx"
     * }
     * </pre>
     * @mtwSampleApiCall   
     * <pre>
     *  // initialize the client with the host verification service properties and call the create tag certificate method
     *  TagCertificates client = new TagCertificates(properties);
     *  TagCertificate obj = new TagCertificate();
     *  TagKvAttribute tagAttribute = new TagKvAttribute();
     *  tagAttribute.setName("Location");
     *  tagAttribute.setValue("SantaClara");
     *  ArrayList<TagKvAttribute> selectionContent = new ArrayList();
     *  selectionContent.add(tagAttribute); 
     *  obj.setSelectionContent(selectionContent);
     *  obj.setHardwareUuid("216d20b1-ea6a-e211-bc9e-001e677cxxxx");     *  
     *  obj = client.createCertificate(obj);
     * </pre>
     */
    public TagCertificate createCertificate(TagCertificateCreateCriteria obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        TagCertificate createdObj = getTarget().path("tag-certificates").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), TagCertificate.class);
        return createdObj;
    }

     /**
     * Deletes the tag certificate with the specified ID.
     * @param certificateId Id of the tag certificate to be deleted.
     * @since ISecL 1.0
     * @mtwRequiresPermissions tag_certificate:delete
     * @mtwContentTypeReturned None
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/tag-certificates/695e8d32-0dd8-46bb-90d6-d2520ff5e2f0
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  TagCertificate client = new TagCertificate(properties);
     *  client.deleteCertificate("695e8d32-0dd8-46bb-90d6-d2520ff5e2f0");
     * </pre>
     */
    public void deleteCertificate(String certificateId) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", certificateId);
        Response obj = getTarget().path("tag-certificates/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        if (!obj.getStatusInfo().getFamily().equals(Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete certificate failed");
        }
    }

    /**
     * Retrieves the tag certificates based on the search criteria specified.
     * @param criteria The content models of the TagCertificateFilterCriteria java model object can be used as query parameters.
     * <pre>
     *    id                    Id of the asset tag certificate in the host verification service database table.
     *    subjectEqualTo        Complete host hardware UUID. 
     *    subjectContains       Partial or complete host hardware UUID.  
     *    issuerEqualTo         Issuer of the asset tag certificate. 
     *    issuerContains        Partial issuer name of the asset tag certificate.
     *    statusEqualTo         The status of the certificate.
     *    validOn               The timestamp at which the certificate is valid on.
     *    validBefore           The timestamp before which the certificate should be valid.
     *    validAfter            The timestamp after which the certificate should be valid.
     *    hardwareUuid          The hardware UUID of the host
     *
     * If the user wants to retrieve all the records, filter=false criteria can be specified.
     * This would override any other filter criteria that the user would have specified.
     * </pre>
     * @return <pre>The serialized TagCertificateCollection java model object that was searched with collection of tag certificates each containing:
     *          id
     *          certificate
     *          subject
     *          issuer
     *          not_before
     *          not_after
     *          hardware_uuid</pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions tag_certificates:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall      <pre>
     * https://server.com:8443/mtwilson/v2/tag-certificates?subjectEqualTo=064866ea-620d-11e0-b1a9-001e671043c4
     * 
     * 
     * Output: 
     * {
     *      "tag_certificates": [
     *          {
     *              "id": "f472a68e-108c-41f1-89d9-5681835f752a",
     *              "certificate": "MIIB1DCBvQIBATAfoR2kGzAZMRcwFQYBaQQQgOVDQpTy5xGQbgAVYKBAYqAgMB6kHDAaMRgwFgYDVQ
     *                              QDDA9hc3NldFRhZ1NlcnZpY2UwDQYJKoZIhvcNAQELBQACBgFiB5kf2zAiGA8yMDE4MDMwODIxNTEz
     *                              OFoYDzIwMTkwMzA4MjE1MTM4WjA6MBsGBVUEhhUCMRIwEAwEa2V5MjAIDAZ2YWx1ZTEwGwYFVQSGFQ
     *                              IxEjAQDARrZXkzMAgMBnZhbHVlMTANBgkqhkiG9w0BAQsFAAOCAQEAop3a2dNjYtlCW2tAj4XAHQYS
     *                              cgTvyAV9by5ap28GZA95X4VmEnZZh60DCbrDq3JVLZ0LQ4kefyHXMl8R4oTgIkDArPVJwyv4um5WRi
     *                              FH+n4kYrL/tFk41vyC9jBoQVnTszZqxPJ+CAh6aklBv/HLvHWo3UYFdseuKO/aS3A7BtceVMDQ9d9C
     *                              JNL+1HZ0KMUl62eO87vQ4iS15/OQ/CG9oA5YuZtUXORyo5Qk0/MMnupd9X2QLJtgMDD1oApAFK4mxq
     *                              4/0CfZ7GjGagIjeN8mgGhsD0NZQvzr87DSyoeJe4sUx6Dj0yP21Dm3ameu5vFxm1qE2ohkvpcNRdg==",
     *              "subject": "064866ea-620d-11e0-b1a9-001e671043c4",
     *              "issuer": "CN=assetTagService",
     *              "not_before": "2018-03-08T13:51:38-0800",
     *              "not_after": "2019-03-08T13:51:38-0800",
     *              "hardware_uuid": "80e54342-94f2-e711-906e-001560a04062"
     *           }
     *      ]
     *  }
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  TagCertificate client = new TagCertificate(properties);
     *  TagCertificateFilterCriteria criteria = new TagCertificateFilterCriteria();
     *  criteria.subjectEqualTo = "064866ea-620d-11e0-b1a9-001e671043c4";
     *  TagCertificateCollection objCollection = client.searchCertificates(criteria);
     * </pre>
     */
    public TagCertificateCollection searchCertificates(TagCertificateFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        TagCertificateCollection objCollection = getTargetPathWithQueryParams("tag-certificates", criteria)
                .request(MediaType.APPLICATION_JSON).get(TagCertificateCollection.class);
        return objCollection;
    }

}
