/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.rest.v2.resource;

import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.core.flavor.common.FlavorToManifestConverter;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorCollection;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.SignedFlavorCollection;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorRepository;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.core.common.utils.ManifestUtils;
import com.intel.wml.manifest.xml.Manifest;
import org.apache.shiro.authz.annotation.RequiresPermissions;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 *
 * @author arijitgh
 */
@V2
@Path("/manifests")
public class ManifestResource {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ManifestResource.class);
    private FlavorRepository repository = new FlavorRepository();

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @RequiresPermissions("flavors:search")
    public Manifest getManifest(@BeanParam FlavorFilterCriteria criteria, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) throws IOException, JAXBException, XMLStreamException{
        ValidationUtil.validate(criteria);
        log.debug("getManifest(): target: {} - {}", httpServletRequest.getRequestURI(), httpServletRequest.getQueryString());
        Flavor flavor = null;
        SignedFlavorCollection flavorCollection = repository.search(criteria);
        if (flavorCollection.getFlavors().size() > 0) {
            flavor = flavorCollection.getFlavors().get(0);
        }
        return ManifestUtils.parseManifestXML(createManifest(flavor));
    }

    private String createManifest(Flavor flavor) throws IOException, JAXBException, XMLStreamException {
        String manifest;
        if (flavor != null && flavor.getMeta().getDescription().getFlavorPart().equals(FlavorPart.SOFTWARE.getValue())) {
            log.debug("createManifest() : Flavor received is a software flavor");
            manifest = FlavorToManifestConverter.getManifestXML(flavor);
        } else {
            if (flavor != null) {
                log.error("createManifest() : Flavor with UUID {} is not a SOFTWARE flavor", flavor.getMeta().getId());
                throw new WebApplicationException("Flavor received is not a software flavor", 400);
            }
            else{
                log.error("createManifest() : Flavor received is null");
                throw new WebApplicationException("Flavor received is null", 400);
            }
        }
        ManifestUtils.parseManifestXML(manifest);
        return manifest;
    }
}
