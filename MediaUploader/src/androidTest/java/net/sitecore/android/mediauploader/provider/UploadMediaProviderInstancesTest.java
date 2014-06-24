package net.sitecore.android.mediauploader.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances.Query;

public class UploadMediaProviderInstancesTest extends BaseUploadMediaProviderTest {

    interface TestItem {
        String URL = "http://test.url";
        String LOGIN = "test_login";
        String PASSWORD = "test_password";
        String DEFAULT_FOLDER = "/sitecore/media library";
        String DATABASE = "web";
        String SITE = "/sitecore/shell";
    }

    interface UpdatedTestItem {

        String UPDATED_NAME = "updated_name";
        String UPDATED_URL = "updated_url";
        String UPDATED_LOGIN = "updated_login";
        String UPDATED_PASSWORD = "updated_password";
        String UPDATED_DEFAULT_FOLDER = "updated_folder";
    }

    public ContentValues getSampleValue() {
        ContentValues value = new ContentValues();
        value.put(Instances.URL, TestItem.URL);
        value.put(Instances.LOGIN, TestItem.LOGIN);
        value.put(Instances.PASSWORD, TestItem.PASSWORD);
        value.put(Instances.ROOT_FOLDER, TestItem.DEFAULT_FOLDER);
        value.put(Instances.DATABASE, TestItem.DATABASE);
        value.put(Instances.SITE, TestItem.SITE);
        value.put(Instances.PUBLIC_KEY, "pub key goes here");

        return value;
    }

    private void checkCursor(Cursor c, int count) {
        assertNotNull(c);
        assertEquals(count, c.getCount());
    }

    public void testInsert() {
        mResolver.insert(Instances.CONTENT_URI, getSampleValue());

        Cursor c = mResolver.query(Instances.CONTENT_URI, null, null, null, null);
        checkCursor(c, 1);
    }

    public void testInsertMultiple() {
        ContentValues[] values = new ContentValues[3];
        for (int i = 0; i < values.length; i++) {
            values[i] = getSampleValue();
        }

        mResolver.bulkInsert(Instances.CONTENT_URI, values);

        Cursor c = mResolver.query(Instances.CONTENT_URI, null, null, null, null);
        checkCursor(c, values.length);
    }

    public void testDelete() {
        mResolver.insert(Instances.CONTENT_URI, getSampleValue());

        assertEquals(1, mResolver.delete(Instances.CONTENT_URI, null, null));

        Cursor c = mResolver.query(Instances.CONTENT_URI, null, null, null, null);
        checkCursor(c, 0);
    }

    public void testQuery() {
        mResolver.insert(Instances.CONTENT_URI, getSampleValue());

        Cursor c = mResolver.query(Instances.CONTENT_URI, Query.PROJECTION, null, null, null);
        checkCursor(c, 1);
        if (!c.moveToFirst()) fail("Trying to read cursor but it is empty");

        assertEquals(TestItem.URL, c.getString(Query.URL));
        assertEquals(TestItem.LOGIN, c.getString(Query.LOGIN));
        assertEquals(TestItem.PASSWORD, c.getString(Query.PASSWORD));
        assertEquals(TestItem.DEFAULT_FOLDER, c.getString(Query.ROOT_FOLDER));
    }

    public void testUpdate() {
        mResolver.insert(Instances.CONTENT_URI, getSampleValue());

        Cursor c = mResolver.query(Instances.CONTENT_URI, null, null, null, null);
        checkCursor(c, 1);

        ContentValues newValue = new ContentValues();
        newValue.put(Instances.URL, UpdatedTestItem.UPDATED_URL);
        newValue.put(Instances.LOGIN, UpdatedTestItem.UPDATED_LOGIN);
        newValue.put(Instances.PASSWORD, UpdatedTestItem.UPDATED_PASSWORD);
        newValue.put(Instances.ROOT_FOLDER, UpdatedTestItem.UPDATED_DEFAULT_FOLDER);

        String selection = Instances.LOGIN + "='" + TestItem.LOGIN + "'";
        assertEquals(1, mResolver.update(Instances.CONTENT_URI, newValue, selection, null));

        c = mResolver.query(Instances.CONTENT_URI, Query.PROJECTION, null, null, null);
        checkCursor(c, 1);
        if (!c.moveToFirst()) fail("Trying to read cursor but it is empty");

        assertEquals(UpdatedTestItem.UPDATED_URL, c.getString(Query.URL));
        assertEquals(UpdatedTestItem.UPDATED_LOGIN, c.getString(Query.LOGIN));
        assertEquals(UpdatedTestItem.UPDATED_PASSWORD, c.getString(Query.PASSWORD));
        assertEquals(UpdatedTestItem.UPDATED_DEFAULT_FOLDER, c.getString(Query.ROOT_FOLDER));
    }

    public void testGetInstanceById() {
        Uri resultUri = mResolver.insert(Instances.CONTENT_URI, getSampleValue());
        assertNotNull(resultUri);

        Cursor cursor = mResolver.query(resultUri, Query.PROJECTION, null, null,
                null);
        checkCursor(cursor, 1);
    }

    public void testDeleteInstanceById() {
        Uri resultUri = mResolver.insert(Instances.CONTENT_URI, getSampleValue());
        assertNotNull(resultUri);

        int count = mResolver.delete(resultUri, null, null);
        assertEquals(1, count);
    }

    public void testUpdateInstanceById() {
        Uri resultUri = mResolver.insert(Instances.CONTENT_URI, getSampleValue());
        assertNotNull(resultUri);

        ContentValues values = new ContentValues();
        values.put(Instances.URL, UpdatedTestItem.UPDATED_URL);
        values.put(Instances.LOGIN, UpdatedTestItem.UPDATED_LOGIN);
        values.put(Instances.PASSWORD, UpdatedTestItem.UPDATED_PASSWORD);
        values.put(Instances.ROOT_FOLDER, UpdatedTestItem.UPDATED_DEFAULT_FOLDER);

        int count = mResolver.update(resultUri, values, null, null);
        assertEquals(1, count);

        Cursor cursor = mResolver.query(resultUri, Query.PROJECTION, null, null,
                null);
        checkCursor(cursor, 1);
        if (!cursor.moveToFirst()) fail("Trying to read cursor but it is empty");

        assertEquals(UpdatedTestItem.UPDATED_URL, cursor.getString(Query.URL));
        assertEquals(UpdatedTestItem.UPDATED_LOGIN, cursor.getString(Query.LOGIN));
        assertEquals(UpdatedTestItem.UPDATED_PASSWORD, cursor.getString(Query.PASSWORD));
        assertEquals(UpdatedTestItem.UPDATED_DEFAULT_FOLDER, cursor.getString(Query.ROOT_FOLDER));
    }

    public void testUnsupportedOperation() {
        try {
            mResolver.insert(Uri.withAppendedPath(Instances.CONTENT_URI, "/error"),
                    getSampleValue());
            fail("UnsupportedOperationException should be here");
        } catch (Exception exception) {
            assertEquals(true, exception instanceof UnsupportedOperationException);
        }
    }

}
