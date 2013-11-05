package net.sitecore.android.mediauploader.ui.browser;

import android.app.Fragment;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.ui.MainActivity;
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.mediauploader.util.Utils;
import net.sitecore.android.sdk.api.RequestQueueProvider;
import net.sitecore.android.sdk.api.ScRequest;
import net.sitecore.android.sdk.api.model.ItemsResponse;
import net.sitecore.android.sdk.api.model.PayloadType;
import net.sitecore.android.sdk.api.model.RequestScope;
import net.sitecore.android.sdk.api.model.ScItem;
import net.sitecore.android.sdk.api.provider.ScItemsContract.Items;
import net.sitecore.android.sdk.api.provider.ScItemsContract.Items.Query;

import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Views;

import static net.sitecore.android.sdk.api.LogUtils.LOGD;

public class MediaBrowserFragment extends ListFragment implements LoaderCallbacks<Cursor>,
        Listener<ItemsResponse>, ErrorListener {

    private ItemsCursorAdapter mAdapter;
    private ItemStack mItemStack = new ItemStack();

    @InjectView(R.id.current_path) TextView mCurrentPath;
    @InjectView(android.R.id.list) ListView mListView;
    @InjectView(android.R.id.empty) TextView mEmptyView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_browser, null);
        Views.inject(this, root);

        mCurrentPath.setMovementMethod(ScrollingMovementMethod.getInstance());
        //mListView.setOnItemClickListener(this);
        //mListView.setEmptyView(mEmptyView);

        return root;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor c = mAdapter.getCursor();
        c.moveToPosition(position);

        final String template = c.getString(Query.TEMPLATE);
        if (ScUtils.isImage(template)) {
            Toast.makeText(getActivity(), "TODO: show image fullscreen", Toast.LENGTH_LONG).show();
            return;
        }

        String itemId = c.getString(Query.ITEM_ID);
        if (!mItemStack.contains(itemId)) {
            mItemStack.push(itemId, c.getString(Query.DISPLAY_NAME));
            updateCurrentPath(mItemStack.getCurrentPath());
        }

        getLoaderManager().restartLoader(0, null, this);
        refresh(itemId);
    }

    @OnClick(R.id.button_go_up)
    void goUp() {
        if (!mItemStack.canGoUp()) {
            Toast.makeText(getActivity(), "You are in root folder", Toast.LENGTH_LONG).show();
        } else {
            mItemStack.removeLastParent();
            refresh(mItemStack.getCurrentParentId());
            updateCurrentPath(mItemStack.getCurrentPath());
            getLoaderManager().restartLoader(0, null, this);
        }
        refresh(mItemStack.getCurrentParentId());
    }

    void refresh(String itemId) {
        ScRequest request = MainActivity.mSession.getItems(this, this)
                .byItemId(itemId)
                .withScope(RequestScope.CHILDREN)
                .withPayloadType(PayloadType.FULL)
                .build();

        RequestQueueProvider.getRequestQueue(getActivity()).add(request);
    }

    private void updateCurrentPath(String text) {
        mCurrentPath.setText(text);
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
        mItemStack.init(item.getLongId(), item.getPath());
        updateCurrentPath(mItemStack.getCurrentPath());
        getLoaderManager().restartLoader(0, null, MediaBrowserFragment.this);
    }

    @Override
    public void onResponse(ItemsResponse itemsResponse) {
        getLoaderManager().restartLoader(0, null, MediaBrowserFragment.this);
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        LOGD(Utils.getMessageFromError(volleyError));
    }


}
