package de.th.ro.datavis.ui.bottomSheet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.slider.Slider;

import java.util.LinkedList;
import java.util.List;

import de.th.ro.datavis.R;
import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.interfaces.IObserver;
import de.th.ro.datavis.interfaces.ISubject;
import de.th.ro.datavis.models.MetaData;
import de.th.ro.datavis.ui.progressBar.ProgressbarHolder;
import de.th.ro.datavis.util.Helper;
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

    private ProgressbarHolder progressbar;


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


    private LiveData<List<MetaData>> sqlQueryMetadata;
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
        tilt = 3;
        changedTilt  = 3;
    }

    /**
     * Method to open the bottom Sheet
     */
    @SuppressLint("DefaultLocale")
    public void showBottomSheetDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.modal_bottom_sheet);

        // init ProgressBar
        progressbar = new ProgressbarHolder(bottomSheetDialog.findViewById(R.id.simpleProgressBar));


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

                progressbar.showProgressBar();

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

    /**
     * Methods to display and update Metadata:
     * readMetaDataFromDB() fetches a List of all available Metadata for a certain AntennaID, frequency and tilt
     * createMetaDataObserver() assigns an Observer to check for updates in the LiveData
     * updateMetaData() on a change goes through the List of available Metadata and for each tries to find a TextView to update
     *      this is done by String matching the [Metadata type] to the [TextView ID]
     */
    private void readMetaDataFromDB(){
        //TODO: Antenna Hardcoded
        sqlQueryMetadata = db.metadataDao().findAll_Background(5,frequency,tilt);
        Log.d(TAG, "sqlQueryMetadata built "+sqlQueryMetadata.toString());
    }

    private void createMetaDataObserver(BottomSheetDialog bsd){
        Observer<List<MetaData>> sqlMetadataObs = changeMetaData -> { updateMetadata(bsd, changeMetaData);};
        sqlQueryMetadata.observe((AppCompatActivity)context, sqlMetadataObs);
    }

    private void updateMetadata(BottomSheetDialog bsd, List<MetaData> changeMetaData){
        for(MetaData m: changeMetaData) {

            int resID = context.getResources().getIdentifier(("meta_" + m.getType()), "id", context.getPackageName());
            try {
                TextView textView = bsd.findViewById(resID);
                textView.setText(m.getValue());
                Log.d(TAG, "TextView " + textView.toString() + " updated to: " + m.getValue());
            } catch (Exception e) {
            }
        }
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
    public Context getContext() {
        return this.context;
    }
}
