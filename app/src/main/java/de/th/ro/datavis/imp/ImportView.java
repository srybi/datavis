package de.th.ro.datavis.imp;

import android.widget.Button;
import android.widget.EditText;
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
import de.th.ro.datavis.ui.progressBar.ProgressbarHolder;

public abstract class ImportView implements IImportOptions {


    private ListView listViewAntennaFields;

    private ProgressbarHolder progressBar;

    private Button btnAddImportAntenna;
    private Button btnAddDefaultAntenna;
    private Button btnAddFFS;
    private Button btnAddMetaDataFolder;

    private EditText configName;


    TextView tvHeadLine;
    TextView tvMetaIndicator;

    FragmentActivity fragmentActivity;


    public ImportView(FragmentActivity fa, Antenna antenna, List<AntennaField> fieldList, MetaData metaData) {

        fragmentActivity = fa;
        initButtons(fa);
        initConfigName(fa, antenna);
        initAntennaHeadLine(fa, antenna);
        initListView(fa, fieldList);
        initProgressBar(fa);
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

        configName = fa.findViewById(R.id.configName);
        btnAddImportAntenna = fa.findViewById(R.id.btn_import_antenna);
        btnAddDefaultAntenna = fa.findViewById(R.id.btn_add_default);
        btnAddMetaDataFolder = fa.findViewById(R.id.btn_import_add_metadataFolder);
        btnAddFFS = fa.findViewById(R.id.btn_import_add_ffs);

        configName.addTextChangedListener(descriptionChanged());
        btnAddImportAntenna.setOnClickListener(v -> { addImportAntenna(); });
        btnAddDefaultAntenna.setOnClickListener(v -> {addDefaultAntenna();});
        btnAddMetaDataFolder.setOnClickListener( v -> { addMetaDataFolder(); });
        btnAddFFS.setOnClickListener( v -> { addFFS(); });
    }

    private void initConfigName(FragmentActivity fragmentActivity, Antenna antenna){
        configName = fragmentActivity.findViewById(R.id.configName);
        if (antenna == null){
            return;
        }
        configName.setText(antenna.description);
    }

    private void initAntennaHeadLine(FragmentActivity fragmentActivity, Antenna antenna){

        if (antenna == null){
            return;
        }
        tvHeadLine = fragmentActivity.findViewById(R.id.tv_import_headLine_antenna);

        String s = "Antenna " + antenna.filename;
        tvHeadLine.setText(s);
    }

    private void initProgressBar(FragmentActivity fa){
        progressBar = new ProgressbarHolder(fa.findViewById(R.id.simpleProgressBar));
    }

    public void hideProgressBar(){
        progressBar.hideProgressBar();
    }

    public void showProgressBar(){
        progressBar.showProgressBar();
    }


    public void updateData(FragmentActivity fa, Antenna antenna, List<AntennaField> antennaFieldList, MetaData metaData){
        initAntennaHeadLine(fa, antenna);
        initListView(fa, antennaFieldList);
    }


    public FragmentActivity getFragmentActivity() {
        return fragmentActivity;
    }
}
