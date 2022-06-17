package de.th.ro.datavis.imp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.nfc.Tag;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.interpreter.csv.MetadataInterpreter;
import de.th.ro.datavis.interpreter.ffs.FFSInterpreter;
import de.th.ro.datavis.interpreter.ffs.FFSService;
import de.th.ro.datavis.models.AntennaField;
import de.th.ro.datavis.models.MetaData;
import de.th.ro.datavis.util.exceptions.FFSInterpretException;
import de.th.ro.datavis.util.filehandling.FileHandler;

public class ImportFolderWorker extends Worker {


   private final String TAG = "ImportFolderWorker";

   FFSService ffsService;
   String[] csvList;
   String[] ffsList;
   MetadataInterpreter metaInt;

   public ImportFolderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
      super(context, workerParams);
   }

   @NonNull
   @Override
   public Result doWork() {

      handleInputData();
      ffsService = new FFSService(new FFSInterpreter(), getApplicationContext());
      AppDatabase appDb = AppDatabase.getInstance(getApplicationContext());

      metaInt = new MetadataInterpreter();
      persistFFSFolder(appDb, ffsList);
      persistMetadataFolder(appDb, csvList);



      return Result.success();
   }



   private void handleInputData(){
      csvList = getInputData().getStringArray("URICSV");
      ffsList = getInputData().getStringArray("URIFFS");

      //uri = Uri.parse(inpURI);
   }

   public void persistFFSFolder(AppDatabase appDb, String[] ffsList){
      Log.d(TAG, "FFS list length: "+ffsList.length);
      for (String s: ffsList) {
         try {
            Uri uri = Uri.parse(s);
            String filename = FileHandler.queryName(getApplicationContext().getContentResolver(),uri);
            persistFFS(appDb, uri, filename);
         } catch (Exception e){
            e.printStackTrace();
            Log.d(TAG, "Could not parse Uri: "+s);
         }
      }
   }

   public void persistFFS(AppDatabase appDb, Uri uri, String fileName){

      Log.d(TAG, "persistFFS");

      // Background
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
      int antennaId = preferences.getInt("ID", 1);



      InputStream in = null;
      try {
         in = getApplicationContext().getContentResolver().openInputStream(uri);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      }
      Log.d(TAG, "got InputStream");

      try {
         ffsService.interpretData(in,0.4, antennaId, fileName);
      } catch (FFSInterpretException e) {
         e.printStackTrace();
         Log.d(TAG, "FFSInterpretException " + e.getMessage());
         return;
      }
      Log.d(TAG, "interprete Data done");
      //Save Antenna and file to database
      AntennaField antennaField = new AntennaField(uri, fileName, antennaId);
      appDb.antennaFieldDao().insert(antennaField);


      Log.d(TAG, "persistFFS done");
   }

   public void persistMetadata(AppDatabase appDb, Uri uri){
      // Background
      List<MetaData> list = metaInt.getCSVMetadata(uri, getApplicationContext().getContentResolver());
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
      int antennaId = preferences.getInt("ID", 1);

      for(MetaData e : list){
         e.setAntennaID(antennaId);
         appDb.metadataDao().insert(e);
      }
   }

   /**
    * Calls MetadataInterpreter to iterate through files in a folder
    * Calls persistMetadata
    */
   public void persistMetadataFolder(AppDatabase appDb, String[] uriList) {
      Log.d(TAG, "CSV list length: "+uriList.length);
      for(String s: uriList){
         Log.d(TAG, "persisting Uri: " +s);
         try{
            Uri u = Uri.parse(s);
            persistMetadata(appDb, u);
         } catch (SQLiteConstraintException sq){
            //If single Metadata was added manually before, RoomDB won't let it added
            Log.d(TAG, "Metadata already inserted - PK Error: " + sq.getMessage());
         }catch (Exception e){
            Log.d(TAG, "Could not parse Uri" + s);
         }
      }

   }



}
