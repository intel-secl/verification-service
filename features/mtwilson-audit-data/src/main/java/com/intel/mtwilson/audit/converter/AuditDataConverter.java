/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.audit.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.mtwilson.audit.data.AuditTableData;
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
 * @author hmgowda
 */
@Converter
public class AuditDataConverter implements AttributeConverter<AuditTableData, PGobject> {
    private static final Logger log = LoggerFactory.getLogger(AuditDataConverter.class);
    
    @Override
    public PGobject convertToDatabaseColumn(AuditTableData auditTableData) {
        try {
            PGobject po = new PGobject();
            po.setType("json");
            Extensions.register(Module.class, BouncyCastleModule.class);
            Extensions.register(Module.class, ValidationModule.class);
            ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            po.setValue(mapper.writeValueAsString(auditTableData));
            return po;
        } catch (JsonProcessingException | SQLException e) {
            log.error("Could not convert audit table data model to postgresql object", e);
            return null;
        } 
    }
    
    @Override
    public AuditTableData convertToEntityAttribute(PGobject po) {
        try {
            if(po == null){
                return null;
            }
            ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
            return mapper.readValue(po.getValue(), AuditTableData.class);
        } catch (IOException e) {
            log.error("Could not convert postgresql object to audit table data model", e);
            return null;
        }
    }
}
