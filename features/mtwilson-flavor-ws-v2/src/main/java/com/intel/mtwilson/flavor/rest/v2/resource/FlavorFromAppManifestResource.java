/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.rest.v2.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.mtwilson.core.common.utils.ManifestUtils;
import com.intel.mtwilson.core.common.utils.MeasurementUtils;
import com.intel.mtwilson.core.flavor.SoftwareFlavor;
import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.core.flavor.common.PlatformFlavorUtil;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.core.flavor.model.SignedFlavor;
import com.intel.mtwilson.core.host.connector.HostConnector;
import com.intel.mtwilson.core.host.connector.HostConnectorFactory;
import com.intel.mtwilson.flavor.rest.v2.model.Flavorgroup;
import com.intel.mtwilson.flavor.rest.v2.utils.FlavorGroupUtils;
import com.intel.mtwilson.flavor.rest.v2.utils.FlavorUtils;
import com.intel.mtwilson.flavor.rest.v2.utils.HostConnectorUtils;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyFactoryUtil;
import com.intel.mtwilson.util.crypto.keystore.PrivateKeyStore;
import com.intel.wml.manifest.xml.Manifest;
import com.intel.wml.manifest.xml.ManifestRequest;
import com.intel.wml.measurement.xml.Measurement;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.intel.mtwilson.core.common.model.SoftwareFlavorPrefix;

/**
 *
 *
 * @author rawatar
 */
@V2
@Path("flavor-from-app-manifest")
public class FlavorFromAppManifestResource {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FlavorFromAppManifestResource.class);
    private static final String FLAVOR_SIGNER_KEYSTORE_FILE = "flavor.signer.keystore.file";
    private static final String FLAVOR_SIGNER_KEYSTORE_PASSWORD = "flavor.signer.keystore.password";
    private static final String FLAVOR_SIGNING_KEY_ALIAS = "flavor.signing.key.alias";
    @POST
    @Consumes({MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @RequiresPermissions("software_flavors:create")
    public Flavor createFlavor(ManifestRequest manifestRequest) {
        log.info("FlavorFromAppManifestResource - Got request to create software flavor from app manifest {}.", manifestRequest.getManifest());
        validateDefaultManifest(manifestRequest.getManifest());
        try {
            TlsPolicy tlsPolicy = TlsPolicyFactoryUtil.createTlsPolicy(
                    HostConnectorUtils.getTlsPolicyDescriptor(manifestRequest.getConnectionString(),
                            getHostId(manifestRequest)));
            //call host connector to get measurement from manifest
            String manifestXml = ManifestUtils.getManifestString(manifestRequest.getManifest());
            Measurement measurementXml = getMeasurementFromManifest(manifestRequest,
                    ManifestUtils.parseManifestXML(manifestXml), tlsPolicy);
            String measurementString = MeasurementUtils.getMeasurementString(measurementXml);

            SoftwareFlavor softwareFlavor = new SoftwareFlavor(measurementString);
            PrivateKeyStore privateKeyStore = new PrivateKeyStore("PKCS12", new File(MSConfig.getConfiguration().getString(FLAVOR_SIGNER_KEYSTORE_FILE)), MSConfig.getConfiguration().getString(FLAVOR_SIGNER_KEYSTORE_PASSWORD).toCharArray());
            PrivateKey privateKey = privateKeyStore.getPrivateKey(MSConfig.getConfiguration().getString(FLAVOR_SIGNING_KEY_ALIAS, "flavor-signing-key"));
            SignedFlavor signedFlavor = PlatformFlavorUtil.getSignedFlavor(softwareFlavor.getSoftwareFlavor(), privateKey);

            // Add Flavor to the Flavorgroup
            Map<String, List<SignedFlavor>> flavorPartFlavorMap = new HashMap<>();
            List<SignedFlavor> flavors = new ArrayList();
            flavors.add(signedFlavor);
            flavorPartFlavorMap.put(FlavorPart.SOFTWARE.getValue(), flavors);
            List<String> partialFlavorTypes = new ArrayList();
            partialFlavorTypes.add(FlavorPart.SOFTWARE.getValue());

            Flavorgroup flavorgroup = FlavorGroupUtils.getFlavorGroupByName(HostConnectorUtils.getFlavorgroupName(manifestRequest.getFlavorgroupName()));
            if(flavorgroup == null) {
                flavorgroup = FlavorGroupUtils.createFlavorGroupByName(manifestRequest.getFlavorgroupName());            
            }
            new FlavorResource().addFlavorToFlavorgroup(flavorPartFlavorMap, flavorgroup.getId());
            return signedFlavor.getFlavor();
        } catch(Exception ex) {
            log.error("FlavorFromAppManifestResource - Error during flavor creation.", ex);
            throw new RepositoryException(ex);
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_XML})
    @RequiresPermissions("software_flavors:create")
    public Flavor createFlavorXML(ManifestRequest manifestRequest){
            Flavor softwareFlavor = createFlavor(manifestRequest);
            return FlavorUtils.updatePathSeparatorForXML(softwareFlavor);
    }

    private void validateDefaultManifest(Manifest manifest){
        if (manifest.getLabel().contains(SoftwareFlavorPrefix.DEFAULT_APPLICATION_FLAVOR_PREFIX.getValue())
                || manifest.getLabel().contains(SoftwareFlavorPrefix.DEFAULT_WORKLOAD_FLAVOR_PREFIX.getValue())){
            log.error("Default manifest cannot be provided for flavor creation");
            throw new WebApplicationException("Default manifest cannot be provided for flavor creation", 400);
        }
    }

    private UUID getHostId(ManifestRequest manifestRequest) {
        UUID hostId = null;
        if(manifestRequest.getHostId() != null && !manifestRequest.getHostId().isEmpty()) {
            hostId = UUID.valueOf(manifestRequest.getHostId());
        }
        return hostId;
    }

    private Measurement getMeasurementFromManifest(ManifestRequest manifestRequest, Manifest manifest, TlsPolicy tlsPolicy) throws IOException {
        try {
            log.debug("Calling the host Connector library getMeasurementFromManifest method to retrieve the measurement");
            HostConnectorFactory factory = new HostConnectorFactory();
            HostConnector hostConnector = factory.getHostConnector(
                    HostConnectorUtils.getHostConnectionString(manifestRequest.getConnectionString(),
                            getHostId(manifestRequest)), tlsPolicy);
            return hostConnector.getMeasurementFromManifest(manifest);
        } catch (IOException ex) {
            log.error("Unable to get measurement from manifest.");
            throw new IOException("Unable to get measurement from manifest.", ex);
        }
    }
}