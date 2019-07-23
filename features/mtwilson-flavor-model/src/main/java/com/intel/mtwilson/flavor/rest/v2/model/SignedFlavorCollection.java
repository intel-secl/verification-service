package com.intel.mtwilson.flavor.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.core.flavor.model.SignedFlavor;

import java.util.ArrayList;
import java.util.List;

public class SignedFlavorCollection {
    private ArrayList<SignedFlavor> signedFlavors = new ArrayList<>();
    private ArrayList<Flavor> flavors = new ArrayList<>();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JacksonXmlElementWrapper(localName="signedFlavors")
    @JacksonXmlProperty(localName="signedFlavors")
    public List<SignedFlavor> getFlavorsWithSignature() { return signedFlavors; }

    public void setFlavorsWithSignature(ArrayList<SignedFlavor> signedFlavors){
        this.signedFlavors = signedFlavors;
    }

    public ArrayList<Flavor> getFlavors() {
        for (SignedFlavor signedFlavor : signedFlavors) {
            Flavor flavor = signedFlavor.getFlavor();
            flavors.add(flavor);
        }
        return flavors;
    }

    public void setFlavors(ArrayList<Flavor> flavors){
        for (Flavor flavor: flavors) {
            SignedFlavor signedFlavor = new SignedFlavor(flavor, null);
            this.signedFlavors.add(signedFlavor);
        }
    }

}
