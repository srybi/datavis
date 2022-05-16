package de.th.ro.datavis.main;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import de.th.ro.datavis.ARActivity;
import de.th.ro.datavis.R;
import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.interfaces.IInterpreter;
import de.th.ro.datavis.interpreter.ffs.FFSInterpreter;
import de.th.ro.datavis.models.AntennaField;
import de.th.ro.datavis.util.filehandling.FileHandler;
import de.th.ro.datavis.util.fragment.BaseFragment;

import static android.os.Build.VERSION.SDK_INT;

public class MainFragment extends BaseFragment {

    private IInterpreter ffsInterpreter;
    private AppDatabase appDb;

    private ListView listView;

    private LiveData<List<AntennaField>> antennaFields = new MutableLiveData<>(new ArrayList<>());

    private FragmentActivity context;

    public MainFragment() {
    }

    public MainFragment(int fragmentContainer) {
        super(fragmentContainer);
    }

    @Override
    public int registerLayoutResource() {
        return R.layout.fragment_main;
    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Log.d("datavis", "Used SDK " + SDK_INT);
        if (SDK_INT >= Build.VERSION_CODES.R) {
            Log.d("myz", ""+SDK_INT);
            if (!Environment.isExternalStorageManager()) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 1);//permission request code is just an int
            }



            Intent i = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivity(i);

        }else {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        context = getActivity();
        ffsInterpreter = new FFSInterpreter();

        initAntennaList();
        findButton();
        findTriggerButton();

    }

    private void findListView() {
        listView = getActivity().findViewById(R.id.list_antenna_fields);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity().getApplicationContext(), ARActivity.class);
                AntennaField item = antennaFields.getValue().get(i);
                intent.putExtra("fileUri", item.uri);
                intent.putExtra("interpretationMode", "Linear");
                getActivity().startActivity(intent);

                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity().getApplicationContext(), ARActivity.class);
                AntennaField item = antennaFields.getValue().get(i);
                intent.putExtra("fileUri", item.uri);
                intent.putExtra("interpretationMode", "Logarithmic");
                getActivity().startActivity(intent);
            }
        });
    }

    public void initAntennaList(){
        appDb = AppDatabase.getInstance(getActivity().getApplicationContext());
        antennaFields = appDb.antennaFieldDao().getAll();

        findListView();

        antennaFields.observe(getActivity(), list -> {
            AntennaFieldAdapter adapter = new AntennaFieldAdapter(context.getApplicationContext(), list);
            listView.setAdapter(adapter);
        });

    }


    private void findButton(){
        Button button = getActivity().findViewById(R.id.btn_ar_main);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(), ARActivity.class);
                getActivity().startActivity(intent);
            }
        });

    }

    private void findTriggerButton(){
        Button button = getActivity().findViewById(R.id.btn_ffs_interpret);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileDialog(view);
            }
        });

    }



    public void openFileDialog(View view){

        if (SDK_INT >= Build.VERSION_CODES.Q) {
            openFileDialog_Android11(view);
        } else {
            openFileDialog_Android9(view);
        }

    }

    private void openFileDialog_Android9(View view){

        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        getActivity().startActivityForResult(chooseFile, 666);
    }

    private void openFileDialog_Android11(View view){
        Intent data = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        data.setType("*/*");
        data.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        data = Intent.createChooser(data, "Choose one .ffs file");
        activityResultLauncher.launch(data);
    }

    // use ab Android 10
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();


                        Executors.newSingleThreadExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Uri uri = data.getData();
                                    String name = FileHandler.queryName(
                                            getActivity()
                                                    .getContentResolver(), uri);
                                    appDb.antennaFieldDao().insert(new AntennaField(uri, name));
                                }catch(Exception e){
                                    //TODO: Improve exception handling
                                    e.printStackTrace();
                                }
                            }
                        });

                    }
                }
            }
    );


}
