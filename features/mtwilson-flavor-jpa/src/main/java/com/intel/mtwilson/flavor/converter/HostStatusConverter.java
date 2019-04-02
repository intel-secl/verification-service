
/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider;
import java.io.IOException;
import java.sql.SQLException;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.postgresql.util.PGobject;
import com.intel.mtwilson.flavor.model.HostStatusInformation;

/**
 *
 * @author hmgowda
 */
@Converter
public class HostStatusConverter implements AttributeConverter<HostStatusInformation, PGobject> {
    private static final Logger log = LoggerFactory.getLogger(HostManifestConverter.class);
    
    @Override
    public PGobject convertToDatabaseColumn(HostStatusInformation hostStatus) {
        try {
            PGobject po = new PGobject();
            po.setType("json");
            ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
            po.setValue(mapper.writeValueAsString(hostStatus));
            return po;
        } catch (JsonProcessingException | SQLException e) {
            log.error("Could not convert host status model to postgresql object", e);
            return null;
        }
    }
    
    @Override
    public HostStatusInformation convertToEntityAttribute(PGobject po) {
        try {
            ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
            return mapper.readValue(po.getValue(), HostStatusInformation.class);
        } catch (IOException e) {
            log.error("Could not convert postgresql object to host status model", e);
            return null;
        }
    }

    
}
