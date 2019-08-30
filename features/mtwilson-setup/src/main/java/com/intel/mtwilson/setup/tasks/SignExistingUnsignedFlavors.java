/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.setup.tasks;

import com.intel.mtwilson.core.flavor.common.PlatformFlavorUtil;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.core.flavor.model.SignedFlavor;
import com.intel.mtwilson.flavor.data.MwFlavor;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorRepository;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.setup.ConfigurationException;
import com.intel.mtwilson.setup.LocalSetupTask;
import com.intel.mtwilson.util.crypto.keystore.PrivateKeyStore;

import java.io.*;
import java.security.*;
import java.util.List;

public class SignExistingUnsignedFlavors extends LocalSetupTask {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SignExistingUnsignedFlavors.class);
    private static final String FLAVOR_SIGNER_KEYSTORE_PASSWORD = "flavor.signer.keystore.password";
    private static final String FLAVOR_SIGNER_KEYSTORE_FILE = "flavor.signer.keystore.file";
    private static final String FLAVOR_SIGNING_KEY_ALIAS = "flavor.signing.key.alias";

    @Override
    protected void configure() throws Exception {
        String keystoreFile = getConfiguration().get(FLAVOR_SIGNER_KEYSTORE_FILE);
        if (keystoreFile == null || keystoreFile.isEmpty())
        {
            log.error("Flavor signing keystore file is not configured");
            throw new ConfigurationException("Flavor signing keystore file is not configured");
        }
        else if (!new File(keystoreFile).exists()) {
                log.debug("Flavor Signing keystore file is missing");
                configuration("Flavor Signing keystore file is missing");
        }

        FileInputStream keystoreFIS = new FileInputStream(keystoreFile);
        try {
            String flavorSigningKeystorePassword = getConfiguration().get(FLAVOR_SIGNER_KEYSTORE_PASSWORD);
            if (flavorSigningKeystorePassword == null || flavorSigningKeystorePassword.isEmpty()) {
                log.error("Flavor signing keystore password is not configured");
                throw new ConfigurationException("Flavor signing keystore password is not configured");
            }

            String keyAlias = getConfiguration().get(FLAVOR_SIGNING_KEY_ALIAS,"flavor-signing-key");
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(keystoreFIS, flavorSigningKeystorePassword.toCharArray());
            if (!keystore.containsAlias(keyAlias)) {
                log.debug("Flavor Signing key is not present in keystore");
                configuration("Flavor Signing key is not present in keystore");
            }
        } catch (Exception ex) {
            log.debug("Cannot load flavor signing keystore", ex);
            configuration(ex, "Cannot load flavor signing keystore");
        } finally {
            keystoreFIS.close();
        }
    }

    @Override
    protected void validate() {
        List<MwFlavor> mwFlavorList = new FlavorRepository().retrieveUnsignedMwFlavorList();
        int numberOfUnsignedFlavors = mwFlavorList.size();
        if ( numberOfUnsignedFlavors > 0) {
            log.info("Number of unsigned flavors: {}",numberOfUnsignedFlavors);
            validation("Number of unsigned flavors: {}",numberOfUnsignedFlavors);
        }
    }

    @Override
    protected void execute() throws Exception {
        FlavorRepository flavorRepository = new FlavorRepository();
        List<MwFlavor> mwFlavorList = flavorRepository.retrieveUnsignedMwFlavorList();
        PrivateKeyStore privateKeyStore = new PrivateKeyStore("PKCS12", new File(MSConfig.getConfiguration().getString(FLAVOR_SIGNER_KEYSTORE_FILE)), MSConfig.getConfiguration().getString(FLAVOR_SIGNER_KEYSTORE_PASSWORD).toCharArray());
        PrivateKey privateKey = privateKeyStore.getPrivateKey(MSConfig.getConfiguration().getString(FLAVOR_SIGNING_KEY_ALIAS, "flavor-signing-key"));
        for (MwFlavor mwFlavor : mwFlavorList) {
            SignedFlavor signedFlavor = PlatformFlavorUtil.getSignedFlavor(Flavor.serialize(mwFlavor.getContent()), privateKey);
            mwFlavor.setSignature(signedFlavor.getSignature());
            flavorRepository.storeEntity(mwFlavor);
        }
    }
}
