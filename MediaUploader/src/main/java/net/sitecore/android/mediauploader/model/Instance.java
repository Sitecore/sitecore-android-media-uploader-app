package net.sitecore.android.mediauploader.model;

import android.content.ContentValues;
import android.database.Cursor;

import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;

import static net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances.Query;

public class Instance {
    public final String url;
    public final String login;
    public final String password;
    public final String rootFolder;

    public Instance(String url, String login, String password, String rootFolder) {
        this.url = url;
        this.login = login;
        this.password = password;
        this.rootFolder = rootFolder;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(Instances.URL, url);
        values.put(Instances.LOGIN, login);
        values.put(Instances.PASSWORD, password);
        values.put(Instances.ROOT_FOLDER, rootFolder);
        return values;
    }

    public Instance(Cursor cursor) {
        this.url = cursor.getString(Query.URL);
        this.login= cursor.getString(Query.LOGIN);
        this.password = cursor.getString(Query.PASSWORD);
        this.rootFolder = cursor.getString(Query.ROOT_FOLDER);
    }
}
