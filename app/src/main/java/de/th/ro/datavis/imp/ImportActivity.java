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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.th.ro.datavis.R;
import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.interpreter.csv.MetadataInterpreter;
import de.th.ro.datavis.interpreter.ffs.FFSInterpreter;
import de.th.ro.datavis.interpreter.ffs.FFSService;
import de.th.ro.datavis.models.Antenna;
import de.th.ro.datavis.models.AntennaField;
import de.th.ro.datavis.models.MetaData;
import de.th.ro.datavis.util.activity.BaseActivity;
import de.th.ro.datavis.util.constants.*;
import de.th.ro.datavis.util.dialog.DialogExistingAntenna;
import de.th.ro.datavis.util.exceptions.FFSInterpretException;
import de.th.ro.datavis.util.filehandling.FileHandler;

public class ImportActivity extends BaseActivity{

    private final String TAG = "ImportActivity";

    AppDatabase appDb;
    FFSService ffsService;

    Antenna currentAntenna;
    MetaData currentMetaData;
    List<AntennaField> currentAntenaFields;
    List<Antenna> allAntennas;

    ImportView importView;

    MetadataInterpreter metaInt = new MetadataInterpreter();

    static boolean firstRun = true;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_import);
        setFragmentContainerView(R.id.importFragment);

        Toolbar toolbar = findViewById(R.id.import_toolbar);
        setSupportActionBar(toolbar);


        appDb  = AppDatabase.getInstance(getApplicationContext());
        ffsService = new FFSService(new FFSInterpreter(), this);

        setDefaultAntennaData();
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
                ExecutorService executorService  = Executors.newSingleThreadExecutor();
                executeRunnable(getSetDefaultAntennaRunnable());
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
     * @param data
     * @param requestCode
     * @return
     */
    public Runnable getHandelResultRunnable(Intent data, int requestCode){
        return new Runnable() {
            @Override
            public void run() {
                try {
                    Uri uri = data.getData();

                    AppDatabase appDb = AppDatabase.getInstance(getApplicationContext());

                    if (requestCode == FileRequests.REQUEST_CODE_ANTENNA){
                        String name = FileHandler.queryName( getContentResolver(), uri);
                        updateAntenna(appDb, uri, name);
                    } else if(requestCode == FileRequests.REQUEST_CODE_FFS) {
                        String name = FileHandler.queryName( getContentResolver(), uri);
                        persistFFS(appDb, uri, name, data);
                    } else if(requestCode == FileRequests.REQUEST_CODE_METADATA) {
                        persistMetadata(appDb, uri);
                    } else {
                        persistMetadataFolder(appDb, uri);
                    }

                }catch(Exception e){
                    //TODO: Improve exception handling
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
        // Background
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int antennaId = preferences.getInt("ID", 1);

        AntennaField antennaField = new AntennaField(uri, name, antennaId);
        appDb.antennaFieldDao().insert(antennaField);

        InputStream in = null;
        try {
            in = getContentResolver().openInputStream(intent.getData());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            ffsService.interpretData(in,0.4, antennaId);
        } catch (FFSInterpretException e) {
            e.printStackTrace();
        }

        handelGetAntennaInBackground(appDb, antennaId);
        handleNewlyInsertedAntennaField(appDb, antennaId);

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

    /*
     * Calls MetadataInterpreter to iterate through files in a folder
     * Calls persistMetadata
     */
    public void persistMetadataFolder(AppDatabase appDb, Uri rootUri) {
        ArrayList<Uri> listUri = metaInt.traverseDirectoryEntries(rootUri, this.getContentResolver());
        Log.d(TAG, "directory list length: "+listUri.size());
        for(Uri u: listUri){
            //String nameUri = FileHandler.queryName(getContentResolver(), u);
            Log.d(TAG, "persisting Uri: " +u.toString());
            persistMetadata(appDb, u);
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
            currentAntenna = allAntennas.get(0);
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
        Log.d(TAG, "Activity result");
        if(resultCode == Activity.RESULT_OK){
            Log.d(TAG, "Request Code: "+requestCode);
            executeRunnable(getHandelResultRunnable( data, requestCode));
            initImportView();
        }
    }


}
