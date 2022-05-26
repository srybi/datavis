package de.th.ro.datavis.ui.bottomSheet;

import android.content.Context;
import android.widget.LinearLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import de.th.ro.datavis.R;

public class BottomSheet {
    private Context context;

    public BottomSheet(Context ctx){
        this.context = ctx;
    }

    public void showBottomSheetDialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.modal_bottom_sheet);

        LinearLayout share = bottomSheetDialog.findViewById(R.id.shareLinearLayout);
        LinearLayout download = bottomSheetDialog.findViewById(R.id.download);

        bottomSheetDialog.show();
    }
}
