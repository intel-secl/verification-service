/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.v2.file.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.v2.file.model.File;
import com.intel.mtwilson.v2.file.model.FileCollection;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import com.intel.mtwilson.v2.file.model.FileFilterCriteria;

/**
 *
 * @author jbuhacoff
 */


/**
 *
 * These resources are used to manage files.
 * <pre>
 *
 * The Files API is used to create/retrieve and search files.
 * It supports file search based on file name as well as content type.
 * </pre>
 */

public class Files extends MtWilsonClient {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Files.class);
    
     /**
     * Constructor.
     *
     * @param properties This java properties model must include server connection details for the API client initialization.
     * <pre>
     * mtwilson.api.url - Host Verification Service (HVS) base URL for accessing REST APIs
     *
     * <b>Example:</b>
     * Properties properties = new Properties();
     * properties.put(“mtwilson.api.url”, “https://hvs.server.com:port/mtwilson/v2”);
     *
     * properties.put("mtwilson.api.keystore", System.getProperty("user.home", ".")+java.io.File.separator+username+".jks");
     * properties.put("mtwilson.api.keystore.password", password);
     * properties.put("mtwilson.api.key.alias", username);
     * properties.put("mtwilson.api.key.password", password);
     * Files client = new Files(properties);
     * </pre>
     * @throws Exception
     */
    public Files(Properties properties) throws Exception {
        super(properties);
    }
    
    /**
     * Search for Files that match a specified Filter criteria.
     * @param criteria The content models of the FileFilterCriteria java model object can be used as query parameters.
     * <pre>
     *          id                  Provide ID of the file as a search criteria.
     *          nameEqualTo         Provide complete name of the file as a search criteria.
     *          nameContains        Provide complete or partial name of the file as a search criteria.
     *          content_type        Provide the content type of the file as a search criteria.
     * </pre>
     * @return <pre>The serialized FileCollection java model object that was searched:
     *          Id
     *          name
     *          content
     *          content_type</pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions files : search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwPreRequisite None
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/host-files?id=a165948d-d44d-496a-8b26-f4ce71560e84
     * https://server.com:8443/mtwilson/v2/host-files?nameEquals=testfile
     * https://server.com:8443/mtwilson/v2/host-files?nameContains=test
     * https://server.com:8443/mtwilson/v2/host-files?content_type=text/plain
     * 
     * Output :
     * {  
     *      "files":[  
     *         {  
     *            "Id":"a165948d-d44d-496a-8b26-f4ce71560e84",
     *            "name":"testfile",
     *            "content_type":"text/plain",
     *            "content": "Sample file content"
     *         }
     *      ]
     * }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     *  Files client = new Files(properties);
     *  FileFilterCriteria criteria = new FileFilterCriteria();
     *  criteria.nameEquals = "testfile";
     *  FileCollection fileCollection = client.searchFiles(criteria);
     * </pre></div>
     * */
    
    public FileCollection searchFiles(FileFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        FileCollection files = getTargetPathWithQueryParams("host-files", criteria).request().accept(MediaType.APPLICATION_JSON).get(FileCollection.class);
        return files;
    }
    
    
    /**
     * Retrieve the File with the specified id.
     * @param id The ID of the file to be retrieved.
     * @return <pre>The serialized File java model object that was retrieved:
     *          Id
     *          name
     *          content
     *          content_type</pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions files : retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwPreRequisite None
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/host-files/a165948d-d44d-496a-8b26-f4ce71560e84
     * 
     * Output :
     * {
     *    "id":"a165948d-d44d-496a-8b26-f4ce71560e84",
     *    "name":"testfile",
     *    "content_type":"text/plain",
     *    "content": "Sample file content"
     * }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     *  Files client = new Files(properties);
     *  File file = client.retrieveFile((new UUID()).toString());
     * </pre></div>
     * */
    
    public File retrieveFile(String id) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", id);
        File file = getTarget().path("host-files/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).get(File.class);
        return file;
    }
    
    
    /**
     * Creates files in the database with the name, content and contentType parameters.
     * @param file The serialized File java model object represents the content of the request body.
     * <pre>
     *          name            Name of the file that has to be created in string format. This parameter cannot have special characters
     *                          such as "_@#$%" etc.
     *          content         The content of the file.
     *          content_type    The content type of the file such as text, json etc.
     *</pre>
     * @return <pre>The serialized File java model object that was created:
     *          Id
     *          name
     *          content
     *          content_type</pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions files : create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwPreRequisite None
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/host-files
     * Input :
     * {
     *      "name": "TestName",
     *      "content_type": "text/plain",
     *      "content": "Sample file content"
     * }
     * 
     * Output :
     * {
     *      "id": "fec3726f-4c24-4246-913f-c7d8a567ab08",
     *      "name": "TestName",
     *      "content_type": "text/plain",
     *      "content": "Sample file content" 
     * }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     *  Files client = new Files(properties);
     *  File file = new File();
     *  file.setName("TestName");
     *  file.setContent("Sample file content");
     *  file.setContentType("text/plain");
     *  File file = client.createFile(file);
     * </pre></div>
     * */
    
    public File createFile(File file) {
        log.debug("target: {}", getTarget().getUri().toString());
        File responseFile = getTarget().path("host-files").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(file)).readEntity(File.class);
        return responseFile;
    }
    
}
