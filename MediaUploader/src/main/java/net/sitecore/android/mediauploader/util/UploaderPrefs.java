package net.sitecore.android.mediauploader.util;

import android.content.Context;
import android.text.TextUtils;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.model.Instance;

public class UploaderPrefs {
    private final Prefs mPrefs;

    private UploaderPrefs(Context context) {
        mPrefs = Prefs.from(context);
    }

    public static UploaderPrefs from(Context context) {
        return new UploaderPrefs(context);
    }

    public void setSelectedInstance(Instance instance) {
        mPrefs.put(R.string.key_instance_url, instance.getUrl());
        mPrefs.put(R.string.key_instance_login, instance.getLogin());
        mPrefs.put(R.string.key_instance_password, instance.getPassword());
        mPrefs.put(R.string.key_instance_root_folder, instance.getRootFolder());
        mPrefs.put(R.string.key_instance_site, instance.getSite());
        mPrefs.put(R.string.key_instance_public_key, instance.getPublicKey());
        mPrefs.put(R.string.key_instance_database, instance.getDatabase());
    }

    public void cleanSelectedInstance() {
        mPrefs.put(R.string.key_instance_url, "");
        mPrefs.put(R.string.key_instance_login, "");
        mPrefs.put(R.string.key_instance_password, "");
        mPrefs.put(R.string.key_instance_root_folder, "");
        mPrefs.put(R.string.key_instance_site, "");
        mPrefs.put(R.string.key_instance_public_key, "");
        mPrefs.put(R.string.key_instance_database, "");
    }

    public Instance getCurrentInstance() {
        String url = mPrefs.getString(R.string.key_instance_url);
        if (TextUtils.isEmpty(url)) return null;

        Instance instance = new Instance();
        instance.setUrl(url);
        instance.setLogin(mPrefs.getString(R.string.key_instance_login));
        instance.setPassword(mPrefs.getString(R.string.key_instance_password));
        instance.setRootFolder(mPrefs.getString(R.string.key_instance_root_folder));
        instance.setDatabase(mPrefs.getString(R.string.key_instance_database));
        instance.setSite(mPrefs.getString(R.string.key_instance_site));
        instance.setPublicKey(mPrefs.getString(R.string.key_instance_public_key));
        instance.setSelected(true);
        return instance;
    }
}
