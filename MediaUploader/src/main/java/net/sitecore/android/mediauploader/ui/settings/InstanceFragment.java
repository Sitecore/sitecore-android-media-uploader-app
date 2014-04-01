package net.sitecore.android.mediauploader.ui.settings;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.EditText;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.util.ScUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class InstanceFragment extends Fragment {
    private Instance mInstance;

    @InjectView(R.id.instance_url) EditText mInstanceUrl;
    @InjectView(R.id.instance_login) EditText mInstanceLogin;
    @InjectView(R.id.instance_password) EditText mInstancePassword;
    @InjectView(R.id.instance_database) EditText mInstanceDatabase;
    @InjectView(R.id.instance_site) EditText mInstanceSite;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_instance_fields, null);
        ButterKnife.inject(this, root);
        return root;
    }

    public boolean isFieldsValid() {
        boolean valid = true;

        String url = mInstanceUrl.getText().toString();
        if (TextUtils.isEmpty(url) || !URLUtil.isValidUrl(url)) {
            mInstanceUrl.setError(getString(R.string.error_wrong_instance_url));
            valid = false;
        }

        String login = mInstanceLogin.getText().toString();
        if (TextUtils.isEmpty(login)) {
            mInstanceLogin.setError(getString(R.string.error_wrong_instance_login));
            valid = false;
        }

        String password = mInstancePassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mInstancePassword.setError(getString(R.string.error_wrong_instance_password));
            valid = false;
        }

        String site = mInstanceSite.getText().toString();
        if (TextUtils.isEmpty(site) && site.startsWith("/")) {
            mInstanceSite.setError(getString(R.string.error_wrong_instance_database));
            valid = false;
        }

        String database = mInstanceDatabase.getText().toString();
        if (TextUtils.isEmpty(database)) {
            mInstanceDatabase.setError(getString(R.string.error_wrong_instance_database));
            valid = false;
        }

        return valid;
    }

    public void setSourceInstance(Instance instance) {
        mInstance = instance;
        initViews();
    }

    public Instance getEnteredInstance() {
        Instance instance = new Instance();
        instance.setUrl(mInstanceUrl.getText().toString());
        instance.setLogin(mInstanceLogin.getText().toString());
        instance.setPassword(mInstancePassword.getText().toString());
        instance.setDatabase(mInstanceDatabase.getText().toString());
        instance.setSite(mInstanceSite.getText().toString());
        if (mInstance != null) instance.setRootFolder(mInstance.getRootFolder());
        else instance.setRootFolder(ScUtils.PATH_MEDIA_LIBRARY);
        return instance;
    }

    private void initViews() {
        mInstanceUrl.setText(mInstance.getUrl());
        mInstanceLogin.setText(mInstance.getLogin());
        mInstancePassword.setText(mInstance.getPassword());
        mInstanceDatabase.setText(mInstance.getDatabase());
        mInstanceSite.setText(mInstance.getSite());
    }
}
