package net.sitecore.android.mediauploader.model;

import android.content.ContentValues;

import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;

public class Instance {
    public final String name;
    public final String url;
    public final String login;
    public final String password;
    public final String rootFolder;

    public Instance(String name, String url, String login, String password, String rootFolder) {
        this.name = name;
        this.url = url;
        this.login = login;
        this.password = password;
        this.rootFolder = rootFolder;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(Instances.NAME, name);
        values.put(Instances.URL, url);
        values.put(Instances.LOGIN, login);
        values.put(Instances.PASSWORD, password);
        values.put(Instances.ROOT_FOLDER, rootFolder);
        return values;
    }
}
