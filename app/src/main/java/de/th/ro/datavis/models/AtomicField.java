package de.th.ro.datavis.models;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import java.util.List;

import de.th.ro.datavis.util.constants.InterpretationMode;

@Entity(tableName = "atomic_field", primaryKeys = {"antennaId","" + "tilt", "frequency", "interpretationMode"}, foreignKeys ={ @ForeignKey(entity = Antenna.class,
                parentColumns = "id",
                childColumns = "antennaId",
                onDelete = CASCADE)})
public class AtomicField {

    public double tilt;
    public double frequency;
    @NonNull
    public InterpretationMode interpretationMode;
    public List<Sphere> spheres;
    public int antennaId;
    public int antennaFieldId;
    public double maxIntensity;

    public AtomicField(int tilt, double frequency, InterpretationMode interpretationMode, List<Sphere> spheres, double maxIntensity, int antennaId, int antennaFieldId) {
        this.tilt = tilt;
        this.frequency = frequency;
        this.interpretationMode = interpretationMode;
        this.spheres = spheres;
        this.maxIntensity = maxIntensity;
        this.antennaId = antennaId;
        this.antennaFieldId = antennaFieldId;
    }

    public AtomicField(){

    }

    public double getTilt() {
        return tilt;
    }

    public void setTilt(double tilt) {
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


    public void setSpheres(List<Sphere> spheres) {
        this.spheres = spheres;
    }


    public void setAntennaId(int antennaId) {
        this.antennaId = antennaId;
    }



}
