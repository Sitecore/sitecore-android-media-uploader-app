package net.sitecore.android.mediauploader.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import net.sitecore.android.mediauploader.provider.UploadMediaContract.InstancesColumns;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;

public class UploaderMediaDatabase extends SQLiteOpenHelper {
    private static final String DB_NAME = "uploader_media.db";

    private static final int VERSION = 3;

    interface Tables {
        String INSTANCES = "instances";
        String UPLOADS = "uploads";
    }

    public UploaderMediaDatabase(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.INSTANCES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + InstancesColumns.URL + " TEXT NOT NULL,"
                + InstancesColumns.LOGIN + " TEXT NOT NULL,"
                + InstancesColumns.PASSWORD + " TEXT NOT NULL,"
                + InstancesColumns.ROOT_FOLDER + " TEXT NOT NULL )");

        db.execSQL("CREATE TABLE " + Tables.UPLOADS + " ("
                + Uploads._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Uploads.URL + " TEXT NOT NULL, "
                + Uploads.USERNAME + " TEXT NOT NULL, "
                + Uploads.PASSWORD + " TEXT NOT NULL, "
                + Uploads.ITEM_NAME + " TEXT NOT NULL, "
                + Uploads.ITEM_PATH + " TEXT NOT NULL, "
                + Uploads.FILE_URI + " TEXT NOT NULL, "
                + Uploads.STATUS + " TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Tables.INSTANCES);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.UPLOADS);

        onCreate(db);
    }

}
