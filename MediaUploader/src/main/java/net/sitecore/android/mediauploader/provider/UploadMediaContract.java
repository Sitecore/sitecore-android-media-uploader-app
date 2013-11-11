package net.sitecore.android.mediauploader.provider;

import android.net.Uri;
import android.provider.BaseColumns;

import static android.content.ContentResolver.CURSOR_DIR_BASE_TYPE;
import static android.content.ContentResolver.CURSOR_ITEM_BASE_TYPE;

public class UploadMediaContract {

    public static final String CONTENT_AUTHORITY = "net.sitecore.android.provider";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static class Instances implements BaseColumns, InstancesColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath("instances").build();

        public static final String CONTENT_TYPE = CURSOR_DIR_BASE_TYPE + "/vnd.sitecore.instances";
        public static final String CONTENT_ITEM_TYPE = CURSOR_ITEM_BASE_TYPE + "/vnd.sitecore.instance";

        public static Uri buildInstanceUri(String instanceId) {
            Uri temp = CONTENT_URI.buildUpon().appendPath(instanceId).build();
            return CONTENT_URI.buildUpon().appendPath(instanceId).build();
        }

        public static String getInstanceId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public interface Query {
            String[] PROJECTION = {
                    Instances._ID,
                    Instances.URL,
                    Instances.LOGIN,
                    Instances.PASSWORD,
                    Instances.ROOT_FOLDER
            };

            int _ID = 0;
            int URL = 1;
            int LOGIN = 2;
            int PASSWORD = 3;
            int ROOT_FOLDER = 4;
        }
    }

    public static final class Uploads implements UploadColumns, BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath("uploads").build();

        public static final String CONTENT_TYPE = CURSOR_DIR_BASE_TYPE + "/vnd.sitecore.uploads";
        public static final String CONTENT_ITEM_TYPE = CURSOR_ITEM_BASE_TYPE + "/vnd.sitecore.upload";

        public static String getUploadId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public interface Status {
            String PENDING = "pending";
            String DONE = "done";
            String IN_PROGRESS = "in_progress";
            String ERROR = "error";
        }

    }

    interface InstancesColumns {
        String URL = "instance_url";
        String LOGIN = "instance_login";
        String PASSWORD = "instance_password";
        String ROOT_FOLDER = "root_folder";
    }

    interface UploadColumns {
        String URL = "instance_url";
        String USERNAME = "instance_login";
        String PASSWORD = "instance_password";
        String ITEM_NAME = "item_name";
        String FILE_URI = "file_uri";
        String STATUS = "status";
    }
}
