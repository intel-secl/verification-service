package com.intel.mtwilson.flavor.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Properties;

/**
 * This resource is used to create software flavor from application manifest.
 * <pre>
 *
 * A flavor is a set of measurements and metadata organized in a flexible format that allows for ease of further extension. The
 * measurements included in the flavor pertain to various hardware, software and feature categories, and their respective metadata
 * sections provide descriptive information.
 *
 * When a flavor is created, it is associated with a flavor group. This means that the measurements for that flavor type are deemed
 * acceptable to obtain a trusted status. If a host, associated with the same flavor group, matches the measurements contained within
 * that flavor, the host is trusted for that particular flavor category (dependent on the flavor group policy). If no flavor group name
 * is defined in input, flavor is, by default, associated with automatic flavor group.
 *
 * A manifest is a list of files/directories/symlinks that are to be measured. The manifest provided can be used to create SOFTWARE
 * flavor only.
 *
 * The Verification Service exposes this REST API to create and store SOFTWARE flavor as per the manifest provided.
 *
 * </pre>
 * @author rawatar
 */
public class CreateFlavorFromAppManifest extends MtWilsonClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateFlavorFromAppManifest.class);

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
     * CreateFlavorFromAppManifest client = new CreateFlavorFromAppManifest(properties);
     * </pre>
     * @throws Exception
     */
    public CreateFlavorFromAppManifest(Properties properties) throws Exception {
        super(properties);
    }

    /**
     * Creates a software flavor for list of files/directories/symlinks provided in input.
     * @param connectionString connection details to connect to service. If not used, hostId must be provided.
     * @param hostId id of the host on which the manifest needs to be deployed. If not used, connectionString must be provided.
     * @param manifest a XML with list of files, directories and symlinks.
     * @param flavorgroupName name of the flavor group to which the newly created flavor is to be associated. If not provided,
     *                        newly created flavor is associated with automatic flavor group(Optional)
     *
     * @return <pre>SoftwareFlavor in JSON/YAML format with list of files, directories
     *  and symlinks with respective hash</pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions software_flavors:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwPreRequisite Host Create API
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     *
     * Headers
     * Content-Type: application/xml
     * Accept: application/json
     *
     * https://server.com:port/mtwilson/v2/flavor-from-app-manifest
     *
     * Input:{@code
     *  <ManifestRequest xmlns="lib:wml:manifests-req:1.0">
     *  <connectionString>intel:https://trustagent.server.com:1443;u=trustagentUsername;p=trustagentPassword</connectionString>
     *  <Manifest xmlns="lib:wml:manifests:1.0" DigestAlg="SHA256" Label="Tomcat" Uuid="">+
     *  <Dir Type="dir" Include=".*" Exclude="" Path="/opt/trustagent/hypertext/WEB-INF"/>
     *  <Symlink Path="/opt/trustagent/bin/tpm_nvinfo"/>
     *  <File Path="/opt/trustagent/bin/module_analysis_da.sh"/>
     *  </Manifest>
     *  </ManifestRequest>
     *  }
     *
     * Output:
     * {
     *     "flavor": {
     *         "meta": {
     *             "schema": {
     *                 "uri": "lib:wml:measurements:1.0"
     *             },
     *             "id": "791ea952-59a8-4ea2-8a11-b8482380695a",
     *             "description": {
     *                 "flavor_part": "SOFTWARE",
     *                 "label": "Tomcat",
     *                 "digest_algorithm": "SHA256"
     *             }
     *         },
     *         "software": {
     *             "measurements": {
     *                 "/opt/trustagent/hypertext/WEB-INF": {
     *                     "type": "directoryMeasurementType",
     *                     "value": "32e57aeaaedca32e788a358c3ffbc62a3c654c49ca2fa8344705dae78e1fea1e",
     *                     "Path": "/opt/trustagent/hypertext/WEB-INF",
     *                     "Include": ".*",
     *                     "Exclude": ""
     *                 },
     *                 "/opt/trustagent/bin/module_analysis.sh": {
     *                     "type": "fileMeasurementType",
     *                     "value": "269f8157fba982ece492cb4a64138c4891accff53c6badf2b403715d1e9ade32",
     *                     "Path": "/opt/trustagent/bin/module_analysis.sh"
     *                 },
     *                 "/opt/trustagent/bin/tpm_nvinfo": {
     *                     "type": "symlinkMeasurementType",
     *                     "value": "8abf32c2cdc1a0842b155a3e034d6df3768ca418b4606d969e96d49596500cbf",
     *                     "Path": "/opt/trustagent/bin/tpm_nvinfo"
     *                 }
     *             },
     *             "cumulative_hash": "8345ba0ccacfc9d0bf37a6ae41bad59caf03701f8ed270e92fab48c911745366"
     *         }
     *     }
     * }    
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the client and call the createFlavorFromAppManifest API with connection_string and manifest as input
     * CreateFlavorFromAppManifest client = new CreateFlavorFromAppManifest(properties);
     * client.createFlavorFromAppManifest("intel:https://trustagent.server.com:1443;u=trustagentUsername;p=trustagentPassword","");
     * </pre></div>
     */
    public void createFlavorFromAppManifest(String connectionString, UUID hostId, String manifest, String flavorgroupName){
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String, Object> map = new HashMap<>();
        if (connectionString != null && !connectionString.isEmpty())
            map.put("connectionString", connectionString);
        else if (hostId != null && !hostId.toString().isEmpty())
            map.put("hostId", hostId);
        map.put("manifest", manifest);
        if (flavorgroupName != null && !flavorgroupName.isEmpty())
            map.put("flavorgroupName", flavorgroupName);
        Response obj = getTarget().path("/flavor-from-app-manifest").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(map));
        if (!obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Create flavor from app manifest failed");
        }

    }
}
