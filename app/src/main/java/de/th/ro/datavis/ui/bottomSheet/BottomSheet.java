package de.th.ro.datavis.ui.bottomSheet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.ToggleButton;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.LinkedList;

import de.th.ro.datavis.R;
import de.th.ro.datavis.interfaces.IObserver;
import de.th.ro.datavis.interfaces.ISubject;
import de.th.ro.datavis.util.enums.InterpretationMode;

/**
 * This java class is the logical representation of modal_bottom_sheet.xml
 */
public class BottomSheet implements ISubject {
    //Log Tag
    final private String TAG = "BottomSheet";

    //Context needed for init
    private Context context;
    private LinkedList<IObserver> observers;

    /**
     * List of all settings. All setting have their actual State and a changing state.
     * - InterpretationMode
     */
    private InterpretationMode mode;
    private InterpretationMode changedMode;


    public InterpretationMode getMode(){
        return this.mode;
    }

    public BottomSheet(Context ctx){
        this.context = ctx;
        observers = new LinkedList<>();
        //default values
        mode = InterpretationMode.Logarithmic;
    }

    /**
     * Method to open the bottom Sheet
     */
    public void showBottomSheetDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.modal_bottom_sheet);

        //get all interactables
        Switch modeSwitch = bottomSheetDialog.findViewById(R.id.switchMode);
        Button applyButton = bottomSheetDialog.findViewById(R.id.apply);
        //init with current setting
        keepSettings(bottomSheetDialog);

        //handler for ModeSwitch
        modeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged: handeling ModeSwitch");
                handleModeSwitch(compoundButton, isChecked);
            }
        });

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
    }

    /**
     * This function checks if the bottom sheet has changed while being open.
     * See comments on how to extend it!
     * @return if anything has changed
     */
    private boolean checkChanges(){
        boolean modeChange = changedMode != mode;
        //boolean oderChange = changedSetting != setting;
        return modeChange; // || oderChange;
    }

    /**
     * This method actually updates the settings;
     */
    private void updateSetting(){
        mode = changedMode;
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

    //Observer Pattern
    @Override
    public void subscribe(IObserver observer) {
        observers.add(observer);
    }
}
