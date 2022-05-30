package de.th.ro.datavis.db.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import de.th.ro.datavis.db.Converters.Converter;
import de.th.ro.datavis.db.daos.AntennaDao;
import de.th.ro.datavis.db.daos.AntennaFieldDao;
import de.th.ro.datavis.db.daos.AtomicFieldDao;
import de.th.ro.datavis.db.daos.MetadataDao;
import de.th.ro.datavis.models.Antenna;
import de.th.ro.datavis.models.AntennaField;
import de.th.ro.datavis.models.AtomicField;
import de.th.ro.datavis.models.MetaData;

@TypeConverters({Converter.class})
@Database(entities = {AntennaField.class, Antenna.class, MetaData.class, AtomicField.class}, version = 13)
public abstract class AppDatabase extends RoomDatabase {

    public abstract AntennaFieldDao antennaFieldDao();
    public abstract AntennaDao antennaDao();
    public abstract MetadataDao metadataDao();
    public abstract AtomicFieldDao atomicFieldDao();

    private static final String DB_NAME = "antenna_fields";
    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}