/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.intel.mtwilson.core.flavor.common.PlatformFlavorUtil;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.core.flavor.model.SignedFlavor;
import com.intel.mtwilson.jaxrs2.DocumentCollection;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.util.crypto.keystore.PrivateKeyStore;

import java.io.File;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

public class SignedFlavorCollection extends DocumentCollection<SignedFlavor> {
    private List<SignedFlavor> signedFlavors = new ArrayList<>();
    private static final String FLAVOR_SIGNER_KEYSTORE_FILE = "flavor.signer.keystore.file";
    private static final String FLAVOR_SIGNER_KEYSTORE_PASSWORD = "flavor.signer.keystore.password";
    private static final String FLAVOR_SIGNING_KEY_ALIAS = "flavor.signing.key.alias";

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JacksonXmlElementWrapper(localName="signedFlavors")
    @JacksonXmlProperty(localName="signedFlavors")
    public List<SignedFlavor> getSignedFlavors() { return signedFlavors; }

    public void setSignedFlavors(ArrayList<SignedFlavor> signedFlavors){
        this.signedFlavors = signedFlavors;
    }

    @JsonIgnore
    public ArrayList<Flavor> getFlavors() {
        ArrayList<Flavor> flavors = new ArrayList<>();
        for (SignedFlavor signedFlavor : signedFlavors) {
            Flavor flavor = signedFlavor.getFlavor();
            flavors.add(flavor);
        }
        return flavors;
    }

    public void setFlavors(ArrayList<Flavor> flavors) throws Exception {
        List<String> flavorList = new ArrayList<>();
        PrivateKeyStore privateKeyStore = new PrivateKeyStore("PKCS12", new File(MSConfig.getConfiguration().getString(FLAVOR_SIGNER_KEYSTORE_FILE)), MSConfig.getConfiguration().getString(FLAVOR_SIGNER_KEYSTORE_PASSWORD).toCharArray());
        PrivateKey privateKey = privateKeyStore.getPrivateKey(MSConfig.getConfiguration().getString(FLAVOR_SIGNING_KEY_ALIAS, "flavor-signing-key"));
        for (Flavor flavor: flavors) {
            String flavorString = Flavor.serialize(flavor);
            flavorList.add(flavorString);
        }
        this.signedFlavors = PlatformFlavorUtil.getSignedFlavorList(flavorList, privateKey);
    }

    @Override
    public List<SignedFlavor> getDocuments() {
        return getSignedFlavors();
    }

}
