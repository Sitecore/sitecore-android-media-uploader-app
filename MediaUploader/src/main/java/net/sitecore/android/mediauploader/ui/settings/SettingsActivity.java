package net.sitecore.android.mediauploader.ui.settings;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.ui.instancemanager.InstancesListAdapter;

public class SettingsActivity extends Activity implements LoaderCallbacks<Cursor> {
    private ListView mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mList = (ListView) findViewById(R.id.list_instances);
        mList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        Button footerView = new Button(this);
        footerView.setText(getString(R.string.text_add_new_instance));
        footerView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(SettingsActivity.this, EditInstanceActivity.class));
            }
        });

        mList.addFooterView(footerView);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getLoaderManager().destroyLoader(0);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, Instances.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0) {
            findViewById(R.id.empty_text).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.empty_text).setVisibility(View.GONE);
        }
        InstancesListAdapter adapter = new InstancesListAdapter(this, data);
        mList.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
