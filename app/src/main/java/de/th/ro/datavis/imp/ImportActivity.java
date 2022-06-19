package de.th.ro.datavis.imp;

import static android.os.Build.VERSION.SDK_INT;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;
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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
import de.th.ro.datavis.util.dialog.DialogExistingAntenna;
import de.th.ro.datavis.util.exceptions.FFSInterpretException;
import de.th.ro.datavis.util.filehandling.FileHandler;
import de.th.ro.datavis.util.worker.WorkerRequestUtil;

public class ImportActivity extends BaseActivity{

    private final String TAG = "ImportActivityTAG";

    AppDatabase appDb;
    FFSService ffsService;

    Antenna currentAntenna;
    int givenAntennaId;
    MetaData currentMetaData;
    List<AntennaField> currentAntenaFields;
    List<Antenna> allAntennas;

    ImportView importView;

    MetadataInterpreter metaInt = new MetadataInterpreter();

    static boolean firstRun = true;

    WorkManager workManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "OnCreate");

        setContentView(R.layout.activity_import);
        setFragmentContainerView(R.id.importFragment);

        Toolbar toolbar = findViewById(R.id.import_toolbar);
        setSupportActionBar(toolbar);


        appDb  = AppDatabase.getInstance(getApplicationContext());


            try {
                givenAntennaId = getIntent().getExtras().getInt(IntentConst.INTENT_EXTRA_ANTENNA_ID);
            }catch (NullPointerException np){
                Log.d(TAG, "No Antenna given.");
            }catch (Exception e){
                e.printStackTrace();
                Log.d(TAG, "Given Antenna could no be found.");
            }


        ffsService = new FFSService(new FFSInterpreter(), this);
        workManager = WorkManager.getInstance(this);

        setDefaultAntennaData();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        firstRun=true;
    }

    private void initImportView(){
        Log.d(TAG, "initImportView: Initializing ... " + currentAntenna +", "+ currentAntenaFields +", "+ currentMetaData);

        importView = new ImportView(this, currentAntenna, currentAntenaFields, currentMetaData) {

            public TextWatcher descriptionChanged(){
                return new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable editable) {
                        executeRunnable(new Runnable() {
                            @Override
                            public void run() {
                                currentAntenna.setDescription(editable.toString());
                                appDb.antennaDao().update(currentAntenna);
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
            public void insertNewConfig(){
                Log.d(TAG, "insertNewConfig: new Config");
                handleNewConfigInsert();
            }

            @Override
            public void chooseExistingConfig() {
                // Antennen zeigen
                executeRunnable(getAllAntennas());
                displayChooseAntennaDialog(allAntennas);
            }

            @Override
            public void addImportAntenna() {
                openFileDialog(FileRequests.REQUEST_CODE_ANTENNA);
            }

            public void addDefaultAntenna(){
                executeRunnable(getSetDefaultAntennaRunnable());
                initImportView();
            }

            @Override
            public void addMetaData() {
                if (currentAntenna == null){
                    Toast.makeText(getFragmentActivity(), "No Antenna ", Toast.LENGTH_LONG).show();
                    return;
                }
                setPreferenceID();

                openFileDialog(FileRequests.REQUEST_CODE_METADATA);
            }

            @Override
            public void addMetaDataFolder() {
                if (currentAntenna == null){
                    Toast.makeText(getFragmentActivity(), "No Antenna ", Toast.LENGTH_LONG).show();
                    return;
                }
                setPreferenceID();

                openFolderDialog(FileRequests.REQUEST_CODE_METADATAFOLDER);
            }

            @Override
            public void addFFS() {
                if (currentAntenna == null){
                    Toast.makeText(getFragmentActivity(), "No Antenna ", Toast.LENGTH_LONG).show();
                    return;
                }

                setPreferenceID();

                openFileDialog(FileRequests.REQUEST_CODE_FFS);
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
                                updateAntenna(appDb, uri, antennaName);
                            } else {
                                showToast(getString(R.string.toastInvalidAntenna));
                            }
                            break;
                        case FileRequests.REQUEST_CODE_METADATA:
                            if(FileHandler.fileCheck(getContentResolver(), uri, requestCode)){
                                //If Metadata with the same Primary Key are read, it runs into a System.err and is caught here
                                try{
                                    persistMetadata(appDb, uri);
                                    showToast(getString(R.string.toastValidMetadata));
                                } catch (Exception e) {
                                    Log.e(TAG, "Metadata already inserted - PK Error: " + e.getMessage());
                                    showToast(getString(R.string.toastRedundantMetadata));
                                }
                            } else {
                                showToast(getString(R.string.toastInvalidMetadata));
                            }
                            break;
                        case FileRequests.REQUEST_CODE_METADATAFOLDER:
                            //Currently does not return whether any files from the Folder were imported
                            persistMetadataFolder(appDb, uri);
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



    public Runnable getSetDefaultAntennaRunnable(){
        return new Runnable() {
            @Override
            public void run() {
                AppDatabase appDb = AppDatabase.getInstance(getApplicationContext());
                Uri uri = Uri.parse("models/datavis_antenna_asm.glb");
                String filename = "datavis_default";
                updateAntenna(appDb, uri, filename);
            }
        };
    }

    public Runnable addNewAntennaConfig(Antenna antenna){
        Log.d(TAG, "addNewAntennaConfig: executing background thread");
        return new Runnable() {
            @Override
            public void run() {
                AppDatabase appDb = AppDatabase.getInstance(getApplicationContext());
                //changing default antenna name
                int currentSize = appDb.antennaDao().getAll_Background().size();
                antenna.description = antenna.description + (currentSize+1);
                appDb.antennaDao().insert(antenna);
                handleNewlyInsertedAntenna(appDb);
            }
        };
    }

    public Runnable getAllAntennas(){
        return new Runnable() {
            @Override
            public void run() {
                AppDatabase appDb = AppDatabase.getInstance(getApplicationContext());
                List<Antenna> antennaList = appDb.antennaDao().getAll_Background();
                allAntennas = antennaList;
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


    public void updateAntenna(AppDatabase appDb, Uri uri, String filename){
        // Background
        currentAntenna.setAntennaFile(uri.toString(), filename);
        appDb.antennaDao().update(currentAntenna);
    }

    public void persistFFS(AppDatabase appDb, Uri uri, String name, Intent intent){

        Log.d(TAG, "persistFFS");

        // Background
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int antennaId = preferences.getInt("ID", 1);



        InputStream in = null;
        try {
            in = getContentResolver().openInputStream(intent.getData());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "got InputStream");

        try {
            ffsService.interpretData(in,0.4, antennaId, name);
        } catch (FFSInterpretException e) {
            e.printStackTrace();
            Log.d(TAG, "FFSInterpretException " + e.getMessage());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        Log.d(TAG, "interprete Data done");
        //Save Antenna and file to database
        AntennaField antennaField = new AntennaField(uri, name, antennaId);
        appDb.antennaFieldDao().insert(antennaField);

        handelGetAntennaInBackground(appDb, antennaId);
        handleNewlyInsertedAntennaField(appDb, antennaId);

        Log.d(TAG, "persistFFS done");
    }

    /**
     * Uses MetadataInterpreter to run through a .csv
     * then adds antennaID
     * then persists it
     */
    public void persistMetadata(AppDatabase appDb, Uri uri){
        // Background
        List<MetaData> list = metaInt.getCSVMetadata(uri, this.getContentResolver());
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int antennaId = preferences.getInt("ID", 1);

        for(MetaData e : list){
            e.setAntennaID(antennaId);
            appDb.metadataDao().insert(e);
        }
        handleNewlyInsertedMetadata(appDb);
    }

    /**
     * Calls MetadataInterpreter to iterate through files in a folder
     * Calls persistMetadata
     */
    public void persistMetadataFolder(AppDatabase appDb, Uri rootUri) {
        ArrayList<Uri> listUri = metaInt.traverseDirectoryEntries(rootUri, this.getContentResolver());
        Log.d(TAG, "directory list length: "+listUri.size());
        for(Uri u: listUri){
            Log.d(TAG, "persisting Uri: " +u.toString());
            try{
                persistMetadata(appDb, u);
            } catch (SQLiteConstraintException sq){
                //If single Metadata was added manually before, RoomDB won't let it added
                Log.d(TAG, "Metadata already inserted - PK Error: " + sq.getMessage());
            }
        }
    }


    private void updateAntennaField(AppDatabase appDb){
        // Background
        List<AntennaField> fieldList = new ArrayList<>();
        fieldList = appDb.antennaFieldDao().findByAntennaId_BackGround(currentAntenna.id); // todo anpassen
        this.currentAntenaFields = fieldList;
    }

    /**
     * Gets new Antenna from database and sets it as the current antenna
     */
    private void handleNewlyInsertedAntenna(AppDatabase appDb){
        Log.d(TAG, "handleNewlyInsertedAntenna: Setting newly insert antenna as current");
        // Background
        List<Antenna> data = new ArrayList<>();
        data = appDb.antennaDao().getAll_Background();
        // Last Antenna
        this.currentAntenna = data.get(data.size() - 1);
    }

    private void handleNewlyInsertedAntennaField(AppDatabase appDb, int antennaId){
        // Background
        List<AntennaField> data =new ArrayList<>();
        data = appDb.antennaFieldDao().findByAntennaId_BackGround(antennaId);
        this.currentAntenaFields = data;
    }

    private void handleNewlyInsertedMetadata(AppDatabase appDb){
        // Background
        List<MetaData> data =new ArrayList<>();
        data = appDb.metadataDao().getAll_Background();
        int size2 = data.size();
        // Last Antenna
        MetaData meta = data.get(size2 -1);
        this.currentMetaData = meta;
    }

    private void handelGetAntennaInBackground(AppDatabase appDb, int antennaId){
        // Background
        List<Antenna> data =new ArrayList<>();
        data = appDb.antennaDao().find_Background(antennaId);
        this.currentAntenna = data.get(0);
    }

    private void handleNewConfigInsert(){
        Log.d(TAG, "handleNewConfigInsert: creating background thread");
        Antenna insert = new Antenna("Antenna #");
        //Background
        executeRunnable(addNewAntennaConfig(insert));
        //Reset other options
        currentAntenaFields = null;
        currentMetaData = null;
        initImportView();
    }

    /**
     * Method is called when the import activity is opened for the first time.
     * Sets the currentAntenna to the first antenna in the DB. If DB is empty,
     * the default ImportView will be shown.
     */
    private void setDefaultAntennaData(){
        if (currentAntenna != null || !firstRun){
            return;
        }
        firstRun = false;
        executeRunnable(getAllAntennas());
        if(allAntennas.isEmpty()){
            initImportView();
            return;
        }else {
            if(givenAntennaId == 0) {
                currentAntenna = allAntennas.get(0);
            } else{
                currentAntenna = allAntennas.stream().filter(x -> x.id == givenAntennaId).findFirst().get();
            }


            loadAntennaFieldsByAntennaId(currentAntenna.id);
            initImportView();
        }
    }

    /**
     * Opens a dialog with all available antennas. If one is selected, the dialog will close and
     * a new current antenna will be set
     * @param antennaList - all available antennas from the database
     */
    private void displayChooseAntennaDialog( List<Antenna> antennaList){
        Log.d(TAG, "displayChooseAntennaDialog: Opening...");
        DialogExistingAntenna dialog = new DialogExistingAntenna(this, "Antenna", R.layout.dialog_import_existing_antenna, antennaList) {
            @Override
            public void handelAntennaItemClick(Antenna antenna) {

                Toast.makeText(getApplicationContext(), "Antenna " + antenna.description, Toast.LENGTH_LONG).show();
                currentAntenna = antenna;
                loadAntennaFieldsByAntennaId(currentAntenna.id);


                initImportView();
                this.getDialog().dismiss();
            }
        };
        dialog.showDialog();
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
            if (requestCode == FileRequests.REQUEST_CODE_FFS){
                String ffsName = FileHandler.queryName( getContentResolver(), uri);
                if(FileHandler.fileCheck(getContentResolver(), uri, requestCode)){
                    handleFFSImportWork(uri, ffsName);
                } else {
                    showToast(getString(R.string.toastInvalidFFS));
                }
            } else {
                // Antenna, Metadata, ...
                executeRunnable(getHandelResultRunnable( data, requestCode));
                initImportView();
            }

        }



    }


    /**
     * Handle Import Result of FFD Data.
     * Queue Work to an Non blocking Thread
     * @param uri Uri to the FFS file
     */
    private void handleFFSImportWork(Uri uri, String fileName){

        // Build InputData
        Data.Builder builder = new Data.Builder();
        builder.putString("URI", uri.toString());
        builder.putString("FILENAME", fileName);
        Data input = builder.build();

        // Create WorkRequest
        OneTimeWorkRequest workRequest = WorkerRequestUtil.getOneTimeRequest(ImportWorker.class, input);
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


    /**
     * Utility method to call Toasts in UI Thread
     * @param toast
     */
    public void showToast(final String toast)
    {
        runOnUiThread(() -> Toast.makeText(this, toast, Toast.LENGTH_SHORT).show());
    }

}
