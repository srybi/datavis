package de.th.ro.datavis.db.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import de.th.ro.datavis.db.daos.AntennaFieldDao;
import de.th.ro.datavis.models.AntennaField;

@Database(entities = {AntennaField.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract AntennaFieldDao antennaFieldDao();
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