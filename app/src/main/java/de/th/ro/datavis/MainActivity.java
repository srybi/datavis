package de.th.ro.datavis;

import android.os.Bundle;

import androidx.annotation.Nullable;

import de.th.ro.datavis.main.MainFragment;
import de.th.ro.datavis.util.activity.BaseActivity;

public class MainActivity extends BaseActivity{



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setFragmentContainerView(R.id.mainFragment);

        navigateTo(new MainFragment(getFragmentContainerView()));

    }

}