package com.intel.mtwilson.flavor.utils;

import com.intel.mtwilson.core.common.model.HostManifest;
import com.intel.mtwilson.core.common.utils.MeasurementUtils;
import com.intel.wml.measurement.xml.Measurement;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ddhawale
 */
public class HostMeasurementUtils {
    public static List<String> getMeasurementLabels(HostManifest hostManifest) {
        List<String> measurementLabels = new ArrayList<>();
        for(String measurementXml : hostManifest.getMeasurementXmls()) {
            Measurement measurement;
            try {
                measurement = MeasurementUtils.parseMeasurementXML(measurementXml);
                measurementLabels.add(measurement.getLabel());
            } catch (IOException | JAXBException | XMLStreamException e) {
                e.printStackTrace();
            }
        }
        return measurementLabels;
    }
}
