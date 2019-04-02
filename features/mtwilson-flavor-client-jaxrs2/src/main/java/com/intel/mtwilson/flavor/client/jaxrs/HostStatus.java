/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.client.jaxrs;

import com.intel.mtwilson.flavor.rest.v2.model.HostStatusCollection;
import com.intel.mtwilson.flavor.rest.v2.model.HostStatusFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.HostStatusLocator;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * These resources are used to obtain host states.
 * <pre>
 * 
 * A host status gives the current state of a host. When a host is registered or created, the backend queue, flavor verification 
 * process is initiated for that host, and a connection is attempted to the host. Other activities will also automatically trigger 
 * backend queue flavor verifications which may force connections to the host.
 * 
 * If a successful connection is made, the host status will reflect a CONNECTED state and include the host manifest. The host manifest 
 * contains information collected from the host that is used to verify the host against respective flavors within the host’s associated 
 * flavor group.
 * 
 * If the connection fails or a problem occurs when attempting to retrieve the required information from the host, the host status will 
 * reflect an error state.
 * 
 * Host States:
 * CONNECTED                      Host is in a good, connected state
 * QUEUE                          Host is currently in the flavor verification queue
 * CONNECTION_FAILURE             A connection failure occurred
 * UNAUTHORIZED                   The Host Verification Service is NOT authorized to access the host
 * AIK_NOT_PROVISIONED            Host AIK certificate is not provisioned
 * EC_NOT_PRESENT                 Host Endorsement Certificate (EC) is not present
 * MEASURED_LAUNCH_FAILURE        Host failed to launch TXT
 * TPM_OWNERSHIP_FAILURE          Host agent does not have TPM ownership
 * TPM_NOT_PRESENT                No TPM exists on the host
 * UNSUPPORTED_TPM                Host TPM version is unsupported
 * UNKNOWN                        Host is in unknown state
 *  
 * </pre>
 */
public class HostStatus extends MtWilsonClient {
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
     * HostStatus client = new HostStatus(properties);
     * </pre>
     * @throws Exception 
     */
    public HostStatus(Properties properties) throws Exception {
        super(properties);
    }
    
    /**
     * Retrieves the host status for an active host.
     * @param locator The content models of the HostStatusLocator java model object can be used as path parameter.
     * <pre>
     *              id (required)         Host status ID specified as a path parameter.
     * </pre>
     * @return <pre>The serialized HostStatus java model object that was retrieved:
     *          id
     *          host_id
     *          status
     *          created
     *          host_manifest
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions host_status:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/host-status/7df1dcc9-31b9-4596-9a38-0a72bb57d7c8   
     * output:
     * {
     *      "id":"5f6bf2bb-0aab-4273-973c-4354371fc19d",
     *      "host_id":"7af05d65-4275-48fb-bd0b-ee1204fc0789",
     *      "status":{
     *          "host_state": "CONNECTED",
     *          "last_time_connected": "2018-03-01T12:52:12-0800"
     *      },
     *      "created":"2018-03-01T12:52:12-0800",
     *      "host_manifest":{
     *         "aik_certificate": "MIICzjCCAbagAwIBAgIGAWHjDdITMA0GCSqGSIb3DQEBCwUAMBsxGTAXBgNVBAMTEG10
     *                             d2lsc29uLXBjYS1haWswHhcNMTgwMzAxMTkzMzA5WhcNMjgwMjI5MTkzMzA5WjAAMIIB
     *                             IjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAluToT78mpGc0Yb/qBCakLv983KHU
     *                             QKvLEHe55g+t/4OLzwrwFtLKxDJsKuUfZTJp+XAy/Jh9z5MdO0D9vzNqgBFW5HiCTwX/
     *                             lMOJCQySSxA2Jp4Cc+tXCnmctemuYbflmd9KPmuPa+uGaGFTXx7ViOqP4EIPu53sIpa+
     *                             ZThIlxxxHzT6lrloyelIcdDPqsdNGHySaiO9bdw5nYyOTgW58iLK34jt7MPC4Qj63WWV
     *                             7aXsigkbGUyc6zCXxPOCJaxYnj2ndVRgTnJGTRIe0skdC81KS2kjHFB+82xYUblBXBQi
     *                             WHME4dQ/GbA1af07dfb1guxl689g69GZZgynnth+zwIDAQABozMwMTAvBgNVHREBAf8E
     *                             JTAjgSEAC/39/f0lRF5bP/39/ThT/XBK/S5KdP0+N/39/Qs3/QQwDQYJKoZIhvcNAQEL
     *                             BQADggEBAH4SKXIRnTRW+aKwpkrqOm/DUCALjWCDrSeH9gjMtWvV0bMhcwehAQtYuf61
     *                             UR4cGTrzQltCgAmDF2GWObSvJ+/eyTOENqrrVYZ6nLnJfgto0UYqW1JapP3ya19b3Knz
     *                             lrWrJsI7+uNVyTWnTG9DHM9C2xhZCijd5Me8z411us+pPugoJYhHhL1ix6lhtMH5LSHe
     *                             Kce9pHqA6PaNff0le+MGON/oDn/FFUdNnB3YcphUkdbzF6LOMZTznbwqnzP5Nbx63ip1
     *                             gLiey2BlMkkTdZRn2iNFKIGNDqchVTG6DAkpHQHc+pQevqZ2TxGkO/MmjKr2CJppSYeF
     *                             Sn9Zo5ZQplw=",
     *         "asset_tag_digest": "Y1YbyhI4iyD+EoHrgsHyfbBjb1FXnbpLGe0YkPEJyJc=",
     *         "host_info": {
     *             "host_name": "hostname1",
     *             "bios_name": "Intel Corporation",
     *             "bios_version": "Bios.version",
     *             "hardware_uuid": "Hardware.uuid",
     *             "os_name": "RedHatEnterpriseServer",
     *             "os_version": "7.4",
     *             "processor_info": "54 06 05 00 FF FB EB BF",
     *             "vmm_name": "",
     *             "vmm_version": "",
     *             "processor_flags": "fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat 
     *                                 pse36 clflush dts acpi mmx fxsr sse sse2 ss ht tm pbe syscall nx 
     *                                 pdpe1gb rdtscp lm constant_tsc art arch_perfmon pebs bts rep_good 
     *                                 nopl xtopology nonstop_tsc aperfmperf eagerfpu pni pclmulqdq dtes64
     *                                 monitor ds_cpl vmx smx est tm2 ssse3 fma cx16 xtpr pdcm pcid dca 
     *                                 sse4_1 sse4_2 x2apic movbe popcnt tsc_deadline_timer aes xsave avx 
     *                                 f16c rdrand lahf_lm abm 3dnowprefetch epb cat_l3 cdp_l3 invpcid_single 
     *                                 intel_pt tpr_shadow vnmi flexpriority ept vpid fsgsbase tsc_adjust 
     *                                 bmi1 hle avx2 smep bmi2 erms invpcid rtm cqm mpx rdt_a avx512f avx512dq 
     *                                 rdseed adx smap clflushopt clwb avx512cd avx512bw avx512vl xsaveopt 
     *                                 xsavec xgetbv1 cqm_llc cqm_occup_llc cqm_mbm_total cqm_mbm_local 
     *                                 dtherm ida arat pln pts hwp hwp_act_window hwp_epp hwp_pkg_req",
     *             "tpm_version": "2.0",
     *             "pcr_banks": [
     *                 "SHA1",
     *                 "SHA256"
     *             ],
     *             "no_of_sockets": "2",
     *             "tpm_enabled": "true",
     *             "txt_enabled": "true"
     *         },
     *         "pcr_manifest": {
     *             "sha1pcrs": [
     *                 {
     *                     "digest_type": "com.intel.mtwilson.lib.common.model.PcrSha1",
     *                     "index": "pcr_0",
     *                     "value": "d2ed125942726641a7260c4f92beb67d531a0def",
     *                     "pcr_bank": "SHA1"
     *                 }...
     *             ],
     *             "sha2pcrs": [
     *                 {
     *                     "digest_type": "com.intel.mtwilson.lib.common.model.PcrSha256",
     *                     "index": "pcr_0",
     *                     "value": "db83f0e8a1773c21164c17986037cdf8afc1bbdc1b815772c6da1befb1a7f8a3",
     *                     "pcr_bank": "SHA256"
     *                 }...
     *             ],
     *             "measurement_xml": "",
     *             "pcr_event_log_map": {
     *                 "SHA1": [
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.PcrEventLogSha1",
     *                         "pcr_index": "pcr_17",
     *                         "event_log": [
     *                             {
     *                                 "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                                 "value": "2fb7d57dcc5455af9ac08d82bdf315dbcc59a044",
     *                                 "label": "HASH_START",
     *                                 "info": {
     *                                     "ComponentName": "HASH_START",
     *                                     "EventName": "OpenSource.EventName"
     *                                 }
     *                             }...
     *                         ],
     *                         "pcr_bank": "SHA1"
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.PcrEventLogSha1",
     *                         "pcr_index": "pcr_18",
     *                         "event_log": [
     *                             {
     *                                 "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                                 "value": "a395b723712b3711a89c2bb5295386c0db85fe44",
     *                                 "label": "SINIT_PUBKEY_HASH",
     *                                 "info": {
     *                                     "ComponentName": "SINIT_PUBKEY_HASH",
     *                                     "EventName": "OpenSource.EventName"
     *                                 }
     *                             }...
     *                         ],
     *                         "pcr_bank": "SHA1"
     *                     }
     *                 ],
     *                 "SHA256": [
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.PcrEventLogSha256",
     *                         "pcr_index": "pcr_17",
     *                         "event_log": [
     *                             {
     *                                 "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                                 "value": "9301981c093654d5aa3430ba05c880a52eb22b9e18248f5f93e1fe1dab1cb947",
     *                                 "label": "HASH_START",
     *                                 "info": {
     *                                     "ComponentName": "HASH_START",
     *                                     "EventName": "OpenSource.EventName"
     *                                 }
     *                             }...
     *                         ],
     *                         "pcr_bank": "SHA256"
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.PcrEventLogSha256",
     *                         "pcr_index": "pcr_18",
     *                         "event_log": [
     *                             {
     *                                 "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                                 "value": "da256395df4046319ef0af857d377a729e5bc0693429ac827002ffafe485b2e7",
     *                                 "label": "SINIT_PUBKEY_HASH",
     *                                 "info": {
     *                                     "ComponentName": "SINIT_PUBKEY_HASH",
     *                                     "EventName": "OpenSource.EventName"
     *                                 }
     *                             }...
     *                         ],
     *                         "pcr_bank": "SHA256"
     *                     }
     *                 ]
     *             },
     *             "pcrs": [
     *                 {
     *                     "digest_type": "com.intel.mtwilson.lib.common.model.PcrSha1",
     *                     "index": "pcr_0",
     *                     "value": "d2ed125942726641a7260c4f92beb67d531a0def",
     *                     "pcr_bank": "SHA1"
     *                 }...
     *             ],
     *             "provisioned_tag": "Y1YbyhI4iyD+EoHrgsHyfbBjb1FXnbpLGe0YkPEJyJc="
     *         },
     *         "tpm_enabled": true,
     *         "txt_enabled": true
     *      }
     * }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the host status locator model and set the locator id
     * HostStatusLocator locator = new HostStatusLocator();
     * locator.id = "826501bd-3c75-4839-a08f-db5f744f8498";
     * 
     * // Create the client and call the retrieve API
     * HostStatus client = new HostStatus(properties);
     * HostStatus obj = client.retrieve(locator);
     * </pre></div>
     */
    public com.intel.mtwilson.flavor.rest.v2.model.HostStatus retrieve(HostStatusLocator locator) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", locator.pathId.toString());
        com.intel.mtwilson.flavor.rest.v2.model.HostStatus obj = getTarget().path("host-status/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(com.intel.mtwilson.flavor.rest.v2.model.HostStatus.class);
        return obj;      
    }
    
    /**
     * Searches for host status records.
     * @param filterCriteria The content models of the HostStatusFilterCriteria java model object can be used as query parameters.
     * <pre>
     *          filter              Boolean value to indicate whether the response should be filtered to return no results 
     *                              instead of listing all host statuses with default parameter settings. This parameter 
     *                              will only be evaluated if no other parameters are specified by the user. Default value
     *                              is true. 
     * 
     * All query parameters listed below are evaluated in conjunction with each other. 
     * 
     * Host status identifiers:
     *          id                  Host status ID.
     * 
     *          hostStatus          State of host. A list of host states is defined in the 
     *                              description section above.
     *
     * Host identifiers :
     *          hostId              Host ID. 
     *   
     *          hostName            Host name. If this parameter is specified, it will return host status records 
     *                              only for active hosts with specified name. 
     * 
     *          hostHardwareId      Hardware UUID of the host. 
     * 
     *          aikCertificate      AIK certificate of the host.
     *  
     * Date filters :
     * To further filter the host status results, additional date restrictions can be specified.
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
     * Limit filters :
     * Optionally user can restrict the number of host statuses retrieved by using the below criteria.
     *          latestPerHost       By default this is set to TRUE, returning only the latest host status for each host.
     *                              If laterPerHost is specified in conjuction with a date filter, it will return the 
     *                              latest host status for within the specified date range per host.
     * 
     *          limit               This limits the overall number of results (all hosts included); default value is set 
     *                              to 10,000.
     * </pre>
     * @return <pre>The serialized HostStatusCollection java model object that was searched with collection of host status objects each 
     * containing:
     *          id
     *          hostId
     *          status
     *          created
     *          host_manifest
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions host_status:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/host-status?hostStatus=CONNECTED
     * output:
     * {
     *      "host_status":[{
     *          "id":"5f6bf2bb-0aab-4273-973c-4354371fc19d",
     *          "host_id":"7af05d65-4275-48fb-bd0b-ee1204fc0789",
     *          "status":{
     *              "host_state": "CONNECTED",
     *              "last_time_connected": "2018-03-01T12:52:12-0800"
     *          },
     *          "created":"2018-03-01T12:52:12-0800",
     *          "host_manifest":{...}
     *      }]
     * }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the host status filter criteria model and set the criteria to be searched
     * HostStatusFilterCriteria filterCriteria = new HostStatusFilterCriteria();
     * filterCriteria.hostStatus = "CONNECTED";
     * 
     * // Create the client and call the search API
     * HostStatus client = new HostStatus(properties);
     * HostStatusCollection obj = client.search(filterCriteria));
     * </pre></div>
     */
    public HostStatusCollection search(HostStatusFilterCriteria filterCriteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HostStatusCollection objCollection = getTargetPathWithQueryParams("host-status", filterCriteria).request(MediaType.APPLICATION_JSON).get(HostStatusCollection.class);
        return objCollection;              
    }
}
