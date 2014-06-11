package net.sitecore.android.mediauploader.ui.upload;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadsAsyncHandler;
import net.sitecore.android.mediauploader.ui.upload.RetryUploadDialogFragment.RetryDialogCallbacks;
import net.sitecore.android.mediauploader.ui.upload.UploadsListFragment.UploadsListCallbacks;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class UploadsListActivity extends Activity implements UploadsListCallbacks, RetryDialogCallbacks {

    @InjectView(R.id.radio_uploads_filter) RadioGroup mUploadsFilter;

    private UploadsListFragment mUploadsListFragment;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_uploads);
        ButterKnife.inject(this);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mUploadsListFragment = (UploadsListFragment) getFragmentManager().findFragmentById(R.id.fragment_uploads);

        mUploadsFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_uploads_all) {
                mUploadsListFragment.showAllUploads();
            } else if (checkedId == R.id.radio_uploads_completed) {
                mUploadsListFragment.showCompletedUploads();
            } else if (checkedId == R.id.radio_uploads_not_completed) {
                mUploadsListFragment.showNotCompletedUploads();
            }
        });
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
