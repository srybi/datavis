package de.th.ro.datavis.imp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import de.th.ro.datavis.models.Antenna;
import de.th.ro.datavis.util.fragment.BaseFragment;

public class ImportFragment extends BaseFragment  implements IImportOptions{


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

        initButtons();
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

        antennaList.observe(getActivity(), list -> {

            if (list != null && list.size() > 0)
            Toast.makeText(getContext(), list.get(0).toString(), Toast.LENGTH_LONG).show();

        });

    }

    @Override
    public void addNewAntenna() {

        Toast.makeText(getContext(), "Click addNewAntenna", Toast.LENGTH_LONG).show();
        openFileDialog_Android9();
    }

    @Override
    public void addMetaData() {

    }

    @Override
    public void addFFS() {

    }


    private void openFileDialog_Android9(){

        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        getActivity().startActivityForResult(chooseFile, 666);
    }


}
