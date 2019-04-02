/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.core.verifier.policy.TrustReport;
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
 * @author dtiwari
 */
@Converter
public class TrustReportConverter implements AttributeConverter<TrustReport, PGobject> {
    private static final Logger log = LoggerFactory.getLogger(TrustReportConverter.class);
    
    @Override
    public PGobject convertToDatabaseColumn(TrustReport trustReport) {
        try {
            PGobject po = new PGobject();
            po.setType("json");
            ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
            po.setValue(mapper.writeValueAsString(trustReport));
            return po;
        } catch (JsonProcessingException | SQLException e) {
            log.error("Could not convert trust report model to postgresql object", e);
            return null;
        }
    }
    
    @Override
    public TrustReport convertToEntityAttribute(PGobject po) {
        try {
            ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
            return mapper.readValue(po.getValue(), TrustReport.class);
        } catch (IOException e) {
            log.error("Could not convert postgresql object to trust report model", e);
            return null;
        }
    }
}
