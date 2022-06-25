package de.th.ro.datavis.imp.adapter;

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

public class MetaDataAdapter extends ArrayAdapter<String> {

    public MetaDataAdapter(@NonNull Context context, @NonNull List<String> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String data = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_antenna_field, parent, false);
        }
        TextView tvName = (TextView) convertView.findViewById(R.id.field_name);
        tvName.setText(data);

        return convertView;
    }

}
