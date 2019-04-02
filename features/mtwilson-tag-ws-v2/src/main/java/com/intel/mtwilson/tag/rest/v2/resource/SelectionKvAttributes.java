//package com.intel.mtwilson.tag.rest.v2.resource;
//
//import com.intel.mtwilson.tag.model.SelectionKvAttributeCollection;
//import com.intel.mtwilson.tag.model.SelectionKvAttributeFilterCriteria;
//import com.intel.mtwilson.tag.model.SelectionKvAttributeLocator;
//import com.intel.mtwilson.jaxrs2.NoLinks;
//import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
//import com.intel.mtwilson.launcher.ws.ext.V2;
//import com.intel.mtwilson.tag.model.SelectionKvAttribute;
//import com.intel.mtwilson.tag.rest.v2.repository.SelectionKvAttributeRepository;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.ws.rs.BeanParam;
//import javax.ws.rs.DELETE;
//import javax.ws.rs.GET;
//import javax.ws.rs.POST;
//import javax.ws.rs.Path;
//import javax.ws.rs.WebApplicationException;
//import javax.ws.rs.core.Context;
//import javax.ws.rs.core.Response;
//import javax.ws.rs.core.Response.Status;
//import org.apache.shiro.authz.annotation.RequiresPermissions;
//import com.intel.dcsg.cpg.io.UUID;
//import com.intel.dcsg.cpg.validation.ValidationUtil;
//
///**
// *
// * @author ssbangal
// */
////@V2
//@Path("/tag-selection-kv-attributes")
//public class SelectionKvAttributes extends AbstractJsonapiResource<SelectionKvAttribute, SelectionKvAttributeCollection, SelectionKvAttributeFilterCriteria, NoLinks<SelectionKvAttribute>, SelectionKvAttributeLocator> {
//
//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SelectionKvAttributes.class);
//
//    private SelectionKvAttributeRepository repository;
//    
//    public SelectionKvAttributes() {
//        repository = new SelectionKvAttributeRepository();
//    }
//    
//    @Override
//    protected SelectionKvAttributeCollection createEmptyCollection() {
//        return new SelectionKvAttributeCollection();
//    }
//
//    @Override
//    protected SelectionKvAttributeRepository getRepository() {
//        return repository;
//    }
//    
//    @GET
//    @Override
//    @RequiresPermissions("tag_selection_kv_attributes:search") 
//    public SelectionKvAttributeCollection searchCollection(@BeanParam SelectionKvAttributeFilterCriteria selector) {
//        ValidationUtil.validate(selector); // throw new MWException(e, ErrorCode.AS_INPUT_VALIDATION_ERROR, input, method.getName());
//        return getRepository().search(selector);
//    }
//
//    
//    @POST
//    @Override
//    @RequiresPermissions("tag_selection_kv_attributes:create") 
//    public SelectionKvAttribute createOne(@BeanParam SelectionKvAttributeLocator locator, SelectionKvAttribute item, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
//        locator.copyTo(item);
//        ValidationUtil.validate(item);
//        if (item.getId() == null) {
//            item.setId(new UUID());
//        }
//        getRepository().create(item);
//        httpServletResponse.setStatus(Status.CREATED.getStatusCode());
//        return item;
//    }
//
//    
//    @Path("/{id}")
//    @DELETE
//    @Override
//    @RequiresPermissions("tag_selection_kv_attributes:delete") 
//    public void deleteOne(@BeanParam SelectionKvAttributeLocator locator, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
//        SelectionKvAttribute item = getRepository().retrieve(locator); // subclass is responsible for validating the id in whatever manner it needs to;  most will return null if !UUID.isValid(id)  but we don't do it here because a resource might want to allow using something other than uuid as the url key, for example uuid OR hostname for hosts
//        if (item == null) {
//            throw new WebApplicationException(Response.Status.NOT_FOUND); 
//        }
//        getRepository().delete(locator);
//        httpServletResponse.setStatus(Status.NO_CONTENT.getStatusCode());
//       
//    }
//    
//    
//    @Path("/{id}")
//    @GET
//    @Override
//    @RequiresPermissions("tag_selection_kv_attributes:retrieve") 
//    public SelectionKvAttribute retrieveOne(@BeanParam SelectionKvAttributeLocator locator, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
//       
//        SelectionKvAttribute existing = getRepository().retrieve(locator);
//        if (existing == null) {
//            httpServletResponse.setStatus(Status.NOT_FOUND.getStatusCode());
//            return null;
//        }
//        return existing;
//    }
//            
//}
