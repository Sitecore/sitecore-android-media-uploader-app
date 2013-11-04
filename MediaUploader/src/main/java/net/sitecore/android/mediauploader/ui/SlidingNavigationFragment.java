package net.sitecore.android.mediauploader.ui;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.ui.upload.UploadActivity;

public class SlidingNavigationFragment extends ListFragment {

    private String[] mNavigationItems = {
            "Upload Media",
            "Media browser",
            "My Uploads",
            "Instances Manager"
    };
    private ArrayAdapter<String> mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_navigation, container, false);

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mNavigationItems);
        setListAdapter(mAdapter);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (position == 0) {
            startActivity(new Intent(getActivity(), UploadActivity.class));
        } else if (position == 3) {
            startActivity(new Intent(getActivity(), EditInstanceActivity.class));
        }

    }
}
