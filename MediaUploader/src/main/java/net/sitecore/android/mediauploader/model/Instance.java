package net.sitecore.android.mediauploader.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;

import static net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances.Query;

public class Instance implements Parcelable {
    private String url;
    private String login;
    private String password;
    private String rootFolder;
    private String database;
    private String site;
    private String publicKey;
    private boolean isSelected;

    public Instance() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRootFolder() {
        return rootFolder;
    }

    public void setRootFolder(String rootFolder) {
        this.rootFolder = rootFolder;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(Instances.URL, url);
        values.put(Instances.LOGIN, login);
        values.put(Instances.PASSWORD, password);
        values.put(Instances.ROOT_FOLDER, rootFolder);
        values.put(Instances.DATABASE, database);
        values.put(Instances.SITE, site);
        values.put(Instances.PUBLIC_KEY, publicKey);
        values.put(Instances.SELECTED, isSelected ? 1 : 0);
        return values;
    }

    public Instance(Cursor cursor) {
        this.url = cursor.getString(Query.URL);
        this.login = cursor.getString(Query.LOGIN);
        this.password = cursor.getString(Query.PASSWORD);
        this.rootFolder = cursor.getString(Query.ROOT_FOLDER);
        this.database = cursor.getString(Query.DATABASE);
        this.site = cursor.getString(Query.SITE);
        this.publicKey = cursor.getString(Query.PUBLIC_KEY);
        this.isSelected = (cursor.getInt(Query.SELECTED) != 0);
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeString(this.login);
        dest.writeString(this.password);
        dest.writeString(this.rootFolder);
        dest.writeString(this.database);
        dest.writeString(this.site);
        dest.writeString(this.publicKey);
        dest.writeByte(isSelected ? (byte) 1 : (byte) 0);
    }

    private Instance(Parcel in) {
        this.url = in.readString();
        this.login = in.readString();
        this.password = in.readString();
        this.rootFolder = in.readString();
        this.database = in.readString();
        this.site = in.readString();
        this.publicKey = in.readString();
        this.isSelected = in.readByte() != 0;
    }

    public static Creator<Instance> CREATOR = new Creator<Instance>() {
        public Instance createFromParcel(Parcel source) {
            return new Instance(source);
        }

        public Instance[] newArray(int size) {
            return new Instance[size];
        }
    };
}
