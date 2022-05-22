package de.th.ro.datavis.imp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import de.th.ro.datavis.R;
import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.interfaces.IImportOptions;
import de.th.ro.datavis.main.AntennaFieldAdapter;
import de.th.ro.datavis.models.Antenna;
import de.th.ro.datavis.models.AntennaField;
import de.th.ro.datavis.util.constants.FileRequests;
import de.th.ro.datavis.util.dialog.DialogExistingAntenna;
import de.th.ro.datavis.util.fragment.BaseFragment;

public class ImportFragment extends BaseFragment  implements IImportOptions{



    private Antenna currentAntenna;
    private ListView listViewAntennaFields;


    @Override
    public int registerLayoutResource() {
        return R.layout.fragment_import;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listViewAntennaFields = getActivity().findViewById(R.id.lv_import_antenna_fields);
        initButtons();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        int antennaId = preferences.getInt("ID", 0);


        if (antennaId != 0){
            // Load last used Antenna
            AppDatabase appDb = AppDatabase.getInstance(getActivity().getApplicationContext());
            LiveData<List<Antenna>> antennaList = new MutableLiveData<>(new ArrayList<>());

            antennaList = appDb.antennaDao().getAll();
            antennaList.observe(getActivity(), list -> {
                setCurrentAntenna(list.get(0));
                displayAntennafields();
            });

        }


    }

    private void initButtons(){

        Button btnChooseAntena = getActivity().findViewById(R.id.btn_import_choose_antenna);
        Button btnAddNewAntenna = getActivity().findViewById(R.id.btn_import_add_antenna);
        Button btnAddMetaData = getActivity().findViewById(R.id.btn_import_add_metadata);
        Button btnAddFFS = getActivity().findViewById(R.id.btn_import_add_ffs);

        btnChooseAntena.setOnClickListener( v -> { chooseExistingAntenna(); });
        btnAddNewAntenna.setOnClickListener( v -> { addNewAntenna(); });
        btnAddMetaData.setOnClickListener( v -> { addMetaData(); });
        btnAddFFS.setOnClickListener( v -> { addFFS(); });

    }


    @Override
    public void chooseExistingAntenna() {
        // Antennen zeigen

        AppDatabase appDb = AppDatabase.getInstance(getActivity().getApplicationContext());
        LiveData<List<Antenna>> antennaList = new MutableLiveData<>(new ArrayList<>());

        antennaList = appDb.antennaDao().getAll();

        antennaList.observe(getActivity(), this::displayChooseAntennaDialog);

    }

    @Override
    public void addNewAntenna() {
        openFileDialog_Android9(FileRequests.REQUEST_CODE_ANTENNA, 0);
    }

    @Override
    public void addMetaData() {
        Toast.makeText(getContext(), "WIP ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void addFFS() {

        if (currentAntenna == null){
            Toast.makeText(getContext(), "No Antenna ", Toast.LENGTH_LONG).show();
            return;
        }

        openFileDialog_Android9(FileRequests.REQUEST_CODE_FFS, currentAntenna.id);

    }


    private void openFileDialog_Android9(int requestCode, int antennaId){

        // Workaround since Inten Extras dont work with chooseFile
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        preferences.edit().putInt("ID", antennaId).apply();

        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");

        getActivity().startActivityForResult(chooseFile, requestCode);
    }

    public void setCurrentAntenna(Antenna currentAntenna) {
        this.currentAntenna = currentAntenna;
        updateViewCurrentAntenna(currentAntenna);
    }


    private void updateViewCurrentAntenna(Antenna antenna){
        TextView tv = getActivity().findViewById(R.id.tv_import_headLine_antenna);

        String s = getString(R.string.antenna) + " " + antenna.filename;
        tv.setText( s );
    }

    private void displayChooseAntennaDialog( List<Antenna> antennaList){

        DialogExistingAntenna dialog = new DialogExistingAntenna(getActivity(), "Antenna", R.layout.dialog_import_existing_antenna, antennaList) {
            @Override
            public void handelAntennaItemClick(Antenna antenna) {
                Toast.makeText(getContext(), "Antenna " + antenna.filename, Toast.LENGTH_LONG).show();
                setCurrentAntenna(antenna);
                displayAntennafields();

                this.getDialog().dismiss();
            }
        };

        dialog.showDialog();

    }

    private void displayAntennafields(){

        AppDatabase appDb = AppDatabase.getInstance(getActivity().getApplicationContext());

        LiveData<List<AntennaField>> antennaFields = new MutableLiveData<>(new ArrayList<>());
        antennaFields = appDb.antennaFieldDao().findByAntennaId_Main(currentAntenna.id);

        antennaFields.observe(getActivity(), list -> {
            AntennaFieldAdapter adapter = new AntennaFieldAdapter(getActivity().getApplicationContext(), list);
            listViewAntennaFields.setAdapter(adapter);
        });

    }



    public void persistFFS(AppDatabase appDb, Uri uri, String name){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        int antennaId = preferences.getInt("ID", 1);

        AntennaField antennaField = new AntennaField(uri, name, antennaId);
        appDb.antennaFieldDao().insert(antennaField);

        List<Antenna> antenna = appDb.antennaDao().find_Background(antennaId);
        setCurrentAntenna(antenna.get(0));

        List<AntennaField> fields = appDb.antennaFieldDao().findByAntennaId_BackGround(antennaId);

        AntennaFieldAdapter adapter = new AntennaFieldAdapter(getActivity().getApplicationContext(), fields);
        listViewAntennaFields.setAdapter(adapter);
        listViewAntennaFields.deferNotifyDataSetChanged();
    }


}
