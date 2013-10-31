package net.sitecore.android.mediauploader.ui.browser;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Iterator;
import java.util.LinkedList;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.ui.MainActivity;
import net.sitecore.android.sdk.api.RequestQueueProvider;
import net.sitecore.android.sdk.api.ScRequest;
import net.sitecore.android.sdk.api.model.ItemsResponse;
import net.sitecore.android.sdk.api.model.RequestScope;
import net.sitecore.android.sdk.api.model.ScItem;
import net.sitecore.android.sdk.api.provider.ScItemsContract.Items;
import net.sitecore.android.sdk.api.provider.ScItemsContract.Items.Query;

import butterknife.InjectView;
import butterknife.Views;

public class MediaBrowserFragment extends Fragment implements LoaderCallbacks<Cursor>,
        Listener<ItemsResponse>, ErrorListener, OnItemClickListener {
    private ItemsCursorAdapter mAdapter;
    private ItemStack mItemStack = new ItemStack();

    @InjectView(R.id.button_go_up)
    Button mGoUpButton;
    @InjectView(R.id.items_list)
    ListView mListView;
    @InjectView(android.R.id.empty)
    TextView mEmptyView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_browser, null);
        Views.inject(this, root);

        mListView.setOnItemClickListener(this);
        mGoUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goUp();
            }
        });
        mListView.setEmptyView(mEmptyView);

        return root;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor c = mAdapter.getCursor();
        c.moveToPosition(position);

        String itemId = c.getString(Query.ITEM_ID);

        if (!mItemStack.contains(itemId)) {
            mItemStack.push(itemId, c.getString(Query.DISPLAY_NAME));
            updateGoUpButtonText(mItemStack.getCurrentPath());
        }

        ScRequest request = MainActivity.mSession.getItems(this, this)
                .byItemId(itemId)
                .withScope(RequestScope.CHILDREN)
                .build();

        RequestQueueProvider.getRequestQueue(getActivity()).add(request);
    }

    public void goUp() {
        if (mItemStack.size() == 1) {
            Toast.makeText(getActivity(), "You are in root folder", Toast.LENGTH_LONG).show();
        } else {
            mItemStack.removeLastParent();
            updateGoUpButtonText(mItemStack.getCurrentPath());
            getLoaderManager().restartLoader(0, null, MediaBrowserFragment.this);
        }
    }

    private void updateGoUpButtonText(String text) {
        mGoUpButton.setText(text);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = null;
        String parentId = mItemStack.getCurrentParentId();
        if (!TextUtils.isEmpty(parentId)) {
            selection = Items.PARENT_ITEM_ID + "='" + parentId + "'";
        }
        return new CursorLoader(getActivity(), Items.CONTENT_URI, Query.PROJECTION, selection, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mAdapter == null) {
            mAdapter = new ItemsCursorAdapter(getActivity());
            mListView.setAdapter(mAdapter);
        }

        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public void setQueryParent(ScItem item) {
        mItemStack.push(item.getParentItemId(), "../");
        updateGoUpButtonText(mItemStack.getCurrentPath());
        getLoaderManager().restartLoader(0, null, MediaBrowserFragment.this);
    }

    @Override
    public void onResponse(ItemsResponse itemsResponse) {
        getLoaderManager().restartLoader(0, null, MediaBrowserFragment.this);
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

    static class ItemStack {
        private LinkedList<String> mHistory = new LinkedList<String>();
        private LinkedList<String> mItemsNames = new LinkedList<String>();

        ItemStack() {
            mHistory = new LinkedList<String>();
            mItemsNames = new LinkedList<String>();
        }

        public void push(String parentId, String itemName) {
            mHistory.push(parentId);
            mItemsNames.push(itemName);
        }

        public String removeLastParent() {
            mItemsNames.poll();
            return mHistory.poll();
        }

        public String getCurrentParentId() {
            return mHistory.peek();
        }

        public String getCurrentPath() {
            StringBuilder stack = new StringBuilder();

            Iterator<String> reverseIterator = mItemsNames.descendingIterator();
            while (reverseIterator.hasNext()) {
                stack.append(reverseIterator.next()).append("/");
            }
            return stack.toString();
        }

        public int size() {
            return mHistory.size();
        }

        public boolean contains(String id) {
            return mHistory.contains(id);
        }
    }
}
