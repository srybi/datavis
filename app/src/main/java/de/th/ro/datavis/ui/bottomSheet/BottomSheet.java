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

import de.th.ro.datavis.R;
import de.th.ro.datavis.util.enums.InterpretationMode;

/**
 * This java class is the logical representation of modal_bottom_sheet.xml
 */
public class BottomSheet {
    private String TAG = "BottomSheet";
    private Context context;

    InterpretationMode mode;

    public BottomSheet(Context ctx){
        this.context = ctx;
    }

    public void showBottomSheetDialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.modal_bottom_sheet);

        Switch modeSwitch = bottomSheetDialog.findViewById(R.id.switchMode);
        Button applyButton = bottomSheetDialog.findViewById(R.id.apply);

        modeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "onCheckedChanged: checked");
                } else {
                    Log.d(TAG, "onCheckedChanged: uncheck");
                }
            }
        });

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick - applyButton");
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.show();
    }


}
