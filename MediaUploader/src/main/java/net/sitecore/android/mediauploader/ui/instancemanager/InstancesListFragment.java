package net.sitecore.android.mediauploader.ui.instancemanager;

import android.app.Activity;
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
import android.widget.Toast;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances.Query;
import net.sitecore.android.mediauploader.ui.ScFragment;
import net.sitecore.android.mediauploader.ui.instancemanager.InstancesListAdapter.OnDeleteButtonClickListener;
import net.sitecore.android.mediauploader.util.UploaderPrefs;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class InstancesListFragment extends ScFragment implements LoaderCallbacks<Cursor>, OnItemClickListener, OnItemLongClickListener {
    private InstancesListAdapter mListAdapter;
    private OnDefaultInstanceChangeListener mChangedListener;

    @InjectView(android.R.id.list) ListView mListView;

    public interface OnDefaultInstanceChangeListener {
        public void onDefaultInstanceChanged();

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListAdapter = new InstancesListAdapter(getActivity(), new OnDeleteButtonClickListener() {

            @Override
            public void onDeleteButtonClicked(String name) {
                if (UploaderPrefs.from(getActivity()).isDefaultInstance(name)) {
                    Toast.makeText(getActivity(), "You cannot remove default instance",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                showDeleteDialog(name);
            }
        });
        mListView.setAdapter(mListAdapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mChangedListener = (OnDefaultInstanceChangeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnDefaultInstanceChangeListener");
        }
    }

    private void showDeleteDialog(final String instanceName) {
        DeleteInstanceConfirmationDialog.newInstance(instanceName).show(getFragmentManager(), "dialog");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.inject(this, view);

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

        UploaderApp.from(getActivity()).switchInstance(new Instance(cursor));

        mChangedListener.onDefaultInstanceChanged();
        mListAdapter.notifyDataSetChanged();
        return true;
    }
}
