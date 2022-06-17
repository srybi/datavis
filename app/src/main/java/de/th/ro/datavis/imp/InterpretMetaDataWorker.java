package de.th.ro.datavis.imp;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingDeque;

import de.th.ro.datavis.interpreter.csv.MetadataInterpreter;
import de.th.ro.datavis.models.MetaData;

public class InterpretMetaDataWorker extends Worker {


   private final String TAG = "ImportMetaDataWorker";

   String[] csvList;
   MetadataInterpreter metaInt;
   ObjectMapper objectMapper;

   public InterpretMetaDataWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
      super(context, workerParams);
   }

   @NonNull
   @Override
   public Result doWork() {

      handleInputData();

      metaInt = new MetadataInterpreter();
      objectMapper = new ObjectMapper();

      List<MetaData> data = persistMetadataFolder(csvList);
      Data.Builder builder = new Data.Builder();
      builder.putStringArray("result", prepareOutput(data));
      Data output = builder.build();
      return Result.success(output);
   }



   private void handleInputData(){
      csvList = getInputData().getStringArray("URICSV");
   }

   private String[] prepareOutput(List<MetaData> result){
      String[] dataArray = new String[result.size()];
      int i = 0;
      for(MetaData md : result){
         try {
            dataArray[i] = objectMapper.writeValueAsString(md);
            i++;
         } catch (IOException e) {
            Log.d(TAG, "prepareOutput: Something went wrong...Skipping meta data object");
         }
      }
      return dataArray;
   }

   public List<MetaData> interpretMetaData(Uri uri){
      // Background
      return metaInt.getCSVMetadata(uri, getApplicationContext().getContentResolver());
   }

   /**
    * Calls MetadataInterpreter to iterate through files in a folder
    * Calls persistMetadata
    */
   public List<MetaData> persistMetadataFolder(String[] uriList) {
      List<MetaData> result = new LinkedList<>();
      for(String s: uriList){
         Log.d(TAG, "persisting Uri: " +s);
         try{
            Uri u = Uri.parse(s);
            result.addAll(interpretMetaData(u));
         } catch (SQLiteConstraintException sq){
            //If single Metadata was added manually before, RoomDB won't let it added
            Log.d(TAG, "Metadata already inserted - PK Error: " + sq.getMessage());
         }catch (Exception e){
            Log.d(TAG, "Could not parse Uri" + s);
         }
      }
      return result;
   }



}
