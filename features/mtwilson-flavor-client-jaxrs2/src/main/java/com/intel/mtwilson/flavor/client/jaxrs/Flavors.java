/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.client.jaxrs;

import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorCollection;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorCreateCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorLocator;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * These resources are used to manage flavors.
 * <pre>
 * 
 * A flavor is a set of measurements and metadata organized in a flexible format that allows for ease of further extension. The 
 * measurements included in the flavor pertain to various hardware, software and feature categories, and their respective metadata 
 * sections provide descriptive information.
 * 
 * The four current flavor categories:
 * PLATFORM, OS, ASSET_TAG, HOST_UNIQUE (See the product guide for a detailed explanation)
 * 
 * When a flavor is created, it is associated with a flavor group. This means that the measurements for that flavor type are deemed
 * acceptable to obtain a trusted status. If a host, associated with the same flavor group, matches the measurements contained within 
 * that flavor, the host is trusted for that particular flavor category (dependent on the flavor group policy).
 * </pre>
 */
public class Flavors extends MtWilsonClient {

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
     * Flavors client = new Flavors(properties);
     * </pre>
     * @throws Exception 
     */
    public Flavors(Properties properties) throws Exception {
        super(properties);
    }
    
    
    /**
     * Creates a flavor(s).
     * <pre>
     * Flavors can be created by directly providing the flavor content in the request body, or they can be imported from a host. 
     * If the flavor content is provided, the flavor parameter must be set in the request. If the flavor is being imported from a 
     * host, the host connection string must be specified.
     * 
     * If a flavor group is not specified, the flavor(s) created will be assigned to the default “mtwilson_automatic” flavor group, 
     * with the exception of the host unique flavors, which are associated with the “mtwilson_unique” flavor group. If a flavor group 
     * is specified and does not already exist, it will be created with a default flavor match policy.
     * 
     * Partial flavor types can be specified as an array input. In this fashion, the user can choose which flavor types to import from 
     * a host. Only flavor types that are defined in the flavor group flavor match policy can be specified. If no partial flavor types 
     * are provided, the default action is to attempt retrieval of all flavor types. The response will contain all flavor types that 
     * it was able to create.
     * 
     * A TLS policy ID can be specified if one has previously been created (See mtwilson-tls-policy-client-jaxrs2). Alternatively, if the 
     * string text “TRUST_FIRST_CERTIFICATE” is specified, that generic policy will be used (See the product guide for further description 
     * on generic TLS policies). If no TLS policy is provided, the service will automatically use the default TLS policy specified during 
     * HVS installation (See product guide for description on default TLS policies).
     * 
     * If generic flavors are created, all hosts in the flavor group will be added to the backend queue, flavor verification process to 
     * re-evaluate their trust status. If host unique flavors are created, the individual affected hosts are added to the flavor 
     * verification process.
     * </pre>
     * @param createCriteria The serialized FlavorCreateCriteria java model object represents the content of the request body.
     * <pre> 
     *          connection_string               The host connection string. 
     * 
     *                                          For INTEL & MICROSOFT hosts, this would have the vendor name, the IP addresses, 
     *                                          or DNS host name and credentials.
     *                                          i.e.: "intel:https://trustagent.server.com:1443;u=trustagentUsername;p=
     *                                          trustagentPassword"
     *                                          microsoft:https://trustagent.server.com:1443;u=trustagentUsername;p=
     *                                          trustagentPassword
     * 
     *                                          For VMware, this includes the vCenter and host IP address or DNS host name.
     *                                          i.e.: "vmware:https://vCenterServer.com:443/sdk;h=trustagent.server.com;u=
     *                                          vCenterUsername;p=vCenterPassword"
     * 
     *          flavor_collection               A collection of flavors in the defined flavor format.
     * 
     *          flavorgroup_name                Flavor group name that the created flavor(s) will be associated.
     * 
     *          partial_flavor_types            List array input of flavor types to be imported from a host. See the product guide 
     *                                          for more details on how flavor types are broken down for each host type.
     * 
     *          tls_policy_id                   ID of the TLS policy for connection from the HVS to the host.
     * 
     * Only one of the above parameters can be specified. The parameters listed here are in the order of priority that will be evaluated.
     * </pre>
     * @return <pre>The serialized FlavorCollection java model object that was created with collection of flavors each containing:
     *          meta (descriptive information)
     *          pcrs (measurements)</pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions flavors:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/flavors
     * input: 
     * { 
     *    "connection_string": "intel:https://trustagent.server.com:1443;u=trustagentUsername;p=trustagentPassword"
     * }
     * 
     * output:
     * {
     *     "flavors": [
     *         {
     *             "meta": {
     *                 "id": "9335ba27-0b8c-47c5-9957-b60868f81f70",
     *                 "vendor": "INTEL",
     *                 "description": {
     *                     "flavor_part": "HOST_UNIQUE",
     *                     "source": "source1",
     *                     "bios_name": "Intel Corporation",
     *                     "bios_version": "BIOS.version",
     *                     "os_name": "RedHatEnterpriseServer",
     *                     "os_version": "7.4",
     *                     "tpm_version": "2.0",
     *                     "hardware_uuid": "Hardware.uuid"
     *                 }
     *             },
     *             "pcrs": {
     *                 "SHA1": {
     *                     "pcr_17": {
     *                         "value": "8c0696b6f3fbfe1ecbdaa3cad2a41d33d88af187",
     *                         "event": [
     *                             {
     *                                 "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                                 "value": "9069ca78e7450a285173431b3e52c5c25299e473",
     *                                 "label": "LCP_CONTROL_HASH",
     *                                 "info": {
     *                                     "ComponentName": "LCP_CONTROL_HASH",
     *                                     "EventName": "OpenSource.EventName"
     *                                 }
     *                             },
     *                             {
     *                                 "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                                 "value": "0a2039ce3a42e55ece79fb42bc317aeed6fcf405",
     *                                 "label": "initrd",
     *                                 "info": {
     *                                     "ComponentName": "initrd",
     *                                     "EventName": "OpenSource.EventName"
     *                                 }
     *                             }
     *                         ]
     *                     },
     *                     "pcr_18": {
     *                         "value": "983ec7db975ed31e2c85ef8e375c038d6d307efb",
     *                         "event": [
     *                             {
     *                                 "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                                 "value": "9069ca78e7450a285173431b3e52c5c25299e473",
     *                                 "label": "LCP_CONTROL_HASH",
     *                                 "info": {
     *                                     "ComponentName": "LCP_CONTROL_HASH",
     *                                     "EventName": "OpenSource.EventName"
     *                                 }
     *                             }
     *                         ]
     *                     }
     *                 },
     *                 "SHA256": {
     *                     "pcr_17": {
     *                         "value": "de171f355eb95af406fa127ca70d267bf68d0315f2f928ae293ee902c5ef9e17",
     *                         "event": [
     *                             {
     *                                 "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                                 "value": "df3f619804a92fdb4057192dc43dd748ea778adc52bc498ce80524c014b81119",
     *                                 "label": "LCP_CONTROL_HASH",
     *                                 "info": {
     *                                     "ComponentName": "LCP_CONTROL_HASH",
     *                                     "EventName": "OpenSource.EventName"
     *                                 }
     *                             },
     *                             {
     *                                 "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                                 "value": "de20f8a774db6bd0fef145fd0aa273a29e901a4fc3abe9c3a5fc7738d92020da",
     *                                 "label": "initrd",
     *                                 "info": {
     *                                     "ComponentName": "initrd",
     *                                     "EventName": "OpenSource.EventName"
     *                                 }
     *                             }
     *                         ]
     *                     },
     *                     "pcr_18": {
     *                         "value": "c1f7bfdae5f270d9f13aa9620b8977951d6b759f1131fe9f9289317f3a56efa1",
     *                         "event": [
     *                             {
     *                                 "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                                 "value": "df3f619804a92fdb4057192dc43dd748ea778adc52bc498ce80524c014b81119",
     *                                 "label": "LCP_CONTROL_HASH",
     *                                 "info": {
     *                                     "ComponentName": "LCP_CONTROL_HASH",
     *                                     "EventName": "OpenSource.EventName"
     *                                 }
     *                             }
     *                         ]
     *                     }
     *                 }
     *             }
     *         }
     *     ]
     * }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the flavor filter criteria model and set the criteria to be searched
     * FlavorCreateCriteria createCriteria = new FlavorCreateCriteria();
     * createCriteria.setConnectionString("intel:https://trustagent.server.com:1443;u=trustagentUsername;
     *              p=trustagentPassword");
     * 
     * // Create the client and call the create API
     * Flavors client = new Flavors(properties);
     * FlavorCollection obj = client.create(createCriteria);
     * </pre></div>
     */
    public FlavorCollection create(FlavorCreateCriteria createCriteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        FlavorCollection newObj = getTarget().path("flavors").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(createCriteria), FlavorCollection.class);
        return newObj;
    }
    
    /**
     * Retrieves a flavor.
     * @param locator The content model of the FlavorLocator java model object can be used as a path parameter.
     * <pre>
     *          id (required)          Flavor ID specified as a path parameter.
     * </pre>
     * @return <pre>The serialized Flavor java model object that was retrieved:
     *          meta (descriptive information)
     *          pcrs (measurements)</pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions flavors:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/flavors/21f7d831-85b3-46bc-a499-c2d14ff136c8
     * output:
     * { 
     *     "meta": {
     *         "id": "d82d200d-e972-4df6-8228-4c2ed4b15566",
     *         "vendor": "INTEL",
     *         "description": {
     *             "flavor_part": "PLATFORM",
     *             "source": "source1",
     *             "bios_name": "Intel Corporation",
     *             "bios_version": "Bios.version",
     *             "tpm_version": "2.0"
     *         }
     *     },
     *     "hardware": {
     *         "processor_info": "54 06 05 00 FF FB EB BF",
     *         "processor_flags": "fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge 
     *                             mca cmov pat pse36 clflush dts acpi mmx fxsr sse sse2 
     *                             ss ht tm pbe syscall nx pdpe1gb rdtscp lm constant_tsc 
     *                             art arch_perfmon pebs bts rep_good nopl xtopology 
     *                             nonstop_tsc aperfmperf eagerfpu pni pclmulqdq dtes64     
     *                             monitor ds_cpl vmx smx est tm2 ssse3 fma cx16 xtpr pdcm 
     *                             pcid dca sse4_1 sse4_2 x2apic movbe popcnt tsc_deadline_timer    
     *                             aes xsave avx f16c rdrand lahf_lm abm 3dnowprefetch epb cat_l3   
     *                             cdp_l3 invpcid_single intel_pt tpr_shadow vnmi flexpriority ept 
     *                             vpid fsgsbase tsc_adjust bmi1 hle avx2 smep bmi2 erms invpcid 
     *                             rtm cqm mpx rdt_a avx512f avx512dq rdseed adx smap clflushopt 
     *                             clwb avx512cd avx512bw avx512vl xsaveopt xsavec xgetbv1 cqm_llc 
     *                             cqm_occup_llc cqm_mbm_total cqm_mbm_local dtherm ida arat pln    
     *                             pts hwp hwp_act_window hwp_epp hwp_pkg_req",
     *         "feature": {
     *             "txt": {
     *                 "enabled": true
     *             },
     *             "tpm": {
     *                 "enabled": true,
     *                 "pcr_banks": [
     *                     "SHA1",
     *                     "SHA256"
     *                 ]
     *             }
     *         }
     *     },
     *     "pcrs": {
     *         "SHA1": {
     *             "pcr_0": {
     *                 "value": "d2ed125942726641a7260c4f92beb67d531a0def"
     *             },
     *             "pcr_17": {
     *                 "value": "af755642ff864e9de0b168a63578950e0f0fc9d5",
     *                 "event": [
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                         "value": "2fb7d57dcc5455af9ac08d82bdf315dbcc59a044",
     *                         "label": "HASH_START",
     *                         "info": {
     *                             "ComponentName": "HASH_START",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                         "value": "ffb1806465d2de1b7531fd5a2a6effaad7c5a047",
     *                         "label": "BIOSAC_REG_DATA",
     *                         "info": {
     *                             "ComponentName": "BIOSAC_REG_DATA",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                         "value": "3c585604e87f855973731fea83e21fab9392d2fc",
     *                         "label": "CPU_SCRTM_STAT",
     *                         "info": {
     *                             "ComponentName": "CPU_SCRTM_STAT",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                         "value": "5ba93c9db0cff93f52b521d7420e43f6eda2784f",
     *                         "label": "LCP_DETAILS_HASH",
     *                         "info": {
     *                             "ComponentName": "LCP_DETAILS_HASH",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                         "value": "5ba93c9db0cff93f52b521d7420e43f6eda2784f",
     *                         "label": "STM_HASH",
     *                         "info": {
     *                             "ComponentName": "STM_HASH",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                         "value": "3c585604e87f855973731fea83e21fab9392d2fc",
     *                         "label": "OSSINITDATA_CAP_HASH",
     *                         "info": {
     *                             "ComponentName": "OSSINITDATA_CAP_HASH",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                         "value": "1feb856e9e46ed7b19e9115332bb6a25db07d2a2",
     *                         "label": "MLE_HASH",
     *                         "info": {
     *                             "ComponentName": "MLE_HASH",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                         "value": "274f929dbab8b98a7031bbcd9ea5613c2a28e5e6",
     *                         "label": "NV_INFO_HASH",
     *                         "info": {
     *                             "ComponentName": "NV_INFO_HASH",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                         "value": "ca96de412b4e8c062e570d3013d2fccb4b20250a",
     *                         "label": "tb_policy",
     *                         "info": {
     *                             "ComponentName": "tb_policy",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     }
     *                 ]
     *             },
     *             "pcr_18": {
     *                 "value": "983ec7db975ed31e2c85ef8e375c038d6d307efb",
     *                 "event": [
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                         "value": "a395b723712b3711a89c2bb5295386c0db85fe44",
     *                         "label": "SINIT_PUBKEY_HASH",
     *                         "info": {
     *                             "ComponentName": "SINIT_PUBKEY_HASH",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                         "value": "3c585604e87f855973731fea83e21fab9392d2fc",
     *                         "label": "CPU_SCRTM_STAT",
     *                         "info": {
     *                             "ComponentName": "CPU_SCRTM_STAT",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                         "value": "3c585604e87f855973731fea83e21fab9392d2fc",
     *                         "label": "OSSINITDATA_CAP_HASH",
     *                         "info": {
     *                             "ComponentName": "OSSINITDATA_CAP_HASH",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                         "value": "5ba93c9db0cff93f52b521d7420e43f6eda2784f",
     *                         "label": "LCP_AUTHORITIES_HASH",
     *                         "info": {
     *                             "ComponentName": "LCP_AUTHORITIES_HASH",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                         "value": "274f929dbab8b98a7031bbcd9ea5613c2a28e5e6",
     *                         "label": "NV_INFO_HASH",
     *                         "info": {
     *                             "ComponentName": "NV_INFO_HASH",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                         "value": "ca96de412b4e8c062e570d3013d2fccb4b20250a",
     *                         "label": "tb_policy",
     *                         "info": {
     *                             "ComponentName": "tb_policy",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     }
     *                 ]
     *             }
     *         },
     *         "SHA256": {
     *             "pcr_0": {
     *                 "value": "db83f0e8a1773c21164c17986037cdf8afc1bbdc1b815772c6da1befb1a7f8a3"
     *             },
     *             "pcr_17": {
     *                 "value": "6c4318b65be3eb364fe2c781c35e9c17549dd0876e90898d827791742bf7216f",
     *                 "event": [
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                         "value": "9301981c093654d5aa3430ba05c880a52eb22b9e18248f5f93e1fe1dab1cb947",
     *                         "label": "HASH_START",
     *                         "info": {
     *                             "ComponentName": "HASH_START",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                         "value": "2785d1ed65f6b5d4b555dc24ce5e068a44ce8740fe77e01e15a10b1ff66cca90",
     *                         "label": "BIOSAC_REG_DATA",
     *                         "info": {
     *                             "ComponentName": "BIOSAC_REG_DATA",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                         "value": "67abdd721024f0ff4e0b3f4c2fc13bc5bad42d0b7851d456d88d203d15aaa450",
     *                         "label": "CPU_SCRTM_STAT",
     *                         "info": {
     *                             "ComponentName": "CPU_SCRTM_STAT",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                         "value": "6e340b9cffb37a989ca544e6bb780a2c78901d3fb33738768511a30617afa01d",
     *                         "label": "LCP_DETAILS_HASH",
     *                         "info": {
     *                             "ComponentName": "LCP_DETAILS_HASH",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                         "value": "6e340b9cffb37a989ca544e6bb780a2c78901d3fb33738768511a30617afa01d",
     *                         "label": "STM_HASH",
     *                         "info": {
     *                             "ComponentName": "STM_HASH",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                         "value": "67abdd721024f0ff4e0b3f4c2fc13bc5bad42d0b7851d456d88d203d15aaa450",
     *                         "label": "OSSINITDATA_CAP_HASH",
     *                         "info": {
     *                             "ComponentName": "OSSINITDATA_CAP_HASH",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                         "value": "111731901ab11e7ad7c5dc6ed771691f69f83baa91656500f7426206ad261992",
     *                         "label": "MLE_HASH",
     *                         "info": {
     *                             "ComponentName": "MLE_HASH",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                         "value": "0f6e0c7a5944963d7081ea494ddff1e9afa689e148e39f684db06578869ea38b",
     *                         "label": "NV_INFO_HASH",
     *                         "info": {
     *                             "ComponentName": "NV_INFO_HASH",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                         "value": "27808f64e6383982cd3bcc10cfcb3457c0b65f465f779d89b668839eaf263a67",
     *                         "label": "tb_policy",
     *                         "info": {
     *                             "ComponentName": "tb_policy",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     }
     *                 ]
     *             },
     *             "pcr_18": {
     *                 "value": "c1f7bfdae5f270d9f13aa9620b8977951d6b759f1131fe9f9289317f3a56efa1",
     *                 "event": [
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                         "value": "da256395df4046319ef0af857d377a729e5bc0693429ac827002ffafe485b2e7",
     *                         "label": "SINIT_PUBKEY_HASH",
     *                         "info": {
     *                             "ComponentName": "SINIT_PUBKEY_HASH",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                         "value": "67abdd721024f0ff4e0b3f4c2fc13bc5bad42d0b7851d456d88d203d15aaa450",
     *                         "label": "CPU_SCRTM_STAT",
     *                         "info": {
     *                             "ComponentName": "CPU_SCRTM_STAT",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                         "value": "67abdd721024f0ff4e0b3f4c2fc13bc5bad42d0b7851d456d88d203d15aaa450",
     *                         "label": "OSSINITDATA_CAP_HASH",
     *                         "info": {
     *                             "ComponentName": "OSSINITDATA_CAP_HASH",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                         "value": "6e340b9cffb37a989ca544e6bb780a2c78901d3fb33738768511a30617afa01d",
     *                         "label": "LCP_AUTHORITIES_HASH",
     *                         "info": {
     *                             "ComponentName": "LCP_AUTHORITIES_HASH",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                         "value": "0f6e0c7a5944963d7081ea494ddff1e9afa689e148e39f684db06578869ea38b",
     *                         "label": "NV_INFO_HASH",
     *                         "info": {
     *                             "ComponentName": "NV_INFO_HASH",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     },
     *                     {
     *                         "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                         "value": "27808f64e6383982cd3bcc10cfcb3457c0b65f465f779d89b668839eaf263a67",
     *                         "label": "tb_policy",
     *                         "info": {
     *                             "ComponentName": "tb_policy",
     *                             "EventName": "OpenSource.EventName"
     *                         }
     *                     }
     *                 ]
     *             }
     *         }
     *     }
     * }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the flavor locator model and set the locator id
     * FlavorLocator locator = new FlavorLocator();
     * locator.pathId = UUID.valueOf("21f7d831-85b3-46bc-a499-c2d14ff136c8");
     * 
     * // Create the client and call the retrieve API
     * Flavors client = new Flavors(properties);
     * Flavor obj = client.retrieve(locator);
     * </pre></div>
     */
    public Flavor retrieve(FlavorLocator locator) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", locator.pathId.toString());
        Flavor obj = getTarget().path("flavors/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(Flavor.class);
        return obj;
    }
    
//    public void deleteFlavor(FlavorFilterCriteria criteria) {
//        log.debug("target: {}", getTarget().getUri().toString());
//    }
    
    /**
     * Searches for flavors.
     * @param filterCriteria The content models of the FlavorFilterCriteria java model object can be used as query parameters.
     * <pre>
     *          filter                  Boolean value to indicate whether the response should be filtered to return no 
     *                                  results instead of listing all flavors. Default value is true.
     * 
     *          id                      Flavor ID.
     * 
     *          key and value           The key can be any “key” field from the meta description section of a flavor. The
     *                                  value can be any “value” of the specified key field in the flavor meta description
     *                                  section. Both key and value query parameters need to be specified.
     * 
     *          flavorgroupId           Flavor group ID.
     * 
     *          flavorParts             List array input of flavor types. See the product guide for more details on flavor 
     *                                  types.
     * 
     * Only one of the above parameters can be specified. The parameters listed here are in the order of priority that will be evaluated.
     * </pre>
     * @return <pre>The serialized FlavorCollection java model object that was searched with collection of flavors each containing:
     *          meta (descriptive information)
     *          pcrs (measurements)</pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions flavors:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/flavors?key=os_name&amp;value=RedHatEnterpriseServer
     * output:
     * {
     *     "flavors": [
     *         {
     *             "meta": {
     *                 "id": "8d982427-ee29-461d-a83f-f81160aebef1",
     *                 "vendor": "INTEL",
     *                 "description": {
     *                     "flavor_part": "HOST_UNIQUE",
     *                     "source": "source1",
     *                     "bios_name": "Intel Corporation",
     *                     "bios_version": "Bios.version",
     *                     "os_name": "RedHatEnterpriseServer",
     *                     "os_version": "7.4",
     *                     "tpm_version": "2.0",
     *                     "hardware_uuid": "Hardware.uuid"
     *                 }
     *             },
     *             "pcrs": {
     *                 "SHA1": {
     *                     "pcr_17": {
     *                         "value": "af755642ff864e9de0b168a63578950e0f0fc9d5",
     *                         "event": [
     *                             {
     *                                 "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                                 "value": "9069ca78e7450a285173431b3e52c5c25299e473",
     *                                 "label": "LCP_CONTROL_HASH",
     *                                 "info": {
     *                                     "ComponentName": "LCP_CONTROL_HASH",
     *                                     "EventName": "OpenSource.EventName"
     *                                 }
     *                             },
     *                             {
     *                                 "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                                 "value": "212dea77058773fdf5ace6b8b63c5df717548846",
     *                                 "label": "initrd",
     *                                 "info": {
     *                                     "ComponentName": "initrd",
     *                                     "EventName": "OpenSource.EventName"
     *                                 }
     *                             }
     *                         ]
     *                     },
     *                     "pcr_18": {
     *                         "value": "983ec7db975ed31e2c85ef8e375c038d6d307efb",
     *                         "event": [
     *                             {
     *                                 "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                                 "value": "9069ca78e7450a285173431b3e52c5c25299e473",
     *                                 "label": "LCP_CONTROL_HASH",
     *                                 "info": {
     *                                     "ComponentName": "LCP_CONTROL_HASH",
     *                                     "EventName": "OpenSource.EventName"
     *                                 }
     *                             }
     *                         ]
     *                     }
     *                 },
     *                 "SHA256": {
     *                     "pcr_17": {
     *                         "value": "6c4318b65be3eb364fe2c781c35e9c17549dd0876e90898d827791742bf7216f",
     *                         "event": [
     *                             {
     *                                 "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                                 "value": "df3f619804a92fdb4057192dc43dd748ea778adc52bc498ce80524c014b81119",
     *                                 "label": "LCP_CONTROL_HASH",
     *                                 "info": {
     *                                     "ComponentName": "LCP_CONTROL_HASH",
     *                                     "EventName": "OpenSource.EventName"
     *                                 }
     *                             },
     *                             {
     *                                 "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                                 "value": "3286fcfafb7f71c410485b80c1efd554abf273f32b69d6859b2bd6e24f6b86d5",
     *                                 "label": "initrd",
     *                                 "info": {
     *                                     "ComponentName": "initrd",
     *                                     "EventName": "OpenSource.EventName"
     *                                 }
     *                             }
     *                         ]
     *                     },
     *                     "pcr_18": {
     *                         "value": "c1f7bfdae5f270d9f13aa9620b8977951d6b759f1131fe9f9289317f3a56efa1",
     *                         "event": [
     *                             {
     *                                 "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                                 "value": "df3f619804a92fdb4057192dc43dd748ea778adc52bc498ce80524c014b81119",
     *                                 "label": "LCP_CONTROL_HASH",
     *                                 "info": {
     *                                     "ComponentName": "LCP_CONTROL_HASH",
     *                                     "EventName": "OpenSource.EventName"
     *                                 }
     *                             }
     *                         ]
     *                     }
     *                 }
     *             }
     *         },
     *         {
     *             "meta": {
     *                 "id": "b37580dd-f300-4229-8358-2640936c3841",
     *                 "vendor": "INTEL",
     *                 "description": {
     *                     "flavor_part": "OS",
     *                     "source": "source1",
     *                     "os_name": "RedHatEnterpriseServer",
     *                     "os_version": "7.4",
     *                     "vmm_name": "",
     *                     "vmm_version": "",
     *                     "tpm_version": "2.0"
     *                 }
     *             },
     *             "pcrs": {
     *                 "SHA1": {
     *                     "pcr_17": {
     *                         "value": "af755642ff864e9de0b168a63578950e0f0fc9d5",
     *                         "event": [
     *                             {
     *                                 "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha1",
     *                                 "value": "5e5d18dc535a3a4978b2c67acb9c1f1f858f062b",
     *                                 "label": "vmlinuz",
     *                                 "info": {
     *                                     "ComponentName": "vmlinuz",
     *                                     "EventName": "OpenSource.EventName"
     *                                 }
     *                             }
     *                         ]
     *                     }
     *                 },
     *                 "SHA256": {
     *                     "pcr_17": {
     *                         "value": "6c4318b65be3eb364fe2c781c35e9c17549dd0876e90898d827791742bf7216f",
     *                         "event": [
     *                             {
     *                                 "digest_type": "com.intel.mtwilson.lib.common.model.MeasurementSha256",
     *                                 "value": "7904fb1f3a8d2fae705a8820292ffa92c31dc340a45e7644f2620956b3a65026",
     *                                 "label": "vmlinuz",
     *                                 "info": {
     *                                     "ComponentName": "vmlinuz",
     *                                     "EventName": "OpenSource.EventName"
     *                                 }
     *                             }
     *                         ]
     *                     }
     *                 }
     *             }
     *         }
     *     ]
     * }
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the flavor filter criteria model and set a key and value
     * FlavorFilterCriteria filterCriteria = new FlavorFilterCriteria();
     * filterCriteria.key = "os_name";
     * filterCriteria.value = "RedHatEnterpriseServer";
     * 
     * // Create the client and call the search API
     * Flavors client = new Flavors(properties);
     * FlavorCollection obj = client.search(filterCriteria);
     * </pre></div>
     */
    public FlavorCollection search(FlavorFilterCriteria filterCriteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        FlavorCollection newObj = getTargetPathWithQueryParams("flavors", filterCriteria).request(MediaType.APPLICATION_JSON).get(FlavorCollection.class);
        return newObj;        
    }
         
    /**
     * Deletes a flavor.
     * <pre>
     * All host associations with the specified flavor will be deleted. These 
     * associations are used for caching the trust status for performance reasons.
     * 
     * The flavor group associations with the specified flavor will be deleted. 
     * All hosts in affected flavor groups will be added to the backend queue, 
     * flavor verification process.
     * </pre>
     * @param locator The content model of the FlavorLocator java model object can be used as path parameter.
     * <pre>
     *          id (required)         Flavor ID specified as a path parameter.
     * </pre>
     * @since ISecL 1.0
     * @mtwRequiresPermissions flavors:delete
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * https://server.com:8443/mtwilson/v2/flavors/21f7d831-85b3-46bc-a499-c2d14ff136c8
     * output: 204 No content
     * </pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 1024px"><pre>
     * // Create the flavor locator model and set the locator id
     * FlavorLocator locator = new FlavorLocator();
     * locator.pathId = UUID.valueOf("21f7d831-85b3-46bc-a499-c2d14ff136c8");
     * 
     * // Create the client and call the delete API
     * Flavors client = new Flavors(properties);
     * client.delete(locator);
     * </pre></div>
     */
    public void delete(FlavorLocator locator) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", locator.pathId.toString());
        Response obj = getTarget().path("flavors/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }
}
