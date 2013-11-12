package net.sitecore.android.mediauploader.ui.instancemanager;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances.Query;
import net.sitecore.android.mediauploader.ui.ScFragment;
import net.sitecore.android.mediauploader.util.Prefs;

import butterknife.InjectView;
import butterknife.Views;

public class InstancesListFragment extends ScFragment implements LoaderCallbacks<Cursor>, OnItemClickListener, OnItemLongClickListener {
    private InstancesListAdapter mListAdapter;
    private Prefs mPrefs;

    @InjectView(android.R.id.list) ListView mListView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListAdapter = new InstancesListAdapter(getActivity());
        mListView.setAdapter(mListAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        Views.inject(this, view);

        mPrefs = Prefs.from(getActivity());

        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        getLoaderManager().initLoader(0, null, this);

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_instances_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_instance:
                startActivity(new Intent(getActivity(), EditInstanceActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Instances.CONTENT_URI, Instances.Query.PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mListAdapter.swapCursor(data);
        setContentShown(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mListAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor c = mListAdapter.getCursor();
        c.moveToPosition(position);

        Intent intent = new Intent(getActivity(), EditInstanceActivity.class);
        intent.setData(Instances.buildInstanceUri(c.getString(Query._ID)));
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = mListAdapter.getCursor();
        cursor.moveToPosition(position);

        saveInstanceToPrefs(cursor);

        mListAdapter.notifyDataSetChanged();
        return true;
    }

    private void saveInstanceToPrefs(Cursor cursor) {
        String name = cursor.getString(Query.NAME);
        String url = cursor.getString(Query.URL);
        String login = cursor.getString(Query.LOGIN);
        String password = cursor.getString(Query.PASSWORD);
        String folder = cursor.getString(Query.ROOT_FOLDER);

        mPrefs.put(R.string.key_instance_name, name);
        mPrefs.put(R.string.key_instance_url, url);
        mPrefs.put(R.string.key_instance_login, login);
        mPrefs.put(R.string.key_instance_password, password);
        mPrefs.put(R.string.key_instance_root_folder, folder);
    }
}
