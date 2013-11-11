package net.sitecore.android.mediauploader.provider;

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

import static net.sitecore.android.sdk.api.LogUtils.LOGD;

public class UploadMediaProvider extends ContentProvider {

    private static final int INSTANCES = 100;

    private static final int UPLOADS = 200;
    private static final int UPLOAD_ID = 201;

    private UploaderMediaDatabase mDatabaseHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = UploadMediaContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "instances", INSTANCES);

        matcher.addURI(authority, "uploads", UPLOADS);

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
                db.insertOrThrow(Tables.INSTANCES, null, values);
                resolver.notifyChange(Instances.CONTENT_URI, null);
                return Instances.buildInstanceUri(values.getAsString(Instances._ID));
            }

            case UPLOADS: {
                long id = db.insertOrThrow(Tables.UPLOADS, null, values);
                resolver.notifyChange(Uploads.CONTENT_URI, null);
                return Instances.buildInstanceUri(Long.toString(id));
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

            default:
                throw new UnsupportedOperationException("Unknown delete uri: " + uri);
        }

        int result = builder.where(selection, selectionArgs).delete(db);
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

            default:
                throw new UnsupportedOperationException("Unknown update uri: " + uri);
        }

        int result = builder.where(selection, selectionArgs).update(db, values);
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
