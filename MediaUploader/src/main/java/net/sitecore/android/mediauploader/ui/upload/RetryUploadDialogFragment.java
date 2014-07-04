package net.sitecore.android.mediauploader.ui.upload;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import net.sitecore.android.mediauploader.R;

public class RetryUploadDialogFragment extends DialogFragment {

    public interface RetryDialogCallbacks {
        public void onRetryUpload(String uploadId);
    }

    private static final String EXTRA_UPLOAD_ID = "uploadId";
    private static final String EXTRA_FAIL_MESSAGE = "message";
    private static final String EXTRA_ITEM_NAME = "name";

    public static RetryUploadDialogFragment newInstance(String name, String uploadId, String failMessage) {
        RetryUploadDialogFragment fragment = new RetryUploadDialogFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_UPLOAD_ID, uploadId);
        args.putString(EXTRA_FAIL_MESSAGE, failMessage);
        args.putString(EXTRA_ITEM_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    private RetryDialogCallbacks mCallbacks;

    public RetryUploadDialogFragment() {
    }

    @Override public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (RetryDialogCallbacks) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String uploadId = getArguments().getString(EXTRA_UPLOAD_ID);
        String failMessage = getArguments().getString(EXTRA_FAIL_MESSAGE);
        String name = getArguments().getString(EXTRA_ITEM_NAME);

        return new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_upload_error)
                .setTitle(name)
                .setMessage(failMessage)
                .setPositiveButton(
                        R.string.text_retry,
                        (dialog, whichButton) -> mCallbacks.onRetryUpload(uploadId))
                .setNegativeButton(R.string.text_cancel, (dialog, whichButton) -> dismiss())
                .create();
    }

}
