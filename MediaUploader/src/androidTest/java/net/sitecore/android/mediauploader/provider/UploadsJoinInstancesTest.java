package net.sitecore.android.mediauploader.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads.UploadWithInstanceQuery;

public class UploadsJoinInstancesTest extends BaseUploadMediaProviderTest {

    private String mUploadId;

    @Override protected void setUp() throws Exception {
        super.setUp();

        ContentValues instance = new ContentValues();
        instance.put(Instances.URL, "http://sample.com");
        instance.put(Instances.LOGIN, "login");
        instance.put(Instances.PASSWORD, "password");
        instance.put(Instances.ROOT_FOLDER, "/sitecore/content");
        instance.put(Instances.DATABASE, "master");
        instance.put(Instances.SITE, "");
        instance.put(Instances.PUBLIC_KEY, "blablabla");
        instance.put(Instances.SELECTED, "1");

        Uri instanceUri = mResolver.insert(Instances.CONTENT_URI, instance);
        String id = Instances.getInstanceId(instanceUri);

        ContentValues upload = new ContentValues();
        upload.put(Uploads.ITEM_NAME, "item name");
        upload.put(Uploads.FILE_URI, "http://test.com/image.png");
        upload.put(Uploads.STATUS, UploadStatus.IN_PROGRESS.name());
        upload.put(Uploads.INSTANCE_ID, id);
        Uri uploadUri = mResolver.insert(Uploads.CONTENT_URI, upload);
        mUploadId = Uploads.getUploadId(uploadUri);
    }

    public void testQuery() {
        Uri uri = Uploads.buildUploadWithInstanceUri(mUploadId);
        Cursor c = mResolver.query(uri, UploadWithInstanceQuery.PROJECTION, null, null, null);
        c.moveToFirst();
        assertEquals(1, c.getCount());
        assertEquals(c.getString(UploadWithInstanceQuery.ITEM_NAME), "item name");
        assertEquals(c.getString(UploadWithInstanceQuery.LOGIN), "login");
    }

    public void testInsertNotSupported() {
        Uri uri = Uploads.buildUploadWithInstanceUri("1");

        try {
            mResolver.insert(uri, null);
        } catch (UnsupportedOperationException e) {
            assertNotNull(e);
        }
    }
}
