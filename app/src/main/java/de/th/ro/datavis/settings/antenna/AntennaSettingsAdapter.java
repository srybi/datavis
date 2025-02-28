package de.th.ro.datavis.settings.antenna;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.th.ro.datavis.R;
import de.th.ro.datavis.database.AppDatabase;
import de.th.ro.datavis.models.Antenna;

public class AntennaSettingsAdapter extends ArrayAdapter<Antenna> {
    public AntennaSettingsAdapter(@NonNull Context context, @NonNull List<Antenna> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Antenna antenna = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_antenna_settings, parent, false);
        }
        TextView tvId = (TextView) convertView.findViewById(R.id.text_view_antenna_id);
        tvId.setText(String.valueOf("ID: "+antenna.id));
        TextView tvName = (TextView) convertView.findViewById(R.id.text_view_antenna_name);
        tvName.setText(antenna.description);

        ImageButton deleteButton = (ImageButton) convertView.findViewById(R.id.button_antenna_delete);
        View finalConvertView = convertView;
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Do your Yes progress
                                deleteAntenna(antenna);                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //Do your No progress
                                break;
                        }
                    }
                };
                AlertDialog.Builder ab = new AlertDialog.Builder(finalConvertView.getContext());
                ab.setMessage(getContext().getString(R.string.clear_one_confirm) + " " + antenna.description + " ?").setPositiveButton(R.string.yes, dialogClickListener)
                        .setNegativeButton(R.string.no, dialogClickListener).show();

            }
        });

        return convertView;
    }

    private void deleteAntenna(Antenna antenna){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future future = executor.submit(new Runnable() {
            @Override
            public void run() {
                AppDatabase.getInstance(getContext()).antennaDao().delete(antenna);
            }
        });
        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

}
