package de.th.ro.datavis.imp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
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
import de.th.ro.datavis.util.enums.MetadataType;
import de.th.ro.datavis.util.exceptions.CSVException;
import de.th.ro.datavis.util.exceptions.FFSInterpretException;
import de.th.ro.datavis.util.filehandling.FileHandler;

public class ImportActivity extends BaseActivity{

    private final String LOG_TAG = "ImportActivity";

    AppDatabase appDb;

    FFSService ffsService;

    Antenna currentAntenna;
    MetaData currentMetaData;
    List<AntennaField> currentAntenaFields;
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

        importView = new ImportView(this, currentAntenna, currentAntenaFields) {
            @Override
            public void chooseExistingAntenna() {
                // Antennen zeigen
                AppDatabase appDb = AppDatabase.getInstance(getApplicationContext());
                LiveData<List<Antenna>> antennaList = new MutableLiveData<>(new ArrayList<>());

                antennaList = appDb.antennaDao().getAll();

                antennaList.observe(getFragmentActivity(), list -> {

                    displayChooseAntennaDialog(list);

                });
            }

            @Override
            public void addNewAntenna() {
                openFileDialog_Android9(FileRequests.REQUEST_CODE_ANTENNA);
            }

            @Override
            public void addMetaData() {
                if (currentAntenna == null){
                    Toast.makeText(getFragmentActivity(), "No Antenna ", Toast.LENGTH_LONG).show();
                    return;
                }
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getFragmentActivity());
                preferences.edit().putInt("ID", currentAntenna.id).apply();

                openFileDialog_Android9(FileRequests.REQUEST_CODE_METADATA);
            }

            public void addMetaDataFolder() {
                if (currentAntenna == null){
                    Toast.makeText(getFragmentActivity(), "No Antenna ", Toast.LENGTH_LONG).show();
                    return;
                }
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getFragmentActivity());
                preferences.edit().putInt("ID", currentAntenna.id).apply();

                openFolderDialog(FileRequests.REQUEST_CODE_METADATAFOLDER);
            }

            @Override
            public void addFFS() {
                if (currentAntenna == null){
                    Toast.makeText(getFragmentActivity(), "No Antenna ", Toast.LENGTH_LONG).show();
                    return;
                }

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getFragmentActivity());
                preferences.edit().putInt("ID", currentAntenna.id).apply();

                openFileDialog_Android9(FileRequests.REQUEST_CODE_FFS);
            }
        };
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "Activity result");
        if(resultCode == Activity.RESULT_OK){
            Log.d(LOG_TAG, "Request Code: "+requestCode);
            //Files can only be read from Main/UI Thread

            /*if(requestCode == FileRequests.REQUEST_CODE_METADATA){
                Uri uri = data.getData();
                String name = FileHandler.queryName(getContentResolver(), uri);

                AppDatabase appDb = AppDatabase.getInstance(getApplicationContext());
                persistMetadata(appDb, uri, name, data);
            }*/

            ExecutorService executorService  = Executors.newSingleThreadExecutor();

            Future future = executorService.submit( getHandelResultRunnable( data, requestCode) );

            try {
                future.get();

                initImportView();

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Exception " + e.getMessage());
            }

            // dD

        }

    }


    public Runnable getHandelResultRunnable(Intent data, int requestCode){
        return new Runnable() {
            @Override
            public void run() {
                try {
                    Uri uri = data.getData();


                    AppDatabase appDb = AppDatabase.getInstance(getApplicationContext());

                    if (requestCode == FileRequests.REQUEST_CODE_ANTENNA){
                        String name = FileHandler.queryName( getContentResolver(), uri);
                        persistAntenna(appDb, uri, name);
                    } else if(requestCode == FileRequests.REQUEST_CODE_FFS) {
                        String name = FileHandler.queryName( getContentResolver(), uri);
                        persistFFS(appDb, uri, name, data);
                    } else if(requestCode == FileRequests.REQUEST_CODE_METADATA) {
                        String name = FileHandler.queryName( getContentResolver(), uri);
                        persistMetadata(appDb, uri, name, data);
                    } else {
                        persistMetadataFolder(appDb, uri, data);
                    }

                }catch(Exception e){
                    //TODO: Improve exception handling
                    e.printStackTrace();
                    Log.e(LOG_TAG, "Exception " + e.getMessage());
                }
            }
        };
    }


    public void persistAntenna(AppDatabase appDb, Uri uri, String name){
        // Background
        Antenna antenna = new Antenna(uri, name);
        appDb.antennaDao().insert(antenna);

        handelNewlyInsertedAntenna(appDb);

        updateAntennaField(appDb);

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
        handelNewlyInsertedAntennaField(appDb, antennaId);

    }

    /*
    Simple method to iterate through all files of a folder
    Calls persistMetadata
     */
    public void persistMetadataFolder(AppDatabase appDb, Uri rootUri, Intent data) {
        ArrayList<Uri> listUri = traverseDirectoryEntries(rootUri);
        Log.d(LOG_TAG, "listlen: "+listUri.size());
        for(Uri u: listUri){
            String nameUri = FileHandler.queryName(getContentResolver(), u);
        }
        for(Uri u: listUri){
            String nameUri = FileHandler.queryName(getContentResolver(), u);
            Log.d(LOG_TAG, "Persist Uri: " + nameUri + " " +u);
            persistMetadata(appDb, u, nameUri, data);
        }
    }


    /**
     * Takes the directory URI, iterates through the files and builds a list of all .csv
     * @param rootUri
     * @return
     */
    ArrayList<Uri> traverseDirectoryEntries(Uri rootUri) {
        ArrayList<Uri> listUri = new ArrayList<>();
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri,
                DocumentsContract.getTreeDocumentId(rootUri));
        // Keep track of our directory hierarchy
        List<Uri> dirNodes = new LinkedList<>();
        dirNodes.add(childrenUri);

        while(!dirNodes.isEmpty()) {
            childrenUri = dirNodes.remove(0); // get the item from top
            Log.d(LOG_TAG, "node uri: " + childrenUri);
            Cursor c = getContentResolver().query(childrenUri, new String[]{
                            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                            DocumentsContract.Document.COLUMN_MIME_TYPE},
                    null, null, null);
             try {

                while (c.moveToNext()) {
                    final String docId = c.getString(0);
                    final String name = c.getString(1);
                    final String mime = c.getString(2);
                    Log.d(LOG_TAG, "docId: " + docId + ", name: " + name + ", mime: " + mime);
                    if (name.contains(".csv")) {
                        final Uri newNode = DocumentsContract.buildDocumentUriUsingTree(rootUri, docId);
                        listUri.add(newNode);
                    }
                }


            } finally {
                try {
                    c.close();
                } catch(RuntimeException re) {
                    re.printStackTrace();
                }
            }
        }
        Log.d(LOG_TAG, "gathered URIs");
        return listUri;
    }
    private static boolean isDirectory(String mimeType) {
        return DocumentsContract.Document.MIME_TYPE_DIR.equals(mimeType);
    }

    /**
     * Persists Metadata
     */
    public void persistMetadata(AppDatabase appDb, Uri uri, String name, Intent data){
        // Background
        List<MetaData> list = getCSVMetadata(data);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int antennaId = preferences.getInt("ID", 1);

        for(MetaData e : list){
            Log.d(LOG_TAG, "Saving Metadata");
            e.setAntennaID(antennaId);
            if(name.contains(".")) name=name.substring(0, name.lastIndexOf('.'));
            e.setType(name);
            appDb.metadataDao().insert(e);
        }
        handelNewlyInsertedMetadata(appDb);
    }

    public List<MetaData> getCSVMetadata(Intent data){
        List<MetaData> m = null;

        try {
            if(data.getData() == null){
                m.add(new MetaData(0,0,"N/A"));
            }else{
                //getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                InputStream in = getContentResolver().openInputStream(data.getData());
                Log.d(LOG_TAG, "Input Stream open");
                m = metaInt.getMetadataFromLines(metaInt.interpretCSV(in));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch(SecurityException se){
            se.printStackTrace();
            //Toast.makeText(this, "Unable to load the file, due to missing permissions.", Toast.LENGTH_SHORT).show();
            return null;
        }
        return m;
    }




    private void updateAntennaField(AppDatabase appDb){
        // Background
        List<AntennaField> fieldList = new ArrayList<>();

        fieldList = appDb.antennaFieldDao().findByAntennaId_BackGround(currentAntenna.id); // todo anpassen

        this.currentAntenaFields = fieldList;

    }


    private void handelNewlyInsertedAntenna(AppDatabase appDb){
        // Background
        List<Antenna> data =new ArrayList<>();
        data = appDb.antennaDao().getAll_Background();

        int size2 = data.size();

        // Last Antenna
        Antenna antenna = data.get(size2 -1);

        this.currentAntenna = antenna;

    }

    private void handelNewlyInsertedAntennaField(AppDatabase appDb, int antennaId){
        // Background
        List<AntennaField> data =new ArrayList<>();
        data = appDb.antennaFieldDao().findByAntennaId_BackGround(antennaId);

        this.currentAntenaFields = data;

    }

    private void handelNewlyInsertedMetadata(AppDatabase appDb){
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


    private void setDefaultAntennaData(){

        if (currentAntenna != null || !firstRun){
            return;
        }

        firstRun = false;

        LiveData<List<Antenna>> antennas = new MutableLiveData<>(new ArrayList<>());

        // Get Antenna
        antennas = appDb.antennaDao().getAll();
        antennas.observe(this, list -> {

            if (list.isEmpty()){
                initImportView();
                return;
            }

            currentAntenna = list.get(0);

            // Get Antennafield
            loadAntennaFieldsByAntennaId(currentAntenna.id);


        });

    }

    private void displayChooseAntennaDialog( List<Antenna> antennaList){

        DialogExistingAntenna dialog = new DialogExistingAntenna(this, "Antenna", R.layout.dialog_import_existing_antenna, antennaList) {
            @Override
            public void handelAntennaItemClick(Antenna antenna) {

                Toast.makeText(getApplicationContext(), "Antenna " + antenna.filename, Toast.LENGTH_LONG).show();
                currentAntenna = antenna;
                loadAntennaFieldsByAntennaId(currentAntenna.id);


                initImportView();

                this.getDialog().dismiss();
            }
        };

        dialog.showDialog();

    }


    public void loadAntennaFieldsByAntennaId(int antennaId){

        LiveData<List<AntennaField>> antennaFields = new MutableLiveData<>(new ArrayList<>());
        antennaFields = appDb.antennaFieldDao().findByAntennaId_Main(antennaId);
        antennaFields.observe(this, fieldList -> {
            currentAntenaFields = fieldList;

            // Update UI
            initImportView();
        });


    }


}
