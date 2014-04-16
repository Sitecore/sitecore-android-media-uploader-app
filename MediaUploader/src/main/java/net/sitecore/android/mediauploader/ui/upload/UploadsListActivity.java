package net.sitecore.android.mediauploader.ui.upload;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadsAsyncHandler;
import net.sitecore.android.mediauploader.ui.upload.RetryUploadDialogFragment.RetryDialogCallbacks;
import net.sitecore.android.mediauploader.ui.upload.UploadsListFragment.UploadsListCallbacks;

public class UploadsListActivity extends Activity implements UploadsListCallbacks, RetryDialogCallbacks {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new UploadsListFragment()).commit();
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override public void onStartPendingUpload(String uploadId) {
        startUpload(uploadId);
    }

    @Override public void onErrorUploadClicked(String uploadId, String itemName, String failMessage) {
        RetryUploadDialogFragment.newInstance(itemName, uploadId, failMessage)
                .show(getFragmentManager(), "dialog");
    }

    @Override public void onRetryUpload(String uploadId) {
        startUpload(uploadId);
    }

    private void startUpload(final String uploadId) {
        new UploadsAsyncHandler(getContentResolver()) {
            @Override protected void onUpdateComplete(int token, Object cookie, int result) {
                new StartUploadTask(getApplicationContext()).execute(uploadId);
            }
        }.updateUploadStatus(uploadId, UploadStatus.PENDING);
    }
}
