package net.sitecore.android.mediauploader.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;

import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances.Query;

public class UploadMediaProviderInstancesTest extends ProviderTestCase2<UploadMediaProvider> {

    private final String URL_SAMPLE = "http://test.url";
    private final String LOGIN_SAMPLE = "test_login";
    private final String PASSWORD_SAMPLE = "test_password";
    private final String DEFAULT_FOLDER_SAMPLE = "/sitecore/media library";

    private ContentResolver mContentResolver;

    public UploadMediaProviderInstancesTest() {
        super(UploadMediaProvider.class, UploadMediaContract.CONTENT_AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContentResolver = getMockContentResolver();
    }

    public ContentValues getSampleValue() {
        ContentValues value = new ContentValues();
        value.put(Instances.URL, URL_SAMPLE);
        value.put(Instances.LOGIN, LOGIN_SAMPLE);
        value.put(Instances.PASSWORD, PASSWORD_SAMPLE);
        value.put(Instances.DEFAULT_FOLDER, DEFAULT_FOLDER_SAMPLE);

        return value;
    }

    private void checkCursor(Cursor c, int count) {
        assertNotNull(c);
        assertEquals(count, c.getCount());
    }

    public void testInsert() {

        mContentResolver.insert(Instances.CONTENT_URI, getSampleValue());

        Cursor c = mContentResolver.query(Instances.CONTENT_URI, null, null, null, null);
        checkCursor(c, 1);
    }

    public void testInsertMultiple() {
        ContentValues[] values = new ContentValues[3];
        for (int i = 0; i < values.length; i++) {
            values[i] = getSampleValue();
        }

        mContentResolver.bulkInsert(Instances.CONTENT_URI, values);

        Cursor c = mContentResolver.query(Instances.CONTENT_URI, null, null, null, null);
        checkCursor(c, values.length);
    }

    public void testDelete() {
        mContentResolver.insert(Instances.CONTENT_URI, getSampleValue());

        assertEquals(1, mContentResolver.delete(Instances.CONTENT_URI, null, null));

        Cursor c = mContentResolver.query(Instances.CONTENT_URI, null, null, null, null);
        checkCursor(c, 0);
    }

    public void testQuery() {
        mContentResolver.insert(Instances.CONTENT_URI, getSampleValue());

        Cursor c = mContentResolver.query(Instances.CONTENT_URI, Query.PROJECTION, null, null, null);
        checkCursor(c, 1);
        if (!c.moveToFirst()) fail("Trying to read cursor but it is empty");

        assertEquals(URL_SAMPLE, c.getString(Query.URL));
        assertEquals(LOGIN_SAMPLE, c.getString(Query.LOGIN));
        assertEquals(PASSWORD_SAMPLE, c.getString(Query.PASSWORD));
        assertEquals(DEFAULT_FOLDER_SAMPLE, c.getString(Query.DEFAULT_FOLDER));
    }

    public void testUpdate() {
        mContentResolver.insert(Instances.CONTENT_URI, getSampleValue());

        Cursor c = mContentResolver.query(Instances.CONTENT_URI, null, null, null, null);
        checkCursor(c, 1);

        ContentValues newValue = new ContentValues();
        newValue.put(Instances.URL, "updated_url");
        newValue.put(Instances.LOGIN, "updated_login");
        newValue.put(Instances.PASSWORD, "updated_password");
        newValue.put(Instances.DEFAULT_FOLDER, "updated_folder");

        String selection = Instances.LOGIN + "='" + LOGIN_SAMPLE + "'";
        assertEquals(1, mContentResolver.update(Instances.CONTENT_URI, newValue, selection, null));

        c = mContentResolver.query(Instances.CONTENT_URI, Query.PROJECTION, null, null, null);
        checkCursor(c, 1);
        if (!c.moveToFirst()) fail("Trying to read cursor but it is empty");

        assertEquals("updated_url", c.getString(Query.URL));
        assertEquals("updated_login", c.getString(Query.LOGIN));
        assertEquals("updated_password", c.getString(Query.PASSWORD));
        assertEquals("updated_folder", c.getString(Query.DEFAULT_FOLDER));
    }

    public void testUnsupportedOperation() {
        try {
            mContentResolver.insert(Uri.withAppendedPath(Instances.CONTENT_URI, "/error"),
                    getSampleValue());
            fail("UnsupportedOperationException should be here");
        } catch (Exception exception) {
            assertEquals(true, exception instanceof UnsupportedOperationException);
        }
    }

}
