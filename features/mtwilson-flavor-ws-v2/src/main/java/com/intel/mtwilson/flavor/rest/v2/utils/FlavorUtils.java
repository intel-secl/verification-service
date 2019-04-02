package com.intel.mtwilson.flavor.rest.v2.utils;

import com.intel.mtwilson.core.flavor.model.Flavor;
import com.intel.wml.measurement.xml.MeasurementType;

import java.util.LinkedHashMap;

public class FlavorUtils {

    //create proper format for XML response for flavor returned
    //In XML response, path name is used as XML tag which cannot have '/' which has been replaced with '-'
    public static Flavor updatePathSeparatorForXML(Flavor flavor) {
        LinkedHashMap<String, MeasurementType> measurementMap = new LinkedHashMap<>();
        for (MeasurementType mt : flavor.getSoftware().getMeasurements().values()) {
            measurementMap.put(mt.getPath().replace("/", "-").substring(1), mt);
        }
        flavor.getSoftware().setMeasurements(measurementMap);
        return flavor;
    }
}
