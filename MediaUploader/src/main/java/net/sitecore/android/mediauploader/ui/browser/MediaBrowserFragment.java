package net.sitecore.android.mediauploader.ui.browser;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.ui.IntentExtras;
import net.sitecore.android.mediauploader.ui.upload.UploadActivity;
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.sdk.api.RequestQueueProvider;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.model.ScItem;
import net.sitecore.android.sdk.widget.ItemsBrowserFragment;
import net.sitecore.android.sdk.widget.ItemsBrowserFragment.NavigationEventsListener;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static net.sitecore.android.sdk.api.LogUtils.LOGD;
import static net.sitecore.android.sdk.api.LogUtils.LOGE;

public class MediaBrowserFragment extends Fragment implements NavigationEventsListener {

    private static final String ARG_ITEM_ROOT = "item_root";
    @InjectView(R.id.current_path) TextView mCurrentPath;
    private ItemsFragment mItemsFragment;

    public static MediaBrowserFragment newInstance(String root) {
        LOGD("MediaBrowserFragment.newInstance:" + root);
        final MediaBrowserFragment fragment = new MediaBrowserFragment();
        final Bundle bundle = new Bundle();

        bundle.putString(ARG_ITEM_ROOT, root);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (mItemsFragment == null) {
            mItemsFragment = new ItemsFragment(UploaderApp.from(getActivity()).getSession(),
                    RequestQueueProvider.getRequestQueue(getActivity()));
            mItemsFragment.setNavigationEventsListener(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_media_browser, null);
        ButterKnife.inject(this, root);

        getFragmentManager().beginTransaction().add(R.id.fragment_browser_container, mItemsFragment).commit();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        try {
            getFragmentManager().beginTransaction().remove(mItemsFragment).commit();
        } catch (Exception e) {
            LOGE("Exception in onDestroyView()", e);
        }
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_browser_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_upload_here:
                final Intent intent = new Intent(getActivity(), UploadActivity.class);
                intent.putExtra(IntentExtras.ITEM_PATH, mItemsFragment.getCurrentItem().getPath());
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateCurrentPath(String text) {
        mCurrentPath.setText(text);
    }

    public void setRootFolder(String root) {
        getArguments().putString(ARG_ITEM_ROOT, root);
    }

    public void refresh() {
        if (getActivity() != null) {
//            TODO : refresh items after refresh menu item clicked
        }
    }

    @Override
    public void onGoUp(ScItem item) {
        updateCurrentPath(item.getPath());
    }

    @Override
    public void onGoInside(ScItem item) {
        updateCurrentPath(item.getPath());
    }

    @Override
    public void onInitialized(ScItem item) {
        updateCurrentPath(item.getPath());
    }

    public static class ItemsFragment extends ItemsBrowserFragment {

        public ItemsFragment(ScApiSession apiSession, RequestQueue requestQueue) {
            super(apiSession, requestQueue);
        }

        @Override
        protected View onCreateUpButtonView(LayoutInflater inflater) {
            return inflater.inflate(R.layout.layout_media_browser_up_view, null);
        }

        @Override
        public void onScItemClick(ScItem item) {
            if (item.hasChildren()) {
                super.onScItemClick(item);
            } else {
                if (ScUtils.isImageTemplate(item.getTemplate())) {
                    Intent intent = new Intent(getActivity(), PreviewActivity.class);
                    intent.putExtra(PreviewActivity.IMAGE_ITEM_ID_KEY, item.getId());
                    startActivity(intent);
                }
            }
        }

        @Override
        public void onScItemLongClick(ScItem item) {
            Toast.makeText(getActivity(), item.getDisplayName() + " long clicked", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected ItemViewBinder onGetListItemView() {
            return new ItemsListAdapter(UploaderApp.from(getActivity()).getImageLoader());
        }
    }
}
