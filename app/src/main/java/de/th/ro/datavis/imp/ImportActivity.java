package de.th.ro.datavis.imp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import java.util.concurrent.Executors;

import de.th.ro.datavis.R;
import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.models.Antenna;
import de.th.ro.datavis.models.AntennaField;
import de.th.ro.datavis.util.activity.BaseActivity;
import de.th.ro.datavis.util.constants.FileRequests;
import de.th.ro.datavis.util.filehandling.FileHandler;

public class ImportActivity extends BaseActivity {


    private ImportFragment importFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_import);
        setFragmentContainerView(R.id.importFragment);

        Toolbar toolbar = findViewById(R.id.import_toolbar);
        setSupportActionBar(toolbar);


        importFragment = new ImportFragment();
        navigateTo(importFragment);

    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK){

            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {

                        Uri uri = data.getData();
                        String name = FileHandler.queryName( getContentResolver(), uri);

                        AppDatabase appDb = AppDatabase.getInstance(getApplicationContext());

                        if (requestCode == FileRequests.REQUEST_CODE_ANTENNA){
                            persistAntenna(appDb, uri, name);
                        } else {
                            persistFFS(appDb, uri, name);
                        }

                    }catch(Exception e){
                        //TODO: Improve exception handling
                        e.printStackTrace();
                    }
                }
            });

        }

    }


    public void persistAntenna(AppDatabase appDb, Uri uri, String name){
        Antenna antenna = new Antenna(uri, name);
        appDb.antennaDao().insert(antenna);

        importFragment.setCurrentAntenna(antenna);
    }

    public void persistFFS(AppDatabase appDb, Uri uri, String name){
        AntennaField antennaField = new AntennaField(uri, name);
        appDb.antennaFieldDao().insert(antennaField);

    }


}
