package net.sitecore.android.mediauploader.ui.browser;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.ui.IntentExtras;
import net.sitecore.android.mediauploader.ui.ScFragment;
import net.sitecore.android.mediauploader.ui.upload.UploadActivity;
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.mediauploader.util.Utils;
import net.sitecore.android.sdk.api.RequestQueueProvider;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.ScRequest;
import net.sitecore.android.sdk.api.model.ItemsResponse;
import net.sitecore.android.sdk.api.model.RequestScope;
import net.sitecore.android.sdk.api.model.ScItem;
import net.sitecore.android.sdk.api.provider.ScItemsContract.Items;
import net.sitecore.android.sdk.api.provider.ScItemsContract.Items.Query;

import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Views;

import static net.sitecore.android.sdk.api.LogUtils.LOGD;

public class MediaBrowserFragment extends ScFragment implements LoaderCallbacks<Cursor>,
        Listener<ItemsResponse>, ErrorListener, OnItemClickListener {

    private static final String ARG_ITEM_ROOT = "item_root";

    public static MediaBrowserFragment newInstance(String root) {
        LOGD("MediaBrowserFragment.newInstance:" + root);
        final MediaBrowserFragment fragment = new MediaBrowserFragment();
        final Bundle bundle = new Bundle();

        bundle.putString(ARG_ITEM_ROOT, root);

        fragment.setArguments(bundle);
        return fragment;
    }

    private ItemsCursorAdapter mAdapter;
    private ItemStack mItemStack = new ItemStack();

    @InjectView(R.id.current_path) TextView mCurrentPath;
    @InjectView(android.R.id.list) ListView mListView;
    @InjectView(R.id.list_empty) View mEmptyList;

    @InjectView(R.id.layout_navigate_up) View mNavigateUp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    protected View onCreateContentView(LayoutInflater inflater) {
        View root = inflater.inflate(R.layout.fragment_media_browser, null);
        Views.inject(this, root);

        mListView.setEmptyView(mEmptyList);
        mListView.setOnItemClickListener(this);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new ItemsCursorAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        setContentShown(true);
        updateRootItem();
    }

    private void updateRootItem() {
        if (getArguments() != null) {
            final String root = getArguments().getString(ARG_ITEM_ROOT);

            UploaderApp.from(getActivity()).getSession(new Listener<ScApiSession>() {
                @Override
                public void onResponse(ScApiSession scApiSession) {
                    ScRequest request = scApiSession.getItems(mRootItemReceived, mRootItemNotReceived)
                            .byItemPath(root)
                            .build();
                    RequestQueueProvider.getRequestQueue(getActivity()).add(request);
                }
            }, this);
        }
    }

    private Listener<ItemsResponse> mRootItemReceived = new Listener<ItemsResponse>() {
        @Override
        public void onResponse(ItemsResponse itemsResponse) {
            if (itemsResponse.getTotalCount() == 0) {
                setEmpty(true);
                return;
            }
            setRootItem(itemsResponse.getItems().get(0));

            getLoaderManager().restartLoader(0, null, MediaBrowserFragment.this);
        }
    };

    private ErrorListener mRootItemNotReceived = new ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            //setEmpty(true);
            //TODO: show error view
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_browser_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_upload_here:
                final Intent intent = new Intent(getActivity(), UploadActivity.class);
                intent.putExtra(IntentExtras.ITEM_PATH, mItemStack.getCurrentFullPath());
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mNavigateUp.getVisibility() == View.GONE) mNavigateUp.setVisibility(View.VISIBLE);
        Cursor c = mAdapter.getCursor();
        c.moveToPosition(position);

        final String template = c.getString(Query.TEMPLATE);
        if (ScUtils.isImageTemplate(template)) {
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

    @OnClick(R.id.layout_navigate_up)
    public void goUp() {
        if (mItemStack.canGoUp()) {
            mItemStack.goUp();
            updateChildren(mItemStack.getCurrentItemId());
            updateCurrentPath(mItemStack.getCurrentPath());
            getLoaderManager().restartLoader(0, null, this);

            if (!mItemStack.canGoUp()) mNavigateUp.setVisibility(View.GONE);
        } else {
            Toast.makeText(getActivity(), "You are in root folder", Toast.LENGTH_LONG).show();
        }
    }

    private void updateChildren(final String itemId) {
        UploaderApp.from(getActivity()).getSession(new Listener<ScApiSession>() {
            @Override
            public void onResponse(ScApiSession apiSession) {
                ScRequest request = apiSession.getItems(MediaBrowserFragment.this, MediaBrowserFragment.this)
                        .byItemId(itemId)
                        .withScope(RequestScope.CHILDREN)
                        .build();

                RequestQueueProvider.getRequestQueue(getActivity()).add(request);
                getActivity().setProgressBarIndeterminateVisibility(true);
            }
        }, null);
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
        LOGD("onLoadFinished: cursor with " + data.getCount() + " items.");
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private void setRootItem(ScItem item) {
        mItemStack.clear();
        mItemStack.goInside(item.getId(), item.getDisplayName(), item.getPath());

        updateCurrentPath(mItemStack.getCurrentPath());
        updateChildren(mItemStack.getCurrentItemId());
    }

    @Override
    public void onResponse(ItemsResponse itemsResponse) {
        if (getActivity() != null) getActivity().setProgressBarIndeterminateVisibility(false);

    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        if (getActivity() != null) getActivity().setProgressBarIndeterminateVisibility(false);
        LOGD(Utils.getMessageFromError(volleyError));
        //setEmpty(true);
    }

    public void setRootFolder(String root) {
        getArguments().putString(ARG_ITEM_ROOT, root);
    }

    public void refresh() {
        if (getActivity() != null) {
            updateRootItem();
        }
    }
}
