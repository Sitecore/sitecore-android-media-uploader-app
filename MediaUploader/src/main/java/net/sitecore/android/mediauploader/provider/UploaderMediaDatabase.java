package net.sitecore.android.mediauploader.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import net.sitecore.android.mediauploader.provider.UploadMediaContract.InstancesColumns;

public class UploaderMediaDatabase extends SQLiteOpenHelper {
    private static final String DB_NAME = "uploader_media.db";

    private static final int VERSION = 1;

    interface Tables {
        String INSTANCES = "instances";
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
                + InstancesColumns.PASSWORD + " TEXT NOT NULL )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Tables.INSTANCES);

        onCreate(db);
    }

}
