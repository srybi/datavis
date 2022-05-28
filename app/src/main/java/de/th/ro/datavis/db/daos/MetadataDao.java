package de.th.ro.datavis.db.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import de.th.ro.datavis.models.MetaData;

@Dao
public interface MetadataDao {
    @Query("SELECT * FROM metadata")
    LiveData<List<MetaData>> getAll();

    @Query("SELECT * FROM metadata")
    List<MetaData> getAll_Background();

    @Query("SELECT * FROM metadata where id = :ID")
    LiveData<List<MetaData>> find_Main(int ID);

    @Query("SELECT * FROM metadata where id = :ID")
    List<MetaData> find_Background(int ID);

    @Query("SELECT * FROM metadata where freq = :FREQ")
    LiveData<List<MetaData>> find_Main(String FREQ);

    @Query("SELECT * FROM metadata where tilt = :TILT")
    List<MetaData> find_Background(String TILT);

    //Identifies Metadata by AntennenID, Freq und Tilt
    @Query("SELECT * FROM metadata where id = :ID AND freq = :FREQ AND tilt = :TILT")
    LiveData<List<MetaData>> findByMetadata_Main(int ID, String FREQ, String TILT);

    @Insert
    void insert(MetaData metaData);

    @Update
    void update(MetaData metaData);

    @Delete
    void delete(MetaData metaData);
}
