package de.th.ro.datavis;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import java.util.concurrent.Executors;

import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.imp.ImportActivity;
import de.th.ro.datavis.main.MainFragment;
import de.th.ro.datavis.models.AntennaField;
import de.th.ro.datavis.util.activity.BaseActivity;
import de.th.ro.datavis.util.filehandling.FileHandler;

public class MainActivity extends BaseActivity{


    MainFragment mainFragment;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setFragmentContainerView(R.id.mainFragment);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        mainFragment = new MainFragment(getFragmentContainerView());

        navigateTo( mainFragment);

    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem itemImport = menu.findItem(R.id.action_import);
        MenuItem itemSettings = menu.findItem(R.id.action_settings);

        itemImport.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                // switch to import

                Intent intent = new Intent(getApplicationContext(), ImportActivity.class);
                startActivity(intent);

                return false;
            }
        });

        itemSettings.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                // switch to settings

                return false;
            }
        });

        return true;
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


                        mainFragment.initAntennaList();

                    }catch(Exception e){
                        //TODO: Improve exception handling
                        e.printStackTrace();
                    }
                }
            });

        }

    }


    }