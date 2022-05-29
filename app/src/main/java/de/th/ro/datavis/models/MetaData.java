package de.th.ro.datavis.models;


import static androidx.room.ForeignKey.CASCADE;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import de.th.ro.datavis.util.enums.MetadataType;

@Entity(foreignKeys = {
        @ForeignKey(entity = Antenna.class , parentColumns = "id", childColumns="antennaID", onDelete = CASCADE)
})
public class MetaData {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int antennaID;
    public String type;

    public double freq;
    public int tilt;
    public String value;


    // todo Fields

    //ID, Tilt, Freq, Value, Type

    public MetaData(double freq, int tilt, String value) {
        this.freq = freq;
        this.tilt = tilt;
        this.value = value;
    }

    @Ignore
    public MetaData(int antennaID) {
        this.antennaID = antennaID;
    }

    @Override
    public String toString() {
        return "Metadata "+this.type+"("+id+")" + " Freq: "+this.freq+" Tilt: "+this.tilt+" = "+this.value;
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

    public double getFreq() {
        return freq;
    }

    public void setFreq(double freq) {
        this.freq = freq;
    }

    public int getTilt() {
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
