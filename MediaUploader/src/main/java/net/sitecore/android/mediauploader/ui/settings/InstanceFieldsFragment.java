package net.sitecore.android.mediauploader.ui.settings;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.RadioGroup;

import java.util.Locale;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.util.ScUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class InstanceFieldsFragment extends Fragment {

    private Instance mInstance;

    @InjectView(R.id.radio_protocol_type) RadioGroup mProtocol;
    @InjectView(R.id.instance_url) EditText mInstanceUrl;
    @InjectView(R.id.instance_site) EditText mInstanceSite;
    @InjectView(R.id.text_media_folder) EditText mInstanceFolder;
    @InjectView(R.id.instance_login) EditText mInstanceLogin;
    @InjectView(R.id.instance_password) EditText mInstancePassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_instance_fields, null);
        ButterKnife.inject(this, root);

        return root;
    }

    public boolean isFieldsValid() {
        mInstanceUrl.setError(null);
        mInstanceLogin.setError(null);
        mInstancePassword.setError(null);

        if (!isUrlValid()) return false;
        if (!isLoginValid()) return false;

        String password = mInstancePassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mInstancePassword.setError(getString(R.string.error_wrong_instance_password));
            return false;
        }

        return true;
    }

    private boolean isUrlValid() {
        String url = mInstanceUrl.getText().toString().toLowerCase(Locale.getDefault());
        if (TextUtils.isEmpty(url)) {
            mInstanceUrl.setError(getString(R.string.error_empty_instance_url));
            return false;
        } else {
            String fullUrl = getCheckedProtocol().concat(url);
            if (!URLUtil.isValidUrl(fullUrl)) {
                mInstanceUrl.setError(getString(R.string.error_wrong_instance_url, fullUrl));
                return false;
            }
            return true;
        }
    }

    private boolean isLoginValid() {
        String login = mInstanceLogin.getText().toString().toLowerCase(Locale.getDefault());
        if (TextUtils.isEmpty(login)) {
            mInstanceLogin.setError(getString(R.string.error_empty_instance_login));
            return false;
        } else {
            return true;
        }
    }

    public void setSourceInstance(Instance instance) {
        mInstance = instance;
        initViews();
    }

    public Instance getEnteredInstance() {
        Instance instance = new Instance();

        String fullUrl = getCheckedProtocol().concat(mInstanceUrl.getText().toString());
        instance.setUrl(fullUrl.toLowerCase(Locale.getDefault()));

        instance.setLogin(mInstanceLogin.getText().toString().toLowerCase(Locale.getDefault()));
        instance.setPassword(mInstancePassword.getText().toString());
        instance.setSite(mInstanceSite.getText().toString().toLowerCase(Locale.getDefault()));
        instance.setDatabase("master");
        if (mInstance != null) instance.setRootFolder(mInstance.getRootFolder());
        else instance.setRootFolder(ScUtils.PATH_MEDIA_LIBRARY);
        return instance;
    }

    private String getCheckedProtocol() {
        int checkedRadioButtonId = mProtocol.getCheckedRadioButtonId();
        String protocol;
        switch (checkedRadioButtonId) {
            case R.id.radio_protocol_http:
                protocol = getString(R.string.text_http_protocol);
                break;
            case R.id.radio_protocol_https:
                protocol = getString(R.string.text_https_protocol);
                break;
            default:
                protocol = getString(R.string.text_http_protocol);
        }
        return protocol;
    }

    private void initViews() {
        String url = mInstance.getUrl();

        String http = getString(R.string.text_http_protocol);
        String https = getString(R.string.text_https_protocol);

        if (url.contains(https)) {
            mProtocol.check(R.id.radio_protocol_https);
        } else {
            mProtocol.check(R.id.radio_protocol_http);
        }
        url = url.replace(http, "").replace(https, "");

        mInstanceUrl.setText(url);
        mInstanceSite.setText(mInstance.getSite());

        setMediaFolderText(mInstance.getRootFolder());

        mInstanceLogin.setText(mInstance.getLogin());
        mInstancePassword.setText(mInstance.getPassword());
    }

    private void setMediaFolderText(String mediaFolder) {
        if (TextUtils.isEmpty(mediaFolder)) {
            mInstanceFolder.setVisibility(View.GONE);
        } else {
            mInstanceFolder.setVisibility(View.VISIBLE);
            mInstanceFolder.setText(mediaFolder);
        }
    }
}
