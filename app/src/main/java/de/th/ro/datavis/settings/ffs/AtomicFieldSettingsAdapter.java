package de.th.ro.datavis.settings.ffs;

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
import de.th.ro.datavis.models.AtomicField;

public class AtomicFieldSettingsAdapter extends ArrayAdapter<AtomicField> {
    public AtomicFieldSettingsAdapter(@NonNull Context context, @NonNull List<AtomicField> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        AtomicField field = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_atomicfield_settings, parent, false);
        }

        TextView tvAntId = (TextView) convertView.findViewById(R.id.text_view_field_antenna_id);
        tvAntId.setText("Antenna Id: "+field.antennaId);
        TextView tvFreq = (TextView) convertView.findViewById(R.id.text_view_field_freq);
        tvFreq.setText("Freq: " +field.frequency);
        TextView tvTilt = (TextView) convertView.findViewById(R.id.text_view_field_tilt);
        tvTilt.setText("Tilt: "+field.tilt);
        TextView tvIntMode = (TextView) convertView.findViewById(R.id.text_view_field_int_mode);
        tvIntMode.setText(String.valueOf(field.interpretationMode));

        ImageButton deleteButton = (ImageButton) convertView.findViewById(R.id.button_field_delete);
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
                                deleteField(field);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //Do your No progress
                                break;
                        }
                    }
                };
                AlertDialog.Builder ab = new AlertDialog.Builder(finalConvertView.getContext());
                String fieldDescription = "Antenna ID: " +field.antennaId + "; Freq: " + field.frequency + "; Tilt: " + field.tilt + "; Interpretationmode: " + field.interpretationMode;
                ab.setMessage(getContext().getString(R.string.clear_one_confirm) + " " + fieldDescription + " ?").setPositiveButton(R.string.yes, dialogClickListener)
                        .setNegativeButton(R.string.no, dialogClickListener).show();

            }
        });

        return convertView;
    }

    private void deleteField(AtomicField field){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future future = executor.submit(new Runnable() {
            @Override
            public void run() {
                AppDatabase.getInstance(getContext()).atomicFieldDao().delete(field);
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
