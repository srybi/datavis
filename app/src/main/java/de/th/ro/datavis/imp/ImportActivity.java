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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.fasterxml.jackson.databind.ObjectMapper;

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
import de.th.ro.datavis.util.constants.IntentConst;
import de.th.ro.datavis.util.filehandling.FileHandler;
import de.th.ro.datavis.util.worker.WorkerRequestUtil;

public class ImportActivity extends BaseActivity{

    private final String TAG = "ImportActivityTAG";

    AppDatabase appDb;
    FFSService ffsService;
    MetadataInterpreter metaInt;
    WorkManager workManager;

    Antenna currentAntenna;
    List<String> currentMetaDataType;
    List<MetaData> currentMetaData;

    List<AntennaField> currentAntennaFields;
    String[] ffsUris;
    String[] metaDataUris;

    ImportView importView;

    int givenAntennaId;
    boolean editMode= false;
    boolean hasChanged=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "OnCreate");

        setContentView(R.layout.activity_import);
        setFragmentContainerView(R.id.importFragment);

        Toolbar toolbar = findViewById(R.id.import_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        appDb  = AppDatabase.getInstance(getApplicationContext());
        ffsService = new FFSService(new FFSInterpreter(), this);
        metaInt = new MetadataInterpreter();
        workManager = WorkManager.getInstance(this);

        try {
            givenAntennaId = getIntent().getExtras().getInt(IntentConst.INTENT_EXTRA_ANTENNA_ID);
            editMode = true;
        }catch (Exception e){
            Log.d(TAG, "No Antenna given");
        }
        if(editMode){
            executeRunnable(initEditMode());
        }else{
            executeRunnable(initImport());
        }
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
        Log.d(TAG, "initImportView: Initializing ... " + currentAntenna +", "+ currentAntennaFields +", "+ currentMetaDataType);

        importView = new ImportView(this, currentAntenna, currentAntennaFields, currentMetaDataType) {

            public TextWatcher descriptionChanged(){
                return new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable editable) {
                        executeRunnable(new Runnable() {
                            @Override
                            public void run() {
                                currentAntenna.setDescription(editable.toString());
                                hasChanged = true;
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
                hasChanged = true;
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
                if(editMode){
                    executeRunnable(deleteCurrentAntenna());
                }
                executeRunnable(saveAntenna());

                int currentAntennaID = currentAntenna.id;

                storeAntennaIDinPreferences(currentAntennaID);
                handleMetaDataImportWork(currentAntennaID);

                executeRunnable(persistAntennaFields());
                handleFFSImportWork("URIFFS", ffsUris);
            }

            private void storeAntennaIDinPreferences(int currentAntennaID){
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getFragmentActivity());
                preferences.edit().putInt("ID", currentAntennaID).apply();
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
                if(!editMode){
                    handleNewlyInsertedAntenna(appDb);
                }
            }
        };
    }

    private Runnable deleteCurrentAntenna(){
        return new Runnable() {
            @Override
            public void run() {
                appDb.antennaDao().delete(currentAntenna);
            }
        };
    }

    private Runnable initEditMode(){
        return new Runnable() {
            @Override
            public void run() {
                //load Antenna
                currentAntenna = appDb.antennaDao().find_Background(givenAntennaId).get(0);
                Log.d(TAG, "initEditMode: " + currentAntenna.toString());
                currentAntennaFields = appDb.antennaFieldDao().findByAntennaId_BackGround(givenAntennaId);
                currentMetaDataType = appDb.metadataDao().findDistinctTypesByAntennaId_Background(givenAntennaId);
                currentMetaData = appDb.metadataDao().findAllByAntennaId_Background(givenAntennaId);
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

    public void setMetaDataTypes(String[] metaDataUris) {
        Log.d(TAG, "setMetaDataUris: storing uris in var"  );
        currentMetaDataType = new LinkedList<>();

        for(String metaData : metaDataUris){
            Uri u = Uri.parse(metaData);
            currentMetaDataType.add(FileHandler.queryName(getContentResolver(), u));
            Log.d(TAG, "setMetaDataUris: storing metadatatypes in var");
        }
        Log.d(TAG, "setMetaDataUris: finished"  );
    }

    private Runnable persistAntennaFields(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int antennaId = preferences.getInt("ID", 1);
        return new Runnable() {
            @Override
            public void run() {
                for(AntennaField antennaField : currentAntennaFields){
                    antennaField.antennaId = antennaId;
                    appDb.antennaFieldDao().insert(antennaField);
                }
            }
        };
    }

    public void setAntennaFields(String[] antennaFieldsUri){
        currentAntennaFields = new LinkedList<>();
        for(String antennaField : antennaFieldsUri){
            Uri u = Uri.parse(antennaField);
            String filename = FileHandler.queryName(getContentResolver(), u);
            currentAntennaFields.add(new AntennaField(u, filename));
        }
        ffsUris = antennaFieldsUri;
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
        Log.d(TAG, "handleNewlyInsertedAntenna: " + currentAntenna.toString());
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

        Log.d(TAG, "Activity result initiated. Data was selected: "+(data!=null));
        if(resultCode == Activity.RESULT_OK && data!=null){
            hasChanged = true;
            Uri uri = data.getData();
            importView.showProgressBar();
            switch(requestCode){
                case FileRequests.REQUEST_CODE_FOLDER:
                    handleFolderImport(uri);
                    //showToast(getString(R.string.toastFolderImport));
                    initImportView();
                    break;
                case FileRequests.REQUEST_CODE_ANTENNA:
                    String antennaName = FileHandler.queryName(getContentResolver(), uri);
                    if(FileHandler.fileCheck(getContentResolver(), uri, requestCode)){
                        currentAntenna.filename = antennaName;
                        currentAntenna.uri = uri.toString();
                    } else {
                        showToast(getString(R.string.toastInvalidAntenna));
                    }
                    initImportView();
                    break;
            }
        }
    }

    private void handleFolderImport(Uri uri){
        Map<Integer, String[]> pairURI = FileHandler.traverseDirectoryEntries(uri, getContentResolver());
        setMetaDataTypes(pairURI.get(0));

        // Cache Metadata URIs for Worker
        metaDataUris = pairURI.get(0);

        setAntennaFields(pairURI.get(1));
    }

    private void handleFFSImportWork(String key, String[] values){
        // Build InputData
        if(values == null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            return;
        }
        Data.Builder builder = new Data.Builder();
        builder.putStringArray(key, values);
        builder.putStringArray("FILENAMEFFS", getFilenames(values));
        Data input = builder.build();
        importView.showProgressBar();

        // Create WorkRequest
        OneTimeWorkRequest workRequest = WorkerRequestUtil.getOneTimeRequest(InterpretFFSWorker.class, input);
        workManager.enqueue(workRequest);
        workManager.getWorkInfoByIdLiveData(workRequest.getId()).observe(this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                Log.d(TAG, "WorkState FFS " + workInfo.getState());
                if (workInfo.getState().isFinished()){

                    if (workInfo.getState() == WorkInfo.State.FAILED
                            || workInfo.getState() == WorkInfo.State.CANCELLED){
                        return;
                    }

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
            }
        });

    }

    private String[] getFilenames(String[] uris){
        String[] result = new String[uris.length];
        for(int i = 0; i < uris.length; i++){
            result[i] = FileHandler.queryName(getContentResolver(), Uri.parse(uris[i]));
        }
        return result;
    }

    /** Handle Folder Work
     */
    private void handleMetaDataImportWork(int currentAntennaID){

        String key = "URICSV";
        String[] values = metaDataUris;

        // Build InputData
        Data.Builder builder = new Data.Builder();
        builder.putStringArray(key, values);
        builder.putInt(IntentConst.INTENT_EXTRA_ANTENNA_ID, currentAntennaID);
        Data input = builder.build();

        // Create WorkRequest
        OneTimeWorkRequest workRequest = WorkerRequestUtil.getOneTimeRequest(InterpretMetaDataWorker.class, input);
        workManager.enqueue(workRequest);
        workManager.getWorkInfoByIdLiveData(workRequest.getId()).observe(this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                Log.d(TAG, "WorkState FOLDER " + workInfo.getState());
                if (workInfo.getState().isFinished()){
                    if (workInfo.getState() == WorkInfo.State.FAILED
                            || workInfo.getState() == WorkInfo.State.CANCELLED){

                        return;
                    }

                    metaDataUris = null;
                    //showToast(getString(R.string.toastFolderImportDone));
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
