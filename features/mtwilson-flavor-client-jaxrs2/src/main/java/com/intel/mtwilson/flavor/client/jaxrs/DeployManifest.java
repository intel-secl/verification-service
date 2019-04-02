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
 * This resource is used to deploy software flavor on a host.
 * <pre>
 * A manifest is a list of files/directories/symlinks that are to be measured. The manifest can be deployed or pushed directly
 * to the host using the REST API described here. The Verification Service exposes this REST API to create manifest from flavor
 * retrieved from database based cn the flavor id provided by the user and deploy it to the host whose information has been provided
 * in the input as host id (if host is already registered to Verification Service).
 * </pre>
 * @author rawatar
 */
public class DeployManifest extends MtWilsonClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DeployManifest.class);

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
     * DeployManifest client = new DeployManifest(properties);
     * </pre>
     * @throws Exception
     */
    public DeployManifest(Properties properties) throws Exception {
        super(properties);
    }

    /**
     * Creates the manifest from a software flavor which is retrieved using the flavor uuid
     * and deploys it to the host based on the hostId provided as parameter.
     * @param flavorId UUID of the flavor that needs to be deployed on the host.
     * This UUID is obtained when a softwareFlavor is created using the SoftwareFlavors API.
     * @param hostId UUID of the host where the manifest is to be deployed.
     * This UUID is obtained when a host is registered with the Verification Service.
     * @since ISecL 1.0
     * @mtwRequiresPermissions software_flavors:deploy
     * @mtwContentTypeReturned JSON
     * @mtwMethodType POST
     * @mtwPreRequisite Software Flavor Create API
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:port/mtwilson/v2/rpc/deploy-software-manifest
     * Input: {
     *      "flavor_id":"a6544ff4-6dc7-4c74-82be-578592e7e3ba",
     *      "host_id":"a6544ff4-6dc7-4c74-82be-578592e7e3ba"
     * }
     * </pre>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the client and call the deployFlavor API with flavor_id and host_id as input
     * DeployManifest client = new CreateFlavorFromAppManifest(properties);
     * client.deployManifest(UUID.valueOf("a6544ff4-6dc7-4c74-82be-578592e7e3ba"),UUID.valueOf("a6544ff4-6dc7-4c74-82be-578592e7e3ba"));
     * </pre></div>
     */
    public void deployManifest(UUID flavorId, UUID hostId) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("flavor_id", flavorId);
        map.put("host_id", hostId);
        Response obj = getTarget().path("rpc/deploy-software-manifest").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(map));
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Deploy flavor on host failed");
        }
    }

}
