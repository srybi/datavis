package de.th.ro.datavis.util.dialog;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.fragment.app.FragmentActivity;

public class BaseDialog {

    private final AlertDialog dialog;
    private final View dialogView;


    public BaseDialog(FragmentActivity fragmentActivity, String titel, @LayoutRes int layout) {

        dialogView = LayoutInflater.from(fragmentActivity).inflate(layout, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(fragmentActivity);
        builder.setTitle(titel);

        builder.setView(dialogView);

        dialog = builder.create();

    }

    public void addClickListenerToButton(@IdRes int buttonRes, View.OnClickListener listener ){

        Button button = dialogView.findViewById(buttonRes);
        button.setOnClickListener(listener);

    }

    public View getDialogView() {
        return dialogView;
    }

    public AlertDialog getDialog() {
        return dialog;
    }

    public void showDialog(){
        dialog.show();
    }

}