package net.sitecore.android.mediauploader.ui;

import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.sitecore.android.sdk.api.RequestQueueProvider;
import net.sitecore.android.sdk.api.ScRequest;
import net.sitecore.android.sdk.api.model.ItemsResponse;
import net.sitecore.android.sdk.api.model.RequestScope;
import net.sitecore.android.sdk.api.provider.ScItemsContract.Items;
import net.sitecore.android.sdk.api.provider.ScItemsContract.Items.Query;

import butterknife.InjectView;
import butterknife.Views;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

public class ItemsListFragment extends ListFragment implements LoaderCallbacks<Cursor>,
        Listener<ItemsResponse>,
        ErrorListener {

    private ItemsCursorAdapter mAdapter;

    private String mParentId;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
        setEmptyText("No items.");
}

    public void goUp() {
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = null;
        if (!TextUtils.isEmpty(mParentId)) {
            selection = Items.PARENT_ITEM_ID + "='" + mParentId + "'";
        }
        return new CursorLoader(getActivity(), Items.CONTENT_URI, Query.PROJECTION, selection, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mAdapter == null) {
            mAdapter = new ItemsCursorAdapter(getActivity());
            setListAdapter(mAdapter);
        }

        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public void setQueryParent(String parentId) {
        mParentId = parentId;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getLoaderManager().restartLoader(0, null, ItemsListFragment.this);
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor c = mAdapter.getCursor();
        c.moveToPosition(position);
        String itemId = c.getString(Query.ITEM_ID);
        mParentId = itemId;
        ScRequest request = MainActivity.mSession.getItems(this, this)
                .byItemId(itemId)
                .withScope(RequestScope.CHILDREN)
                .build();

        RequestQueueProvider.getRequestQueue(getActivity()).add(request);
    }

    @Override
    public void onResponse(ItemsResponse itemsResponse) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getLoaderManager().restartLoader(0, null, ItemsListFragment.this);
            }
        });
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {

    }

    static class ItemsCursorAdapter extends CursorAdapter {

        public ItemsCursorAdapter(Context context) {
            super(context, null, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View v = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
            v.setTag(new ViewHolder(v));
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor c) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.name.setText(c.getString(Query.DISPLAY_NAME));
        }

        static class ViewHolder {
            @InjectView(android.R.id.text1) TextView name;
            ViewHolder(View parent) {
                Views.inject(this, parent);
            }
        }
    }
}
