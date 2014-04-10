package net.sitecore.android.mediauploader.ui.settings;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.util.ScUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class InstanceFieldsFragment extends Fragment {

    private static final int HTTPS_POSITION = 1;
    private static final int HTTP_POSITION = 0;

    private Instance mInstance;

    @InjectView(R.id.spinner_protocol) Spinner mProtocol;
    @InjectView(R.id.instance_url) EditText mInstanceUrl;
    @InjectView(R.id.instance_login) EditText mInstanceLogin;
    @InjectView(R.id.instance_password) EditText mInstancePassword;
    @InjectView(R.id.instance_site) EditText mInstanceSite;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_instance_fields, null);
        ButterKnife.inject(this, root);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.protocols,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mProtocol.setAdapter(adapter);

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
        String url = mInstanceUrl.getText().toString().toLowerCase();
        if (TextUtils.isEmpty(url)) {
            mInstanceUrl.setError(getString(R.string.error_empty_instance_url));
            return false;
        } else {
            String protocol = (String) mProtocol.getSelectedItem();
            String fullUrl = protocol.concat(url);
            if (!URLUtil.isValidUrl(fullUrl)) {
                mInstanceUrl.setError(getString(R.string.error_wrong_instance_url, fullUrl));
                return false;
            }
            return true;
        }
    }

    private boolean isLoginValid() {
        String login = mInstanceLogin.getText().toString().toLowerCase();
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

        String protocol = (String) mProtocol.getSelectedItem();
        String fullUrl = protocol.concat(mInstanceUrl.getText().toString());
        instance.setUrl(fullUrl.toLowerCase());

        instance.setLogin(mInstanceLogin.getText().toString().toLowerCase());
        instance.setPassword(mInstancePassword.getText().toString());
        instance.setSite(mInstanceSite.getText().toString().toLowerCase());
        instance.setDatabase("master");
        if (mInstance != null) instance.setRootFolder(mInstance.getRootFolder());
        else instance.setRootFolder(ScUtils.PATH_MEDIA_LIBRARY);
        return instance;
    }

    private void initViews() {
        String url = mInstance.getUrl();

        String[] schemesArray = getResources().getStringArray(R.array.protocols);
        String http = schemesArray[0];
        String https = schemesArray[1];

        if (url.contains(https)) {
            mProtocol.setSelection(HTTPS_POSITION);
        } else {
            mProtocol.setSelection(HTTP_POSITION);
        }
        url = url.replace(http, "").replace(https, "");

        mInstanceUrl.setText(url);
        mInstanceLogin.setText(mInstance.getLogin());
        mInstancePassword.setText(mInstance.getPassword());
        mInstanceSite.setText(mInstance.getSite());
    }
}
