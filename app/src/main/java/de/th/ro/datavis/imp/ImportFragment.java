package de.th.ro.datavis.imp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import de.th.ro.datavis.R;
import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.models.Antenna;
import de.th.ro.datavis.models.AntennaField;
import de.th.ro.datavis.util.constants.FileRequests;
import de.th.ro.datavis.util.dialog.DialogExistingAntenna;

public class ImportFragment extends Fragment {



    private Antenna currentAntenna;
    private List<AntennaField> currentAntennaFieldList;

    private ImportView importView;

    private SharedPreferences preferences;


    public ImportFragment() {
        super(R.layout.fragment_import);
    }

    public ImportFragment(Antenna currentAntenna, List<AntennaField> antennaFieldList) {
        super(R.layout.fragment_import);
        this.currentAntenna = currentAntenna;
        this.currentAntennaFieldList = antennaFieldList;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setImportView();

    }

    public void setImportView(){

        importView = new ImportView(getActivity(), currentAntenna, currentAntennaFieldList) {
            @Override
            public void chooseExistingAntenna() {
                // Antennen zeigen
                AppDatabase appDb = AppDatabase.getInstance(getActivity().getApplicationContext());
                LiveData<List<Antenna>> antennaList = new MutableLiveData<>(new ArrayList<>());

                antennaList = appDb.antennaDao().getAll();

                antennaList.observe(getActivity(), list -> {

                    displayChooseAntennaDialog(list);

                });
            }

            @Override
            public void addNewAntenna() {
                openFileDialog_Android9(FileRequests.REQUEST_CODE_ANTENNA, 0);
            }

            @Override
            public void addMetaData() {

            }

            @Override
            public void addFFS() {
                if (currentAntenna == null){
                    Toast.makeText(getContext(), "No Antenna ", Toast.LENGTH_LONG).show();
                    return;
                }

                openFileDialog_Android9(FileRequests.REQUEST_CODE_FFS, currentAntenna.id);

            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private void openFileDialog_Android9(int requestCode, int antennaId){

        // Workaround since Inten Extras dont work with chooseFile
        //preferences.edit().putInt("ID", antennaId).apply();

        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");

        getActivity().startActivityForResult(chooseFile, requestCode);
    }

    public void setCurrentAntenna(Antenna currentAntenna) {
        this.currentAntenna = currentAntenna;
    }


    private void displayChooseAntennaDialog( List<Antenna> antennaList){

        DialogExistingAntenna dialog = new DialogExistingAntenna(getActivity(), "Antenna", R.layout.dialog_import_existing_antenna, antennaList) {
            @Override
            public void handelAntennaItemClick(Antenna antenna) {
                Toast.makeText(getContext(), "Antenna " + antenna.filename, Toast.LENGTH_LONG).show();
                setCurrentAntenna(antenna);
                displayAntennas();

                this.getDialog().dismiss();
            }
        };

        dialog.showDialog();

    }

    private void displayAntennas(){

        AppDatabase appDb = AppDatabase.getInstance(getActivity().getApplicationContext());

        LiveData<List<AntennaField>> antennaFields = new MutableLiveData<>(new ArrayList<>());
        antennaFields = appDb.antennaFieldDao().findByAntennaId_Main(currentAntenna.id);

        antennaFields.observe(getActivity(), list -> {
            //importView.setAntennaFieldListViewItems(getActivity(), list);
        });

    }


}
