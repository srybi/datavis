package de.th.ro.datavis.util.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public abstract class BaseActivity extends AppCompatActivity {

    private int fragmentContainerView;

    public void setFragmentContainerView(int fragmentContainerView){
        this.fragmentContainerView = fragmentContainerView;
    }


    /**
     * Navigation Methode to switch between Fragments
     * @param fragment the fragment to switch to
     */
    public void navigateTo(Fragment fragment){

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);

        transaction.replace(fragmentContainerView, fragment, null);
        transaction.commit();

    }


    public int getFragmentContainerView() {
        return fragmentContainerView;
    }

}
