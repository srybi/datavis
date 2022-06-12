package de.th.ro.datavis.ui.settings;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import de.th.ro.datavis.ui.settings.antenna.AntennaFragment;
import de.th.ro.datavis.R;
import de.th.ro.datavis.ui.settings.ffs.FfsFragment;
import de.th.ro.datavis.ui.settings.metadata.MetadataFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_antenna, R.string.tab_ffs, R.string.tab_metadata};
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = AntennaFragment.newInstance(0);
                break;
            case 1:
                fragment = FfsFragment.newInstance(1);
                break;
            default:
                fragment = MetadataFragment.newInstance(2);
                break;
        };

        return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 3;
    }
}