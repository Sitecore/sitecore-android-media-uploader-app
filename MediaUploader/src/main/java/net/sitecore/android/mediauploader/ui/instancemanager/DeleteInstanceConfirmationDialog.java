package net.sitecore.android.mediauploader.ui.instancemanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class DeleteInstanceConfirmationDialog extends DialogFragment {

    private static final String INSTANCE_NAME = "instance_name";

    private OnPerformDeleteListener mDeleteListener;

    interface OnPerformDeleteListener {
        public void onPerformDelete();
    }

    public void setDeleteListener(OnPerformDeleteListener deleteListener) {
        mDeleteListener = deleteListener;
    }

    static DeleteInstanceConfirmationDialog newInstance(String instanceName, OnPerformDeleteListener listener) {
        DeleteInstanceConfirmationDialog f = new DeleteInstanceConfirmationDialog();

        f.setDeleteListener(listener);

        Bundle args = new Bundle();
        args.putString(INSTANCE_NAME, instanceName);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String name = getArguments().getString(INSTANCE_NAME);
        return new AlertDialog.Builder(getActivity())
                .setTitle("Delete instance")
                .setMessage("Do you really want to delete \"" + name + "\"?")
                .setPositiveButton("Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mDeleteListener.onPerformDelete();
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                )
                .create();
    }

}
