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


    public AntennaField(int id, String uri) {
        this.id = id;
        this.uri = uri;
    }

    @Ignore
    public AntennaField(Uri uri) {
        this.uri = uri.toString();
    }
}
