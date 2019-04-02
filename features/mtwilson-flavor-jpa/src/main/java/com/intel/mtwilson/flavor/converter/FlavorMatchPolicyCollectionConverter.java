/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.flavor.model.FlavorMatchPolicyCollection;
import com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider;
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
public class FlavorMatchPolicyCollectionConverter implements AttributeConverter<FlavorMatchPolicyCollection, PGobject> {
    private static final Logger log = LoggerFactory.getLogger(FlavorMatchPolicyCollectionConverter.class);
    
    @Override
    public PGobject convertToDatabaseColumn(FlavorMatchPolicyCollection flavorMatchPolicyCollection) {
        try {
            PGobject po = new PGobject();
            po.setType("json");
            ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
            po.setValue(mapper.writeValueAsString(flavorMatchPolicyCollection));
            return po;
        } catch (JsonProcessingException | SQLException e) {
            log.error("Could not convert flavor match policy collection model to postgresql object", e);
            return null;
        }
    }
    
    @Override
    public FlavorMatchPolicyCollection convertToEntityAttribute(PGobject po) {
        try {
            ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
            return mapper.readValue(po.getValue(), FlavorMatchPolicyCollection.class);
        } catch (IOException e) {
            log.error("Could not convert postgresql object to flavor match policy collection model", e);
            return null;
        }
    }
}
