//package com.intel.mtwilson.tag.client.jaxrs;
//
//import com.intel.dcsg.cpg.io.UUID;
//import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
//import com.intel.mtwilson.tag.model.TpmPassword;
//import com.intel.mtwilson.tag.model.TpmPasswordCollection;
//import com.intel.mtwilson.tag.model.TpmPasswordFilterCriteria;
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
// * These resources are used to manage the tpm passwords.
// * <pre>
// * The Trusted Platform Module(TPM) is a specialized chip containing registers called PCR's which stores the 
// * hardware measurement of the host and compares them against the expected values to determine if the host can be trusted. 
// * Each TPM is protected with a password which is used by the trust agent when it tries to take ownership of the TPM to perform various operations.
// * </pre>
// * @author ssbangal
// */
//public class TpmPasswords extends MtWilsonClient {
//
//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TpmPasswords.class);
//
//    
//     /**
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
//     *
//     * <b>Example:</b>
//     * Properties properties = new Properties();
//     * properties.put(“mtwilson.api.url”, “https://server.com:port/mtwilson/v2”);
//     * 
//     * // basic authentication
//     * properties.put(“mtwilson.api.username”, “user”);
//     * properties.put(“mtwilson.api.password”, “*****”);
//     * properties.put("mtwilson.api.tls.policy.certificate.sha256", "ae8b50d9a45d1941d5486df204b9e05a433e3a5bc13445f48774af686d18dcfc");
//     * TpmPasswords client = new TpmPasswords(properties);
//     * @throws Exception
//    */
//    
//    public TpmPasswords(Properties properties) throws Exception {
//        super(properties);
//    }    
//    /**
//     * Creates a new TPM password entry for the host.  
//     * @param obj The serialized TpmPassword java model object represents the content of the request body.
//     * <pre>
//     *       id (required)              The host's hardware UUID for which the TPM password is being created.
//     *                                  The hardware UUID can be obtained by running the dmidecode command.
//     *      
//     *       password (required)        The tpm password of the host.
//     * </pre>
//     * @return <pre>The serialized TpmPassword java model object that was created:
//     *          id
//     *          etag
//     *          modified_on
//     *          password</pre>
//     * @since ISecL 1.0
//     * @mtwRequiresPermissions tpm_passwords:create
//     * @mtwContentTypeReturned JSON/XML/YAML
//     * @mtwMethodType POST
//     * @mtwSampleRestCall
//     * <pre>
//     * https://server.com:8443/mtwilson/v2/host-tpm-passwords
//     * Input: 
//     *  {
//     *      "id"        :   "80e54342-94f2-e711-906e-001560a04062",
//     *      "password"  :   "f7c12f9ff881812ecaace2d2d664565488a94942"
//     *  }
//     * Output: 
//     * {
//     *      "id"            : "80e54342-94f2-e711-906e-001560a04062",
//     *      "etag"          : "0ac23dcc4062c9c1786fbc0e561d565a87324873",
//     *      "modified_on"   : "2018-03-13T00:35:17-0700",
//     *      "password"      : "f7c12f9ff881812ecaace2d2d664565488a94942"
//     * }
//     * </pre>
//     * @mtwSampleApiCall
//     * <pre>
//     *  TpmPasswords client = new TpmPasswords(properties);
//     *  TpmPassword obj = new TpmPassword();
//     *  obj.setId("80e54342-94f2-e711-906e-001560a04062");
//     *  obj.setPassword("f7c12f9ff881812ecaace2d2d664565488a94942");
//     *  obj = client.createTpmPassword(obj);
//     * </pre>
//     */
//    public TpmPassword createTpmPassword(TpmPassword obj) {
//        log.debug("target: {}", getTarget().getUri().toString());
//        TpmPassword createdObj = getTarget().path("host-tpm-passwords").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), TpmPassword.class);
//        return createdObj;
//    }
//
//    /**
//     * Deletes the TPM password entry with the specified Id.
//     * @param uuid  Hardware UUID of the host for which the tpm password entry has to be deleted as a query parameter.
//     * @since ISecL 1.0
//     * @mtwRequiresPermissions tpm_passwords:delete
//     * @mtwContentTypeReturned None
//     * @mtwMethodType DELETE
//     * @mtwSampleRestCall
//     * <pre>
//     * https://hvs.server.com:8443/mtwilson/v2/host-tpm-passwords/07217f9c-f625-4c5a-a538-73f1880abdda
//     * </pre>
//     * @mtwSampleApiCall
//     * <pre>
//     *  TpmPasswords client = new TpmPasswords(properties);
//     *  client.deleteTpmPassword(UUID.valueOf("07217f9c-f625-4c5a-a538-73f1880abdda"));
//     * </pre>
//     */
//    public void deleteTpmPassword(UUID uuid) {
//        log.debug("target: {}", getTarget().getUri().toString());
//        HashMap<String,Object> map = new HashMap<>();
//        map.put("id", uuid);
//        Response obj = getTarget().path("host-tpm-passwords/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
//        if( !obj.getStatusInfo().getFamily().equals(Family.SUCCESSFUL)) {
//            throw new WebApplicationException("Delete selection failed");
//        }
//    }
//    
//    /**
//     * Deletes the list of TPM password entries based on the search criteria specified. 
//     * @param criteria The content models of the TpmPasswordFilterCriteria java model object can be used as query parameters.
//     * <pre>
//     *         Id         Hardware UUID of the host.
//     * </pre>
//     * @since ISecL 1.0
//     * @mtwRequiresPermissions tpm_passwords:delete,search
//     * @mtwContentTypeReturned None
//     * @mtwMethodType DELETE
//     * @mtwSampleRestCall
//     * <pre>
//     * https://server.com:8181/mtwilson/v2/host-tpm-passwords?id=07217f9c-f625-4c5a-a538-73f1880abdda
//     * </pre>
//     * @mtwSampleApiCall
//     * <pre>
//     *  TpmPasswords client = new TpmPasswords(properties);
//     *  TpmPasswordFilterCriteria criteria = new TpmPasswordFilterCriteria();
//     *  criteria.id = UUID.valueOf("07217f9c-f625-4c5a-a538-73f1880abdda");
//     *  client.deleteTpmPassword(criteria);
//     * </pre>
//     */
//    public void deleteTpmPassword(TpmPasswordFilterCriteria criteria) {
//        log.debug("target: {}", getTarget().getUri().toString());
//        Response obj = getTargetPathWithQueryParams("host-tpm-passwords", criteria).request(MediaType.APPLICATION_JSON).delete();
//        
//        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
//            throw new WebApplicationException("Delete TpmPassword failed");
//        }
//    }
//    
//    /**
//     * Updates the TPM password entry with the specified Id.
//     * <pre>
//     * Allows the user to update the TPM password for the specified ID, which is the
//     * host's hardware UUID.
//     * </pre>
//     * @param obj The serialized TpmPassword java model object represents the content of the request body.
//     * <pre>
//     *      id            The hardware UUID  of the host.
//     *
//     *      password      The tpm password of the host.
//     * </pre>
//     * @return <pre>The serialized TpmPassword java model object that was updated:
//     *          id
//     *          etag
//     *          modified_on
//     *          password</pre>
//     * @since ISecL 1.0
//     * @mtwRequiresPermissions tpm_passwords:store
//     * @mtwContentTypeReturned JSON/XML/YAML
//     * @mtwMethodType PUT
//     * @mtwSampleRestCall
//     * <pre>
//     * https://server.com:8443/mtwilson/v2/host-tpm-passwords/80e54342-94f2-e711-906e-001560a04062
//     * Input: 
//     * {
//     *      "password":"password updated"
//     * }
//     * 
//     * Output: 
//     * {
//     *       "id"            : "80e54342-94f2-e711-906e-001560a04062",
//     *       "etag"          : "97aefbb30ac734851ad5601e44a4a9e8c59c8ff5",
//     *       "modified_on"   : "2018-03-13T02:01:10-0700",
//     *       "password"      : "password updated"
//     * }
//     * </pre>
//     * @mtwSampleApiCall
//     * <pre>
//     *  TpmPasswords client = new TpmPasswords(properties);
//     *  TpmPassword obj = new TpmPassword();
//     *  obj.setId("80e54342-94f2-e711-906e-001560a04062");
//     *  obj.setPassword("Password updated");
//     *  obj = obj.editTpmPassword(obj);
//     * </pre>
//     */
//    public TpmPassword editTpmPassword(TpmPassword obj) {
//        log.debug("target: {}", getTarget().getUri().toString());
//        HashMap<String,Object> map = new HashMap<>();
//        map.put("id", obj.getId().toString());
//        TpmPassword updatedObj = getTarget().path("host-tpm-passwords/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), TpmPassword.class);
//        return updatedObj;
//    }
//
//    /**
//     * Retrieves the TPM password value for the specified hardware UUID of the host. 
//     * @param uuid Hardware UUID of the host for which the tpm password needs to be retrieved.
//     * @return <pre>The serialized TpmPassword java model object that was retrieved:
//     *          id
//     *          etag
//     *          modified_on
//     *          password</pre>
//     * @since ISecL 1.0
//     * @mtwRequiresPermissions tpm_passwords:retrieve
//     * @mtwContentTypeReturned JSON/XML/YAML
//     * @mtwMethodType GET
//     * @mtwSampleRestCall
//     * <pre>
//     * https://server.com:8443/mtwilson/v2/host-tpm-passwords/80e54342-94f2-e711-906e-001560a04062
//     * Output: 
//     * {
//     *              "id"            : "80e54342-94f2-e711-906e-001560a04062",
//     *              "etag"          : "97aefbb30ac734851ad5601e44a4a9e8c59c8ff5",
//     *              "modified_on"   : "2018-03-13T02:01:10-0700",
//     *              "password"      : "password updated"
//     *  }
//     * </pre>
//     * @mtwSampleApiCall
//     * <pre>
//     *  TpmPasswords client = new TpmPasswords(properties);
//     *  TpmPassword obj = client.retrieveTpmPassword(UUID.valueOf("80e54342-94f2-e711-906e-001560a04062"));
//     * </pre>
//     */
//    public TpmPassword retrieveTpmPassword(UUID uuid) {
//        log.debug("target: {}", getTarget().getUri().toString());
//        HashMap<String,Object> map = new HashMap<>();
//        map.put("id", uuid);
//        TpmPassword obj = getTarget().path("host-tpm-passwords/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(TpmPassword.class);
//        return obj;
//    }
//
//    /**
//     * Retrieves the TPM password based on the search criteria specified.
//     * <pre>
//     * Note that the output does not include the password. The user need to have the
//     * tpm_passwords:retrieve permission and call into the retrieve method to get the password.
//     * </pre>
//     * @param criteria The content models of the TpmPasswordFilterCriteria java model object can be used as query parameters.
//     * <pre>
//     *                 id           The hardware UUID  of the host.
//     * </pre>
//     * @return <pre>The serialized TpmPasswordCollection java model object that was searched with collection of tpm passwords each containing:
//     *          id
//     *          etag
//     *          modified_on
//     *          password</pre>
//     * @since ISecL 1.0
//     * @mtwRequiresPermissions tpm_passwords:search
//     * @mtwContentTypeReturned JSON/XML/YAML
//     * @mtwMethodType GET
//     * @mtwSampleRestCall
//     * <pre>
//     * https://hvsserver.com:8443/mtwilson/v2/host-tpm-passwords?id=80e54342-94f2-e711-906e-001560a04062
//     * Output: 
//     * {
//     *      "tpm_passwords": [
//     *          {
//     *              "id"            : "80e54342-94f2-e711-906e-001560a04062",
//     *              "etag"          : "97aefbb30ac734851ad5601e44a4a9e8c59c8ff5",
//     *              "modified_on"   : "2018-03-13T02:01:10-0700",
//     *              "password"      : "password updated"
//     *          }
//     *      ]
//     *  }
//     * </pre>
//     * @mtwSampleApiCall
//     * <pre>
//     *  TpmPasswords client = new TpmPasswords(properties);
//     *  TpmPasswordFilterCriteria criteria = new TpmPasswordFilterCriteria();
//     *  criteria.id = UUID.valueOf("80e54342-94f2-e711-906e-001560a04062");
//     *  TpmPasswordCollection objCollection = client.searchTpmPasswords(criteria);
//     * </pre>
//     */
//    public TpmPasswordCollection searchTpmPasswords(TpmPasswordFilterCriteria criteria) {
//        log.debug("target: {}", getTarget().getUri().toString());
//        TpmPasswordCollection objCollection = getTargetPathWithQueryParams("host-tpm-passwords", criteria).request(MediaType.APPLICATION_JSON).get(TpmPasswordCollection.class);
//        return objCollection;
//    }
//    
//    
//}
