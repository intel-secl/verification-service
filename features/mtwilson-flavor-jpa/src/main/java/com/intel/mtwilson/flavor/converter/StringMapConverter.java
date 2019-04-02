/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
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
public class StringMapConverter implements AttributeConverter<Map<String, String>, PGobject> {
    private static final Logger log = LoggerFactory.getLogger(StringMapConverter.class);
    
    @Override
    public PGobject convertToDatabaseColumn(Map<String, String> stringMap) {
        try {
            PGobject po = new PGobject();
            po.setType("json");
            po.setValue((new ObjectMapper()).writeValueAsString(stringMap));
            return po;
        } catch (JsonProcessingException | SQLException e) {
            log.error("Could not convert string map to postgresql object", e);
            return null;
        }
    }
    
    @Override
    public Map<String, String> convertToEntityAttribute(PGobject po) {
        try {
            return (new ObjectMapper()).readValue(po.getValue(), Map.class);
        } catch (IOException e) {
            log.error("Could not convert postgresql object to string map", e);
            return null;
        }
    }
}
