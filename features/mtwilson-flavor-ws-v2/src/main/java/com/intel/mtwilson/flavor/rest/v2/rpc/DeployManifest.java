package com.intel.mtwilson.flavor.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.mtwilson.core.common.utils.ManifestUtils;
import com.intel.mtwilson.core.flavor.common.FlavorToManifestConverter;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.core.host.connector.HostConnector;
import com.intel.mtwilson.core.host.connector.HostConnectorFactory;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorLocator;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorRepository;
import com.intel.mtwilson.flavor.rest.v2.utils.HostConnectorUtils;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyFactoryUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * The "deploy" link next to each certificate in the UI calls this RPC
 *
 * @author ddhawal
 */
@RPC("deploy-software-manifest")
@JacksonXmlRootElement(localName="deploy_flavor")
public class DeployManifest implements Runnable{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DeployManifest.class);
    private UUID flavorId;
    private UUID hostId;

    public UUID getFlavorId() {
        return flavorId;
    }

    public void setFlavorId(UUID flavorId) {
        this.flavorId = flavorId;
    }

    public UUID getHostId() {
        return hostId;
    }

    public void setHostId(UUID hostId) {
        this.hostId = hostId;
    }

    @Override
    @RequiresPermissions("software_flavors:deploy")
    public void run() {
        log.error("RPC: DeployFlavors - Got request to deploy flavor with ID {} on host with ID {}.", flavorId, hostId);
        try {
            //Search flavor by id
            FlavorLocator flavorLocator = new FlavorLocator();
            flavorLocator.id = flavorId;
            Flavor flavor = new FlavorRepository().retrieve(flavorLocator);

            if (flavor == null) {
                log.error("The flavor with specified id was not found {}", flavorId);
                throw new RepositoryInvalidInputException(flavorLocator);
            }

            TlsPolicy tlsPolicy = TlsPolicyFactoryUtil.createTlsPolicy(HostConnectorUtils.getTlsPolicyDescriptor(null, hostId));
            //call host connector to deploy flavor to host
            deployManifestToHost(flavor, tlsPolicy);

        } catch (Exception ex) {
            log.error("RPC: DeployManifest - Error during manifest deployment.", ex);
            throw new RepositoryException(ex);
        }
    }

    private void deployManifestToHost(Flavor flavor, TlsPolicy tlsPolicy) throws IOException {
        try {
            //Host connector core library method call
            log.debug("Calling the host Connector library deployFlavor method to deploy the flavor");
            HostConnectorFactory factory = new HostConnectorFactory();
            HostConnector hostConnector = factory.getHostConnector(
                    HostConnectorUtils.getHostConnectionString(null, hostId), tlsPolicy);
            hostConnector.deployManifest(ManifestUtils.parseManifestXML(FlavorToManifestConverter.getManifestXML(flavor)));
        } catch (IOException ex) {
            log.error("Unable to deploy manifest to host.");
            throw new IOException("Unable to deploy manifest to host.", ex);
        } catch (JAXBException | XMLStreamException ex) {
            log.error("Invalid Host record.");
            throw new RuntimeException("Invalid Host record.", ex);
        }
    }
}
