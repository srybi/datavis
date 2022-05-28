package de.th.ro.datavis.db.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import de.th.ro.datavis.models.AntennaField;
import de.th.ro.datavis.models.AtomicField;
import de.th.ro.datavis.util.enums.InterpretationMode;

@Dao
public interface AtomicFieldDao {

    @Insert
    void insert(AtomicField atomicField);

    @Update
    void update(AtomicField atomicField);

    @Delete
    void delete(AtomicField atomicField);

    @Query("DELETE FROM atomic_field")
    void deleteAll();

    @Query("SELECT * from atomic_field ORDER BY tilt ASC")
    LiveData<List<AtomicField>> getAllAtomicFields();

    @Query("SELECT * from atomic_field WHERE interpretationMode = :interpretationMode AND antennaId = :antennaId AND tilt = :tilt AND frequency = :frequency")
    LiveData<AtomicField> getAtomicFields(int antennaId, int tilt, double frequency, InterpretationMode interpretationMode);


    @Query("SELECT * from atomic_field WHERE antennaId = :antennaId")
    LiveData<List<AtomicField>> getAtomicFieldsByAntennaFieldId(int antennaId);

    @Query("SELECT * from atomic_field WHERE antennaId = :antennaId")
    List<AtomicField> getAtomicFieldsByAntennaFieldIdSync(int antennaId);

    @Query("SELECT * from atomic_field ORDER BY antennaId DESC LIMIT 1")
    AtomicField getLastAtomicField();
}
