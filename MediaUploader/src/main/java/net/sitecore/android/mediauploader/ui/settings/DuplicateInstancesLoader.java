package net.sitecore.android.mediauploader.ui.settings;

import android.content.Context;
import android.content.CursorLoader;

import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;

public class DuplicateInstancesLoader extends CursorLoader {

    private static final String SELECTION = Instances.URL + "=? and " + Instances.LOGIN + "=? and " +
            Instances.PASSWORD + "=? and " + Instances.ROOT_FOLDER + "=? and " + Instances.SITE + "=?";

    public DuplicateInstancesLoader(Context context, Instance instance) {
        super(context);
        final String[] selectionArgs = new String[]{
                instance.getUrl(),
                instance.getLogin(),
                instance.getPassword(),
                instance.getRootFolder(),
                instance.getSite()
        };

        setUri(Instances.CONTENT_URI);
        setSelection(SELECTION);
        setSelectionArgs(selectionArgs);
    }
}
