package de.th.ro.datavis.main;

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
import de.th.ro.datavis.models.AntennaField;

public class AntennaFieldAdapter extends ArrayAdapter<AntennaField> {

    public AntennaFieldAdapter(@NonNull Context context, @NonNull List<AntennaField> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        AntennaField field = getItem(position);
         if(convertView == null){
             convertView = LayoutInflater.from(getContext()).inflate(R.layout.antenna_field_list_item, parent, false);
         }
        TextView tvName = (TextView) convertView.findViewById(R.id.field_name);
         tvName.setText(field.uri);

         return convertView;
    }
}
