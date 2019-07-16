/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.jackson.bouncycastle.BouncyCastleModule;
import com.intel.mtwilson.jackson.validation.ValidationModule;
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
public class FlavorConverter implements AttributeConverter<Flavor, PGobject> {
    private static final Logger log = LoggerFactory.getLogger(FlavorConverter.class);
    
    @Override
    public PGobject convertToDatabaseColumn(Flavor flavor) {
        try {
            PGobject po = new PGobject();
            po.setType("json");
            Extensions.register(Module.class, BouncyCastleModule.class);
            Extensions.register(Module.class, ValidationModule.class);
            ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            po.setValue(mapper.writeValueAsString(flavor));
            return po;
        } catch (JsonProcessingException | SQLException e) {
            log.error("Could not convert flavor model to postgresql object", e);
            return null;
        }
    }
    
    @Override
    public Flavor convertToEntityAttribute(PGobject po) {
        try {
            ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
            return mapper.readValue(po.getValue(), Flavor.class);
        } catch (IOException e) {
            log.error("Could not convert postgresql object to flavor model", e);
            return null;
        }
    }
}
