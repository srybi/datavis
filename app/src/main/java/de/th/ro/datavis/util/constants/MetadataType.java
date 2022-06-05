package de.th.ro.datavis.util.constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Metadata we want to import (by filename) - extendable
 * Checked by MetadataInterpreter in isMetaDataImportable()
 */
public class MetadataType {

    public static List<String> MetaDataTypeList=new ArrayList<String>(){{
        //Important
        add("HHPBW_deg");
        add("VHPBW_deg");
        add("Directivity_dBi");
        //Optional
        add("Nullfill_dB");
        add("Squint_deg");
        add("Tilt_deg");
        add("TiltDeviation_deg");

    }};

}
