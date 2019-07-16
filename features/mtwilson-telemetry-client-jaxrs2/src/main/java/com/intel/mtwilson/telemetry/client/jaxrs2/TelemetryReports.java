/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.telemetry.client.jaxrs2;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This resource is used to retrieve a report of the number of hosts registered 
 * in the Host Verification Service.  
 * <pre>
 * This number is updated daily, and the API will return the registration count 
 * over the past 90 days. Every day a new entry is created with number of hosts 
 * registered with Verification service and the date when this report was created.
 * 
 * It returns a SAML report with this information.
 * </pre>
 */
public class TelemetryReports extends MtWilsonClient {

    Logger log = LoggerFactory.getLogger(getClass().getName()); 

    /**
     * Constructor.
     * 
     * @param properties This java properties model must include server connection details for the API client initialization.
     * <pre>
     * mtwilson.api.url - Host Verification Service (HVS) base URL for accessing REST APIs
     * 
     * // basic authentication
     * mtwilson.api.username - Username for API basic authentication with the HVS
     * mtwilson.api.password - Password for API basic authentication with the HVS
     * 
     * <b>Example:</b>
     * Properties properties = new Properties();
     * properties.put(“mtwilson.api.url”, “https://server.com:port/mtwilson/v2”);
     * 
     * // basic authentication
     * properties.put(“mtwilson.api.username”, “admin”);
     * properties.put(“mtwilson.api.password”, “password”);
     * properties.put("mtwilson.api.tls.policy.certificate.sha256", "bfc4884d748eff5304f326f34a986c0b3ff0b3b08eec281e6d08815fafdb8b02");
     * TelemetryReports client = new TelemetryReports(properties);
     * </pre>
     * @throws Exception 
     */
    public TelemetryReports(Properties properties) throws Exception {
        super(properties);
    }
    
    /**
     * Creates a new report on the number of hosts managed by the attestation service. 
     * <pre>The # of hosts being managed is captured on a daily basis. This interval 
     * is configurable. This returns a SAML report that includes the records of 
     * last 90 entries (1 quarter if it is recorded daily)
     * </pre>
     * @return SAML report on the number of hosts being managed
     * @since ISecL 1.0
     * @mtwRequiresPermissions telemetry:report
     * mtwAcceptType application/samlassertion+xml
     * @mtwContentTypeReturned application/samlassertion+xml
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/telemetry_report
     * output:
     * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;saml2:Assertion ID="MapAssertion" 
     * IssueInstant="2018-01-23T11:40:45.784Z" Version="2.0" xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion"&gt;
     * &lt;saml2:Issuer&gt;https://192.168.0.1:8443&lt;/saml2:Issuer&gt;&lt;Signature
     * xmlns="http://www.w3.org/2000/09/xmldsig#"&gt;&lt;SignedInfo&gt;
     * &lt;CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments"/&gt;&lt;SignatureMethod 
     * Algorithm="http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"/&gt;
     * &lt;Reference URI="#MapAssertion"&gt;&lt;Transforms&gt;&lt;Transform 
     * Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/&gt;&lt;/Transforms&gt;
     * &lt;DigestMethod Algorithm="http://www.w3.org/2001/04/xmlenc#sha256"/&gt;&lt;DigestValue&gt;
     * T1LkOqejT6DxaaDXiPgriV1iKs35K+OTOmol+6eyoCg=&lt;/DigestValue&gt;&lt;/Reference&gt;&lt;/SignedInfo&gt;
     * &lt;SignatureValue&gt;YXM269elklMBHlOjMHGObpE6edMJ9aSKzVvFkn1FYt9/FA9okPQS3KHz6kRrzpJqmIDLC9gWnUdh 
     * ...==&lt;/SignatureValue&gt;&lt;KeyInfo&gt;&lt;X509Data&gt;
     * &lt;X509Certificate&gt;MIIDYzCCAkugAwIBAgIEQIpTsjANBgkqhkiG9w0BAQsFADBiMQswCQYDVQQGEwJVUzELMAkGA1UE....
     * TgwQEmgw==&lt;/X509Certificate&gt;&lt;/X509Data&gt;&lt;/KeyInfo&gt;&lt;/Signature&gt;
     * &lt;saml2:Subject&gt;&lt;saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified"/&gt;
     * &lt;saml2:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:sender-vouches"&gt;
     * &lt;saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified"&gt;
     * Intel Security Libraries&lt;/saml2:NameID&gt;
     * &lt;saml2:SubjectConfirmationData NotBefore="2018-01-23T11:40:45.815Z"
     * NotOnOrAfter="2018-01-23T12:40:45.815Z"/&gt;&lt;/saml2:SubjectConfirmation&gt;&lt;/saml2:Subject&gt;
     * &lt;saml2:AttributeStatement&gt;&lt;saml2:Attribute Name="Records"&gt;&lt;saml2:AttributeValue 
     * xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string"&gt;
     * Date: Fri Jan 12 15:55:51 PST 2018 Host_number: 1
     * Date: Sat Jan 13 15:55:51 PST 2018 Host_number: 1
     * Date: Sun Jan 14 15:55:51 PST 2018 Host_number: 1
     * Date: Mon Jan 15 15:55:51 PST 2018 Host_number: 1
     * Date: Mon Jan 22 04:26:00 PST 2018 Host_number: 1
     * &lt;/saml2:AttributeValue&gt;&lt;/saml2:Attribute&gt;&lt;/saml2:AttributeStatement&gt;&lt;/saml2:Assertion&gt;
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the client and call the retrieveTelemetryReport API
     * TelemetryReports client = new TelemetryReports(properties);
     * String samlReport = client.retrieveTelemetryReport();
     * </pre></div>
     */
    public String retrieveTelemetryReport() {
        log.debug("target: {}", getTarget().getUri().toString());
        String samlAssertion = getTarget().path("telemetry_report").request(CryptoMediaType.APPLICATION_SAML).get(String.class);
        return samlAssertion;
    }
    
}
