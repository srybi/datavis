package de.th.ro.datavis.models;


import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import de.th.ro.datavis.util.enums.MetadataType;

@Entity
public class MetaData {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int antennaID;
    public String filename;
    public String uri;
    public String type;


    public String freq;
    public String tilt;
    public String value;

    public String HHPBW_deg, H10dBBW_deg, VHPBW_deg, Directivity_dBi = "N/A";

    public String Nullfill_dB, Squint_deg, Tilt_deg, TiltDeviation_deg = "N/A";

    // todo Fields


    public MetaData(String freq, String tilt, String value) {
        this.freq = freq;
        this.tilt = tilt;
        this.value = value;
    }

    @Ignore
    public MetaData(int antennaID) {
        this.antennaID = antennaID;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAntennaID() {
        return antennaID;
    }

    public void setAntennaID(int antennaID) {
        this.antennaID = antennaID;
    }

    public String getFreq() {
        return freq;
    }

    public void setFreq(String freq) {
        this.freq = freq;
    }

    public String getTilt() {
        return tilt;
    }

    public void setTilt(String tilt) {
        this.tilt = tilt;
    }

    public String getHHPBW_deg() {
        return HHPBW_deg;
    }

    public void setHHPBW_deg(String HHPBW_deg) {
        this.HHPBW_deg = HHPBW_deg;
    }

    public String getH10dBBW_deg() {
        return H10dBBW_deg;
    }

    public void setH10dBBW_deg(String h10dBBW_deg) {
        H10dBBW_deg = h10dBBW_deg;
    }

    public String getVHPBW_deg() {
        return VHPBW_deg;
    }

    public void setVHPBW_deg(String VHPBW_deg) {
        this.VHPBW_deg = VHPBW_deg;
    }

    public String getDirectivity_dBi() {
        return Directivity_dBi;
    }

    public void setDirectivity_dBi(String directivity_dBi) {
        Directivity_dBi = directivity_dBi;
    }

    public String getNullfill_dB() {
        return Nullfill_dB;
    }

    public void setNullfill_dB(String nullfill_dB) {
        Nullfill_dB = nullfill_dB;
    }

    public String getSquint_deg() {
        return Squint_deg;
    }

    public void setSquint_deg(String squint_deg) {
        Squint_deg = squint_deg;
    }

    public String getTilt_deg() {
        return Tilt_deg;
    }

    public void setTilt_deg(String tilt_deg) {
        Tilt_deg = tilt_deg;
    }

    public String getTiltDeviation_deg() {
        return TiltDeviation_deg;
    }

    public void setTiltDeviation_deg(String tiltDeviation_deg) {
        TiltDeviation_deg = tiltDeviation_deg;
    }

}
