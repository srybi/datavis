package de.th.ro.datavis.models;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

import de.th.ro.datavis.util.enums.InterpretationMode;

@Entity(tableName = "atomic_field", primaryKeys = {"antennaId","" + "tilt", "frequency", "interpretationMode"}, foreignKeys ={ @ForeignKey(entity = AntennaField.class,
        parentColumns = "id",
        childColumns = "antennaFieldId",
        onDelete = CASCADE), @ForeignKey(entity = Antenna.class,
                parentColumns = "id",
                childColumns = "antennaId",
                onDelete = CASCADE)})
public class AtomicField {
    public int getTilt() {
        return tilt;
    }

    public void setTilt(int tilt) {
        this.tilt = tilt;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    @NonNull
    public InterpretationMode getInterpretationMode() {
        return interpretationMode;
    }

    public void setInterpretationMode(@NonNull InterpretationMode interpretationMode) {
        this.interpretationMode = interpretationMode;
    }

    public List<Sphere> getSpheres() {
        return spheres;
    }

    public void setSpheres(List<Sphere> spheres) {
        this.spheres = spheres;
    }

    public int tilt;
    public double frequency;
    @NonNull
    public InterpretationMode interpretationMode;
    public List<Sphere> spheres;

    public int antennaId;
    public int antennaFieldId;

    public AtomicField(int tilt, double frequency, InterpretationMode interpretationMode, List<Sphere> spheres, int antennaId, int antennaFieldId) {
        this.tilt = tilt;
        this.frequency = frequency;
        this.interpretationMode = interpretationMode;
        this.spheres = spheres;
        this.antennaId = antennaId;
        this.antennaFieldId = antennaFieldId;
    }


}
