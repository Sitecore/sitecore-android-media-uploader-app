package net.sitecore.android.mediauploader.ui.instancemanager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncQueryHandler;
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
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances.Query;
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.mediauploader.util.UploaderPrefs;
import net.sitecore.android.mediauploader.util.Utils;
import net.sitecore.android.sdk.api.RequestQueueProvider;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.ScApiSessionFactory;
import net.sitecore.android.sdk.api.model.ItemsResponse;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class EditInstanceActivity extends Activity implements LoaderCallbacks<Cursor>, ErrorListener, Listener<ScApiSession> {
    public static final int READ_INSTANCES_ACTION = 0;
    public static final int READ_NAMES_ACTION = 1;

    private Uri mInstanceUri;
    private boolean isEditorMode = false;
    private boolean isDefaultInstance = false;
    private long mInstanceId;

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
        ButterKnife.inject(this);

        if (isEditorMode) getLoaderManager().initLoader(READ_INSTANCES_ACTION, null, this);
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
                if (TextUtils.isEmpty(mInstanceName.getText().toString())) {
                    mInstanceName.setText(mInstanceUrl.getText());
                }
                startAsyncFieldValidation();
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

    boolean validateFields() {
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

    @OnClick(R.id.button_validate)
    public void validateConnection() {
        if (validateFields()) {
            String url = mInstanceUrl.getText().toString();
            String login = mInstanceLogin.getText().toString();
            String password = mInstancePassword.getText().toString();
            ScApiSessionFactory.getSession(RequestQueueProvider.getRequestQueue(this),
                    url, login, password, this, this);
        }
    }

    private void startAsyncFieldValidation() {
        getLoaderManager().restartLoader(READ_NAMES_ACTION, null, EditInstanceActivity.this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case READ_INSTANCES_ACTION:
                return new CursorLoader(this, mInstanceUri, Query.PROJECTION, null, null, null);
            case READ_NAMES_ACTION:
                Instance instance = getInstanceFromFields();
                return new CursorLoader(this, Instances.CONTENT_URI, NamesQuery.PROJECTION, NamesQuery.SELECTION,
                        new String[]{instance.name, String.valueOf(mInstanceId)}, null);
            default:
                return null;
        }
    }

    interface NamesQuery {
        String[] PROJECTION = new String[] {
                Instances._ID,
                Instances.NAME
        };

        String SELECTION = Instances.NAME + "=? and " + Instances._ID + "!=?";
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case READ_INSTANCES_ACTION: {
                if (!data.moveToFirst()) return;
                isDefaultInstance = UploaderPrefs.from(this).isDefaultInstance(data.getString(Query.NAME));
                mInstanceId = data.getLong(Query._ID);
                initViews(new Instance(data));
                break;
            }
            case READ_NAMES_ACTION: {
                if (data.moveToFirst()) {
                    mInstanceName.setError("Instance name already exists");
                    return;
                }
                saveInstanceIfValid();
                data.close();
                getLoaderManager().destroyLoader(READ_NAMES_ACTION);
                break;
            }
        }

    }

    private void saveInstanceIfValid() {
        if (validateFields()) {
            saveOrUpdateInstance();
            finish();
        }
    }

    private void initViews(Instance instance) {
        mInstanceName.setText(instance.name);
        mInstanceUrl.setText(instance.url);
        mInstanceLogin.setText(instance.login);
        mInstancePassword.setText(instance.password);
        mInstanceRootFolder.setText(instance.rootFolder);
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
        Listener<ItemsResponse> success = new Listener<ItemsResponse>() {
            @Override
            public void onResponse(ItemsResponse response) {
                if (response.getTotalCount() != 0) {
                    Toast.makeText(EditInstanceActivity.this, "Entered instance is valid", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(EditInstanceActivity.this, "Entered instance is not valid", Toast.LENGTH_LONG).show();
                }
            }
        };

        String folder = mInstanceRootFolder.getText().toString();
        RequestQueueProvider.getRequestQueue(this).add(session.getItems(success, this).byItemPath(folder).build());
    }

    private void saveOrUpdateInstance() {
        Instance instance = getInstanceFromFields();

        if (isDefaultInstance) {
            UploaderApp.from(this).switchInstance(instance);
        }

        if (isEditorMode) {
            new AsyncQueryHandler(getContentResolver()) {
            }.startUpdate(READ_INSTANCES_ACTION, null, mInstanceUri, instance.toContentValues(), null, null);
        } else {
            new AsyncQueryHandler(getContentResolver()) {
            }.startInsert(READ_INSTANCES_ACTION, null, Instances.CONTENT_URI, instance.toContentValues());
        }
    }

    private Instance getInstanceFromFields() {
        String name = mInstanceName.getText().toString();
        String url = mInstanceUrl.getText().toString();
        String login = mInstanceLogin.getText().toString();
        String password = mInstancePassword.getText().toString();
        String folder = mInstanceRootFolder.getText().toString();
        return new Instance(name, url, login, password, folder);
    }
}