package de.th.ro.datavis.imp;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.interpreter.csv.MetadataInterpreter;
import de.th.ro.datavis.models.MetaData;
import de.th.ro.datavis.util.constants.IntentConst;

public class InterpretMetaDataWorker extends Worker {


   private final String TAG = "ImportMetaDataWorker";

   private String[] csvList;
   private int currentAntennaID;
   private MetadataInterpreter metaInt;
   private ObjectMapper objectMapper;
   private AppDatabase appDb;;

   public InterpretMetaDataWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
      super(context, workerParams);
   }

   @NonNull
   @Override
   public Result doWork() {

      appDb  = AppDatabase.getInstance(getApplicationContext());

      handleInputData();

      metaInt = new MetadataInterpreter();
      objectMapper = new ObjectMapper();

      List<MetaData> metaDataList = interpretMetadataFolder(csvList);

      persistMetadata(metaDataList);

      return Result.success();


   }



   private void handleInputData(){
      csvList = getInputData().getStringArray("URICSV");
      currentAntennaID = getInputData().getInt(IntentConst.INTENT_EXTRA_ANTENNA_ID,0);
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
      return metaInt.getCSVMetadata(uri, getApplicationContext().getContentResolver());
   }

   /**
    * Calls MetadataInterpreter to iterate through files in a folder
    * Calls persistMetadata
    */
   public List<MetaData> interpretMetadataFolder(String[] uriList) {
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
      Log.d(TAG, "interpret MetaData done ");
      return result;
   }

   public void persistMetadata(List<MetaData> metaDataList){
      for(MetaData e : metaDataList){
         e.setAntennaID(currentAntennaID);
         appDb.metadataDao().insert(e);
      }
      Log.d(TAG, "persist MetaData done ");
   }

}
