package de.th.ro.datavis.ui.bottomSheet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.icu.text.CaseMap;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.slider.Slider;

import java.io.DataOutput;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.th.ro.datavis.R;
import de.th.ro.datavis.db.daos.MetadataDao;
import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.interfaces.IObserver;
import de.th.ro.datavis.interfaces.ISubject;
import de.th.ro.datavis.interpreter.ffs.FFSService;
import de.th.ro.datavis.models.Antenna;
import de.th.ro.datavis.models.MetaData;
import de.th.ro.datavis.util.Helper;
import de.th.ro.datavis.util.activity.BaseActivity;
import de.th.ro.datavis.util.enums.InterpretationMode;

/**
 * This java class is the logical representation of modal_bottom_sheet.xml
 */
public class BottomSheet implements ISubject{
    //Log Tag
    final private String TAG = "BottomSheet";

    //Context needed for init
    private Context context;
    private LinkedList<IObserver> observers;

    private Switch modeSwitch;
    private Slider frequencySlider;
    private Button applyButton;


    private List<Double> frequencies;

    /**
     * List of all settings. All setting have their actual State and a changing state.
     * - InterpretationMode
     */
    private InterpretationMode mode;
    private InterpretationMode changedMode;



    private final AppDatabase db;

    private double frequency;
    private double changedFrequency;
    private int tilt;
    private int changedTilt;

    private LiveData<MetaData> Nullfill_dB, Squint_deg, Tilt_deg, TiltDeviation_deg = new MutableLiveData<>();
    MediatorLiveData liveDataMerger = new MediatorLiveData<>();




    public InterpretationMode getMode(){
        return this.mode;
    }

    public BottomSheet(Context ctx, List<Double> frequencies){
        this.context = ctx;
        observers = new LinkedList<>();
        //default values
        mode = InterpretationMode.Logarithmic;
        changedMode = InterpretationMode.Logarithmic;
        this.frequencies = frequencies;
        this.frequency = frequencies.get(0);
        this.changedFrequency = frequencies.get(0);

        db = AppDatabase.getInstance(ctx);
        tilt = 2;
        changedTilt  = 2;
    }

    /**
     * Method to open the bottom Sheet
     */
    @SuppressLint("DefaultLocale")
    public void showBottomSheetDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.modal_bottom_sheet);


        //get all interactables
        Switch modeSwitch = bottomSheetDialog.findViewById(R.id.switchMode);
        Button applyButton = bottomSheetDialog.findViewById(R.id.apply);

        Log.d(TAG, Double.toString(frequency));

        //fetches Metadata
        try {
            readMetaDataFromDB();
        } catch (Exception e) { e.printStackTrace();}

        //Create Observer for Metadata

        createMetaDataObserver(bottomSheetDialog);

        modeSwitch = bottomSheetDialog.findViewById(R.id.switchMode);
        frequencySlider = bottomSheetDialog.findViewById(R.id.sliderFrequency);
        applyButton = bottomSheetDialog.findViewById(R.id.apply);
        //init with current setting
        if(frequencies.size() > 1) {
            float from = frequencies.get(0).floatValue();
            float to = frequencies.get(frequencies.size() - 1).floatValue();

            frequencySlider.setValueFrom(from);
            frequencySlider.setValueTo(to);

            //dynamically calculate step size for slider
            double stepSize = (float)(frequencies.get(1) - frequencies.get(0));

            double truncatedDouble = Helper.scaleDouble(3, stepSize);

            frequencySlider.setStepSize((float)truncatedDouble);

        }else{
            frequencySlider.setEnabled(false);
        }
        keepSettings(bottomSheetDialog);

        //handler for ModeSwitch
        modeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged: handeling ModeSwitch");
                handleModeSwitch(compoundButton, isChecked);
            }
        });
        if(frequencies.size() > 1) {
            frequencySlider.addOnChangeListener(new Slider.OnChangeListener() {
                @Override
                public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                    Log.d(TAG, "onValueChange: handeling FrequencySlider");
                    handleFrequencySlider(value);

                    //With AntennaID possible to live update Metadata:
                    //updateMetadataViews();
                }
            });
        }

        //handler for applyButton
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick - applyButton");

                if(checkChanges()){ //check if anything has changed
                    updateSetting(); //if so update settings
                    for(IObserver o : observers){
                        o.update();
                    }
                }
                bottomSheetDialog.dismiss(); //close bottom sheet
            }
        });


        bottomSheetDialog.show(); //open bottom sheet
    }

    /**
     * This function restores the bottom sheet with its currently used settings.
     * @param btmSheetDialog
     */
    private void keepSettings(BottomSheetDialog btmSheetDialog){
        //mode switch
        Switch modeSwitch = btmSheetDialog.findViewById(R.id.switchMode);
        if(mode == InterpretationMode.Linear){
            modeSwitch.setChecked(true);
        }


        //frequency slider
        if(frequencies.size() > 1) {
            frequencySlider.setValue((float) frequency);
        }

        //tilt slider
    }


    //An update method per field
    private void updateNullfill_dBView(BottomSheetDialog b, MetaData changeMetaData){
        TextView nullfill_dB = b.findViewById(R.id.meta_Nullfill_dB);
        try {
            nullfill_dB.setText(changeMetaData.getValue());
        } catch (Exception e){ Log.d(TAG, "couldn't find nullfill"); }
    }
    private void updateSquint_degView(BottomSheetDialog b, MetaData changeMetaData){
        TextView squint_deg = b.findViewById(R.id.meta_Squint_deg);
        try {
            squint_deg.setText(changeMetaData.getValue());
        } catch (Exception e){ Log.d(TAG, "couldn't find squint"); }
    }
    private void updateTilt_degView(BottomSheetDialog b, MetaData changeMetaData){
        TextView tilt_deg = b.findViewById(R.id.meta_Tilt_deg);
        try {
            tilt_deg.setText(changeMetaData.getValue());
        } catch (Exception e){ Log.d(TAG, "couldn't find Tilt"); }
    }
    private void updateTiltDeviation_degView(BottomSheetDialog b, MetaData changeMetaData){
        TextView tiltDeviation_deg = b.findViewById(R.id.meta_TiltDeviation_deg);
        try {
            tiltDeviation_deg.setText(changeMetaData.getValue());
        } catch (Exception e){ Log.d(TAG, "couldn't find tiltdev"); }
    }


    /**
     * This function checks if the bottom sheet has changed while being open.
     * See comments on how to extend it!
     * @return if anything has changed
     */
    private boolean checkChanges(){
        //boolean oderChange = changedSetting != setting;

        boolean modeChange = changedMode != mode;
        boolean frequencyChange = changedFrequency != frequency;
        boolean tiltChange = changedTilt != tilt;
        return modeChange || frequencyChange;
    }

    /**
     * This method actually updates the settings;
     */
    private void updateSetting(){
        mode = changedMode;
        frequency = Helper.scaleDouble(3,changedFrequency);
        tilt = changedTilt;
    }

    private void handleModeSwitch(CompoundButton switchBtn, boolean isChecked){
        if(isChecked){
            Log.d(TAG, "handleModeSwitch: checking");
            changedMode = InterpretationMode.Linear;
        }else{
            changedMode = InterpretationMode.Logarithmic;
        }
        switchBtn.setChecked(isChecked);
    }

    private void readMetaDataFromDB(){
        //TODO: AntennaID hardcoded
        Nullfill_dB = db.metadataDao().findByMetadata_Background(1, frequency, tilt, "Nullfill_dB");
        Squint_deg = db.metadataDao().findByMetadata_Background(1, frequency, tilt, "Squint_deg");
        Tilt_deg = db.metadataDao().findByMetadata_Background(1, frequency, tilt, "Tilt_deg");
        TiltDeviation_deg = db.metadataDao().findByMetadata_Background(1, frequency, tilt, "TiltDeviation_deg");
    }

    private void createMetaDataObserver(BottomSheetDialog bsd){
        //a single observer for every field
        Observer<MetaData> nullFillObs = changeMetaData -> { updateNullfill_dBView(bsd, changeMetaData);};
        Nullfill_dB.observe((AppCompatActivity)context, nullFillObs);
        Observer<MetaData> squintObs = changeMetaData -> { updateSquint_degView(bsd, changeMetaData);};
        Squint_deg.observe((AppCompatActivity)context, squintObs);
        Observer<MetaData> tiltObs = changeMetaData -> { updateTilt_degView(bsd, changeMetaData);};
        Tilt_deg.observe((AppCompatActivity)context, tiltObs);
        Observer<MetaData> tiltDevOps = changeMetaData -> { updateTiltDeviation_degView(bsd, changeMetaData);};
        TiltDeviation_deg.observe((AppCompatActivity)context, tiltDevOps);
    }

    private void handleFrequencySlider(float value) {
        changedFrequency = value;
    }

    private void handleTiltSlider(int value) {
        changedTilt = value;
    }

    //Observer Pattern
    @Override
    public void subscribe(IObserver observer) {
        observers.add(observer);
    }

    public double getFrequency() {
        return this.frequency;
    }
    public int getTilt() {
        return this.tilt;
    }
}
