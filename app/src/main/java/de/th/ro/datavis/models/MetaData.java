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
    public String filename;
    public String uri;
    public String type;

    public String freq;
    public String tilt;
    public String value;


    // todo Fields

    //ID, Tilt, Freq, Value, Type

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

}
