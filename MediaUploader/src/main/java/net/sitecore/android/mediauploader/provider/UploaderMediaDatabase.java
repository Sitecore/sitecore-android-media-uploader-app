package net.sitecore.android.mediauploader.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;

public class UploaderMediaDatabase extends SQLiteOpenHelper {
    private static final String DB_NAME = "uploader_media.db";

    private static final int VERSION = 6;

    public interface Tables {
        String INSTANCES = "instances";
        String UPLOADS = "uploads";

        String UPLOADS_JOIN_INSTANCE = UPLOADS + " LEFT OUTER JOIN " + INSTANCES + " ON "
                + UPLOADS + "." + Uploads.INSTANCE_ID + "=" + INSTANCES + "." + Instances._ID;
    }

    public UploaderMediaDatabase(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.INSTANCES + " ("
                + Instances._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Instances.URL + " TEXT NOT NULL,"
                + Instances.LOGIN + " TEXT NOT NULL,"
                + Instances.PASSWORD + " TEXT NOT NULL,"
                + Instances.ROOT_FOLDER + " TEXT NOT NULL,"
                + Instances.DATABASE + " TEXT NOT NULL,"
                + Instances.SITE + " TEXT NOT NULL,"
                + Instances.PUBLIC_KEY + " TEXT NOT NULL,"
                + Instances.SELECTED + " INTEGER)");

        db.execSQL("CREATE TABLE " + Tables.UPLOADS + " ("
                + Uploads._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Uploads.INSTANCE_ID + " INTEGER, "
                + Uploads.ITEM_NAME + " TEXT NOT NULL, "
                + Uploads.FILE_URI + " TEXT NOT NULL, "
                + Uploads.IS_IMAGE + " INTEGER, "
                + Uploads.STATUS + " TEXT NOT NULL, "
                + Uploads.IMAGE_SIZE + " TEXT, "
                + Uploads.FAIL_MESSAGE + " TEXT, "
                + Uploads.ADDRESS_NAME + " TEXT, "
                + Uploads.COUNTRY_CODE + " TEXT, "
                + Uploads.ZIP_CODE + " TEXT, "
                + Uploads.LATITUDE + " REAL, "
                + Uploads.LONGITUDE + " REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Tables.INSTANCES);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.UPLOADS);

        onCreate(db);
    }

}
