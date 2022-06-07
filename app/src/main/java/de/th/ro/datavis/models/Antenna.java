package de.th.ro.datavis.models;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "antenna")
public class Antenna {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String uri;
    public String filename;
    public String name;


    public Antenna(int id, String uri, String filename, String name) {
        this.id = id;
        this.uri = uri;
        this.filename = filename;
        this.name = name;
    }

    @Ignore
    public Antenna(Uri uri, String filename) {
        this.uri = uri.toString();
        this.filename = filename;
    }

    @Ignore
    public Antenna(String name){
        this.name = name;
    }

    public void setAntennaFile(String uri, String filename){
        this.uri = uri;
        this.filename = filename;
    }

    @NonNull
    @Override
    public String toString() {
        return "[" + id + "] "
                + "[" + filename + "] ";
    }
}
