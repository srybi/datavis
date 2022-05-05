package de.th.ro.datavis.db.daos;

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
    @Query("SELECT * FROM antennafield")
    LiveData<List<AntennaField>> getAll();

    @Insert
    void insert(AntennaField antennaField);

    @Update
    void update(AntennaField antennaField);

    @Delete
    void delete(AntennaField antennaField);
}
