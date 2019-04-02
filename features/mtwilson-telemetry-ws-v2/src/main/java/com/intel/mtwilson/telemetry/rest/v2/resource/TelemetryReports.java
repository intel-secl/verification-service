/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.telemetry.rest.v2.resource;

import com.intel.mtwilson.flavor.saml.IssuerConfigurationFactory;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.supplemental.saml.MapFormatter;
import com.intel.mtwilson.supplemental.saml.SAML;
import com.intel.mtwilson.supplemental.saml.SamlAssertion;
import com.intel.mtwilson.telemetry.rest.v2.model.TelemetryCollection;
import com.intel.mtwilson.telemetry.rest.v2.model.TelemetryRecord;
import com.intel.mtwilson.telemetry.rest.v2.repository.TelemetryRepository;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.xml.io.MarshallingException;

/**
 *
 * @author hdxia
 */
@V2
@Path("/telemetry_report")
public class TelemetryReports {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TelemetryReports.class);
    private TelemetryRepository repository;

    public TelemetryReports() {
        repository = new TelemetryRepository();
    }

    protected TelemetryRepository getRepository() {
        return repository;
    }

    /* returns SAML report that contains telemetry information (current only number of managed hosts) up to last 90 days
     * usage: http://<host_verification_server>:<port_number>/mtwilson/v2/telemetry_report
     * return SAL report if there is record, otherwise empty
    */
    @GET
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces(CryptoMediaType.APPLICATION_SAML)
    @RequiresPermissions("telemetry:report")
    public String creatReports() throws InitializationException {
        
        String samlReport = "";
        try {
            /* get the report */
            TelemetryCollection teleCollection = repository.retrieve();
            if (teleCollection != null && teleCollection.getTelmetries() != null && !teleCollection.getTelmetries().isEmpty()) {
                List<TelemetryRecord> RecList = teleCollection.getTelmetries();
                String teleLog = "\n";
                for (TelemetryRecord item : RecList) {
                    teleLog = teleLog + "Date: " + item.getCreateDate() + " " + "Host_number: " + item.getHostNum() + "\n";
                }
                /* saml report */
                SAML saml = new SAML(new IssuerConfigurationFactory().loadIssuerConfiguration());
                Map<String, String> samlMap = new HashMap();
                MapFormatter mapAssertion;
                SamlAssertion mapSamlAssertion;
                samlMap.put("Records", teleLog);
                mapAssertion = new MapFormatter(samlMap);
                mapSamlAssertion = saml.generateSamlAssertion(mapAssertion);
                samlReport = mapSamlAssertion.assertion;
            }
        } catch (MarshallingException | GeneralSecurityException | XMLSignatureException | MarshalException ex) {
            Logger.getLogger(TelemetryReports.class.getName()).log(Level.SEVERE, null, ex);
        }
        return samlReport;
    }
}
