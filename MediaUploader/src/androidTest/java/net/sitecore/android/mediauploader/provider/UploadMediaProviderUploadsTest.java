package net.sitecore.android.mediauploader.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;

import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;

public class UploadMediaProviderUploadsTest extends BaseUploadMediaProviderTest {

    private ContentValues newUploadValues(String itemName) {
        ContentValues values = new ContentValues();
        values.put(Uploads.ITEM_NAME, itemName);
        values.put(Uploads.FILE_URI, "http://test.com/image.png");
        values.put(Uploads.STATUS, UploadStatus.IN_PROGRESS.name());

        return values;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mResolver.insert(Uploads.CONTENT_URI, newUploadValues("init item 1"));
        mResolver.insert(Uploads.CONTENT_URI, newUploadValues("init item 2"));
    }

    public void testQuery() {
        Cursor c = mResolver.query(Uploads.CONTENT_URI, null, null, null, null);

        assertNotNull(c);
        assertEquals(2, c.getCount());
    }

    public void testInsert() {
        Uri itemUri = mResolver.insert(Uploads.CONTENT_URI, newUploadValues("new_item"));
        assertEquals("content://net.sitecore.android.provider/uploads/3", itemUri.toString());

        String id = Uploads.getUploadId(itemUri);
        assertEquals("3", id);

        String where = Uploads._ID + " = ?";

        Cursor c = mResolver.query(Uploads.CONTENT_URI, null, where, new String[]{id}, null);
        assertEquals(1, c.getCount());

        if (!c.moveToFirst()) fail("Not empty cursor expected.");
        String name = c.getString(c.getColumnIndex(Uploads.ITEM_NAME));
        assertEquals("new_item", name);
    }

    public void testUpdate() {
        ContentValues values = newUploadValues("updated");
        String where = Uploads.ITEM_NAME + " = ?";
        int updated = mResolver.update(Uploads.CONTENT_URI, values, where, new String[]{"init item 1"});
        assertEquals(1, updated);
    }

    public void testDeleteAll() {
        int rowsDeleted = mResolver.delete(Uploads.CONTENT_URI, null, null);
        assertEquals(2, rowsDeleted);

        Cursor c = mResolver.query(Uploads.CONTENT_URI, null, null, null, null);
        assertEquals(0, c.getCount());
    }


}

