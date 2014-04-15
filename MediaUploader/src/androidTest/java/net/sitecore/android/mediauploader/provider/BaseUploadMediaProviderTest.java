package net.sitecore.android.mediauploader.provider;

import android.content.ContentResolver;
import android.test.ProviderTestCase2;

public class BaseUploadMediaProviderTest extends ProviderTestCase2<UploadMediaProvider> {

    ContentResolver mResolver;

    public BaseUploadMediaProviderTest() {
        super(UploadMediaProvider.class, UploadMediaContract.CONTENT_AUTHORITY);
    }

    @Override protected void setUp() throws Exception {
        super.setUp();
        mResolver = getMockContentResolver();
    }
}
