package net.sitecore.android.mediauploader.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.ui.upload.UploadActivity;

import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Views;

public class SlidingNavigationFragment extends Fragment {

    public static final int POSITION_UPLOAD = 0;
    public static final int POSITION_MEDIA_BROWSER = 1;
    public static final int POSITION_MY_UPLOADS = 2;
    public static final int POSITION_INSTANCES = 3;

    interface Callbacks {

        public void onMediaBrowserSelected();
        public void onMyUploadsSelected();
        public void onInstanceManagerSelected();

        public void onSelectionDone();

    }

    @InjectView(R.id.spinner_instances) Spinner mInstances;

    private Callbacks mCallbacks;
    private int mCurrentPosition = POSITION_MEDIA_BROWSER;
    private ArrayAdapter<String> mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_navigation, container, false);
        Views.inject(this, v);
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Callbacks) mCallbacks = (Callbacks) activity;
        else throw new IllegalArgumentException("Activity must implement SlidingNavigationFragment.Callbacks");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Pair<String, String> data1 = new Pair<String, String>("My Instance", "/home/hardcoded/text");
        Pair<String, String> data2 = new Pair<String, String>("Other Instance", "/home/other/text");
        ArrayList<Pair<String, String>> items = new ArrayList<Pair<String, String>>();
        items.add(data1);
        items.add(data2);

        mInstances.setAdapter(new InstancesAdapter(getActivity(), items));
    }

    public boolean isMediaBrowserSelected() {
        return mCurrentPosition == POSITION_MEDIA_BROWSER;
    }

    @OnClick(R.id.button_media_browser)
    public void onMediaBrowserClick() {
        if (mCurrentPosition != POSITION_MEDIA_BROWSER) {
            mCallbacks.onMediaBrowserSelected();
            mCurrentPosition = POSITION_MEDIA_BROWSER;
        }
        mCallbacks.onSelectionDone();
    }

    @OnClick(R.id.button_my_uploads)
    public void onMyUploadsClick() {
        if (mCurrentPosition != POSITION_MY_UPLOADS) {
            mCallbacks.onMyUploadsSelected();
            mCurrentPosition = POSITION_MY_UPLOADS;
        }
        mCallbacks.onSelectionDone();
    }

    @OnClick(R.id.button_instances_manager)
    public void onInstancesManagerClick() {
        if (mCurrentPosition != POSITION_INSTANCES) {
            mCallbacks.onInstanceManagerSelected();
            mCurrentPosition = POSITION_INSTANCES;
        }
        mCallbacks.onSelectionDone();
    }

    static class InstancesAdapter extends ArrayAdapter<Pair<String, String>> {

        public InstancesAdapter(Context context, List<Pair<String, String>> objects) {
            super(context, R.layout.layout_instance_info, objects);
            setDropDownViewResource(R.layout.layout_instance_info);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_instance_info, parent, false);
                ViewHolder holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();

            Pair<String, String> data = getItem(position);
            holder.title.setText(data.first);
            holder.description.setText(data.second);

            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
        }

        static class ViewHolder {
            @InjectView(R.id.text_title) TextView title;
            @InjectView(R.id.text_description) TextView description;

            ViewHolder(View v) {
                Views.inject(this, v);
            }
        }
    }
}
