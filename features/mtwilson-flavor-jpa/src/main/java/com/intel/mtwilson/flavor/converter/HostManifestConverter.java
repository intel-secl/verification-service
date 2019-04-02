/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider;
import com.intel.mtwilson.core.common.model.HostManifest;
import java.io.IOException;
import java.sql.SQLException;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rksavino
 */
@Converter
public class HostManifestConverter implements AttributeConverter<HostManifest, PGobject> {
    private static final Logger log = LoggerFactory.getLogger(HostManifestConverter.class);
    
    @Override
    public PGobject convertToDatabaseColumn(HostManifest hostManifest) {
        try {
            PGobject po = new PGobject();
            po.setType("json");
            ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
            po.setValue(mapper.writeValueAsString(hostManifest));
            return po;
        } catch (JsonProcessingException | SQLException e) {
            log.error("Could not convert host manifest model to postgresql object", e);
            return null;
        }
    }
    
    @Override
    public HostManifest convertToEntityAttribute(PGobject po) {
        try {
            if(po == null){
                return null;
            }
            ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
            return mapper.readValue(po.getValue(), HostManifest.class);
        } catch (IOException e) {
            log.error("Could not convert postgresql object to host manifest model", e);
            return null;
        }
    }
}
