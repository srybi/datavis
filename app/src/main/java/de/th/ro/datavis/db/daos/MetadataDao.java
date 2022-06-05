package de.th.ro.datavis.db.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import de.th.ro.datavis.models.Antenna;
import de.th.ro.datavis.models.AtomicField;
import de.th.ro.datavis.models.MetaData;

@Dao
public interface MetadataDao {
    @Query("SELECT * FROM MetaData")
    LiveData<List<MetaData>> getAll();

    @Query("SELECT * FROM MetaData")
    List<MetaData> getAll_Background();

    @Query("SELECT * FROM MetaData where id = :ID")
    MetaData find_Main(int ID);

    @Query("SELECT * FROM MetaData where id = :ID")
    List<MetaData> find_Background(int ID);

    //Identifies Metadata by AntennenID, Freq und Tilt
    @Query("SELECT * FROM MetaData WHERE antennaID = :antennaID AND freq = :freq AND tilt = :tilt AND type LIKE :type")
    MetaData findByMetadata_Main(int antennaID, double freq, double tilt, String type);

    @Query("SELECT * FROM MetaData WHERE antennaID = :antennaID AND freq = :freq AND tilt = :tilt AND type LIKE :type")
    LiveData<MetaData> findByMetadata_Background(int antennaID, double freq, double tilt, String type);

    @Insert
    void insert(MetaData metaData);

    @Update
    void update(MetaData metaData);

    @Delete
    void delete(MetaData metaData);
}
