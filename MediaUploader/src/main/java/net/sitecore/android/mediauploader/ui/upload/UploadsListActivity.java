package net.sitecore.android.mediauploader.ui.upload;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads.Query;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class UploadsListActivity extends Activity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new UploadsListFragment()).commit();
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    static class UploadsListFragment extends ListFragment implements LoaderCallbacks<Cursor> {

        private UploadsCursorAdapter mAdapter;

        @Override public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setEmptyText("No uploads");
            getLoaderManager().initLoader(0, null, this);
        }

        @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(), Uploads.CONTENT_URI, Query.PROJECTION, null, null, null);
        }

        @Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (mAdapter == null) {
                mAdapter = new UploadsCursorAdapter(getActivity());
                setListAdapter(mAdapter);
            }
            mAdapter.swapCursor(data);
        }

        @Override public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
        }
    }

    static class UploadsCursorAdapter extends CursorAdapter {

        public UploadsCursorAdapter(Context context) {
            super(context, null, false);
        }

        @Override public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
            ViewHolder holder = new ViewHolder(v);
            v.setTag(holder);
            return v;
        }

        @Override public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            UploadStatus status = UploadStatus.values()[cursor.getInt(Query.STATUS)];

            holder.text1.setText(cursor.getString(Query.ITEM_NAME));
            holder.text2.setText("status: " + status.name());
        }
    }

    static class ViewHolder {
        @InjectView(android.R.id.text1)TextView text1;
        @InjectView(android.R.id.text2)TextView text2;

        ViewHolder(View v) {
            ButterKnife.inject(this, v);
        }
    }

}
