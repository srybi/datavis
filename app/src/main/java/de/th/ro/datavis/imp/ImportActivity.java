package de.th.ro.datavis.imp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.th.ro.datavis.R;
import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.models.Antenna;
import de.th.ro.datavis.models.AntennaField;
import de.th.ro.datavis.models.MetaData;
import de.th.ro.datavis.util.activity.BaseActivity;
import de.th.ro.datavis.util.constants.*;
import de.th.ro.datavis.util.dialog.DialogExistingAntenna;
import de.th.ro.datavis.util.enums.MetadataType;
import de.th.ro.datavis.util.filehandling.FileHandler;

public class ImportActivity extends BaseActivity{

    private final String LOG_TAG = "ImportActivity";

    AppDatabase appDb;

    Antenna currentAntenna;
    MetaData currentMetaData;
    List<AntennaField> currentAntenaFields;
    ImportView importView;

    static boolean firstRun = true;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_import);
        setFragmentContainerView(R.id.importFragment);

        Toolbar toolbar = findViewById(R.id.import_toolbar);
        setSupportActionBar(toolbar);


        appDb  = AppDatabase.getInstance(getApplicationContext());

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
                openFileDialog_Android9(FileRequests.REQUEST_CODE_METADATA);
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
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");

        startActivityForResult(chooseFile, requestCode);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK){


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
                    String name = FileHandler.queryName( getContentResolver(), uri);

                    AppDatabase appDb = AppDatabase.getInstance(getApplicationContext());

                    if (requestCode == FileRequests.REQUEST_CODE_ANTENNA){
                        persistAntenna(appDb, uri, name);
                    } else if(requestCode == FileRequests.REQUEST_CODE_FFS) {
                        persistFFS(appDb, uri, name);
                    } else {
                            //TODO: Metadatapainpai
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


    public void persistFFS(AppDatabase appDb, Uri uri, String name){
        // Background
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int antennaId = preferences.getInt("ID", 1);

        AntennaField antennaField = new AntennaField(uri, name, antennaId);
        appDb.antennaFieldDao().insert(antennaField);

        handelGetAntennaInBackground(appDb, antennaId);
        handelNewlyInsertedAntennaField(appDb, antennaId);

    }

    /**
     * Persists Metadata by creating Metadata Objects with all relevant parameters
     */
    public void persistMetadata(AppDatabase appDb, Uri uri, int id, int antennaID, String filename, String freq, String tilt, String type){
        // Background
        MetaData meta = new MetaData(id, antennaID, filename, freq, tilt, type);
        appDb.metadataDao().insert(meta);

        handelNewlyInsertedMetadata(appDb);

        //Update Method necessary?
        //updateAntennaField(appDb);
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
