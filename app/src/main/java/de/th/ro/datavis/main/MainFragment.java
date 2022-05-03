package de.th.ro.datavis.main;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.th.ro.datavis.ARActivity;
import de.th.ro.datavis.R;
import de.th.ro.datavis.util.fragment.BaseFragment;

public class MainFragment extends BaseFragment {


    public MainFragment(int fragmentContainer) {
        super(fragmentContainer);
    }

    @Override
    public int registerLayoutResource() {
        return R.layout.fragment_main;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findButton();

    }


    private void findButton(){
        Button button = getActivity().findViewById(R.id.btn_ar_main);
        button.setOnClickListener(view -> changeActivity(ARActivity.class));

    }


}
