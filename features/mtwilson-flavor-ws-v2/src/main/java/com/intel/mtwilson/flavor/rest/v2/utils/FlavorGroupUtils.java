/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.flavor.rest.v2.utils;

import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorCollection;
import com.intel.mtwilson.flavor.rest.v2.model.Flavorgroup;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupLocator;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorgroupRepository;

import java.util.ArrayList;

public class FlavorGroupUtils {

    public static Flavorgroup getFlavorGroupByName(String flavorgroupName) {
        // look for flavorgroup
        FlavorgroupLocator flavorgroupLocator = new FlavorgroupLocator();
        flavorgroupLocator.name = flavorgroupName;
        return new FlavorgroupRepository().retrieve(flavorgroupLocator);
    }

    public static Flavorgroup createFlavorGroupByName(String flavorgroupName) {
        Flavorgroup newFlavorgroup = new Flavorgroup();
        newFlavorgroup.setName(flavorgroupName);
        newFlavorgroup.setFlavorMatchPolicyCollection(Flavorgroup.getAutomaticFlavorMatchPolicy());
        return new FlavorgroupRepository().create(newFlavorgroup);
    }

    //create proper format for XML response for flavor collection returned
    //In XML response, path name is used as XML tag which cannot have '/' which has been replaced with '-'
    public static FlavorCollection updatePathSeparatorForXML(FlavorCollection flavorCollection) {
        ArrayList<Flavor> flavors = new ArrayList();
        for (Flavor flavor : flavorCollection.getFlavors()) {
            if (flavor.getMeta().getDescription().getFlavorPart().equals("SOFTWARE")) {
                flavor = FlavorUtils.updatePathSeparatorForXML(flavor);
            }
            flavors.add(flavor);
        }
        flavorCollection.setFlavors(flavors);
        return flavorCollection;
    }
}
