/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.tag.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.My;
import com.intel.mtwilson.core.common.tag.model.TagCertificate;
import com.intel.mtwilson.tag.model.TagCertificateCollection;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.tag.model.TagCertificateCreateCriteria;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.intel.mtwilson.tag.common.Global;
import com.intel.mtwilson.supplemental.asset.tag.TagCertificateBuilder;
import com.intel.mtwilson.supplemental.asset.tag.TagSelectionBuilder;
import com.intel.mtwilson.supplemental.asset.tag.model.TagKvAttribute;
import com.intel.mtwilson.supplemental.asset.tag.model.TagSelection;
import com.intel.mtwilson.tag.TagConfiguration;
import com.intel.mtwilson.tag.model.TagCertificateFilterCriteria;
import com.intel.mtwilson.tag.model.TagCertificateLocator;
import com.intel.mtwilson.tag.rest.v2.repository.TagCertificateRepository;
import java.io.UnsupportedEncodingException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.bouncycastle.operator.OperatorCreationException;

/**
 *
 * @author dtiwari
 */
@V2
@Path("/tag-certificates")
public class TagCertificates {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TagCertificates.class);

    private TagCertificateRepository repository;

    public TagCertificates() {
        repository = new TagCertificateRepository();
    }

    protected TagCertificateCollection createEmptyCollection() {
        return new TagCertificateCollection();
    }

    protected TagCertificateRepository getRepository() {
        return repository;
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @RequiresPermissions("tag_certificates:create") 
    public TagCertificate createTagCertificate(TagCertificateCreateCriteria tagCertificateCreateCriteria) throws UnsupportedEncodingException, OperatorCreationException {

        if (tagCertificateCreateCriteria == null || tagCertificateCreateCriteria.getHardwareUuid() == null) {
            log.error("Error during Tag Certificate creation");
            throw new RepositoryInvalidInputException("Hardware UUID and Tag Selection must be specified");
        }

        TagSelectionBuilder tagSelectionBuilder = new TagSelectionBuilder();

        if (tagCertificateCreateCriteria.getSelectionId() == null && tagCertificateCreateCriteria.getSelectionName() == null) {
            if (tagCertificateCreateCriteria.getSelectionContent() == null) {
                log.error("Error during Tag Certificate creation");
                throw new RepositoryInvalidInputException("Selection id or name or content must be specified");
            } else { // Selection id or name has not been provided, then use selection content
                for (TagKvAttribute tagKvAttribute : tagCertificateCreateCriteria.getSelectionContent()) {
                    tagSelectionBuilder.textKvAttribute(tagKvAttribute.getName(), tagKvAttribute.getValue());
                }
                tagSelectionBuilder.build();
            }
        }

        PrivateKey cakey = Global.cakey();
        X509Certificate cakeyCert = Global.cakeyCert();
        if (cakey == null || cakeyCert == null) {
            throw new IllegalStateException("Missing tag certificate authority key");
        }

        TagConfiguration configuration = new TagConfiguration(My.configuration().getConfiguration());

        byte[] tagCeritificate = TagCertificateBuilder.factory()
                .selection(new TagSelection(tagSelectionBuilder))
                .issuerName(cakeyCert)
                .issuerPrivateKey(cakey)
                .dateSerial()
                .subjectUuid(tagCertificateCreateCriteria.getHardwareUuid())
                .expires(configuration.getTagValiditySeconds(), TimeUnit.SECONDS)
                .build();

        TagCertificate tagCertificate = TagCertificate.valueOf(tagCeritificate);
        tagCertificate.setHardwareUuid(tagCertificateCreateCriteria.getHardwareUuid());
        repository.create(tagCertificate);
        return tagCertificate;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @RequiresPermissions("tag_certificates:search") 
    public TagCertificateCollection searchTagCertificate(@BeanParam TagCertificateFilterCriteria tagCertificateFilterCriteria, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
        return repository.search(tagCertificateFilterCriteria);
    }

    @DELETE
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Path("{certificateId}")
    @RequiresPermissions("tag_certificates:delete") 
    public void deleteTagCertificate(@PathParam("certificateId") String certificateId) {
        ValidationUtil.validate(certificateId);

        TagCertificateLocator tagCertificateLocator = new TagCertificateLocator();
        tagCertificateLocator.id = UUID.valueOf(certificateId);

        repository.delete(tagCertificateLocator);
    }
}
