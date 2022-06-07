package de.th.ro.datavis.imp;

import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import java.util.List;

import de.th.ro.datavis.R;
import de.th.ro.datavis.interfaces.IImportOptions;
import de.th.ro.datavis.main.AntennaFieldAdapter;
import de.th.ro.datavis.models.Antenna;
import de.th.ro.datavis.models.AntennaField;
import de.th.ro.datavis.models.MetaData;

public abstract class ImportView implements IImportOptions {


    private ListView listViewAntennaFields;

    private Button btnChooseAntena;
    private Button btnAddNewAntenna;
    private Button btnAddDefaultAntenna;
    private Button btnAddMetaData;
    private Button btnAddFFS;
    private Button btnAddMetaDataFolder;


    TextView tvHeadLine;
    TextView tvMetaIndicator;

    FragmentActivity fragmentActivity;


    public ImportView(FragmentActivity fa, Antenna antenna, List<AntennaField> fieldList, MetaData metaData) {

        fragmentActivity = fa;
        initButtons(fa);
        initAntennaHeadLine(fa, antenna);
        initListView(fa, fieldList);
        initMetaDataIndicator(fa, metaData);

    }

    private void initListView(FragmentActivity fa, List<AntennaField> antennaFieldList){

        listViewAntennaFields = fa.findViewById(R.id.lv_import_antenna_fields);

        if (antennaFieldList == null || antennaFieldList.isEmpty()){
            listViewAntennaFields.setAdapter(null);
            return;
        }

        AntennaFieldAdapter adapter = new AntennaFieldAdapter(fa, antennaFieldList);
        listViewAntennaFields.setAdapter(adapter);

    }
    private void initButtons(FragmentActivity fa){

        btnChooseAntena = fa.findViewById(R.id.btn_import_choose_antenna);
        btnAddNewAntenna = fa.findViewById(R.id.btn_import_add_antenna);
        btnAddDefaultAntenna = fa.findViewById(R.id.btn_add_default);
        btnAddMetaData = fa.findViewById(R.id.btn_import_add_metadata);
        btnAddMetaDataFolder = fa.findViewById(R.id.btn_import_add_metadataFolder);
        btnAddFFS = fa.findViewById(R.id.btn_import_add_ffs);


        btnChooseAntena.setOnClickListener( v -> { chooseExistingAntenna(); });
        btnAddNewAntenna.setOnClickListener( v -> { addNewAntenna(); });
        btnAddDefaultAntenna.setOnClickListener(v -> {addDefaultAntenna();});
        btnAddMetaData.setOnClickListener( v -> { addMetaData(); });
        btnAddMetaDataFolder.setOnClickListener( v -> { addMetaDataFolder(); });
        btnAddFFS.setOnClickListener( v -> { addFFS(); });
    }

    private void initAntennaHeadLine(FragmentActivity fragmentActivity, Antenna antenna){

        if (antenna == null){
            return;
        }
        tvHeadLine = fragmentActivity.findViewById(R.id.tv_import_headLine_antenna);

        String s = "Antenna " + antenna.filename;
        tvHeadLine.setText(s);

    }

    private void initMetaDataIndicator(FragmentActivity fa, MetaData m){

        tvMetaIndicator = fa.findViewById(R.id.tv_metadataIndicator);
        if (m == null){
            return;
        }
        tvMetaIndicator = fragmentActivity.findViewById(R.id.tv_metadataIndicator);
        String s = "MetaData Added";
        tvMetaIndicator.setText(s);

    }


    public void updateData(FragmentActivity fa, Antenna antenna, List<AntennaField> antennaFieldList, MetaData metaData){
        initAntennaHeadLine(fa, antenna);
        initListView(fa, antennaFieldList);
        initMetaDataIndicator(fa, metaData);
    }


    public FragmentActivity getFragmentActivity() {
        return fragmentActivity;
    }
}
