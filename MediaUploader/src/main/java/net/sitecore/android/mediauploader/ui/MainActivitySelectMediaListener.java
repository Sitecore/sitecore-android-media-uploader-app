package net.sitecore.android.mediauploader.ui;

import android.net.Uri;

import net.sitecore.android.mediauploader.ui.upload.SelectMediaDialogHelper.SelectMediaListener;
import net.sitecore.android.mediauploader.ui.upload.UploadActivity;

class MainActivitySelectMediaListener implements SelectMediaListener {

    private final MainActivity mActivity;

    MainActivitySelectMediaListener(MainActivity activity) {
        mActivity = activity;
    }

    @Override public void onImageSelected(Uri imageUri) {
        UploadActivity.uploadImage(mActivity, imageUri);
    }

    @Override public void onVideoSelected(Uri videoUri) {
        UploadActivity.uploadVideo(mActivity, videoUri);
    }
}
