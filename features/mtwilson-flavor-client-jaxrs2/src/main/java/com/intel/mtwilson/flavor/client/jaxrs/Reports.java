/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.client.jaxrs;

import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.mtwilson.flavor.rest.v2.model.Report;
import com.intel.mtwilson.flavor.rest.v2.model.ReportCollection;
import com.intel.mtwilson.flavor.rest.v2.model.ReportCreateCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.ReportFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.ReportLocator;
import com.intel.mtwilson.supplemental.saml.TrustAssertion;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import java.io.File;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * These resources are used to obtain reports.
 * <pre>
 * 
 * A report contains the trust information produced by the flavor verification process. It provides details on if the host 
 * is trusted and the flavors it matched, or came closest to matching. If the host is untrusted, the report will include 
 * faults which describe why the host is untrusted. These faults allow for easy analysis and remediation of an untrusted 
 * result.
 * 
 * A report can be returned in normal JSON/XML/YAML format, or it can be returned in SAML format. A SAML report is provided 
 * in XML format and contains the same trust information in a specific attribute format. A SAML report also includes a 
 * signature that can be verified by the Host Verification Service’s SAML public key.
 * 
 * Reports have a configurable validity period with default period of 24 hours or 86400 seconds. The Host Verification service has a background
 * refresh process that queries for reports where the expiration time is within the next 5 minutes, and triggers generation of a new 
 * report for all results. This is checked every 2 minutes by default, and can be configured by changing the appropriate property. 
 * In this way fresh reports are generated before older reports expire.
 * 
 * </pre>
 */
public class Reports extends MtWilsonClient {

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
     * properties.put(“mtwilson.api.username”, “user”);
     * properties.put(“mtwilson.api.password”, “*****”);
     * properties.put("mtwilson.api.tls.policy.certificate.sha256", "bfc4884d748eff5304f326f34a986c0b3ff0b3b08eec281e6d08815fafdb8b02");
     * Reports client = new Reports(properties);
     * </pre>
     * @throws Exception 
     */
    public Reports(Properties properties) throws Exception {
        super(properties);
    }
    
    /**
     * Creates a new attestation report for the specified host by forcing a complete attestation cycle.
     * <pre>
     * Generating a new attestation report generates a new TPM quote from the TPM of the host being attested.
     * </pre>
     * @param createCriteria The serialized ReportCreateCriteria java model object represents the content of the request body.
     * <pre>
     *          host_id             Host ID.
     * 
     *          host_name           Host name.
     * 
     *          hardware_uuid       Hardware UUID of host.
     * 
     *          aik_certificate     AIK certificate.
     * </pre>
     * @return <pre>The serialized Report java model object that was created:
     *          id
     *          host_id
     *          trust_information
     *          created 
     *          expiration
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions reports:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/reports
     * input: {
     *      "host_name":"host-1"
     * }
     * 
     * output:
     * {
     *      "id": "8a545a4f-d282-4d91-8ec5-bcbe439dcfbc",
     *      "host_id": "94824cb6-d6c8-4faf-83b0-125996ceebe2",
     *      "trust_information": {
     *              "flavors_trust": {
     *                          "HOST_UNIQUE":
     *                              {
     *                                "trust": true,
     *                                 "rules":
     *                                     [
     *                                      {
     *                                          "rule":
     *                                              {
     *                                          "rule_name": "com.intel.mtwilson.core.verifier.policy.rule.PcrEventLogIncludes",
     *                                          "markers":
     *                                              [
     *                                                "HOST_UNIQUE"
     *                                              ],
     *                                          "pcr_bank": "SHA256",
     *                                          "pcr_index": "pcr_18",
     *                                          "expected":
     *                                           [
     *                                              {
     *                                          "digest_type": "com.intel.mtwilson.core.common.model.MeasurementSha256",
     *                                          "value": "df3f619804a92fdb4057192dc43dd748ea778adc52bc498ce80524c014b81119",
     *                                          "label": "LCP_CONTROL_HASH",
     *                                          "info":
     *                                             {
     *                                               "ComponentName": "LCP_CONTROL_HASH",
     *                                               "EventName": "OpenSource.EventName"
     *                                              }
     *                                             }
     *                                          ],
     *                                        },
     *                                  "flavor_id": "a774ddad-fca1-4670-86b2-605c88a16dab",
     *                                  "trusted": true
     *                                     },
     *                                   ]
     *                           },
     *                          "OS": {
     *                                          "trust": true,
     *                                          "rules": [...]
     *                                },
     *
     *                          "PLATFORM": {       "trust": true,
     *                                          "rules": [...]
     *                                  },
     *                          }
     *                      },
     *              "OVERALL": true
     *      "created": "2018-07-23T16:39:52-0700",
     *      "expiration": "2018-07-23T17:39:52-0700"
     * }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre><xmp>
     * // Create the report create criteria model and set the host name
     * ReportCreateCriteria createCriteria = new ReportCreateCriteria();
     * createCriteria.hostName("host-1");
     * 
     * // Create the client and call the create API
     * Reports client = new Reports(properties);
     * Report obj = client.create(createCriteria);
     * </xmp></pre></div>
     */
    public Report create(ReportCreateCriteria createCriteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        Report result = getTarget().path("reports").request(MediaType.APPLICATION_JSON).post(Entity.json(createCriteria), Report.class);
        return result;
    }
    
    /**
     * Creates a new SAML report for the specified host by forcing a complete attestation cycle.
     * @param createCriteria The serialized ReportCreateCriteria java model object represents the content of the request body.
     * <pre>
     *          host_id             Host ID.
     * 
     *          host_name           Host name.
     * 
     *          hardware_uuid       Hardware UUID of host.
     * 
     *          aik_certificate     AIK certificate.
     * </pre>
     * @return SAML assertion string. 
     * @since ISecL 1.0
     * @mtwRequiresPermissions reports:create
     * @mtwContentTypeReturned SAML (application/samlassertion+xml)
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/reports
     * input: 
     * {
     *    "host_name":"host-1"
     * }
     * 
     * output:
     * {@code
     * <?xml version="1.0" encoding="UTF-8"?>
     * <saml2:Assertion ID="MapAssertion" IssueInstant="2018-05-15T23:34:37.966Z" Version="2.0" xmlns:saml2=
     *      "urn:oasis:names:tc:SAML:2.0:assertion">
     *     <saml2:Issuer>https://server.com</saml2:Issuer>
     *     <Signature xmlns="http://www.w3.org/2000/09/xmldsig#">
     *         <SignedInfo>
     *             <CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments"/>
     *             <SignatureMethod Algorithm="http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"/>
     *             <Reference URI="#MapAssertion">
     *                 <Transforms>
     *                     <Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/>
     *                 </Transforms>
     *                 <DigestMethod Algorithm="http://www.w3.org/2001/04/xmlenc#sha256"/>
     *                 <DigestValue>ppHvMk7R7PQqu293Xjf5uwlJa7cZUiJmqamz5ezPXS4=</DigestValue>
     *             </Reference>
     *         </SignedInfo>
     *         <SignatureValue>OjebCXBTFDe6YijoV4lD28h/GAkLXxvD0zeK9vyin1sQBjg6bfBBj+IaMu0NDWU0eJIKojnwS708
     * HB+u6aptlk3JftzfVmPwi6+3p1V4XdBvJQeukMGjxuBBjo2XJY4Z1mF72c2jmvg2xw1iaSSIJXE+
     * xzSy31pI1V4sPui0E6eSwKxktw6iP3wzg9tw5IR22tiTgbq1Cq4UkVb9y7HaRnJD5XN9lpZ2gmRr
     * odG52cPaGNDLjS+f0HJi2szFquQh+1f5hgdlukXdpePsE1SxFFWlMjWh5kqZ2PKxX3qt3HE7g+WD
     * 7ZzWy/zV+UgUIf5XMqAqKaXu+Kq+qap/2mv75g==</SignatureValue>
     *         <KeyInfo>
     *             <X509Data>
     *                 <X509Certificate>
     * MIIDYzCCAkugAwIBAgIEC1ZcPjANBgkqhkiG9w0BAQsFADBiMQswCQYDVQQGEwJVUzELMAkGA1UE
     * CBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwxEjAQBgNVBAsTCU10IFdpbHNv
     * bjERMA8GA1UEAxMIbXR3aWxzb24wHhcNMTgwNTA2MjM0MDAyWhcNMjgwNTAzMjM0MDAyWjBiMQsw
     * CQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwx
     * EjAQBgNVBAsTCU10IFdpbHNvbjERMA8GA1UEAxMIbXR3aWxzb24wggEiMA0GCSqGSIb3DQEBAQUA
     * A4IBDwAwggEKAoIBAQCoaCfPve1Pi1mM8SfSCAr1HF3UE5PiHXMz2Iam+MuWynVc0v+L1o6nBLRJ
     * FbolscTIvGjkpXW3IFcaI1dSU5yA+0qD8gKieV4Gumo1LFJQhhjouztjkokhWC9XxX/+5rqHwswb
     * muS42q2Fgq33uqK2wfVLc5hTeU4ovKFucFtx3GIKVZeKepi/5kB0FjIPh71bxY8q81/sUff66UY2
     * q+QtYiy7228pnNXmZhuWo0UMJBZlk6xYF5by4nC1msvwkRnCx7j0Fh2bgreo/5+DBzOAvh0AL9aq
     * Qnw5DNXOCXldKgeZubvGI5ft7bHS/x0m9gALaA2U94UjH7Hid285NAIzAgMBAAGjITAfMB0GA1Ud
     * DgQWBBT7UtwriRXtSXBBAlqfwnPzktz0yDANBgkqhkiG9w0BAQsFAAOCAQEAmeEHnYFSYzZh3Kv2
     * Eq3MVIpFow181kOOpZLlnvAZKYmcrHD3hYz5RgKvz3kflftDuo30SE4XVc+0aZJ0BVsBPqG2ryjl
     * TJPHbeJuDu/ckaaiPTu1H691MCQdMxMLViEWWBOL83pG8dru3ugQTgFQlyAc0VHH2DI1gst/6xRj
     * fl/sF8Wf8zSfvYbsQ2BHQU5phm1Myb4n72KCYxWa/nqvao3yvoLdER/UyUImV7+IDqaBQnYcZW5C
     * Ym1VBg+UdWJRwVZtyU+v8mXOgcZWvR2kY+Z5csiWjX3axQyMiMM0P+B4Ax8mFei1hLwL7QPojMbO
     * qg/0NtI06AUP7FMaPoCVKA==</X509Certificate>
     *             </X509Data>
     *         </KeyInfo>
     *     </Signature>
     *     <saml2:Subject>
     *         <saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">Hostname1</saml2:NameID>
     *         <saml2:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:sender-vouches">
     *             <saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">Intel Security Libraries
     *              </saml2:NameID>
     *             <saml2:SubjectConfirmationData NotBefore="2018-05-15T23:34:37.966Z" 
     *              NotOnOrAfter="2018-05-16T00:34:37.966Z"/>
     *         </saml2:SubjectConfirmation>
     *     </saml2:Subject>
     *     <saml2:AttributeStatement>
     *         <saml2:Attribute Name="biosVersion">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              Bios.version</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="hostName">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              Hostname1</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="tpmVersion">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              2.0</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="processorInfo">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              54 06 05 00 FF FB EB BF</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="vmmName">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              QEMU</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="hardwareUuid">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              Hardware.uuid</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="vmmVersion">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              1.5.3</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="osName">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              RedHatEnterpriseServer</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="noOfSockets">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              2</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="tpmEnabled">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              true</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="biosName">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              Intel Corporation</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="osVersion">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              7.4</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="processorFlags">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush 
     *              dts acpi mmx fxsr sse sse2 ss ht tm pbe syscall nx pdpe1gb rdtscp lm constant_tsc 
     *              art arch_perfmon pebs bts rep_good nopl xtopology nonstop_tsc aperfmperf eagerfpu 
     *              pni pclmulqdq dtes64 monitor ds_cpl vmx smx est tm2 ssse3 fma cx16 xtpr pdcm pcid 
     *              dca sse4_1 sse4_2 x2apic movbe popcnt tsc_deadline_timer aes xsave avx f16c rdrand 
     *              lahf_lm abm 3dnowprefetch epb cat_l3 cdp_l3 invpcid_single intel_pt tpr_shadow vnmi 
     *              flexpriority ept vpid fsgsbase tsc_adjust bmi1 hle avx2 smep bmi2 erms invpcid rtm 
     *              cqm mpx rdt_a avx512f avx512dq rdseed adx smap clflushopt clwb avx512cd avx512bw 
     *              avx512vl xsaveopt xsavec xgetbv1 cqm_llc cqm_occup_llc cqm_mbm_total cqm_mbm_local 
     *              dtherm ida arat pln pts hwp hwp_act_window hwp_epp hwp_pkg_req</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="txtEnabled">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              true</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="pcrBanks">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              [SHA1, SHA256]</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="TRUST_BIOS">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              true</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="TRUST_OS">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              true</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="TRUST_HOST_UNIQUE">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              true</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="TRUST_ASSET_TAG">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              NA</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="TRUST_OVERALL">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              true</saml2:AttributeValue>
     *         </saml2:Attribute>
     *     </saml2:AttributeStatement>
     * </saml2:Assertion>
     * }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre><xmp>
     * // Create the report create criteria model and set the create criteria
     * ReportCreateCriteria createCriteria = new ReportCreateCriteria();
     * createCriteria.hostName("host-1");
     * 
     * // Create the client and call the create API
     * Reports client = new Reports(properties);
     * String samlString = client.createSamlReport(createCriteria);
     * </xmp></pre></div>
     */
    public String createSamlReport(ReportCreateCriteria createCriteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        String samlAssertion = getTarget().path("reports").request(CryptoMediaType.APPLICATION_SAML).post(Entity.json(createCriteria), String.class);
        return samlAssertion;
    }

    /**
     * Retrieves a report for an active host.
     * @param locator The content models of the ReportLocator java model object can be used as path parameter.
     * <pre>
     *              id (required)         Report ID specified as a path parameter.
     * </pre>
     * @return <pre>The serialized Report java model object that was retrieved:
     *          id
     *          host_id
     *          trust_information
     *          created 
     *          expiration
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions reports:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/reports/7df1dcc9-31b9-4596-9a38-0a72bb57d7c8
     * 
     * output:
     * {
     *      "id": "8a545a4f-d282-4d91-8ec5-bcbe439dcfbc",
     *      "host_id": "94824cb6-d6c8-4faf-83b0-125996ceebe2",
     *      "trust_information": {
     *              "flavors_trust": {
     *                          "HOST_UNIQUE": 
     *                              {
     *                                "trust": true,
     *                                 "rules": 
     *                                     [
     *                                      {
     *                                          "rule": 
     *                                              {
     *                                          "rule_name": "com.intel.mtwilson.core.verifier.policy.rule.PcrEventLogIncludes",
     *                                          "markers": 
     *                                              [
     *                                                "HOST_UNIQUE"
     *                                              ],
     *                                          "pcr_bank": "SHA256",
     *                                          "pcr_index": "pcr_18",
     *                                          "expected": 
     *                                           [
     *                                              {
     *                                          "digest_type": "com.intel.mtwilson.core.common.model.MeasurementSha256",
     *                                          "value": "df3f619804a92fdb4057192dc43dd748ea778adc52bc498ce80524c014b81119",
     *                                          "label": "LCP_CONTROL_HASH",
     *                                          "info": 
     *                                             {
     *                                               "ComponentName": "LCP_CONTROL_HASH",
     *                                               "EventName": "OpenSource.EventName"
     *                                              }
     *                                             }
     *                                          ],
     *                                        },
     *                                  "flavor_id": "a774ddad-fca1-4670-86b2-605c88a16dab",
     *                                  "trusted": true
     *                                     },
     *                                   ]
     *                           },
     *                          "OS": {
     *                                          "trust": true,
     *                                          "rules": [...]
     *                                },
     * 
     *                          "PLATFORM": {       "trust": true,
     *                                          "rules": [...]
     *                                  },
     *                          }
     *                      },
     *              "OVERALL": true
     *      "created": "2018-07-23T16:39:52-0700",
     *      "expiration": "2018-07-23T17:39:52-0700"
     * }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre><xmp>
     * // Create the report locator model and set the locator id
     * ReportLocator locator = new ReportLocator();
     * locator.pathId = UUID.valueOf("7df1dcc9-31b9-4596-9a38-0a72bb57d7c8");
     * 
     * // Create the client and call the retrieve API
     * Reports client = new Reports(properties);
     * client.retrieve(locator);
     * </xmp></pre></div>
     */
    public Report retrieve(ReportLocator locator) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", locator.id.toString());
        Report obj = getTarget().path("reports/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(Report.class);
        return obj;      
    }

    /**
     * Searches attestation reports for hosts.
     * @param filterCriteria The content models of the ReportFilterCriteria java model object can be used as query parameters.
     * <pre>
     *          filter             Boolean value to indicate whether the response should be filtered to return no results 
     *                             instead of listing all reports with default parameter settings. This parameter 
     *                             will only be evaluated if no other parameters are specified by the user. Default value
     *                             is true. 
     * 
     * All query parameters listed below are evaluated in conjunction with each other. 
     * 
     * Report identifiers:
     *          id                  Report ID.
     * 
     * Host identifiers:
     *          hostId              Host ID. 
     * 
     *          hostName            Host name. If this parameter is specified, it will return reports only 
     *                              for active hosts with specified host name.
     * 
     *          hostHardwareId      Hardware UUID of the host. If this parameter is specified, it will 
     *                              return reports only for active hosts with specified host hardware uuid.
     * 
     * Host status identifiers:
     *          hostStatus          Current state of an active host.  A list of host states is defined in the description 
     *                              section of the HostStatus javadoc. 
     *                              
     * Date filters:
     * To further filter the report results, additional date restrictions can be specified.
     *          numberOfDays        Results returned will be restricted to between the current 
     *                              date and number of days prior. This option will override other date options.
     * 
     *          fromDate            Results returned will be restricted to after this date.
     * 
     *          toDate              Results returned will be restricted to before this date.
     * 
     * Currently the following ISO 8601 date formats are supported for date parameters:
     *          date.               Ex: fromDate=2015-05-01&amp;toDate=2015-06-01
     *          date+time.          Ex: fromDate=2015-04-05T00:00Z&amp;toDate=2015-06-05T00:00Z
     *          date+time+zone.     Ex: fromDate=2015-04-05T12:30-02:00&amp;toDate=2015-06-05T12:30-02:00
     * 
     * Limit filters:
     * Optionally user can restrict the number of reports retrieved by using the below criteria.
     *          latestPerHost       By default this is set to TRUE, returning only the latest report for each host.
     *                              If latestPerHost is specified in conjuction with a date filter, it will return the 
     *                              latest report for within the specified date range per host.
     *          
     *          limit               This limits the overall number of results (all hosts included); default value is set
     *                              to 10,000.
     *</pre>
     *@return The serialized ReportCollection java model object that was searched with collection of reports each containing:
     * <pre>
     *          id
     *          host_id
     *          trust_information
     *          created 
     *          expiration
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions reports:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/reports?numberOfDays=2&amp;latestPerHost=false
     * output:
     * {
     *      "id": "8a545a4f-d282-4d91-8ec5-bcbe439dcfbc",
     *      "host_id": "94824cb6-d6c8-4faf-83b0-125996ceebe2",
     *      "trust_information": {
     *              "flavors_trust": {
     *                          "HOST_UNIQUE": 
     *                              {
     *                                "trust": true,
     *                                 "rules": 
     *                                     [
     *                                      {
     *                                          "rule": 
     *                                              {
     *                                          "rule_name": "com.intel.mtwilson.core.verifier.policy.rule.PcrEventLogIncludes",
     *                                          "markers": 
     *                                              [
     *                                                "HOST_UNIQUE"
     *                                              ],
     *                                          "pcr_bank": "SHA256",
     *                                          "pcr_index": "pcr_18",
     *                                          "expected": 
     *                                           [
     *                                              {
     *                                          "digest_type": "com.intel.mtwilson.core.common.model.MeasurementSha256",
     *                                          "value": "df3f619804a92fdb4057192dc43dd748ea778adc52bc498ce80524c014b81119",
     *                                          "label": "LCP_CONTROL_HASH",
     *                                          "info": 
     *                                             {
     *                                               "ComponentName": "LCP_CONTROL_HASH",
     *                                               "EventName": "OpenSource.EventName"
     *                                              }
     *                                             }
     *                                          ],
     *                                        },
     *                                  "flavor_id": "a774ddad-fca1-4670-86b2-605c88a16dab",
     *                                  "trusted": true
     *                                     },
     *                                   ]
     *                           },
     *                          "OS": {
     *                                          "trust": true,
     *                                          "rules": [...]
     *                                },
     * 
     *                          "PLATFORM": {       "trust": true,
     *                                          "rules": [...]
     *                                  },
     *                          }
     *                      },
     *              "OVERALL": true
     *      "created": "2018-07-23T16:39:52-0700",
     *      "expiration": "2018-07-23T17:39:52-0700"
     * }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre><xmp>
     * // Create the report filter criteria model and set the search criteria
     * ReportFilterCriteria filterCriteria = new ReportFilterCriteria();
     * filterCriteria.latestPerHost(false);
     * filterCriteria.numberOfDays(2);
     * 
     * // Create the client and call the search API
     * Reports client = new Reports(properties);
     * ReportCollection obj = client.search(filterCriteria);
     * </xmp></pre></div>
     */
    public ReportCollection search(ReportFilterCriteria filterCriteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        ReportCollection objCollection = getTargetPathWithQueryParams("reports", filterCriteria).request(MediaType.APPLICATION_JSON).get(ReportCollection.class);
        return objCollection;              
    }
    
    /**
     * Searches for the SAML attestation reports for the hosts.
     * @param filterCriteria The content models of the ReportFilterCriteria java model object can be used as query parameters.
     * <pre>
     *          filter             Boolean value to indicate whether the response should be filtered to return no results 
     *                             instead of listing all reports with default parameter settings. This parameter 
     *                             will only be evaluated if no other parameters are specified by the user. Default value
     *                             is true. 
     * 
     * All query parameters listed below are evaluated in conjunction with each other. 
     * 
     * Report identifiers:
     *          id                  Report ID.
     * 
     * Host identifiers:
     *          hostId              Host ID. 
     * 
     *          hostName            Host name. If this parameter is specified, it will return reports only 
     *                              for active hosts with specified host name.
     * 
     *          hostHardwareId      Hardware UUID of the host. If this parameter is specified, it will 
     *                              return reports only for active hosts with specified host hardware uuid.
     * 
     * Host status identifiers:
     *          hostStatus          Current state of an active host.  A list of host states is defined in the description 
     *                              section of the HostStatus javadoc. 
     *                              
     * Date filters:
     * To further filter the report results, additional date restrictions can be specified.
     *          numberOfDays        Results returned will be restricted to between the current 
     *                              date and number of days prior. This option will override other date options.
     * 
     *          fromDate            Results returned will be restricted to after this date.
     * 
     *          toDate              Results returned will be restricted to before this date.
     * 
     * Currently the following ISO 8601 date formats are supported for date parameters:
     *          date.               Ex: fromDate=2015-05-01&amp;toDate=2015-06-01
     *          date+time.          Ex: fromDate=2015-04-05T00:00Z&amp;toDate=2015-06-05T00:00Z
     *          date+time+zone.     Ex: fromDate=2015-04-05T12:30-02:00&amp;toDate=2015-06-05T12:30-02:00
     * 
     * Limit filters:
     * Optionally user can restrict the number of reports retrieved by using the below criteria.
     *          latestPerHost       By default this is set to TRUE, returning only the latest report for each host.
     *                              If latestPerHost is specified in conjuction with a date filter, it will return the 
     *                              latest report for within the specified date range per host.
     *          
     *          limit               This limits the overall number of results (all hosts included); default value is set
     *                              to 10,000.
     * </pre>
     * @return String object having the SAML assertion(s) retrieved using the search criteria specified.
     * @since ISecL 1.0
     * @mtwRequiresPermissions reports:search
     * @mtwContentTypeReturned SAML (Accept content type header should be set to "Accept: application/samlassertion+xml")
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/reports?hostId=21f7d831-85b3-46bc-a499-c2d14ff136c8&amp;latestPerHost=false&amp;limit=100
     * output:
     * {@code
     * <?xml version="1.0" encoding="UTF-8"?>
     * <saml2:Assertion ID="MapAssertion" IssueInstant="2018-05-15T23:34:37.966Z" Version="2.0" xmlns:saml2=
     *      "urn:oasis:names:tc:SAML:2.0:assertion">
     *     <saml2:Issuer>https://server.com</saml2:Issuer>
     *     <Signature xmlns="http://www.w3.org/2000/09/xmldsig#">
     *         <SignedInfo>
     *             <CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments"/>
     *             <SignatureMethod Algorithm="http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"/>
     *             <Reference URI="#MapAssertion">
     *                 <Transforms>
     *                     <Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/>
     *                 </Transforms>
     *                 <DigestMethod Algorithm="http://www.w3.org/2001/04/xmlenc#sha256"/>
     *                 <DigestValue>ppHvMk7R7PQqu293Xjf5uwlJa7cZUiJmqamz5ezPXS4=</DigestValue>
     *             </Reference>
     *         </SignedInfo>
     *         <SignatureValue>OjebCXBTFDe6YijoV4lD28h/GAkLXxvD0zeK9vyin1sQBjg6bfBBj+IaMu0NDWU0eJIKojnwS708
     * HB+u6aptlk3JftzfVmPwi6+3p1V4XdBvJQeukMGjxuBBjo2XJY4Z1mF72c2jmvg2xw1iaSSIJXE+
     * xzSy31pI1V4sPui0E6eSwKxktw6iP3wzg9tw5IR22tiTgbq1Cq4UkVb9y7HaRnJD5XN9lpZ2gmRr
     * odG52cPaGNDLjS+f0HJi2szFquQh+1f5hgdlukXdpePsE1SxFFWlMjWh5kqZ2PKxX3qt3HE7g+WD
     * 7ZzWy/zV+UgUIf5XMqAqKaXu+Kq+qap/2mv75g==</SignatureValue>
     *         <KeyInfo>
     *             <X509Data>
     *                 <X509Certificate>
     * MIIDYzCCAkugAwIBAgIEC1ZcPjANBgkqhkiG9w0BAQsFADBiMQswCQYDVQQGEwJVUzELMAkGA1UE
     * CBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwxEjAQBgNVBAsTCU10IFdpbHNv
     * bjERMA8GA1UEAxMIbXR3aWxzb24wHhcNMTgwNTA2MjM0MDAyWhcNMjgwNTAzMjM0MDAyWjBiMQsw
     * CQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwx
     * EjAQBgNVBAsTCU10IFdpbHNvbjERMA8GA1UEAxMIbXR3aWxzb24wggEiMA0GCSqGSIb3DQEBAQUA
     * A4IBDwAwggEKAoIBAQCoaCfPve1Pi1mM8SfSCAr1HF3UE5PiHXMz2Iam+MuWynVc0v+L1o6nBLRJ
     * FbolscTIvGjkpXW3IFcaI1dSU5yA+0qD8gKieV4Gumo1LFJQhhjouztjkokhWC9XxX/+5rqHwswb
     * muS42q2Fgq33uqK2wfVLc5hTeU4ovKFucFtx3GIKVZeKepi/5kB0FjIPh71bxY8q81/sUff66UY2
     * q+QtYiy7228pnNXmZhuWo0UMJBZlk6xYF5by4nC1msvwkRnCx7j0Fh2bgreo/5+DBzOAvh0AL9aq
     * Qnw5DNXOCXldKgeZubvGI5ft7bHS/x0m9gALaA2U94UjH7Hid285NAIzAgMBAAGjITAfMB0GA1Ud
     * DgQWBBT7UtwriRXtSXBBAlqfwnPzktz0yDANBgkqhkiG9w0BAQsFAAOCAQEAmeEHnYFSYzZh3Kv2
     * Eq3MVIpFow181kOOpZLlnvAZKYmcrHD3hYz5RgKvz3kflftDuo30SE4XVc+0aZJ0BVsBPqG2ryjl
     * TJPHbeJuDu/ckaaiPTu1H691MCQdMxMLViEWWBOL83pG8dru3ugQTgFQlyAc0VHH2DI1gst/6xRj
     * fl/sF8Wf8zSfvYbsQ2BHQU5phm1Myb4n72KCYxWa/nqvao3yvoLdER/UyUImV7+IDqaBQnYcZW5C
     * Ym1VBg+UdWJRwVZtyU+v8mXOgcZWvR2kY+Z5csiWjX3axQyMiMM0P+B4Ax8mFei1hLwL7QPojMbO
     * qg/0NtI06AUP7FMaPoCVKA==</X509Certificate>
     *             </X509Data>
     *         </KeyInfo>
     *     </Signature>
     *     <saml2:Subject>
     *         <saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">Hostname1</saml2:NameID>
     *         <saml2:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:sender-vouches">
     *             <saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">Intel Security Libraries
     *              </saml2:NameID>
     *             <saml2:SubjectConfirmationData NotBefore="2018-05-15T23:34:37.966Z" 
     *              NotOnOrAfter="2018-05-16T00:34:37.966Z"/>
     *         </saml2:SubjectConfirmation>
     *     </saml2:Subject>
     *     <saml2:AttributeStatement>
     *         <saml2:Attribute Name="biosVersion">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              Bios.version</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="hostName">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              Hostname1</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="tpmVersion">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              2.0</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="processorInfo">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              54 06 05 00 FF FB EB BF</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="vmmName">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              QEMU</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="hardwareUuid">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              Hardware.uuid</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="vmmVersion">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              1.5.3</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="osName">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              RedHatEnterpriseServer</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="noOfSockets">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              2</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="tpmEnabled">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              true</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="biosName">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              Intel Corporation</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="osVersion">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              7.4</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="processorFlags">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush 
     *              dts acpi mmx fxsr sse sse2 ss ht tm pbe syscall nx pdpe1gb rdtscp lm constant_tsc 
     *              art arch_perfmon pebs bts rep_good nopl xtopology nonstop_tsc aperfmperf eagerfpu 
     *              pni pclmulqdq dtes64 monitor ds_cpl vmx smx est tm2 ssse3 fma cx16 xtpr pdcm pcid 
     *              dca sse4_1 sse4_2 x2apic movbe popcnt tsc_deadline_timer aes xsave avx f16c rdrand 
     *              lahf_lm abm 3dnowprefetch epb cat_l3 cdp_l3 invpcid_single intel_pt tpr_shadow vnmi 
     *              flexpriority ept vpid fsgsbase tsc_adjust bmi1 hle avx2 smep bmi2 erms invpcid rtm 
     *              cqm mpx rdt_a avx512f avx512dq rdseed adx smap clflushopt clwb avx512cd avx512bw 
     *              avx512vl xsaveopt xsavec xgetbv1 cqm_llc cqm_occup_llc cqm_mbm_total cqm_mbm_local 
     *              dtherm ida arat pln pts hwp hwp_act_window hwp_epp hwp_pkg_req</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="txtEnabled">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              true</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="pcrBanks">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              [SHA1, SHA256]</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="TRUST_PLATFORM">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              true</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="TRUST_OS">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              true</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="TRUST_HOST_UNIQUE">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              true</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="TRUST_ASSET_TAG">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              NA</saml2:AttributeValue>
     *         </saml2:Attribute>
     *         <saml2:Attribute Name="TRUST_OVERALL">
     *             <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi=
     *              "http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     *              true</saml2:AttributeValue>
     *         </saml2:Attribute>
     *     </saml2:AttributeStatement>
     * </saml2:Assertion>
     * }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre><xmp>
     * // Create the report filter criteria model and set the search criteria
     * ReportFilterCriteria filterCriteria = new ReportFilterCriteria();
     * filterCriteria.hostId("21f7d831-85b3-46bc-a499-c2d14ff136c8");
     * filterCriteria.latestPerHost(false);
     * filterCriteria.limit(100);
     * 
     * // Create the client and call the search API
     * Reports client = new Reports(properties);
     * String samlString = client.searchSamlReports(filterCriteria);
     * </xmp></pre></div>
     */
    public String searchSamlReports(ReportFilterCriteria filterCriteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        String hostSaml = getTargetPathWithQueryParams("reports", filterCriteria).request(CryptoMediaType.APPLICATION_SAML).get(String.class);
        return hostSaml;            
    }

    
    /**
     * Deletes a report.
     * @param locator The content models of the ReportLocator java model object can be used as path parameter.
     * <pre>
     *              id (required)         Report ID specified as a path parameter.
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions reports:delete
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/reports/21f7d831-85b3-46bc-a499-c2d14ff136c8
     * Output: 204 No content
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre><xmp>
     * // Create the report locator model and set the locator id
     * ReportLocator locator = new ReportLocator();
     * locator.pathId = UUID.valueOf("21f7d831-85b3-46bc-a499-c2d14ff136c8");
     * 
     * // Create the client and call the delete API
     * Reports client = new Reports(properties);
     * client.delete(locator);
     * </xmp></pre></div>
     */
    public void delete(ReportLocator locator) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", locator.pathId.toString());
        Response obj = getTarget().path("reports/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }
        
    /**
     * Verifies the signature of the retrieved SAML assertion using the SAML 
     * certificate stored in the user keystore created during user registration.
     * This functionality is available for the API library users only.
     * @param saml SAML assertion.
     * @return TrustAssertion object having the status of verification.
     * @throws java.security.KeyManagementException
     * @throws java.security.KeyStoreException
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.security.UnrecoverableEntryException
     * @throws java.security.cert.CertificateEncodingException
     * @since ISecL 1.0
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre><xmp>
     * // Create the reports model and set the create criteria
     * Report report = new Report();
     * report.setHostId(UUID.valueOf("de07c08a-7fc6-4c07-be08-0ecb2f803681"));
     * 
     * // Call the createSamlReport API to create saml report
     * String hostSaml = client.createSamlReport(report);
     * 
     * // Create the client and call the verifyTrustAssertion API
     * Reports client = new Reports(properties);
     * TrustAssertion verifyTrustAssertion = attestationClient.verifyTrustAssertion(createHostAttestationSaml);
     * </xmp></pre></div>
     */        
    public TrustAssertion verifyTrustAssertion(String saml) throws KeyManagementException, KeyStoreException, 
            NoSuchAlgorithmException, UnrecoverableEntryException, CertificateEncodingException {
        String keystorePath = getConfiguration().get("mtwilson.api.truststore");
        Password keystorePassword = getPassword("mtwilson.api.truststore.password");
        if( keystorePath != null && !keystorePath.isEmpty() && keystorePassword != null ) {
            SimpleKeystore keystore = new SimpleKeystore(new File(keystorePath), keystorePassword);
            X509Certificate[] trustedSamlCertificates;
            try {
                trustedSamlCertificates = keystore.getTrustedCertificates(SimpleKeystore.SAML);
            }
            catch(KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | CertificateEncodingException e) {
                throw e;
            }
            TrustAssertion trustAssertion = new TrustAssertion(trustedSamlCertificates, saml);
            return trustAssertion;            
        }
        return null;
    }
}
