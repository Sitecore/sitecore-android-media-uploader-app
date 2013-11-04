package net.sitecore.android.mediauploader.ui;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.ui.upload.UploadActivity;

import butterknife.InjectView;
import butterknife.Views;

public class SlidingNavigationFragment extends ListFragment {

    private String[] mNavigationItems = {
            "Upload Media",
            "Media browser",
            "My Uploads",
            "Instances Manager"
    };

    @InjectView(R.id.spinner_instances) Spinner mInstances;

    private ArrayAdapter<String> mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_navigation, container, false);
        Views.inject(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mNavigationItems);
        setListAdapter(mAdapter);


        Pair<String, String> data1 = new Pair<String, String>("My Instance", "/home/hardcoded/text");
        Pair<String, String> data2 = new Pair<String, String>("Other Instance", "/home/other/text");
        ArrayList<Pair<String, String>> items = new ArrayList<Pair<String, String>>();
        items.add(data1);
        items.add(data2);

        mInstances.setAdapter(new InstancesAdapter(getActivity(), items));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (position == 0) {
            startActivity(new Intent(getActivity(), UploadActivity.class));
        } else if (position == 3) {
            startActivity(new Intent(getActivity(), EditInstanceActivity.class));
        }
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
