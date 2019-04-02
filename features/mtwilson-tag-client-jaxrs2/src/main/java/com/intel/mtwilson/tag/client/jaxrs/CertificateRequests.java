//package com.intel.mtwilson.tag.client.jaxrs;
//
//import com.intel.dcsg.cpg.io.UUID;
//import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
//import com.intel.mtwilson.tag.model.CertificateRequest;
//import com.intel.mtwilson.tag.model.CertificateRequestCollection;
//import com.intel.mtwilson.tag.model.CertificateRequestFilterCriteria;
//import com.intel.mtwilson.tag.model.CertificateRequestLocator;
//import java.net.URL;
//import java.util.HashMap;
//import java.util.Properties;
//import javax.ws.rs.WebApplicationException;
//import javax.ws.rs.client.Entity;
//import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.Response;
//import javax.ws.rs.core.Response.Status.Family;
//
///**
// * These resources are used to manage the certificate requests.
// * <pre>
// * The certificate request API would be used only if the certificates are created by an 
// * external CA. This request would contain the key-value pairs that needs to be added to the certificate
// * and stored in the Certificate request database of the host verification service in an encrypted format.
// * </pre>
// * @author ssbangal
// */
//public class CertificateRequests extends MtWilsonClient {
//
//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertificateRequests.class);
//
//    /**
//     * Constructor.
//     * @param properties This java properties model must include server connection details for the API client initialization.
//     * <pre>
//     * mtwilson.api.url - Host Verification Service (HVS) base URL for accessing REST APIs
//     * 
//     * // basic authentication
//     * mtwilson.api.username - Username for API basic authentication with the HVS
//     * mtwilson.api.password - Password for API basic authentication with the HVS
//     * mtwilson.api.tls.policy.certificate.sha256 - sha256 vlaue of the TLS Certificate for API basic authentication with the HVS
//     * 
//     * <b>Example:</b>
//     * Properties properties = new Properties();
//     * properties.put(“mtwilson.api.url”, “https://server.com:port/mtwilson/v2”);
//     * 
//     * // basic authentication
//     * properties.put(“mtwilson.api.username”, “user”);
//     * properties.put(“mtwilson.api.password”, “*****”);
//     * properties.put("mtwilson.api.tls.policy.certificate.sha256", "ae8b50d9a45d1941d5486df204b9e05a433e3a5bc13445f48774af686d18dcfc");
//     * CertificateRequests client = new CertificateRequests(properties);
//     * </pre>
//     * @throws Exception
//    */
//    public CertificateRequests(Properties properties) throws Exception {
//        super(properties);
//    }    
//    /**
//     * Creates a new asset tag certificate request.
//     * <pre>
//     * The certificate request would be created only if the certificates are created by an external CA.
//     * The tag attributes (key-value pairs) that needs to be added to the certificate would also be stored
//     * in the certificate request database in an encrypted format.
//     * </pre>
//     * @param obj The serialized CertificateRequest java model object represents the content of the request body.
//     * <pre>
//     *       subject (required)             Subject is the hardware UUID of the host associated with the certificate.
//     *                                      Can be retrieved by calling into the GET method on the host with a specific filter criteria.
//     * 
//     *       status (optional)              Status is a status of the certificate request created. The default value is "New".
//     *
//     * 
//     *       content (required)             The certificate in base 64 encoded format.
//     *
//     * 
//     *       contentType (optional)         The value is application/xml for plain xml,  message/rfc822 for the encrypted xml with
//     *                                      headers,  application/json  for the json request.
//     * </pre>
//     * @return <pre>The serialized CertificateRequest java model object that was created:
//     *          id
//     *          subject
//     *          status
//     *          content
//     *          content_type</pre>
//     * @since ISecL 1.0
//     * @mtwRequiresPermissions tag_certificate_requests:create
//     * @mtwMethodType POST
//     * @mtwSampleRestCall
//     * <pre>
//     * https://server.com:8443/mtwilson/v2/tag-certificate-requests
//     * Input: 
//     * 
//     * {
//     *      "subject" : "80e54342-94f2-e711-906e-001560a04062",
//     *      "content" : "MIIB1DCBvQIBATAfoR2kGzAZMRcwFQYBaQQQgOVDQpTy5xGQbgAVYK
//     *                   BAYqAgMB6kHDAaMRgwFgYDVQQDDA9hc3NldFRhZ1NlcnZpY2UwDQYJK
//     *                   oZIhvcNAQELBQACBgFiB5kf2zAiGA8yMDE4MDMwODIxNTEzOFoYDzIw
//     *                   MTkwMzA4MjE1MTM4WjA6MBsGBVUEhhUCMRIwEAwEa2V5MjAIDAZ2YWx1
//     *                   ZTEwGwYFVQSGFQIxEjAQDARrZXkzMAgMBnZhbHVlMTANBgkqhkiG9w0B
//     *                   AQsFAAOCAQEAop3a2dNjYtlCW2tAj4XAHQYScgTvyAV9by5ap28GZA95
//     *                   X4VmEnZZh60DCbrDq3JVLZ0LQ4kefyHXMl8R4oTgIkDArPVJwyv4um5W
//     *                   RiFH+n4kYrL/tFk41vyC9jBoQVnTszZqxPJ+CAh6aklBv/HLvHWo3UYF
//     *                   dsJNL+1HZ0KMUl62eO87vQ4iS15/OQ/CG9oA5YuZtUXORyo5Qk0/MMnu
//     *                   pd9X2QLJtgMDD1oApAFK4mxq4/euKO/aS3A7BtceVMDQ9d9Cx8UCPMg0
//     *                   CfZ7GjGagIjeN8mgGhsD0NZQvzr87DSyoeJe4sUx6Dj0yP21Dm3ameu5
//     *                   vFxm1qE2ohkvpcNRdg=="
//     *  }
//     * 
//     * 
//     * Output: 
//     * {
//     *      "id"            : "f693f4a3-208e-42b6-92a8-f98eabc2faad",
//     *      "subject"       : "80e54342-94f2-e711-906e-001560a04062",
//     *      "status"        : "New",
//     *      "content"       : "LS0tLS1CRUdJTiBFTkNSWVBURUQgREFUQS0tLS0tDQpDb250ZW50
//     *                        LUVuY29kaW5nOiBiYXNlNjQNCkVuY3J5cHRpb24tQWxnb3JpdGhtO
//     *                        iBBRVMvQ0JDL1BLQ1M1UGFkZGluZw0KRW5jcnlwdGlvbi1LZXktSW
//     *                        Q6IEE3OWxUbXVoQjJRPTpuYU9xZ0ZZbllWbGpEb0NjUkxjOUVMemR
//     *                        4amluV01UVEM5NGoxMjQ0U1k0PQ0KSW50ZWdyaXR5LUFsZ29yaXRo
//     *                        bTogU0hBMjU2DQpLZXktQWxnb3JpdGhtOiBQQktERjJXaXRoSG1hY
//     *                        1NIQTE7IGl0ZXJhdGlvbnM9MTAwMDsga2V5LWxlbmd0aD0yNTY7IH
//     *                        NhbHQtYnl0ZXM9OA0KDQpDWUpsUXdDMk51SHlBeGxZcHpGUW5KMTg
//     *                        wNUpzSEdGR29RemZSWWdWVEg1RmNLdHViV1puNlljd2xQM3pLZC9L
//     *                        cmRadSt4d1FTTVdWDQp1d3IyS0w3N2RWSDFES2phSzFlamhUSThaT
//     *                        mFVSzIzemp0RGtFS0I0d3FMdUhkQ2NYWU9EQXNweDVzaXh2NExncW
//     *                        IvN2pYNFlJTUsyDQpCT0Z5Vzd6M2tzWWo0aE5XR2RBUVlHQXc4cUN
//     *                        oeDV6ZTV4ekdPY2d6bWpjMHYxUmRwZkwxNFFUTzBCVnZ2L2hWQURO
//     *                        QmNaamRIL1VNDQp5Z2h5VWZoMUJXS3JObkY1YzBKa04xN3lCOUxSN
//     *                        FJtOEVFV2tMSnR1SU1BVmRUU3l1NUVJbGFpWmdiK0VJTm1odExZem
//     *                        5GTkh0bVhnDQo2SGtVVCtoV0ZRRWRvZCtId2NDWEhDeDk1ZTBGVlB
//     *                        XazFmUStDRUU0M084NW8rUUhpcXhIaG9lc3QvcFpWaFNRY21PaHp4
//     *                        SUMrZm8vDQp1OGN4U0s1SHFFNHFhQk9lYklaN3ZLQnp6SFUvS2RmV
//     *                        EI0NjE1T0E0SGxPeGx2VDhDSkRHUDZHdGRKdGN1OTgrYjFmNEVtMX
//     *                        lkVDFJDQoyYUpUTFdOQVdPQVlWZmg5SWQvK3k2RGpTbWVCQTh2Qko
//     *                        xV3VCeUxJMVZocWVPc0YvS1NueVd1TnlReDZNcmhkKzBiS0hmcVZ4
//     *                        RlVhDQpQVGhuVHlQMTY3SzZMT3ZZSFFsREFHMEVYNjd6QXYzcko5c
//     *                        WdkU3g2VDVVQXQ3cFRYSE9GUjkyUkQvbmNIalcwek5pNGdnNlZSVG
//     *                        JODQo1Q0lWZzIrTzlKT002OExlejY0Yzl3MVZnczk2T0dCWXRYc2o
//     *                        5WXNQZ1JjM3dzK1VONXNsV3hlS0piTlNPTGVXWGpZU0ZSeS9wYTBK
//     *                        DQpuY2VBclFINEl6bEplOHViVmlkL3U0Y3FQbmUxUTIwPQ0KLS0tL
//     *                        S1FTkQgRU5DUllQVEVEIERBVEEtLS0tLQ0K",
//     *      "content_type"  : "message/rfc822"
//     * }
//     * </pre>
//     * @mtwSampleApiCall
//     * <pre>
//     *  CertificateRequest obj = new CertificateRequest();
//     *  obj.setSubject("80e54342-94f2-e711-906e-001560a04062");
//     *  obj.setContent(...);
//     *  CertificateRequests client = new CertificateRequests(properties);
//     *  CertificateRequest createdObj = client.createCertificateRequest(obj);
//     * </pre>
//     */
//    public CertificateRequest createCertificateRequest(CertificateRequest obj) {
//        log.debug("target: {}", getTarget().getUri().toString());
//        CertificateRequest createdObj = getTarget().path("tag-certificate-requests").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), CertificateRequest.class);
//        return createdObj;
//    }
//
//    /**
//     * Deletes the asset tag certificate request with the specified ID.
//     * @param locator The content models of the CertificateRequestLocator java model object can be used as query parameters.
//     * <pre>
//     *          id (required)           ID of the tag certificate request in the host verification service database table.
//     *                                  The retrieve API requires only the ID of the certificate to be set. This is a query parameter in the REST call.
//     * 
//     *          subject                 The hardware UUID of the host for which the certificate request was created.
//     *                                  Retrieving the certificate request information using the subject or the hardware UUID of the host
//     *                                  is not supported.
//     * </pre>
//     * @since ISecL 1.0
//     * @mtwRequiresPermissions tag_certificate_requests:delete
//     * @mtwContentTypeReturned None
//     * @mtwMethodType DELETE
//     * @mtwSampleRestCall
//     * <pre>
//     * https://server.com:8443/mtwilson/v2/tag-certificate-requests/f693f4a3-208e-42b6-92a8-f98eabc2faad
//     * </pre>
//     * @mtwSampleApiCall
//     * <pre>
//     *  CertificateRequestLocator locator = new CertificateRequestLocator();
//     *  locator.id = UUID.valueOf("f693f4a3-208e-42b6-92a8-f98eabc2faad");
//     *  CertificateRequests client = new CertificateRequests(properties);
//     *  client.deleteCertificateRequest(locator);
//     * </pre>
//     */
//    public void deleteCertificateRequest(CertificateRequestLocator locator) {
//        log.debug("target: {}", getTarget().getUri().toString());
//        HashMap<String,Object> map = new HashMap<>();
//        map.put("id", locator.id);
//        Response obj = getTarget().path("tag-certificate-requests/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
//        if( !obj.getStatusInfo().getFamily().equals(Family.SUCCESSFUL)) {
//            throw new WebApplicationException("Delete certificate request failed");
//        }
//    }
//
//    
//    
//    /**
//     * Updates the status of asset tag certificate request.
//     * <pre>
//     * Allows the user to update the status of the certificate request after the external CA has completed
//     * processing the request. After the certificate has been created and stored in the system, the status
//     * of the corresponding certificate request has to be updated.
//     * </pre>
//     * @param obj The serialized CertificateRequest java model object represents the content of the request body.
//     * <pre>
//     *          id                  ID of the tag certificate request in the host verification service database table.
//     *                              The retrieve API requires only the ID of the certificate to be set. This is a query parameter in the REST call.
//     *
//     *          status              Status is a updated status of the certificate request.
//     * </pre>
//     * @return <pre>The serialized CertificateRequest java model object with id and status:
//     *          id
//     *          status</pre>
//     * @since ISecL 1.0
//     * @mtwRequiresPermissions tag_certificate_requests:store
//     * @mtwContentTypeReturned JSON/XML/YAML
//     * @mtwMethodType PUT
//     * @mtwSampleRestCall
//     * https://server.com:8443/mtwilson/v2/tag-certificate-requests/0bda9722-36c0-463c-85de-2265a9f329b3
//     * <pre>
//     * Input: 
//     *  {
//     *      "status" : "APPROVED"
//     *  }
//     * 
//     * Output:
//     * {
//     *      "id"    : "0bda9722-36c0-463c-85de-2265a9f329b3",
//     *      "status": "APPROVED"
//     * }
//     * </pre>
//     * @mtwSampleApiCall
//     * <pre>
//     *  CertificateRequest obj = new CertificateRequest();
//     *  obj.setId("0bda9722-36c0-463c-85de-2265a9f329b3");
//     *  obj.setStatus("APPROVED");
//     *  CertificateRequests client = new CertificateRequests(properties);
//     *  CertificateRequest updatedObj = client.editCertificateRequest(locator, obj);
//     * </pre>
//     */
//
//    public CertificateRequest editCertificateRequest(CertificateRequest obj) {
//        log.debug("target: {}", getTarget().getUri().toString());
//        HashMap<String,Object> map = new HashMap<>();
//        map.put("id", obj.getId().toString());
//        CertificateRequest updatedObj = getTarget().path("tag-certificate-requests/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), CertificateRequest.class);
//        return updatedObj;
//    }
//
//    /**
//     * Retrieves the Certificate request details for the specified tag certificate request ID.
//     * @param locator  The content models of the CertificateRequestLocator java model object can be used as query parameters.
//     * <pre>
//     *          id (required)               Id of the tag certificate request in the host verification service database table given as a path parameter.
//     *                                      The retrieve API requires only the Id of the certificate to be set.
//     * 
//     *          subject                     The hardware UUID of the host for which the certificate request was created.
//     *                                      Retrieving the certificate request information using the subject or the hardware UUID of the host
//     *                                      is not supported.
//     * </pre>
//     * @return <pre>The serialized CertificateRequest java model object that was retrieved:
//     *          id
//     *          subject
//     *          status
//     *          status
//     *          content_type</pre>
//     * @since ISecL 1.0
//     * @mtwRequiresPermissions tag_certificate_requests:retrieve
//     * @mtwContentTypeReturned JSON/XML/YAML
//     * @mtwMethodType GET
//     * @mtwSampleRestCall
//     * <pre>
//     * https://server.com:8443/mtwilson/v2/tag-certificate-requests/0bda9722-36c0-463c-85de-2265a9f329b3
//     * Output: 
//     * {
//     *              "id"            : "0bda9722-36c0-463c-85de-2265a9f329b3",
//     *              "subject"       : "80e54342-94f2-e711-906e-001560a04062",
//     *              "status"        : "APPROVED",
//     *              "content"       : "LS0tLS1CRUdJTiBFTkNSWVBURUQgREFUQS0tLS0tDQpDb250ZW50LUVuY29kaW5nOiBiYXNlNjQNCkVuY3J5cHRpb24tQWxnb3Jpd
//     *                                GhtOiBBRVMvQ0JDL1BLQ1M1UGFkZGluZw0KRW5jcnlwdGlvbi1LZXktSWQ6IDRHc0tDQXhhdUpjPTpYR1NDSXNnMStOOVl3aGFmZGE
//     *                                5YUNzUkVHWjEvSmhPM1FvTFM3R3cvQVlZPQ0KSW50ZWdyaXR5LUFsZ29yaXRobTogU0hBMjU2DQpLZXktQWxnb3JpdGhtOiBQQktER
//     *                                jJXaXRoSG1hY1NIQTE7IGl0ZXJhdGlvbnM9MTAwMDsga2V5LWxlbmd0aD0yNTY7IHNhbHQtYnl0ZXM9OA0KDQpCY2VGR3haWnhyblF
//     *                                VTzhFVG1USmtQTGlOY1BlbjJqS1g3THlXdUpwMk82THBEekxiRlQzUDF2SWluYjA2VFQ2Zkk1eEM4anQvMXhjDQp6NDV3MmkreCt5a
//     *                                0NNUS9vSlhtcHNDRXlWdXJQaVEwNlJyYW4zdzA4aHdINFNEWTR1dh1d2FwTTRuVFpEWmFETUhiNzI1a05oakhTDQp2c3ZzSVVpTnN
//     *                                UWE1KRDVkZVpmQ3dSQXlkZkRlQVJ2M2dwVU1VWUVzQk1XOEJVRXl1N3F4K3JtcTY2azhIMnJjVHpXYlNzUy9aZUVzDQo3THJvV0dF
//     *                                YUtyM1N2ZTlLM2hlK091VnRWZ094TE03VG1kK01CWnNRKy85ZS9aUVoybDNLc1RDeWpRS0hOVlhvOEJmL1Yycy9xOVVLDQpZN0JwR
//     *                                UZDS0d6VnEyL085c1hxZ3Mxc0JGOXYvbzErUnk5aS9nbG9vQUZnbVNjaE52Tll0WlJEQllmWU9nTThCeVZ5WGV0M2RYd1U1DQpDaz
//     *                                lSYkFmN21yaVRuZ1ZzVEhUSTlLNWxSSjIzcFlnNllTdnJDdHdxQ2FJM3duS2RKTm5QY0RhS3BCTjlpSm94dUJMMFFSWHoybEtsDQp
//     *                                vUFhNbkU2Tzd4STVXSm1wL3F4RVgxU1F3eGlVUjlzMmZ6b2hHOXd5VnJkenZqT1VkZ3ZrL2tqK3hLeGV1Y1huTmw5aEI3ZVRSNHVq
//     *                                DQpOUkdxUzdGYWdsd3pWQ0E0bmdXZEpyK1ZPQ2gxU2gvV2RpSXFTQ09IbDVRS2hoRU9KT1N0QUdJUmZJNTR4WTloYkFzbHJ6UWNQY0
//     *                                JMDQpsSCtSb3AxZzBZaWdPd1hTdm9LNjE5a0ZKejE0eTE1amFTcmNXQ1N5RDA2K2t1K3VoTmpyczFCam5wKy9MaTV2NXhaWUtKbFow
//     *                                cWl4DQpTS2pvZVRONHREQUdGWjJ3R3hscy9ackJ0dUVFdzRjPQ0KLS0tLS1FTkQgRU5DUllQVEVEIERBVEEtLS0tLQ0K",
//     *              "content_type"  : "message/rfc822"
//     * }
//     * </pre>
//     * @mtwSampleApiCall
//     * <pre>
//     *  CertificateRequestLocator locator = new CertificateRequestLocator();
//     *  locator.id = UUID.valueOf("0bda9722-36c0-463c-85de-2265a9f329b3");
//     *  CertificateRequests client = new CertificateRequests(properties);
//     *  CertificateRequest obj = client.retrieveCertificateRequest(locator);
//     * </pre>
//     */
//    public CertificateRequest retrieveCertificateRequest(CertificateRequestLocator locator) {
//        log.debug("target: {}", getTarget().getUri().toString());
//        HashMap<String,Object> map = new HashMap<>();
//        map.put("id", locator.id);
//        CertificateRequest obj = getTarget().path("tag-certificate-requests/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(CertificateRequest.class);
//        return obj;
//    }
//
//    /**
//     * Retrieves the Certificate requests based on the search criteria specified. 
//     * @param criteria The content models of the CertificateRequestFilterCriteria java model object can be used as query parameters.
//     * <pre>
//     *        id                    Id of asset tag certificate request.
//     *
//     *        subjectEqualTo        The complete hardware UUID of the host that the certificate is created.
//     *
//     *        subjectContains       The partial or the complete hardware UUID of the host that the certificate is created.
//     *
//     *        statusEqualTo         The complete status of the tag certificate.
//     *
//     *        contentTypeEqualTo    The content of the certificate in base 64 encoded string format. 
//     * </pre>
//     * @return <pre>The serialized CertificateRequestCollection java model object that was searched with collection of asset tag certificate requests each containing:
//     *          id
//     *          subject
//     *          status
//     *          content
//     *          content_type</pre>
//     * @since ISecL 1.0
//     * @mtwRequiresPermissions tag_certificate_requests:search
//     * @mtwContentTypeReturned JSON/XML/YAML
//     * @mtwMethodType GET
//     * @mtwSampleRestCall
//     * <pre>
//     * https://server.com:8443/mtwilson/v2/tag-certificate-requests?id=0bda9722-36c0-463c-85de-2265a9f329b3
//     * Output:
//     * {
//     *   "certificate_requests": [
//     *          {
//     *              "id"            : "0bda9722-36c0-463c-85de-2265a9f329b3",
//     *              "subject"       : "80e54342-94f2-e711-906e-001560a04062",
//     *              "status"        : "APPROVED",
//     *              "content"       : "LS0tLS1CRUdJTiBFTkNSWVBURUQgREFUQS0tLS0tDQpDb250ZW50LUVuY29kaW5nOiBiYXNlNjQNCkVuY3J5cHRpb24tQWxnb3Jpd
//     *                                GhtOiBBRVMvQ0JDL1BLQ1M1UGFkZGluZw0KRW5jcnlwdGlvbi1LZXktSWQ6IDRHc0tDQXhhdUpjPTpYR1NDSXNnMStOOVl3aGFmZGE
//     *                                5YUNzUkVHWjEvSmhPM1FvTFM3R3cvQVlZPQ0KSW50ZWdyaXR5LUFsZ29yaXRobTogU0hBMjU2DQpLZXktQWxnb3JpdGhtOiBQQktER
//     *                                jJXaXRoSG1hY1NIQTE7IGl0ZXJhdGlvbnM9MTAwMDsga2V5LWxlbmd0aD0yNTY7IHNhbHQtYnl0ZXM9OA0KDQpCY2VGR3haWnhyblF
//     *                                VTzhFVG1USmtQTGlOY1BlbjJqS1g3THlXdUpwMk82THBEekxiRlQzUDF2SWluYjA2VFQ2Zkk1eEM4anQvMXhjDQp6NDV3MmkreCt5a
//     *                                0NNUS9vSlhtcHNDRXlWdXJQaVEwNlJyYW4zdzA4aHdINFNEWTR1dh1d2FwTTRuVFpEWmFETUhiNzI1a05oakhTDQp2c3ZzSVVpTnN
//     *                                UWE1KRDVkZVpmQ3dSQXlkZkRlQVJ2M2dwVU1VWUVzQk1XOEJVRXl1N3F4K3JtcTY2azhIMnJjVHpXYlNzUy9aZUVzDQo3THJvV0dF
//     *                                YUtyM1N2ZTlLM2hlK091VnRWZ094TE03VG1kK01CWnNRKy85ZS9aUVoybDNLc1RDeWpRS0hOVlhvOEJmL1Yycy9xOVVLDQpZN0JwR
//     *                                UZDS0d6VnEyL085c1hxZ3Mxc0JGOXYvbzErUnk5aS9nbG9vQUZnbVNjaE52Tll0WlJEQllmWU9nTThCeVZ5WGV0M2RYd1U1DQpDaz
//     *                                lSYkFmN21yaVRuZ1ZzVEhUSTlLNWxSSjIzcFlnNllTdnJDdHdxQ2FJM3duS2RKTm5QY0RhS3BCTjlpSm94dUJMMFFSWHoybEtsDQp
//     *                                vUFhNbkU2Tzd4STVXSm1wL3F4RVgxU1F3eGlVUjlzMmZ6b2hHOXd5VnJkenZqT1VkZ3ZrL2tqK3hLeGV1Y1huTmw5aEI3ZVRSNHVq
//     *                                DQpOUkdxUzdGYWdsd3pWQ0E0bmdXZEpyK1ZPQ2gxU2gvV2RpSXFTQ09IbDVRS2hoRU9KT1N0QUdJUmZJNTR4WTloYkFzbHJ6UWNQY0
//     *                                JMDQpsSCtSb3AxZzBZaWdPd1hTdm9LNjE5a0ZKejE0eTE1amFTcmNXQ1N5RDA2K2t1K3VoTmpyczFCam5wKy9MaTV2NXhaWUtKbFow
//     *                                cWl4DQpTS2pvZVRONHREQUdGWjJ3R3hscy9ackJ0dUVFdzRjPQ0KLS0tLS1FTkQgRU5DUllQVEVEIERBVEEtLS0tLQ0K",
//     *              "content_type"  : "message/rfc822"
//     *          }
//     *      ]
//     * }
//     * </pre>
//     * @mtwSampleApiCall
//     * <pre>
//     *  CertificateRequestFilterCriteria criteria = new CertificateRequestFilterCriteria();
//     *  criteria.id = UUID.valueOf("0bda9722-36c0-463c-85de-2265a9f329b3");
//     *  CertificateRequests client = new CertificateRequests(properties);
//     *  CertificateRequestCollection objCollection = client.searchCertificateRequests(criteria);
//     * </pre>
//     */
//    public CertificateRequestCollection searchCertificateRequests(CertificateRequestFilterCriteria criteria) {
//        log.debug("target: {}", getTarget().getUri().toString());
//        CertificateRequestCollection objCollection = getTargetPathWithQueryParams("tag-certificate-requests", criteria).request(MediaType.APPLICATION_JSON).get(CertificateRequestCollection.class);
//        return objCollection;
//    }
//    
//    
//}
