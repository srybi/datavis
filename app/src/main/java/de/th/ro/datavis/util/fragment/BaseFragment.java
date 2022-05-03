package de.th.ro.datavis.util.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public abstract class BaseFragment extends Fragment {


    // layout file
    private final int layoutRes;
    private final int fragmentContainer;


    /**
     * @param fragmentContainer id of FragmentContainer
     */
    public BaseFragment(int fragmentContainer){
        this.fragmentContainer = fragmentContainer;
        this.layoutRes = registerLayoutResource();
    }

    public abstract @LayoutRes int registerLayoutResource();


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(layoutRes, container, false);
    }


    /**
     * Change to the next Fragment
     * @param fragment the next Fragment
     */
    public void changeFragmentTo(@NonNull Fragment fragment){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.replace(fragmentContainer, fragment, null);
        transaction.commit();

    }



    public void changeActivity(Class<? extends AppCompatActivity> aClass){
        Intent intent = new Intent(getActivity() , aClass);
        getActivity().startActivity(intent);
    }

}
