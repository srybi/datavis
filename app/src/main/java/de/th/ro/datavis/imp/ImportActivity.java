package de.th.ro.datavis.imp;

import static android.os.Build.VERSION.SDK_INT;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.th.ro.datavis.MainActivity;
import de.th.ro.datavis.R;
import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.instructions.ImportInstructionsActivity;
import de.th.ro.datavis.interpreter.csv.MetadataInterpreter;
import de.th.ro.datavis.interpreter.ffs.FFSInterpreter;
import de.th.ro.datavis.interpreter.ffs.FFSService;
import de.th.ro.datavis.models.Antenna;
import de.th.ro.datavis.models.AntennaField;
import de.th.ro.datavis.models.MetaData;
import de.th.ro.datavis.util.activity.BaseActivity;
import de.th.ro.datavis.util.constants.FileRequests;
import de.th.ro.datavis.util.filehandling.FileHandler;
import de.th.ro.datavis.util.worker.WorkerRequestUtil;

public class ImportActivity extends BaseActivity{

    private final String TAG = "ImportActivityTAG";

    AppDatabase appDb;
    FFSService ffsService;
    MetadataInterpreter metaInt;
    WorkManager workManager;

    Antenna currentAntenna;
    List<Uri> currentMetaDataUris;
    List<String> currentMetaDataType;
    List<AntennaField> currentAntenaFields;

    ImportView importView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "OnCreate");

        setContentView(R.layout.activity_import);
        setFragmentContainerView(R.id.importFragment);

        Toolbar toolbar = findViewById(R.id.import_toolbar);
        setSupportActionBar(toolbar);


        appDb  = AppDatabase.getInstance(getApplicationContext());
        ffsService = new FFSService(new FFSInterpreter(), this);
        metaInt = new MetadataInterpreter();
        workManager = WorkManager.getInstance(this);

        executeRunnable(initImport());
        initImportView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)  {
        getMenuInflater().inflate(R.menu.menu_import, menu);

        MenuItem itemImportInstructions = menu.findItem(R.id.import_instructions);

        itemImportInstructions.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                // switch to import instructions
                Intent intent = new Intent(getApplicationContext(), ImportInstructionsActivity.class);
                startActivity(intent);
                return false;
            }
        });
        return true;
    }


    private void initImportView(){
        Log.d(TAG, "initImportView: Initializing ... " + currentAntenna +", "+ currentAntenaFields +", "+ currentMetaDataType);

        importView = new ImportView(this, currentAntenna, currentAntenaFields, currentMetaDataType) {

            public TextWatcher descriptionChanged(){
                return new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable editable) {
                        executeRunnable(new Runnable() {
                            @Override
                            public void run() {
                                currentAntenna.setDescription(editable.toString());
                            }
                        });
                    }
                    // ============== NOT USED ==================
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                    // ===========================================
                };
            }


            @Override
            public void addImportAntenna() {
                openFileDialog(FileRequests.REQUEST_CODE_ANTENNA);
            }

            public void addDefaultAntenna(){
                currentAntenna.filename = "datavis_default";
                currentAntenna.uri = "models/datavis_antenna_asm.glb";
                initImportView();
            }

            @Override
            public void addFolder() {
                openFolderDialog(FileRequests.REQUEST_CODE_FOLDER);
            }

            @Override
            public void confirmImport(){
                executeRunnable(saveAntenna());
                setPreferenceID();
                executeRunnable(saveMetadata());
                //Switch back to Landing page
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }

            private void setPreferenceID(){
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getFragmentActivity());
                preferences.edit().putInt("ID", currentAntenna.id).apply();
            }

        };
    }

    /**
     * Handles a chosen file and stores it (using a background thread)
     * Uses the requestCode to find the correct Method to parse the data
     * FileHandler checks the file for validity
     * @return
     */
    public Runnable getHandelResultRunnable(Intent data, int requestCode){
        return new Runnable() {
            @Override
            public void run() {
                try {
                    Uri uri = data.getData();
                    AppDatabase appDb = AppDatabase.getInstance(getApplicationContext());
                    switch (requestCode) {
                        case FileRequests.REQUEST_CODE_ANTENNA:
                            String antennaName = FileHandler.queryName(getContentResolver(), uri);
                            if(FileHandler.fileCheck(getContentResolver(), uri, requestCode)){
                                currentAntenna.filename = antennaName;
                                currentAntenna.uri = uri.toString();
                            } else {
                                showToast(getString(R.string.toastInvalidAntenna));
                            }
                            break;
                        case FileRequests.REQUEST_CODE_FOLDER:
                            Log.d(TAG, "run: Importing Metaddatafolder");
                            //Currently does not return whether any files from the Folder were imported
                            setMetaDataUris(uri);
                            showToast(getString(R.string.toastValidMetadataFolder));
                            break;
                        default: throw new RuntimeException();
                    }
                } catch(Exception e){
                    e.printStackTrace();
                    Log.e(TAG, "Exception " + e.getMessage());
                }
            }
        };
    }


    public Runnable initImport(){
        Log.d(TAG, "addNewAntennaConfig: executing background thread");
        return new Runnable() {
            @Override
            public void run() {
                AppDatabase appDb = AppDatabase.getInstance(getApplicationContext());
                int currentSize = appDb.antennaDao().getAll_Background().size();
                currentAntenna = new Antenna("Antenna #" + (currentSize+1));
            }
        };
    }


    private Runnable saveAntenna(){
        return new Runnable() {
            @Override
            public void run() {
                //save antenna
                appDb.antennaDao().insert(currentAntenna);
                handleNewlyInsertedAntenna(appDb);
            }
        };
    }

    private Runnable saveMetadata(){
        return new Runnable() {
            @Override
            public void run() {
                persistMetadata(appDb);
            }
        };
    }

    public void executeRunnable(Runnable runnable){
        ExecutorService executorService  = Executors.newSingleThreadExecutor();
        Future future = executorService.submit(runnable);
        try{
            future.get();
        }catch (Exception e){
            Log.d(TAG, "executeRunnable: " + e.getMessage());
        }
    }

    /**
     * Uses MetadataInterpreter to run through a .csv
     * then adds antennaID
     * then persists it
     */
    public void persistMetadata(AppDatabase appDb){
        for(Uri u : currentMetaDataUris){
            List<MetaData> list = metaInt.getCSVMetadata(u, this.getContentResolver());
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            int antennaId = preferences.getInt("ID", 1);

            for(MetaData e : list){
                e.setAntennaID(antennaId);
                appDb.metadataDao().insert(e);
            }
        }
        handleNewlyInsertedMetadata(appDb);
    }

    public void setMetaDataUris(Uri rootUri) {
        currentMetaDataUris = metaInt.traverseDirectoryEntries(rootUri, this.getContentResolver());
        currentMetaDataType = new LinkedList<>();
        for(Uri u : currentMetaDataUris){
            currentMetaDataType.add(FileHandler.queryName(getContentResolver(), u));
        }
    }

    /**
     * Gets new Antenna from database and sets it as the current antenna
     */
    private void handleNewlyInsertedAntenna(AppDatabase appDb) {
        Log.d(TAG, "handleNewlyInsertedAntenna: Setting newly insert antenna as current");
        // Background
        List<Antenna> data = new ArrayList<>();
        data = appDb.antennaDao().getAll_Background();
        // Last Antenna
        this.currentAntenna = data.get(data.size() - 1);
    }

    private void handleNewlyInsertedMetadata(AppDatabase appDb){
        /*
        // Background
        List<MetaData> data =new ArrayList<>();
        data = appDb.metadataDao().getAll_Background();
        int size2 = data.size();
        // Last Antenna
        MetaData meta = data.get(size2 -1);
        this.currentMetaData = meta;
         */
    }

    //This needs to be changed. LiveData causes site effects
    public void loadAntennaFieldsByAntennaId(int antennaId){
        List<AntennaField> antennaFields = new ArrayList<>();
        executeRunnable(new Runnable() {
            @Override
            public void run() {
                currentAntenaFields = appDb.antennaFieldDao().findByAntennaId_BackGround(antennaId);
            }
        });
    }

    /**
     *  ==================================
     * |    The functions below are      |
     * |    used for file handling       |
     *  ==================================
     */
    public void openFileDialog(int requestCode){
        if (SDK_INT >= Build.VERSION_CODES.Q) {
            openFileDialog_Android11(requestCode);
        } else {
            openFileDialog_Android9(requestCode);
        }
    }

    private void openFileDialog_Android9(int requestCode){
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");

        startActivityForResult(chooseFile, requestCode);
    }

    private void openFolderDialog(int requestCode){
        Intent browseIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        browseIntent.addFlags(
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                        | Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        );
        startActivityForResult(browseIntent, requestCode);
    }

    private void openFileDialog_Android11(int requestCode){
        Intent data = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        data.setType("*/*");
        data.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        data = Intent.createChooser(data, "Choose a file");
        startActivityForResult(data, requestCode);
    }

    /**
     * This method gets called after finishing the file/folder dialog
     * @param requestCode Type of request (Antenna, FFS, Meta)
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        importView.showProgressBar();
        Log.d(TAG, "Activity result");
        if(resultCode == Activity.RESULT_OK){
//            Log.d(TAG, "Request Code: "+requestCode);
//            executeRunnable(getHandelResultRunnable( data, requestCode));
//            initImportView();

            Uri uri = data.getData();

            // Handle FFS separately since its Work
            /*
            if (requestCode == FileRequests.REQUEST_CODE_FFS){
                String ffsName = FileHandler.queryName( getContentResolver(), uri);
                if(FileHandler.fileCheck(getContentResolver(), uri, requestCode)){
                    handleFFSImportWork(uri, ffsName);
                } else {
                    showToast(getString(R.string.toastInvalidFFS));
                }


            }
            */
            if(requestCode == FileRequests.REQUEST_CODE_FOLDER){
                //Currently does not return whether any files from the Folder were imported
                handleFolderImport(uri);
                showToast(getString(R.string.toastFolderImport));
            } else{
                // Antenna, Metadata, ...
                executeRunnable(getHandelResultRunnable( data, requestCode));
                initImportView();

            }

        }

    }

    private void handleFolderImport(Uri uri){
        Map<Integer, String[]> pairURI = FileHandler.traverseDirectoryEntries(uri, getContentResolver());
        handleFFSImportWork("URIFFS", pairURI.get(1));
        handleMetaDataImportWork("URICSV", pairURI.get(0));
    }


    private void handleFFSImportWork(String key, String[] values){

        // Build InputData
        Data.Builder builder = new Data.Builder();
        builder.putStringArray(key, values);
        Data input = builder.build();

        // Create WorkRequest
        OneTimeWorkRequest workRequest = WorkerRequestUtil.getOneTimeRequest(InterpretFFSWorker.class, input);
        workManager.enqueue( workRequest);
        workManager.getWorkInfoByIdLiveData(workRequest.getId()).observe(this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                Log.d(TAG, "WorkState FFS " + workInfo.getState());
                if (workInfo.getState().isFinished()){

                    if (workInfo.getState() == WorkInfo.State.FAILED
                            || workInfo.getState() == WorkInfo.State.CANCELLED){
                        // Work Problem

                        return;
                    }



                    // Work success
                    loadAntennaFieldsByAntennaId(currentAntenna.id);
                    initImportView();
                }
            }
        });

    }

    /** Handle Folder Work
     */
    private void handleMetaDataImportWork(String key, String[] values){

        // Build InputData
        Data.Builder builder = new Data.Builder();
        builder.putStringArray(key, values);
        Data input = builder.build();

        // Create WorkRequest
        OneTimeWorkRequest workRequest = WorkerRequestUtil.getOneTimeRequest(InterpretMetaDataWorker.class, input);
        workManager.enqueue( workRequest);
        workManager.getWorkInfoByIdLiveData(workRequest.getId()).observe(this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                Log.d(TAG, "WorkState FOLDER " + workInfo.getState());
                if (workInfo.getState().isFinished()){

                    if (workInfo.getState() == WorkInfo.State.FAILED
                            || workInfo.getState() == WorkInfo.State.CANCELLED){
                        // Work Problem

                        return;
                    }

                    // Work success
                    loadAntennaFieldsByAntennaId(currentAntenna.id);
                    initImportView();
                    showToast(getString(R.string.toastFolderImportDone));
                }
            }
        });

    }


    /**
     * Utility method to call Toasts in UI Thread
     * @param toast
     */
    public void showToast(final String toast)
    {
        runOnUiThread(() -> Toast.makeText(this, toast, Toast.LENGTH_LONG).show());
    }

}
