package net.sitecore.android.mediauploader.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.test.ProviderTestCase2;

import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;

public class UploadMediaProviderUploadsTest extends ProviderTestCase2<UploadMediaProvider> {

    private ContentResolver mContentResolver;

    public UploadMediaProviderUploadsTest() {
        super(UploadMediaProvider.class, UploadMediaContract.CONTENT_AUTHORITY);
    }

    private ContentValues newUploadValues(String itemName) {
        ContentValues values = new ContentValues();
        values.put(Uploads.URL, "http://test.com:100500");
        values.put(Uploads.USERNAME, "name");
        values.put(Uploads.PASSWORD, "password");
        values.put(Uploads.ITEM_NAME, itemName);
        values.put(Uploads.FILE_URI, "http://test.com/image.png");
        values.put(Uploads.STATUS, "IN PROGRESS");

        return values;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContentResolver = getMockContentResolver();

        mContentResolver.insert(Uploads.CONTENT_URI, newUploadValues("init item 1"));
        mContentResolver.insert(Uploads.CONTENT_URI, newUploadValues("init item 2"));
    }

    public void testQuery() {
        Cursor c = mContentResolver.query(Uploads.CONTENT_URI, null, null, null, null);

        assertNotNull(c);
        assertEquals(1, c.getCount());
    }

    public void testInsert() {

    }

    public void testUpdate() {
        ContentValues values = newUploadValues("updated");
        String where = Uploads.ITEM_NAME + " = ?";
        int updated = mContentResolver.update(Uploads.CONTENT_URI, values, where, new String[]{"init item 1"});
        assertEquals(1, updated);

    }

    public void testDelete() {

    }

}

