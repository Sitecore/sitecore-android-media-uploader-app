package net.sitecore.android.mediauploader.provider;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;

import net.sitecore.android.sdk.api.provider.ScItemsContract.Items;

public class ItemsAsyncHandler extends AsyncQueryHandler {

    public ItemsAsyncHandler(ContentResolver cr) {
        super(cr);
    }

    public void deleteItemsBrowserCache() {
        startDelete(0, null, Items.CONTENT_URI, null, null);
    }
}
