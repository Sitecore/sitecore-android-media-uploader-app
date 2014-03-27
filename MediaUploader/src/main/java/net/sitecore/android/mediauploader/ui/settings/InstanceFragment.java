package net.sitecore.android.mediauploader.ui.settings;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.ScApiSessionFactory;
import net.sitecore.android.sdk.api.ScRequestQueue;
import net.sitecore.android.sdk.ui.ItemsListBrowserFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnFocusChanged;

public class InstanceFragment extends Fragment {
    private Instance mInstance;

    @InjectView(R.id.instance_url) EditText mInstanceUrl;
    @InjectView(R.id.instance_login) EditText mInstanceLogin;
    @InjectView(R.id.instance_password) EditText mInstancePassword;
    @InjectView(R.id.instance_root_folder) EditText mInstanceRootFolder;

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
            mInstanceUrl.setError("Please enter valid CMS Instance url");
            valid = false;
        }

        String login = mInstanceLogin.getText().toString();
        if (TextUtils.isEmpty(login)) {
            mInstanceLogin.setError("Please enter valid login");
            valid = false;
        }

        String password = mInstancePassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mInstancePassword.setError("Please enter valid password");
            valid = false;
        }

        String folder = mInstanceRootFolder.getText().toString();
        if (TextUtils.isEmpty(folder)) {
            mInstanceRootFolder.setText(ScUtils.PATH_MEDIA_LIBRARY);
        }
        return valid;
    }

    public void setInstance(Instance instance) {
        mInstance = instance;
        initViews();
    }

    public Instance getInstance() {
        String url = mInstanceUrl.getText().toString();
        String login = mInstanceLogin.getText().toString();
        String password = mInstancePassword.getText().toString();
        String folder = mInstanceRootFolder.getText().toString();
        return new Instance(url, login, password, folder);
    }

    private void initViews() {
        mInstanceUrl.setText(mInstance.url);
        mInstanceLogin.setText(mInstance.login);
        mInstancePassword.setText(mInstance.password);
        mInstanceRootFolder.setText(mInstance.rootFolder);
    }

    @SuppressWarnings("unused")
    @OnFocusChanged(R.id.instance_root_folder)
    void focusChanged() {
        if (mInstanceRootFolder.hasFocus()) {
            showFolderSelectionDialog();
        }
    }

    @OnClick(R.id.instance_root_folder)
    void clicked() {
        showFolderSelectionDialog();
    }

    void showFolderSelectionDialog() {
        final FolderSelectionDialog selectionDialog = new FolderSelectionDialog();
        selectionDialog.show(getFragmentManager(), "tag");
        selectionDialog.setRootFolder(ScUtils.PATH_MEDIA_LIBRARY);

        String url = mInstanceUrl.getText().toString();
        String login = mInstanceLogin.getText().toString();
        String password = mInstancePassword.getText().toString();

        Listener<ScApiSession> successListener = new Listener<ScApiSession>() {
            @Override
            public void onResponse(ScApiSession response) {
                selectionDialog.loadContent(response);
            }
        };

        ErrorListener errorListener = new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "Connection error", Toast.LENGTH_LONG);
            }
        };

        ScApiSessionFactory.getSession(new ScRequestQueue(getActivity().getContentResolver()), url, login, password,
                successListener, errorListener);
    }

    private class FolderSelectionDialog extends ItemsListBrowserFragment {
        @Override
        protected View onCreateFooterView(LayoutInflater inflater) {
            View v = inflater.inflate(R.layout.media_folder_selection_dialog_footer, null);

            v.findViewById(R.id.button_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getCurrentItem() != null) mInstanceRootFolder.setText(getCurrentItem().getPath());
                    else mInstanceRootFolder.setText(ScUtils.PATH_MEDIA_LIBRARY);
                    dismiss();
                }
            });

            v.findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            return v;
        }
    }

}
