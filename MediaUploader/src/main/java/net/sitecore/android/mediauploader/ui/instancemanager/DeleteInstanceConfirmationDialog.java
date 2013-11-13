package net.sitecore.android.mediauploader.ui.instancemanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.AsyncQueryHandler;
import android.content.DialogInterface;
import android.os.Bundle;

import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;

public class DeleteInstanceConfirmationDialog extends DialogFragment {

    private static final String INSTANCE_NAME = "instance_name";

    static DeleteInstanceConfirmationDialog newInstance(String instanceName) {
        DeleteInstanceConfirmationDialog f = new DeleteInstanceConfirmationDialog();

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
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String selection = Instances.NAME + " ='?'";
                        new AsyncQueryHandler(getActivity().getContentResolver()) {
                        }.startDelete(0, null, Instances.CONTENT_URI, selection, new String[]{name});
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).create();
    }

}
