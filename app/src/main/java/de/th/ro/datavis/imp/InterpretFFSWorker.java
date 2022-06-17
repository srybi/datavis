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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.interpreter.ffs.FFSInterpreter;
import de.th.ro.datavis.interpreter.ffs.FFSService;
import de.th.ro.datavis.models.AntennaField;
import de.th.ro.datavis.models.AtomicField;
import de.th.ro.datavis.models.MetaData;
import de.th.ro.datavis.util.exceptions.FFSInterpretException;
import de.th.ro.datavis.util.filehandling.FileHandler;

public class InterpretFFSWorker extends Worker {


   private final String TAG = "ImportWorker";

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
      ffsService = new FFSService(new FFSInterpreter(), getApplicationContext());

      for(int i = 0; i < inputURIs.length; i++){
         Uri uri = Uri.parse(inputURIs[i]);
         String filename = inputFilenames[i];
         Pair<ArrayList<AtomicField>, ArrayList<AtomicField>> atomicFields = interpretFFS(uri, filename);
      }



      return Result.success();
   }

   private String[] prepareOutput(List<MetaData> result){
      String[] dataArray = new String[result.size()];
      return dataArray;
   }



   private void handleInputData(){
      inputURIs = getInputData().getStringArray("URIFFS");
      inputFilenames = getInputData().getStringArray("FILENAMEFFS");
   }

   public Pair<ArrayList<AtomicField>, ArrayList<AtomicField>> interpretFFS(Uri uri, String fileName){

      Log.d(TAG, "interpretFFS");
      InputStream in = null;
      try {
         in = getApplicationContext().getContentResolver().openInputStream(uri);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      }
      Log.d(TAG, "got InputStream");

      try {
         return ffsService.interpretData(in,0.4, fileName);
      } catch (FFSInterpretException e) {
         e.printStackTrace();
         Log.d(TAG, "FFSInterpretException " + e.getMessage());
         return null;
      }
   }
}
