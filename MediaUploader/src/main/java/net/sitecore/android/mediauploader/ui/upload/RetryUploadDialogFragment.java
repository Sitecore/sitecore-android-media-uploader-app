package net.sitecore.android.mediauploader.ui.upload;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadsAsyncHandler;
import net.sitecore.android.mediauploader.util.StartUploadTask;

class RetryUploadDialogFragment extends DialogFragment {

    private static final String EXTRA_UPLOAD_ID = "id";
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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String id = getArguments().getString(EXTRA_UPLOAD_ID);
        String failMessage = getArguments().getString(EXTRA_FAIL_MESSAGE);
        String name = getArguments().getString(EXTRA_ITEM_NAME);

        return new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_upload_error)
                .setTitle(name)
                .setMessage(failMessage)
                .setPositiveButton(R.string.text_retry,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //TODO: dont fire update here, move logic to activity
                                startUpload(getActivity(), id);
                            }
                        }
                )
                .setNegativeButton(R.string.text_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dismiss();
                            }
                        }
                )
                .create();
    }

    private void startUpload(final Context context, final String id) {
        new UploadsAsyncHandler(context.getContentResolver()) {
            @Override protected void onUpdateComplete(int token, Object cookie, int result) {
                new StartUploadTask(context).execute(id);
            }
        }.updateUploadStatus(id, UploadStatus.PENDING);
    }
}
