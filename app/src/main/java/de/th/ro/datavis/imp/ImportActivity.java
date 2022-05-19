package de.th.ro.datavis.imp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.concurrent.Executors;

import de.th.ro.datavis.R;
import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.models.Antenna;
import de.th.ro.datavis.util.activity.BaseActivity;
import de.th.ro.datavis.util.filehandling.FileHandler;

public class ImportActivity extends BaseActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_import);
        setFragmentContainerView(R.id.importFragment);


        navigateTo(new ImportFragment());

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

                        Antenna antenna = new Antenna(uri, name);
                        appDb.antennaDao().insert(antenna);

                        Toast.makeText(getApplicationContext(), "Antenna " + antenna.toString(), Toast.LENGTH_LONG).show();

                    }catch(Exception e){
                        //TODO: Improve exception handling
                        e.printStackTrace();
                    }
                }
            });

        }

    }



}
