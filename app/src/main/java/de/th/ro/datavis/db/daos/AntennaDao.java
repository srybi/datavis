package de.th.ro.datavis.db.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import de.th.ro.datavis.models.Antenna;

@Dao
public interface AntennaDao {
    @Query("SELECT * FROM antenna")
    LiveData<List<Antenna>> getAll();

    @Query("SELECT * FROM antenna")
    List<Antenna> getAll_Background();

    @Query("SELECT * FROM antenna where id = :ID")
    LiveData<List<Antenna>> find_Main(int ID);

    @Query("SELECT * FROM antenna where id = :ID")
    List<Antenna> find_Background(int ID);

    @Insert
    void insert(Antenna antenna);

    @Update
    void update(Antenna antenna);

    @Delete
    void delete(Antenna antenna);
}
