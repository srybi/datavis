package de.th.ro.datavis.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import de.th.ro.datavis.R;
import de.th.ro.datavis.models.Antenna;

public class BasicAntennaAdapter extends ArrayAdapter<Antenna> {

    Context context;

    public BasicAntennaAdapter(@NonNull Context context, @NonNull List<Antenna> objects) {
        super(context, 0, objects);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Antenna antenna = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_antenna_basic, parent, false);
        }
        TextView tvName = (TextView) convertView.findViewById(R.id.field_name_basic);
        tvName.setText(antenna.description);

        return convertView;
    }

}
