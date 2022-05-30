package de.th.ro.datavis.models;

import static androidx.room.ForeignKey.CASCADE;

import android.net.Uri;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@Entity(tableName = "antenna_field", foreignKeys ={ @ForeignKey(entity = Antenna.class,
        parentColumns = "id",
        childColumns = "antennaId",
        onDelete = CASCADE)})
public class AntennaField {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String uri;
    public String filename;
    public int antennaId;


    public AntennaField(int id, String uri, String filename, int antennaId) {
        this.id = id;
        this.uri = uri;
        this.filename = filename;
        this.antennaId = antennaId;
    }

    @Ignore
    public AntennaField(Uri uri, String filename) {
        this.uri = uri.toString();
        this.filename = filename;
    }

    public AntennaField(Uri uri, String filename, int antennaId) {
        this.uri = uri.toString();
        this.filename = filename;
        this.antennaId = antennaId;
    }

    @NotNull
    @Override
    public String toString() {
        return "AntennaField{" +
                "id=" + id +
                ", filename='" + filename + '\'' +
                '}';
    }
}
