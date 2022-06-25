package de.th.ro.datavis.imp.worker;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import de.th.ro.datavis.database.AppDatabase;
import de.th.ro.datavis.interpreter.ffs.FFSInterpreter;
import de.th.ro.datavis.interpreter.ffs.FFSService;
import de.th.ro.datavis.models.AntennaField;
import de.th.ro.datavis.util.exceptions.FFSInterpretException;

public class InterpretFFSWorker extends Worker {


   private final String TAG = "ImportWorker";
   AppDatabase appDb;
   String[] inputURIs;
   String[] inputFilenames;
   FFSService ffsService;

   public InterpretFFSWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
      super(context, workerParams);
   }

   @NonNull
   @Override
   public Result doWork() {

      handleInputData();
      appDb  = AppDatabase.getInstance(getApplicationContext());
      ffsService = new FFSService(new FFSInterpreter(), getApplicationContext());

      storeAtomicFields();

      return Result.success();
   }

   private void storeAtomicFields(){
      for(int i = 0; i < inputURIs.length; i++){
         Uri uri = Uri.parse(inputURIs[i]);
         String filename = inputFilenames[i];
         persistFFS(appDb, uri, filename);
      }
   }


   private void handleInputData(){
      inputURIs = getInputData().getStringArray("URIFFS");
      inputFilenames = getInputData().getStringArray("FILENAMEFFS");
   }

   public void persistFFS(AppDatabase appDb, Uri uri, String fileName) {

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
      } finally {
         if (in != null)
            try {
               in.close();
            } catch (IOException ioex) {
               Log.e(TAG, "Input Stream failed to close: " + ioex.getMessage());
            }
      }
      Log.d(TAG, "interpret Data done");
      //Save Antenna and file to database
      AntennaField antennaField = new AntennaField(uri, fileName, antennaId);
      appDb.antennaFieldDao().insert(antennaField);


      Log.d(TAG, "persistFFS done");
   }

}
