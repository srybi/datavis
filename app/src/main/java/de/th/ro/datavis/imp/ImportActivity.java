package de.th.ro.datavis.imp;

import android.os.Bundle;

import androidx.annotation.Nullable;

import de.th.ro.datavis.R;
import de.th.ro.datavis.util.activity.BaseActivity;

public class ImportActivity extends BaseActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_import);
        setFragmentContainerView(R.id.importFragment);


        navigateTo(new ImportFragment());

    }


}
