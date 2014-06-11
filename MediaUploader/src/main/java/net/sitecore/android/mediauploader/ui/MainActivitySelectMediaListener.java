package net.sitecore.android.mediauploader.ui;

import android.content.Intent;
import android.net.Uri;

import net.sitecore.android.mediauploader.ui.upload.SelectMediaDialogHelper.SelectMediaListener;
import net.sitecore.android.mediauploader.ui.upload.UploadActivity;

class MainActivitySelectMediaListener implements SelectMediaListener {

    private final MainActivity mActivity;

    MainActivitySelectMediaListener(MainActivity activity) {
        mActivity = activity;
    }

    @Override public void onImageSelected(Uri imageUri) {
        final Intent intent = new Intent(mActivity, UploadActivity.class);
        intent.setData(imageUri);
        mActivity.startActivity(intent);
    }

    @Override public void onVideoSelected(Uri videoUri) {
        final Intent intent = new Intent(mActivity, UploadActivity.class);
        intent.setData(videoUri);
        mActivity.startActivity(intent);
    }
}
