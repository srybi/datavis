package de.th.ro.datavis.models;


import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class MetaData {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int antennaID;

    // todo Fields


    public MetaData(int id, int antennaID) {
        this.id = id;
        this.antennaID = antennaID;

    }

    @Ignore
    public MetaData(int antennaID) {
        this.antennaID = antennaID;
    }


}
