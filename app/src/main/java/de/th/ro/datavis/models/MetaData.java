package de.th.ro.datavis.models;


import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(primaryKeys = {"antennaID", "freq", "tilt", "type"},
        foreignKeys = {
        @ForeignKey(entity = Antenna.class , parentColumns = "id", childColumns="antennaID", onDelete = CASCADE)
})
public class MetaData {

    public int antennaID;
    @NonNull
    public String type;

    public double freq;
    public double tilt;
    public String value;

    //ID, Tilt, Freq, Value
    public MetaData(double freq, double tilt, String value) {
        this.freq = freq;
        this.tilt = tilt;
        this.value = value;
    }

    //dummy constructor for json converting
    public MetaData(){}

    @Ignore
    public MetaData(int antennaID) {
        this.antennaID = antennaID;
    }

    @Override
    public String toString() {
        return "Metadata "+this.type+ " Freq: "+this.freq+" Tilt: "+this.tilt+" = "+this.value;
    }


    public int getAntennaID() {
        return antennaID;
    }

    public void setAntennaID(int antennaID) {
        this.antennaID = antennaID;
    }

    public double getFreq() {
        return freq;
    }

    public void setFreq(double freq) {
        this.freq = freq;
    }

    public double getTilt() {
        return tilt;
    }

    public void setTilt(int tilt) {
        this.tilt = tilt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
