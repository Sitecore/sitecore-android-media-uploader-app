package net.sitecore.android.mediauploader.ui.settings;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;

public class InstanceAsyncQuery extends AsyncQueryHandler {

    public InstanceAsyncQuery(ContentResolver cr) {
        super(cr);
    }

    public void startQueryLastInstance() {

    }

    public void startUpdateSelected(int instanceId) {
    }

}
