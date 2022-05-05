package de.th.ro.datavis.models;

import android.net.Uri;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class AntennaField {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String uri;
    public String filename;


    public AntennaField(int id, String uri, String filename) {
        this.id = id;
        this.uri = uri;
        this.filename = filename;
    }

    @Ignore
    public AntennaField(Uri uri, String filename) {
        this.uri = uri.toString();
        this.filename = filename;
    }
}
