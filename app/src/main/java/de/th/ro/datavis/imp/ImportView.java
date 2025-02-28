package de.th.ro.datavis.imp;

import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.List;

import de.th.ro.datavis.R;
import de.th.ro.datavis.imp.adapter.AntennaFieldAdapter;
import de.th.ro.datavis.imp.adapter.MetaDataAdapter;
import de.th.ro.datavis.models.Antenna;
import de.th.ro.datavis.models.AntennaField;
import de.th.ro.datavis.ui.progressBar.ProgressbarHolder;

public abstract class ImportView implements IImportOptions {


    private ListView ffsList;

    private ProgressbarHolder progressBar;

    private Button btnAddImportAntenna;
    private Button btnAddDefaultAntenna;
    private Button btnImportFolder;
    private Button btnConfirm;

    private EditText configName;


    TextView tvHeadLine;

    FragmentActivity fragmentActivity;


    public ImportView(FragmentActivity fa, Antenna antenna, List<AntennaField> fieldList, List<String> metaData) {

        fragmentActivity = fa;
        initButtons(fa);
        initConfigName(fa, antenna);
        initAntennaHeadLine(fa, antenna);
        initFFSScrollable(fa, fieldList);
        initMetaDataScrollable(fa, metaData);
        initProgressBar(fa);
    }

    private void initFFSScrollable(FragmentActivity fa, List<AntennaField> antennaFieldList){
        ffsList = fa.findViewById(R.id.lv_import_antenna_fields);
        Log.d("ImportActivity", "initFFSScrollable: " + ffsList);

        if (antennaFieldList == null || antennaFieldList.isEmpty()){
            ffsList.setAdapter(null);
            return;
        }

        AntennaFieldAdapter adapter = new AntennaFieldAdapter(fa, antennaFieldList);
        ffsList.setAdapter(adapter);
        ViewCompat.setNestedScrollingEnabled(ffsList, true);
    }

    private void initMetaDataScrollable(FragmentActivity fa, List<String> metaDataList){
        ffsList = fa.findViewById(R.id.lv_import_meta_data);
        Log.d("ImportActivity", "initFFSScrollable: " + ffsList);

        if (metaDataList == null || metaDataList.isEmpty()){
            ffsList.setAdapter(null);
            return;
        }

        MetaDataAdapter adapter = new MetaDataAdapter(fa, metaDataList);
        ffsList.setAdapter(adapter);
        ViewCompat.setNestedScrollingEnabled(ffsList, true);
    }

    private void initButtons(FragmentActivity fa){

        configName = fa.findViewById(R.id.configName);
        btnAddImportAntenna = fa.findViewById(R.id.btn_import_antenna);
        btnAddDefaultAntenna = fa.findViewById(R.id.btn_add_default);
        btnImportFolder = fa.findViewById(R.id.btn_import_Folder);
        btnConfirm = fa.findViewById(R.id.btn_confirm);

        configName.addTextChangedListener(descriptionChanged());
        btnAddImportAntenna.setOnClickListener(v -> { addImportAntenna(); });
        btnAddDefaultAntenna.setOnClickListener(v -> {addDefaultAntenna();});
        btnImportFolder.setOnClickListener(v -> { addFolder(); });
        btnConfirm.setOnClickListener(v -> {confirmImport(); });
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


    public void showProgressBar(){
        progressBar.showProgressBar();
    }

    public void disableAllButtons(){
        btnAddImportAntenna.setEnabled(false);
        btnAddDefaultAntenna.setEnabled(false);
        btnImportFolder.setEnabled(false);
        btnConfirm.setEnabled(false);
        configName.setEnabled(false);
    }

    public FragmentActivity getFragmentActivity() {
        return fragmentActivity;
    }
}
