package de.th.ro.datavis;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.concurrent.Executors;

import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.main.MainFragment;
import de.th.ro.datavis.models.AntennaField;
import de.th.ro.datavis.util.activity.BaseActivity;
import de.th.ro.datavis.util.filehandling.FileHandler;

public class MainActivity extends BaseActivity{



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setFragmentContainerView(R.id.mainFragment);

        navigateTo(new MainFragment(getFragmentContainerView()));

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

                        AntennaField field = new AntennaField(uri, name);

                        appDb.antennaFieldDao().insert(field);
                    }catch(Exception e){
                        //TODO: Improve exception handling
                        e.printStackTrace();
                    }
                }
            });

        }

    }


    }