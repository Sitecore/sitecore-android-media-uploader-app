package net.sitecore.android.mediauploader.ui.browser;

import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
    private ItemStack mItemStack;

    @InjectView(R.id.current_path) TextView mCurrentPath;
    @InjectView(android.R.id.list) ListView mListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_browser, null);
        Views.inject(this, root);

        mCurrentPath.setMovementMethod(ScrollingMovementMethod.getInstance());

        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_browser_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_upload_here:
                Toast.makeText(getActivity(), mItemStack.getCurrentFullPath(), Toast.LENGTH_LONG).show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor c = mAdapter.getCursor();
        c.moveToPosition(position);

        final String template = c.getString(Query.TEMPLATE);
        if (ScUtils.isImage(template)) {
            Intent intent = new Intent(getActivity(), PreviewActivity.class);
            intent.putExtra(PreviewActivity.IMAGE_ITEM_ID_KEY, c.getString(Query.ITEM_ID));
            startActivity(intent);
            return;
        }

        final String itemId = c.getString(Query.ITEM_ID);
        final String name = c.getString(Query.DISPLAY_NAME);
        final String path = c.getString(Query.PATH);

        mItemStack.goInside(itemId, name, path);
        updateCurrentPath(mItemStack.getCurrentPath());

        getLoaderManager().restartLoader(0, null, this);
        updateChildren(itemId);
    }

    public ItemStack getItemStack() {
        return mItemStack;
    }

    @OnClick(R.id.layout_nav_up_container)
    public void goUp() {
        if (mItemStack.canGoUp()) {
            mItemStack.goUp();
            updateChildren(mItemStack.getCurrentItemId());
            updateCurrentPath(mItemStack.getCurrentPath());
            getLoaderManager().restartLoader(0, null, this);
        } else {
            Toast.makeText(getActivity(), "You are in root folder", Toast.LENGTH_LONG).show();
        }
    }

    private void updateChildren(String itemId) {
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
        String itemId = mItemStack.getCurrentItemId();
        if (!TextUtils.isEmpty(itemId)) {
            selection = Items.PARENT_ITEM_ID + "='" + itemId + "'";
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

    public void setRootItem(ScItem item) {
        mItemStack = new ItemStack(item.getId(), item.getDisplayName(), item.getPath());
        updateCurrentPath(mItemStack.getCurrentPath());
        updateChildren(mItemStack.getCurrentItemId());
        getLoaderManager().initLoader(0, null, MediaBrowserFragment.this);
    }

    @Override
    public void onResponse(ItemsResponse itemsResponse) {
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        LOGD(Utils.getMessageFromError(volleyError));
    }

}
