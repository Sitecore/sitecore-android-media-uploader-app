package net.sitecore.android.mediauploader.provider;

import android.app.ActionBar.Tab;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.ArrayList;

import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;
import net.sitecore.android.mediauploader.provider.UploaderMediaDatabase.Tables;
import net.sitecore.android.sdk.api.provider.SelectionBuilder;

import static net.sitecore.android.sdk.api.internal.LogUtils.LOGD;

public class UploadMediaProvider extends ContentProvider {

    private static final int INSTANCES = 100;
    private static final int INSTANCE_ID = 101;

    private static final int UPLOADS = 200;
    private static final int UPLOAD_ID = 201;
    private static final int UPLOAD_ID_INSTANCE = 202;

    private UploaderMediaDatabase mDatabaseHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = UploadMediaContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "instances", INSTANCES);
        matcher.addURI(authority, "instances/*", INSTANCE_ID);

        matcher.addURI(authority, "uploads", UPLOADS);
        matcher.addURI(authority, "uploads/#", UPLOAD_ID);
        matcher.addURI(authority, "uploads/#/instance", UPLOAD_ID_INSTANCE);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new UploaderMediaDatabase(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case INSTANCES:
                return Instances.CONTENT_TYPE;

            case INSTANCE_ID:
                return Instances.CONTENT_ITEM_TYPE;

            case UPLOADS:
                return Uploads.CONTENT_TYPE;

            case UPLOAD_ID:
                return Uploads.CONTENT_ITEM_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        final SelectionBuilder builder = new SelectionBuilder();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INSTANCES: {
                builder.table(Tables.INSTANCES);
                break;
            }

            case UPLOADS: {
                builder.table(Tables.UPLOADS);
                break;
            }

            case INSTANCE_ID: {
                String instanceId = Instances.getInstanceId(uri);
                builder.table(Tables.INSTANCES).where(Instances._ID + "=?", instanceId);
                break;
            }

            case UPLOAD_ID: {
                String uploadId = Uploads.getUploadId(uri);
                builder.table(Tables.UPLOADS).where(Uploads._ID + "=?", uploadId);
                break;
            }

            case UPLOAD_ID_INSTANCE: {
                String uploadId = Uploads.getUploadId(uri);
                builder.table(Tables.UPLOADS_JOIN_INSTANCE)
                        // upload
                        .mapToTable(Uploads._ID, Tables.UPLOADS)
                        .mapToTable(Uploads.INSTANCE_ID, Tables.UPLOADS)
                        .mapToTable(Uploads.FILE_URI, Tables.UPLOADS)
                        .mapToTable(Uploads.STATUS, Tables.UPLOADS)
                        .mapToTable(Uploads.FAIL_MESSAGE, Tables.UPLOADS)
                        // instance
                        .mapToTable(Instances.URL, Tables.INSTANCES)
                        .mapToTable(Instances.LOGIN, Tables.INSTANCES)
                        .mapToTable(Instances.PASSWORD, Tables.INSTANCES)
                        .mapToTable(Instances.ROOT_FOLDER, Tables.INSTANCES)
                        .mapToTable(Instances.DATABASE, Tables.INSTANCES)
                        .mapToTable(Instances.SITE, Tables.INSTANCES)
                        .mapToTable(Instances.PUBLIC_KEY, Tables.INSTANCES)
                        .where(Tables.UPLOADS + "." + Uploads._ID + "=?", uploadId);
                break;
            }

            default:
                throw new UnsupportedOperationException("Not supported query uri: " + uri);
        }

        Cursor c = builder.where(selection, selectionArgs).query(db, projection, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        LOGD("Insert: " + uri);
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        ContentResolver resolver = getContext().getContentResolver();
        switch (match) {
            case INSTANCES: {
                long id = db.insertOrThrow(Tables.INSTANCES, null, values);
                resolver.notifyChange(Instances.CONTENT_URI, null);
                return Instances.buildInstanceUri(String.valueOf(id));
            }

            case UPLOADS: {
                long id = db.insertOrThrow(Tables.UPLOADS, null, values);
                resolver.notifyChange(Uploads.CONTENT_URI, null);
                return Uploads.buildUploadUri(Long.toString(id));
            }

            default:
                throw new UnsupportedOperationException("Unknown insert uri: " + uri);
        }

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case INSTANCES:
                builder.table(Tables.INSTANCES);
                break;

            case UPLOADS:
                builder.table(Tables.UPLOADS);
                break;

            case INSTANCE_ID:
                String instanceID = Instances.getInstanceId(uri);
                builder.table(Tables.INSTANCES).where(Instances._ID + "=?", instanceID);
                break;

            default:
                throw new UnsupportedOperationException("Unknown delete uri: " + uri);
        }

        int result = builder.where(selection, selectionArgs).delete(db);
        if (match == INSTANCE_ID) {
            getContext().getContentResolver().notifyChange(Instances.CONTENT_URI, null);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case INSTANCES:
                builder.table(Tables.INSTANCES);
                break;

            case UPLOADS:
                builder.table(Tables.UPLOADS);
                break;

            case UPLOAD_ID:
                String uploadId = Uploads.getUploadId(uri);
                builder.table(Tables.UPLOADS).where(Uploads._ID + "=?", uploadId);
                break;

            case INSTANCE_ID:
                String instanceId = Instances.getInstanceId(uri);
                builder.table(Tables.INSTANCES).where(Instances._ID + "=?", instanceId);
                break;

            default:
                throw new UnsupportedOperationException("Unknown update uri: " + uri);
        }

        int result = builder.where(selection, selectionArgs).update(db, values);
        if (match == INSTANCE_ID) {
            getContext().getContentResolver().notifyChange(Instances.CONTENT_URI, null);
        }

        if (match == UPLOAD_ID) {
            getContext().getContentResolver().notifyChange(Uploads.CONTENT_URI, null);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            LOGD("Applying %d operations.", numOperations);
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }
}
