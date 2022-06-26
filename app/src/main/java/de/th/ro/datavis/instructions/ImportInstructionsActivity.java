package de.th.ro.datavis.instructions;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import de.th.ro.datavis.R;
import de.th.ro.datavis.util.BaseActivity;

public class ImportInstructionsActivity extends BaseActivity {


    private ImportInstructionsFragment importInstructionsFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_import_instructions);
        setFragmentContainerView(R.id.importInstructionsFragment);

        Toolbar toolbar = findViewById(R.id.import_instructions_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        importInstructionsFragment = new ImportInstructionsFragment();
        navigateTo(importInstructionsFragment);

    }
}
