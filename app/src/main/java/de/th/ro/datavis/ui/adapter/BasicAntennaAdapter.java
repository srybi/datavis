package de.th.ro.datavis.ui.adapter;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.app.AlertDialog;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.th.ro.datavis.ARActivity;
import de.th.ro.datavis.MainActivity;
import de.th.ro.datavis.R;
import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.imp.ImportActivity;
import de.th.ro.datavis.models.Antenna;
import de.th.ro.datavis.util.constants.IntentConst;

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
