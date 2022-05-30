package de.th.ro.datavis.ui.settings.metadata;

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
import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.models.AtomicField;
import de.th.ro.datavis.models.MetaData;

public class MetadataSettingsAdapter extends ArrayAdapter<MetaData> {
    public MetadataSettingsAdapter(@NonNull Context context, @NonNull List<MetaData> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        MetaData data = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.metadata_settings_list_item, parent, false);
        }
        TextView tvAntId = (TextView) convertView.findViewById(R.id.text_view_metadata_antenna_id);
        tvAntId.setText(String.valueOf(data.antennaID));
        TextView tvFreq = (TextView) convertView.findViewById(R.id.text_view_metadata_freq);
        tvFreq.setText(String.valueOf(data.freq));
        TextView tvTilt = (TextView) convertView.findViewById(R.id.text_view_metadata_tilt);
        tvTilt.setText(String.valueOf(data.tilt));
        TextView tvType = (TextView) convertView.findViewById(R.id.text_view_metadata_type);
        tvType.setText(String.valueOf(data.type));
        TextView tvValue = (TextView) convertView.findViewById(R.id.text_view_metadata_value);
        tvValue.setText(data.value);

        ImageButton deleteButton = (ImageButton) convertView.findViewById(R.id.button_metadata_delete);
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
                                deleteMetadata(data);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //Do your No progress
                                break;
                        }
                    }
                };
                AlertDialog.Builder ab = new AlertDialog.Builder(finalConvertView.getContext());
                String metadataDescription = "Antenna ID: " +data.antennaID + "; Freq: " + data.freq + "; Tilt: " + data.tilt + "; Type: " + data.type + "; Value: " + data.value;
                ab.setMessage(getContext().getString(R.string.clear_one_confirm) + " " + metadataDescription + " ?").setPositiveButton(R.string.yes, dialogClickListener)
                        .setNegativeButton(R.string.no, dialogClickListener).show();

            }
        });

        return convertView;
    }

    private void deleteMetadata(MetaData data){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future future = executor.submit(new Runnable() {
            @Override
            public void run() {
                AppDatabase.getInstance(getContext()).metadataDao().delete(data);
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
