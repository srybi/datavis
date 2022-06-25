package de.th.ro.datavis.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import de.th.ro.datavis.models.AntennaField;

@Dao
public interface AntennaFieldDao {
    @Query("SELECT * FROM antenna_field")
    LiveData<List<AntennaField>> getAll();

    @Query("SELECT * FROM antenna_field")
    List<AntennaField> getAll_Background();

    @Query("SELECT * FROM antenna_field where antennaId = :antennaID")
    List<AntennaField> findByAntennaId_BackGround(int antennaID);

    @Query("SELECT * FROM antenna_field where antennaId = :antennaID")
    LiveData<List<AntennaField>> findByAntennaId_Main(int antennaID);

    @Insert
    void insert(AntennaField antennaField);

    @Update
    void update(AntennaField antennaField);

    @Delete
    void delete(AntennaField antennaField);
}
