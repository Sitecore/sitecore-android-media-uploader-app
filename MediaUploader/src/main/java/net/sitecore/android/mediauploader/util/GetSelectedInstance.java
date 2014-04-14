package net.sitecore.android.mediauploader.util;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;

import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances.Query;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;

public abstract class GetSelectedInstance extends AsyncQueryHandler {

    public GetSelectedInstance(ContentResolver cr) {
        super(cr);
    }

    @Override protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            onComplete(new Instance(cursor), cursor.getInt(Uploads.Query._ID));
            cursor.close();
        } else onComplete(null, -1);
    }

    public abstract void onComplete(Instance instance, int instanceID);

    public void start() {
        startQuery(0, null, Instances.CONTENT_URI, Query.PROJECTION, Instances.SELECTED + "=?",
                new String[]{"1"}, null);
    }
}
