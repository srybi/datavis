package de.th.ro.datavis.about;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import de.th.ro.datavis.R;
import de.th.ro.datavis.util.BaseActivity;

public class AboutActivity extends BaseActivity {


    private AboutFragment aboutFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);
        setFragmentContainerView(R.id.aboutFragment);

        Toolbar toolbar = findViewById(R.id.about_toolbar);
        setSupportActionBar(toolbar);


        aboutFragment = new AboutFragment();
        navigateTo(aboutFragment);
    }
}
