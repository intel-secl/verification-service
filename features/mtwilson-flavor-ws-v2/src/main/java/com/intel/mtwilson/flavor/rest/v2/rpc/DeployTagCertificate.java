/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.rest.v2.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.crypto.Sha384Digest;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.core.flavor.model.SignedFlavor;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import java.io.IOException;
import java.util.Date;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.intel.mtwilson.My;
import com.intel.mtwilson.core.asset.tag.ProvisionAssetTag;
import com.intel.mtwilson.core.flavor.PlatformFlavor;
import com.intel.mtwilson.core.flavor.PlatformFlavorFactory;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.flavor.data.MwHostCredential;
import com.intel.mtwilson.flavor.rest.v2.model.Host;
import com.intel.mtwilson.flavor.rest.v2.repository.HostRepository;
import com.intel.mtwilson.flavor.rest.v2.model.HostLocator;
import com.intel.mtwilson.flavor.rest.v2.resource.FlavorResource;
import com.intel.mtwilson.flavor.rest.v2.resource.HostResource;
import com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider;
import com.intel.mtwilson.core.common.datatypes.ConnectionString;
import com.intel.mtwilson.core.common.tag.model.TagCertificate;
import com.intel.mtwilson.tag.model.TagCertificateLocator;
import com.intel.mtwilson.core.common.tag.model.X509AttributeCertificate;
import com.intel.mtwilson.tag.rest.v2.repository.TagCertificateRepository;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyFactoryUtil;
import java.util.*;

import static com.intel.mtwilson.core.flavor.common.FlavorPart.ASSET_TAG;


/**
 * The "deploy" link next to each certificate in the UI calls this RPC
 * 
 * @author ssbangal
 */
@RPC("deploy-tag-certificate")
@JacksonXmlRootElement(localName="deploy_tag_certificate")
public class DeployTagCertificate implements Runnable{
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DeployTagCertificate.class);

       
    private UUID certificateId;
    private String host;
    
    public UUID getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(UUID certificateId) {
        this.certificateId = certificateId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
    
    
    @Override
    @RequiresPermissions("tag_certificates:deploy")         
    public void run() {
        log.error("RPC: DeployTagCertificate - Got request to deploy certificate with ID {}.", certificateId);        
        try {
            TagCertificateLocator locator = new TagCertificateLocator();
            locator.id = certificateId;
            
            TagCertificate obj = new TagCertificateRepository().retrieve(locator);
            if (obj != null) 
            {
                // verify the certificate validity first
                Date today = new Date();
                log.debug("RPC: DeployTagCertificate - Certificate not before {}", obj.getNotBefore());
                log.debug("RPC: DeployTagCertificate - Certificate not after {}", obj.getNotAfter());
                log.debug("RPC: DeployTagCertificate - Current date {}", today);
                if (today.before(obj.getNotBefore()) || today.after(obj.getNotAfter())) {
                    log.error("RPC: DeployTagCertificate - Certificate with subject {} is expired/invalid. Will not be deployed.", obj.getSubject());
                    throw new RepositoryInvalidInputException(locator);
                }
                
                //Search host by hardware uuid and get connection string and tls policy id
                HostLocator hostLocator = new HostLocator();
                hostLocator.hardwareUuid = obj.getHardwareUuid();
                Host hostObj = new HostRepository().retrieve(hostLocator);
                
                if (hostObj == null) {  
                    log.error("The host with specified hardware UUID was not found {}", host);
                    throw new RepositoryInvalidInputException(locator);
                }

                Sha384Digest tagSha384 = Sha384Digest.digestOf(obj.getCertificate());
                String tlsPolicyId = hostObj.getTlsPolicyId();
                //TlsPolicyFactory tlsPolicyFactory = TlsPolicyFactory.createFactory(hostInfo);
                //TODO: replace tls policy with actual policy
                MwHostCredential  credential = My.jpa().mwHostCredential().findByHostId(hostObj.getId().toString());
                ConnectionString connectionString =  new ConnectionString(String.format("%s;%s",hostObj.getConnectionString(), credential.getCredential()));
                TlsPolicyDescriptor tlsPolicyDescriptor = new HostResource().getTlsPolicy(tlsPolicyId, connectionString, true);
                TlsPolicy tlsPolicy = TlsPolicyFactoryUtil.createTlsPolicy(tlsPolicyDescriptor);
                //call asset tag provisioner to deploy asset tag to host (it will call host connector to deploy it)
                deployAssetTagToHost(tagSha384, hostObj, tlsPolicy);
                
                X509AttributeCertificate attrcert = X509AttributeCertificate.valueOf(obj.getCertificate());
                
                //call flavor library to get asset tag flavor.
                PlatformFlavorFactory flavorFactory = new PlatformFlavorFactory();
                PlatformFlavor platformFlavor = flavorFactory.getPlatformFlavor(connectionString.getVendor().toString(), attrcert);
                ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
                if (!platformFlavor.getFlavorPartWithSignature(ASSET_TAG.getValue()).get(0).getFlavor().toString().isEmpty()) {
                    SignedFlavor flavorAndSignature = platformFlavor.getFlavorPartWithSignature(ASSET_TAG.getValue()).get(0);
                    // Add Flavor to the Flavorgroup
                    Map<String, List<SignedFlavor>> flavorPartFlavorMap = new HashMap<>();
                    List<SignedFlavor> flavors = new ArrayList();
                    flavors.add(flavorAndSignature);
                    flavorPartFlavorMap.put(ASSET_TAG.getValue(), flavors);
                    new FlavorResource().addFlavorToFlavorgroup(flavorPartFlavorMap, null);                    
                } else {
                    log.error("RPC: DeployTagCertificate - Failed to get platform flavor from asset tag certificate");
                    throw new RepositoryInvalidInputException(locator);
                }
            } else {
                log.error("RPC: DeployTagCertificate - Failed to retreive certificate while trying to discover host by certificate ID.");
                throw new RepositoryInvalidInputException(locator);
            }

        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("RPC: DeployTagCertificate - Error during certificate deployment.", ex);
            throw new RepositoryException(ex);
        } 
        
    }

    private void deployAssetTagToHost(Sha384Digest tagSha384, Host host, TlsPolicy tlsPolicy) throws IOException, Exception {
        String certSha384 = tagSha384.toHexString();
        try {
            //Assettag provisioner core library method call
            ProvisionAssetTag provisionTag = new ProvisionAssetTag();
            MwHostCredential  credential = My.jpa().mwHostCredential().findByHostId(host.getId().toString());
            provisionTag.provisionTagCertificate(String.format("%s;%s",host.getConnectionString(), credential.getCredential()),
                                                 certSha384,
                                                 tlsPolicy);

        } catch (IOException ex) {
            log.error("Unable to deploy asset tag to host.");
            throw new IOException("Unable to deploy asset tag to host.", ex);
        } catch (Exception ex) {
            log.error("Invalid Host record.");
            throw new Exception("Invalid Host record.", ex);
        }
    }
}
