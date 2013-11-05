package net.sitecore.android.mediauploader.ui.browser;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
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
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.ui.MainActivity;
import net.sitecore.android.mediauploader.util.BitmapLruCache;
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

public class MediaBrowserFragment extends Fragment implements LoaderCallbacks<Cursor>,
        Listener<ItemsResponse>, ErrorListener, OnItemClickListener {
    private ItemsCursorAdapter mAdapter;
    private ItemStack mItemStack = new ItemStack();
    private static ImageLoader mImageLoader;

    @InjectView(R.id.current_path)
    TextView mCurrentPath;
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

        mImageLoader = new ImageLoader(RequestQueueProvider.getRequestQueue(getActivity()), new BitmapLruCache());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_browser, null);
        Views.inject(this, root);

        mCurrentPath.setMovementMethod(ScrollingMovementMethod.getInstance());
        mListView.setOnItemClickListener(this);
        mListView.setEmptyView(mEmptyView);

        return root;
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor c = mAdapter.getCursor();
        c.moveToPosition(position);

        String itemId = c.getString(Query.ITEM_ID);
        if (!mItemStack.contains(itemId)) {
            mItemStack.push(itemId, c.getString(Query.DISPLAY_NAME));
            updateCurrentPath(mItemStack.getCurrentPath());
        }

        getLoaderManager().restartLoader(0, null, this);
        refresh(itemId);
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

    boolean isImage(String template) {
        if (template.equals("System/Media/Unversioned/Jpeg")) return true;
        else return template.equals("System/Media/Unversioned/Image");
    }

    String getMediaDownloadUrl(String itemId) {
            String id = itemId.replace("{", "").replace("}", "").replace("-", "");
            return String.format("%s/~/media/%s.ashx?w=50", MainActivity.mSession.getBaseUrl(), id);
    }

    class ItemsCursorAdapter extends CursorAdapter {

        public ItemsCursorAdapter(Context context) {
            super(context, null, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View v = LayoutInflater.from(context).inflate(R.layout.layout_browser_item, parent, false);
            v.setTag(new ViewHolder(v));
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor c) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.itemName.setText(c.getString(Query.DISPLAY_NAME));
            if (isImage(c.getString(Query.TEMPLATE))) {
                String url = getMediaDownloadUrl(c.getString(Query.ITEM_ID));
                holder.itemIcon.setImageUrl(url, mImageLoader);
            } else {
                holder.itemIcon.setImageDrawable(null);
            }
        }

        class ViewHolder {
            @InjectView(R.id.item_name) TextView itemName;
            @InjectView(R.id.item_icon) NetworkImageView itemIcon;

            ViewHolder(View parent) {
                Views.inject(this, parent);
            }
        }
    }

    static class ItemStack {
        private static final String MEDIA_LIBRARY_ID = "{3D6658D8-A0BF-4E75-B3E2-D050FABCF4E1}";
        private LinkedHashMap<String, String> map;

        ItemStack() {
            map = new LinkedHashMap<String, String>();
        }

        private void parse(String longID, String path) {
            String[] ids = longID.split("/");
            String[] names = path.split("/");
            for (int i = 0; i < ids.length; i++) {
                if (TextUtils.isEmpty(ids[i])) continue;
                map.put(ids[i], names[i]);
            }
        }

        public void init(String longID, String path) {
            parse(longID, path);
        }

        public void push(String parentId, String itemName) {
            map.put(parentId, itemName);
        }

        public String removeLastParent() {
            return map.remove(getLastKey());
        }

        private String getLastKey() {
            if (map.isEmpty()) return null;
            return new LinkedList<String>(map.keySet()).getLast();
        }

        public String getCurrentParentId() {
            return getLastKey();
        }

        public String getCurrentPath() {
            StringBuilder stack = new StringBuilder();

            Set<String> reverseIterator = map.keySet();
            for (String key : reverseIterator) {
                stack.append(map.get(key)).append("/");
            }
            return stack.toString();
        }

        public boolean canGoUp() {
            return !getLastKey().equals(MEDIA_LIBRARY_ID);
        }

        public boolean contains(String id) {
            return map.containsKey(id);
        }
    }
}
