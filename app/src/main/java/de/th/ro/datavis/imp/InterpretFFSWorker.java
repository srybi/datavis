package de.th.ro.datavis.imp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.interpreter.ffs.FFSInterpreter;
import de.th.ro.datavis.interpreter.ffs.FFSService;
import de.th.ro.datavis.models.AntennaField;
import de.th.ro.datavis.models.AtomicField;
import de.th.ro.datavis.util.exceptions.FFSInterpretException;

public class InterpretFFSWorker extends Worker {


   private final String TAG = "ImportWorker";

   int antennaId;
   Uri uri;
   String fileName;
   FFSService ffsService;

   public InterpretFFSWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
      super(context, workerParams);
   }

   @NonNull
   @Override
   public Result doWork() {

      handleInputData();
      ffsService = new FFSService(new FFSInterpreter(), getApplicationContext());
      AppDatabase appDb = AppDatabase.getInstance(getApplicationContext());

      AntennaField antennaField = new AntennaField(uri, fileName, antennaId);
      Pair<ArrayList<AtomicField>, ArrayList<AtomicField>> atomicFields = interpretFFS(appDb, uri);

      return Result.success();
   }



   private void handleInputData(){

      String inpURI = getInputData().getString("URI");
      uri = Uri.parse(inpURI);
      fileName = getInputData().getString("FILENAME");
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
      antennaId = preferences.getInt("ID", 1);
   }

   public Pair<ArrayList<AtomicField>, ArrayList<AtomicField>> interpretFFS(AppDatabase appDb, Uri uri){

      Log.d(TAG, "interpretFFS");
      InputStream in = null;
      try {
         in = getApplicationContext().getContentResolver().openInputStream(uri);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      }
      Log.d(TAG, "got InputStream");

      try {
         return ffsService.interpretData(in,0.4, antennaId, fileName);
      } catch (FFSInterpretException e) {
         e.printStackTrace();
         Log.d(TAG, "FFSInterpretException " + e.getMessage());
         return null;
      }
   }
}
