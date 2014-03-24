package net.sitecore.android.mediauploader.ui.upload;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.ui.browser.ItemsListAdapter;
import net.sitecore.android.sdk.api.model.ScItem;
import net.sitecore.android.sdk.ui.ItemViewBinder;
import net.sitecore.android.sdk.ui.ItemsBrowserFragment.ContentTreePositionListener;
import net.sitecore.android.sdk.ui.ItemsListBrowserFragment;

public class PathSelectorDialog extends ItemsListBrowserFragment implements ContentTreePositionListener {

    public static final int CURRENT_PATH_PADDING = 8;

    interface PathSelectorListener {
        public void onPathSelected(String path);
    }

    private PathSelectorListener mListener;
    private TextView mCurrentPath;

    public static PathSelectorDialog newInstance(String root) {
        PathSelectorDialog fragment = new PathSelectorDialog();

        Bundle bundle = new Bundle();
        bundle.putString(DEFAULT_ROOT_FOLDER, root);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentTreePositionListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (PathSelectorListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement PathSelectorListener");
        }
    }

    @Override
    protected View onCreateHeaderView(LayoutInflater inflater) {
        mCurrentPath = new TextView(getActivity());
        float scale = getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (CURRENT_PATH_PADDING * scale + 0.5f);
        mCurrentPath.setPadding(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels);
        return mCurrentPath;
    }

    @Override
    protected ItemViewBinder onCreateItemViewBinder() {
        return new ItemsListAdapter(UploaderApp.from(getActivity()).getImageLoader());
    }

    @Override
    public void onGoUp(ScItem item) {
        mCurrentPath.setText(item.getPath());
    }

    @Override
    public void onGoInside(ScItem item) {
        mCurrentPath.setText(item.getPath());
    }

    @Override
    public void onInitialized(ScItem item) {
        mCurrentPath.setText(item.getPath());
    }

    @Override
    protected View onCreateFooterView(LayoutInflater inflater) {
        View layout = inflater.inflate(R.layout.layout_upload_here, null);

        Button uploadHere = (Button) layout.findViewById(R.id.button_upload_here);
        uploadHere.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPathSelected(getCurrentItem().getPath());
                dismiss();
            }
        });

        Button uploadCancel = (Button) layout.findViewById(R.id.button_upload_cancel);
        uploadCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return layout;
    }

    @Override
    public void onScItemClick(ScItem item) {
        super.onScItemClick(item);

    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().setTitle("Select folder to upload");
    }
}
