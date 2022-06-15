package de.th.ro.datavis.imp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.FileNotFoundException;
import java.io.InputStream;

import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.interpreter.ffs.FFSInterpreter;
import de.th.ro.datavis.interpreter.ffs.FFSService;
import de.th.ro.datavis.models.AntennaField;
import de.th.ro.datavis.util.exceptions.FFSInterpretException;

public class ImportWorker extends Worker {


   private final String TAG = "ImportWorker";

   Uri uri;
   String fileName;
   FFSService ffsService;

   public ImportWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
      super(context, workerParams);
   }

   @NonNull
   @Override
   public Result doWork() {

      handleInputData();
      ffsService = new FFSService(new FFSInterpreter(), getApplicationContext());
      AppDatabase appDb = AppDatabase.getInstance(getApplicationContext());

      persistFFS(appDb, uri);


      return Result.success();
   }



   private void handleInputData(){

      String inpURI = getInputData().getString("URI");
      uri = Uri.parse(inpURI);
      fileName = getInputData().getString("FILENAME");

   }

   public void persistFFS(AppDatabase appDb, Uri uri){

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



}
