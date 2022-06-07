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
    public String description;


    public Antenna(int id, String uri, String filename, String description) {
        this.id = id;
        this.uri = uri;
        this.filename = filename;
        this.description = description;
    }

    @Ignore
    public Antenna(Uri uri, String filename) {
        this.uri = uri.toString();
        this.filename = filename;
    }

    @Ignore
    public Antenna(String description){
        this.description = description;
    }

    public void setAntennaFile(String uri, String filename){
        this.uri = uri;
        this.filename = filename;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("id: %2d; description: %s; uri: %s; filename: %s", id, description, uri, filename);
    }
}
