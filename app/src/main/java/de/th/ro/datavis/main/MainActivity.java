package de.th.ro.datavis;

import static android.os.Build.VERSION.SDK_INT;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import java.util.concurrent.Executors;

import de.th.ro.datavis.about.AboutActivity;
import de.th.ro.datavis.database.AppDatabase;
import de.th.ro.datavis.instructions.AppInstructionsActivity;
import de.th.ro.datavis.main.MainFragment;
import de.th.ro.datavis.models.AntennaField;
import de.th.ro.datavis.settings.SettingsActivity;
import de.th.ro.datavis.util.activity.BaseActivity;
import de.th.ro.datavis.filehandling.FileHandler;

public class MainActivity extends BaseActivity{


    MainFragment mainFragment;

    public final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setFragmentContainerView(R.id.mainFragment);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        mainFragment = new MainFragment(getFragmentContainerView());

    }

    @Override
    public void onBackPressed(){
        finishAffinity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem itemAbout = menu.findItem(R.id.about);
        MenuItem itemAppInstructions = menu.findItem(R.id.app_instructions);
        MenuItem itemManage = menu.findItem(R.id.manage);
        //replace these two with actuall buttons

        itemAbout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                // switch to about
                Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(intent);

                return false;
            }
        });

        itemAppInstructions.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                // switch to app instructions

                Intent intent = new Intent(getApplicationContext(), AppInstructionsActivity.class);
                startActivity(intent);

                return false;
            }
        });

        itemManage.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                // switch to manage page
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                return false;
            }
        });
        return true;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (SDK_INT < Build.VERSION_CODES.Q) {
            if (resultCode == Activity.RESULT_OK) {

                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d(LOG_TAG, "entering onActivityResult < android 11");
                            Uri uri = data.getData();
                            String name = FileHandler.queryName(getContentResolver(), uri);

                            AppDatabase appDb = AppDatabase.getInstance(getApplicationContext());

                            AntennaField field = new AntennaField(uri, name);

                            appDb.antennaFieldDao().insert(field);


                            mainFragment.initAntennaList();

                        } catch (Exception e) {
                            //TODO: Improve exception handling
                            e.printStackTrace();
                        }
                    }
                });

            }
        }
    }


    }