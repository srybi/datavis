package de.th.ro.datavis.util.dialog;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.fragment.app.FragmentActivity;

import java.util.List;

import de.th.ro.datavis.R;
import de.th.ro.datavis.models.Antenna;
import de.th.ro.datavis.ui.adapter.AntennaAdapter;

public abstract class DialogExistingAntenna extends BaseDialog{


    private final ListView listView;

    public DialogExistingAntenna(FragmentActivity fragmentActivity, String titel, int layout, List<Antenna> antennaList) {
        super(fragmentActivity, titel, layout);

        listView = getDialogView().findViewById(R.id.lv_import_antenna_fields);

        attachAdapter(fragmentActivity, antennaList);
        attachClickListener();
    }


    private void attachAdapter(FragmentActivity fragmentActivity, List<Antenna> antennaList){

        AntennaAdapter adapter = new AntennaAdapter(fragmentActivity, antennaList);
        listView.setAdapter(adapter);


    }

    private void attachClickListener(){

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int clickedPos, long l) {

                Antenna antenna = (Antenna) adapterView.getAdapter().getItem(clickedPos);
                handelAntennaItemClick(antenna);

            }
        });

    }


    public abstract void handelAntennaItemClick(Antenna antenna);

}
