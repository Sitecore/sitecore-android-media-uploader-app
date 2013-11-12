package net.sitecore.android.mediauploader.ui.instancemanager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances.Query;
import net.sitecore.android.mediauploader.util.Prefs;
import net.sitecore.android.mediauploader.util.Utils;
import net.sitecore.android.sdk.api.RequestQueueProvider;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.model.ItemsResponse;

import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Views;

public class EditInstanceActivity extends Activity implements LoaderCallbacks<Cursor>, ErrorListener, Listener<ScApiSession> {
    private Uri mInstanceUri;
    private boolean isEditorMode = false;
    private boolean isDefaultInstance = false;

    @InjectView(R.id.instance_name) EditText mInstanceName;
    @InjectView(R.id.instance_url) EditText mInstanceUrl;
    @InjectView(R.id.instance_login) EditText mInstanceLogin;
    @InjectView(R.id.instance_password) EditText mInstancePassword;
    @InjectView(R.id.instance_root_folder) EditText mInstanceRootFolder;
    @InjectView(R.id.button_validate) Button mValidate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInstanceUri = getIntent().getData();
        if (mInstanceUri != null) isEditorMode = true;

        initActionBar();

        setContentView(R.layout.activity_edit_instance);
        Views.inject(this);

        if (isEditorMode) getLoaderManager().initLoader(0, null, this);
    }

    private void initActionBar() {
        final LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);

        final View customActionBarView = inflater.inflate(R.layout.layout_cancel_ok, null);

        TextView buttonTitle = (TextView) customActionBarView.findViewById(R.id.textview_button_title);
        if (isEditorMode) buttonTitle.setText("Save");
        else buttonTitle.setText("Done");

        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validate()) return;
                saveOrUpdateInstance();
                finish();
            }
        });
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    boolean validate() {
        boolean valid = true;

        String name = mInstanceName.getText().toString();
        if (TextUtils.isEmpty(name)) {
            mInstanceName.setError("Please enter Instance name");
            valid = false;
        }

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
        return valid;
    }

    @OnClick(R.id.button_validate)
    public void validateConnection() {
        if (validate()) {
            String url = mInstanceUrl.getText().toString();
            String login = mInstanceLogin.getText().toString();
            String password = mInstancePassword.getText().toString();
            ScApiSession.getSession(this, url, login, password, this, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, mInstanceUri, Query.PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) return;

        String defaultInstanceName = Utils.getDefaultInstanceName(this);
        isDefaultInstance = data.getString(Query.NAME).equals(defaultInstanceName);

        initViews(data);
    }

    private void initViews(Cursor cursor) {
        mInstanceName.setText(cursor.getString(Query.NAME));
        mInstanceUrl.setText(cursor.getString(Query.URL));
        mInstanceLogin.setText(cursor.getString(Query.LOGIN));
        mInstancePassword.setText(cursor.getString(Query.PASSWORD));
        mInstanceRootFolder.setText(cursor.getString(Query.ROOT_FOLDER));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this, Utils.getMessageFromError(error), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResponse(ScApiSession session) {
        session.setShouldCache(true);
        Listener<ItemsResponse> success = new Listener<ItemsResponse>() {
            @Override
            public void onResponse(ItemsResponse response) {
                if (response.isSuccess()) {
                    Toast.makeText(EditInstanceActivity.this, "Entered instance is valid", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(EditInstanceActivity.this, "Entered instance is not valid", Toast.LENGTH_LONG).show();
                }
            }
        };

        RequestQueueProvider.getRequestQueue(this).add(session.getItems(success, this).build());
    }

    private void saveOrUpdateInstance() {
        String name = mInstanceName.getText().toString();
        String url = mInstanceUrl.getText().toString();
        String login = mInstanceLogin.getText().toString();
        String password = mInstancePassword.getText().toString();
        String folder = mInstanceRootFolder.getText().toString();

        if (isDefaultInstance) {
            Prefs prefs = Prefs.from(this);

            prefs.put(R.string.key_instance_name, name);
            prefs.put(R.string.key_instance_url, url);
            prefs.put(R.string.key_instance_login, login);
            prefs.put(R.string.key_instance_password, password);
        }

        ContentValues values = new ContentValues();
        values.put(Instances.NAME, name);
        values.put(Instances.URL, url);
        values.put(Instances.LOGIN, login);
        values.put(Instances.PASSWORD, password);
        values.put(Instances.ROOT_FOLDER, folder);

        if (isEditorMode) {
            new AsyncQueryHandler(getContentResolver()) {}
                    .startUpdate(0, null, mInstanceUri, values, null, null);
        } else {
            new AsyncQueryHandler(getContentResolver()) {}
                    .startInsert(0, null, Instances.CONTENT_URI, values);
        }
    }
}