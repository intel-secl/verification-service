//package com.intel.mtwilson.tag.rest.v2.resource;
//
//import com.intel.dcsg.cpg.validation.ValidationUtil;
//import com.intel.mtwilson.tag.model.KvAttribute;
//import com.intel.mtwilson.tag.model.KvAttributeCollection;
//import com.intel.mtwilson.tag.model.KvAttributeFilterCriteria;
//import com.intel.mtwilson.tag.model.KvAttributeLocator;
//import com.intel.mtwilson.jaxrs2.NoLinks;
//import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
//import com.intel.mtwilson.launcher.ws.ext.V2;
//import com.intel.mtwilson.tag.rest.v2.repository.KvAttributeRepository;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.ws.rs.BeanParam;
//import javax.ws.rs.DELETE;
//import javax.ws.rs.GET;
//import javax.ws.rs.POST;
//import javax.ws.rs.PUT;
//import javax.ws.rs.Path;
//import javax.ws.rs.WebApplicationException;
//import javax.ws.rs.core.Context;
//import javax.ws.rs.core.Response;
//import javax.ws.rs.core.Response.Status;
//import org.apache.shiro.authz.annotation.RequiresPermissions;
//import com.intel.dcsg.cpg.io.UUID;
//
//
//
///**
// *
// * @author ssbangal
// */
////@V2
//@Path("/tag-kv-attributes")
//public class KvAttributes extends AbstractJsonapiResource<KvAttribute, KvAttributeCollection, KvAttributeFilterCriteria, NoLinks<KvAttribute>, KvAttributeLocator> {
//
//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KvAttributes.class);
//
//    private KvAttributeRepository repository;
//    
//    public KvAttributes() {
//        repository = new KvAttributeRepository();
//    }
//    
//    @Override
//    protected KvAttributeCollection createEmptyCollection() {
//        return new KvAttributeCollection();
//    }
//
//    @Override
//    protected KvAttributeRepository getRepository() {
//        return repository;
//    }
//    
//    
//    @GET
//    @Override
//    @RequiresPermissions("tag_kv_attributes:search")
//    public KvAttributeCollection searchCollection(@BeanParam KvAttributeFilterCriteria selector) {        
//        ValidationUtil.validate(selector); 
//        return getRepository().search(selector);
//    }
//
//    
//    @POST
//    @Override
//    @RequiresPermissions("tag_kv_attributes:create")
//    public KvAttribute createOne(@BeanParam KvAttributeLocator locator, KvAttribute item, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
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
//    @RequiresPermissions("tag_kv_attributes:delete")
//    public void deleteOne(@BeanParam KvAttributeLocator locator, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
//        KvAttribute item = getRepository().retrieve(locator);
//        if (item == null) {
//            throw new WebApplicationException(Response.Status.NOT_FOUND); 
//        }
//        getRepository().delete(locator);
//        httpServletResponse.setStatus(Status.NO_CONTENT.getStatusCode());
//      
//    }
//    
//    @DELETE
//    @RequiresPermissions("tag_kv_attributes:delete")
//    public void deleteCollection(@BeanParam KvAttributeFilterCriteria selector) {
//        KvAttributeCollection collection = getRepository().search(selector);
//        if( collection.getDocuments().isEmpty() ) {            
//            throw new WebApplicationException(Response.Status.NOT_FOUND); 
//        }
//        // Do the delete here after search
//        getRepository().delete(selector);
//        
//        
//    }
//
//   
//    @Path("/{id}")
//    @GET
//    @Override
//    @RequiresPermissions("tag_kv_attributes:retrieve")
//    public KvAttribute retrieveOne(@BeanParam KvAttributeLocator locator, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) { 
//
//        KvAttribute existing = getRepository().retrieve(locator);
//        if (existing == null) {
//            httpServletResponse.setStatus(Status.NOT_FOUND.getStatusCode());
//            return null;
////            throw new WebApplicationException(Response.Status.NOT_FOUND); 
//        }
//        return existing;
//    }
//
//
//    @Path("/{id}")
//    @PUT
//    @Override
//    @RequiresPermissions("tag_kv_attributes:store")
//    public KvAttribute storeOne(@BeanParam KvAttributeLocator locator, KvAttribute item) {
//        ValidationUtil.validate(item);
//        locator.copyTo(item);
//        KvAttribute existing = getRepository().retrieve(locator); // subclass is responsible for validating id
//        if (existing == null) {
//            getRepository().create(item);
//        } else {
//            getRepository().store(item);
//        }
//
//        return item;
//    }
//
//    
//        
//}
