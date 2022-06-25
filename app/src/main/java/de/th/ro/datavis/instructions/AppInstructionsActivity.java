package de.th.ro.datavis.instructions;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import de.th.ro.datavis.R;
import de.th.ro.datavis.util.BaseActivity;

public class AppInstructionsActivity extends BaseActivity {


    private AppInstructionsFragment appInstructionsFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_app_instructions);
        setFragmentContainerView(R.id.appInstructionsFragment);

        Toolbar toolbar = findViewById(R.id.app_instructions_toolbar);
        setSupportActionBar(toolbar);


        appInstructionsFragment = new AppInstructionsFragment();
        navigateTo(appInstructionsFragment);

    }
}
