/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.flavor.business.FlavorVerify;
import com.intel.mtwilson.flavor.rest.v2.model.Host;
import com.intel.mtwilson.flavor.rest.v2.model.HostLocator;
import com.intel.mtwilson.flavor.rest.v2.model.HostStatusLocator;
import com.intel.mtwilson.flavor.rest.v2.model.Report;
import com.intel.mtwilson.flavor.rest.v2.model.ReportCollection;
import com.intel.mtwilson.flavor.rest.v2.model.ReportCreateCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.ReportFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.ReportLocator;
import com.intel.mtwilson.flavor.rest.v2.repository.HostRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.HostStatusRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.ReportRepository;
import static com.intel.mtwilson.i18n.HostState.CONNECTED;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author srege
 */
@V2
@Path("/reports")
public class ReportResource {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReportResource.class);
    private ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper(); // for debugging only
    private ReportRepository repository;
    
    public ReportResource() {
        repository = new ReportRepository();
    }
    
    protected ReportCollection createEmptyCollection() {
        return new ReportCollection();
    }
    
    protected ReportRepository getRepository() {
        return repository;
    }
    
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @RequiresPermissions("reports:create")
    public Report create(ReportCreateCriteria item) throws Exception {
        try { log.debug("create: {}", mapper.writeValueAsString(item)); } catch(JsonProcessingException e) { log.debug("create: cannot serialize locator: {}", e.getMessage()); }
        ValidationUtil.validate(item);
        if (item == null || (item.getHostId() == null && item.getHostName() == null && item.getHardwareUuid() == null && item.getHostName().trim().isEmpty())) {
            throw new WebApplicationException("Report create criteria must be specified", 400);
        }
        return createReport(item);
    }
    
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces(CryptoMediaType.APPLICATION_SAML)
    @SuppressWarnings("empty-statement")
    public String createSaml(ReportCreateCriteria item) throws Exception {
        ValidationUtil.validate(item);
        Report report = createReport(item);
        if (report != null)
            return report.getSaml();
        else
            return null;
    }
    
    /**
     * Retrieve an item from the collection. Input Content-Type is not
     * applicable. Output Content-Type is any of application/json,
     * application/xml, application/yaml, or text/yaml
     *
     * The output represents a single item NOT wrapped in a collection.
     *
     * @param locator
     * @return
     */
    @Path("/{id}")
    @GET
    @RequiresPermissions("reports:retrieve")
    public Report retrieveOneByPath(@BeanParam ReportLocator locator) {
        ValidationUtil.validate(locator);
        try { log.debug("retrieveOne: {}", mapper.writeValueAsString(locator)); } catch(JsonProcessingException e) { log.debug("retrieveOne: cannot serialize locator: {}", e.getMessage()); }
        return getRepository().retrieve(locator); // subclass is responsible for validating the id in whatever manner it needs to;  most will return null if !UUID.isValid(id)  but we don't do it here because a resource might want to allow using something other than uuid as the url key, for example uuid OR hostname for hosts
    }
    
//    /**
//     * Retrieve an item from the collection. Input Content-Type is not
//     * applicable. Output Content-Type is any of application/json,
//     * application/xml, application/yaml, or text/yaml
//     *
//     * The output represents a single item NOT wrapped in a collection.
//     *
//     * @param id
//     * @param hostId
//     * @return
//     */
//    @GET
//    public Report retrieveOne(@BeanParam ReportLocator locator) {
//        try { log.debug("retrieveOne: {}", mapper.writeValueAsString(locator)); } catch(JsonProcessingException e) { log.debug("retrieveOne: cannot serialize locator: {}", e.getMessage()); }
//        return getRepository().retrieve(locator); // subclass is responsible for validating the id in whatever manner it needs to;  most will return null if !UUID.isValid(id)  but we don't do it here because a resource might want to allow using something other than uuid as the url key, for example uuid OR hostname for hosts
//    }
    
    /**
     * Retrieve an item from the collection. Input Content-Type is not
     * applicable. Output Content-Type is application/vnd.api+json
     *
     * The output item is always wrapped in a collection.
     *
     * @param locator
     * @return
     */
    @Path("/{id}")
    @GET
    @Produces({DataMediaType.APPLICATION_VND_API_JSON})
    @RequiresPermissions("reports:retrieve")
    public ReportCollection retrieveJsonapiCollection(@BeanParam ReportLocator locator) { // misnomer, what we really mean is "retrieve one but wrapped ina  collection for jsonapi"
        ValidationUtil.validate(locator);
        Report item = getRepository().retrieve(locator); // subclass is responsible for validating id
        ReportCollection collection = createEmptyCollection();
        collection.getDocuments().add(item);
        return collection;
    }
    
    @GET
    @RequiresPermissions("reports:search")
    public ReportCollection searchCollection(@BeanParam ReportFilterCriteria selector) {
        ValidationUtil.validate(selector);
        try { log.debug("searchCollection: {}", mapper.writeValueAsString(selector)); } catch(JsonProcessingException e) { log.debug("Cannot serialize selector: {}", e.getMessage()); }
        ValidationUtil.validate(selector); // throw new MWException(e, ErrorCode.AS_INPUT_VALIDATION_ERROR, input, method.getName());
        return getRepository().search(selector);
    }
    
    @GET
    @Produces(DataMediaType.APPLICATION_VND_API_JSON)
    @RequiresPermissions("reports:search")
    public ReportCollection searchJsonapiCollection(@BeanParam ReportFilterCriteria criteria) {
        ValidationUtil.validate(criteria); // throw new MWException(e, ErrorCode.AS_INPUT_VALIDATION_ERROR, input, method.getName());
        return getRepository().search(criteria);
    }
    
    @GET
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces(CryptoMediaType.APPLICATION_SAML) 
    @RequiresPermissions("reports:search")
    public String searchSamlReports(@BeanParam ReportFilterCriteria criteria) {
        ValidationUtil.validate(criteria);
        ReportCollection objCollection = repository.search(criteria);
        
        if (objCollection != null && objCollection.getReports() != null && objCollection.getReports().size() > 0) {
            StringBuilder samlList = new StringBuilder();
            log.info("searchSamlReports: SAML Search - Retrieved {} of results.", objCollection.getReports().size());
            for (Report reportObj : objCollection.getReports()) {
                log.debug("searchSamlReports: Adding SAML report for host {}", reportObj.getHostId());
                samlList.append(reportObj.getSaml());
            }
            return samlList.toString();
        }
        return null;
    }
    
    private Report createReport(ReportCreateCriteria item) throws Exception {
        Host host = new HostRepository().retrieve(convert(item));
        if (host == null) {
            throw new WebApplicationException("Host doesn't exist", 400);
        }
        new FlavorVerify(host.getId(), true).call();
        
        // verify host is in connected state
        HostStatusLocator hostStatusLocator = new HostStatusLocator();
        hostStatusLocator.hostId = host.getId();
        com.intel.mtwilson.flavor.rest.v2.model.HostStatus hostStatus =
                new HostStatusRepository().retrieve(hostStatusLocator);
        if (hostStatus != null && hostStatus.getStatus().getHostState() != CONNECTED) {
            throw new WebApplicationException(String.format("Host is in bad state: %s",
                    hostStatus.getStatus().getHostState().name()));
        }
        
        // return the latest report
        ReportLocator locator = new ReportLocator();
        locator.hostId = host.getId();
        return repository.retrieve(locator);
    }
    
    @DELETE
    @Path("/{id}")
    @RequiresPermissions("reports:delete")
    public void delete(@BeanParam ReportLocator locator) {
        repository.delete(locator);
    }
    
    private HostLocator convert(ReportCreateCriteria item) {
        HostLocator hostLocator = new HostLocator();
        if (item.getHostId() != null)
            hostLocator.id = item.getHostId();
        if (item.getHostName() != null && !item.getHostName().isEmpty())
            hostLocator.name = item.getHostName();
        if (item.getHardwareUuid() != null)
            hostLocator.hardwareUuid = item.getHardwareUuid();
        if (item.getAikCertificate() != null && !item.getAikCertificate().isEmpty())
            hostLocator.aikCertificate = item.getAikCertificate();
        return hostLocator;
    }
}
