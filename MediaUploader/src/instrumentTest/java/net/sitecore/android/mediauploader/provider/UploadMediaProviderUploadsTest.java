package net.sitecore.android.mediauploader.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;

import net.sitecore.android.mediauploader.model.UploadStatus;
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
        values.put(Uploads.ITEM_PATH, "/sitecore/fake/path");
        values.put(Uploads.FILE_URI, "http://test.com/image.png");
        values.put(Uploads.STATUS, UploadStatus.IN_PROGRESS.name());

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
        assertEquals(2, c.getCount());
    }

    public void testInsert() {
        Uri itemUri = mContentResolver.insert(Uploads.CONTENT_URI, newUploadValues("new_item"));
        assertEquals("content://net.sitecore.android.provider/uploads/3", itemUri.toString());

        String id = Uploads.getUploadId(itemUri);
        assertEquals("3", id);

        String where = Uploads._ID + " = ?";

        Cursor c = mContentResolver.query(Uploads.CONTENT_URI, null, where, new String[]{id}, null);
        assertEquals(1, c.getCount());

        if (!c.moveToFirst()) fail("Not empty cursor expected.");
        String name = c.getString(c.getColumnIndex(Uploads.ITEM_NAME));
        assertEquals("new_item", name);
    }

    public void testUpdate() {
        ContentValues values = newUploadValues("updated");
        String where = Uploads.ITEM_NAME + " = ?";
        int updated = mContentResolver.update(Uploads.CONTENT_URI, values, where, new String[]{"init item 1"});
        assertEquals(1, updated);
    }

    public void testDeleteAll() {
        int rowsDeleted = mContentResolver.delete(Uploads.CONTENT_URI, null, null);
        assertEquals(2, rowsDeleted);

        Cursor c = mContentResolver.query(Uploads.CONTENT_URI, null, null, null, null);
        assertEquals(0, c.getCount());
    }

    public void testDeleteById() {
    }

}

