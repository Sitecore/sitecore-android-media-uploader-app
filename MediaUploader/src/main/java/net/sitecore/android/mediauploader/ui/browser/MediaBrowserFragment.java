package net.sitecore.android.mediauploader.ui.browser;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.ui.IntentExtras;
import net.sitecore.android.mediauploader.ui.upload.UploadActivity;
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.mediauploader.util.SimpleNetworkListenerImpl;
import net.sitecore.android.sdk.api.model.ScItem;
import net.sitecore.android.sdk.widget.ItemViewBinder;
import net.sitecore.android.sdk.widget.ItemsBrowserFragment;
import net.sitecore.android.sdk.widget.ItemsBrowserFragment.ContentTreePositionListener;

public class MediaBrowserFragment extends ItemsBrowserFragment implements ContentTreePositionListener {

    private static final String ARG_ITEM_ROOT = "item_root";
    public static final int CURRENT_PATH_PADDDING = 8;

    private TextView mCurrentPath;

    public static MediaBrowserFragment newInstance(String root) {
        final MediaBrowserFragment fragment = new MediaBrowserFragment();
        final Bundle bundle = new Bundle();

        bundle.putString(DEFAULT_ROOT_FOLDER, root);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setContentTreePositionListener(this);
        setNetworkEventsListener(new SimpleNetworkListenerImpl(getActivity()));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_browser_menu, menu);
    }

    @Override
    protected View onCreateHeaderView(LayoutInflater inflater) {
        mCurrentPath = new TextView(getActivity());
        float scale = getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (CURRENT_PATH_PADDDING * scale + 0.5f);
        mCurrentPath.setPadding(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels);
        return mCurrentPath;
    }

    @Override
    protected View onCreateUpButtonView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.layout_media_browser_up_view, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_upload_here:
                final Intent intent = new Intent(getActivity(), UploadActivity.class);
                intent.putExtra(IntentExtras.ITEM_PATH, getCurrentItem().getPath());
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateCurrentPath(String text) {
        mCurrentPath.setText(text);
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

    @Override
    public void onScItemClick(ScItem item) {
        if (ScUtils.isImageTemplate(item.getTemplate())) {
            Intent intent = new Intent(getActivity(), PreviewActivity.class);
            intent.putExtra(PreviewActivity.IMAGE_ITEM_ID_KEY, item.getId());
            startActivity(intent);
        } else {
            super.onScItemClick(item);
        }
    }

    @Override
    public void onScItemLongClick(ScItem item) {
        Toast.makeText(getActivity(), item.getDisplayName() + " long clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected ItemViewBinder onCreateItemViewBinder() {
        return new ItemsListAdapter(UploaderApp.from(getActivity()).getImageLoader());
    }
}