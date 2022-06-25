package de.th.ro.datavis.settings.metadata;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import de.th.ro.datavis.R;
import de.th.ro.datavis.database.AppDatabase;
import de.th.ro.datavis.models.MetaData;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MetadataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MetadataFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    LiveData<List<MetaData>> metadata;

    AppDatabase appDb;

    ListView metadataListView;

    public MetadataFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param index Index.
     * @return A new instance of fragment AntennaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MetadataFragment newInstance(int index) {
        MetadataFragment fragment = new MetadataFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDb = AppDatabase.getInstance(getActivity().getApplicationContext());
        metadata = appDb.metadataDao().getAll();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_metadata, container, false);

        metadataListView = (ListView) rootView.findViewById(R.id.list_view_metadata);
        metadata.observe((AppCompatActivity)rootView.getContext(), list -> {
            MetadataSettingsAdapter adapter = new MetadataSettingsAdapter(rootView.getContext(), list);
            metadataListView.setAdapter(adapter);
        });

        return rootView;
    }
}