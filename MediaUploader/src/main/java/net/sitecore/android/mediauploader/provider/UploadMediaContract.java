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
            return CONTENT_URI.buildUpon().appendPath(instanceId).build();
        }

        public static String getInstanceId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public interface Query {
            public String[] PROJECTION = {
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

        public static Uri buildUploadUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public interface Query {
            public String[] PROJECTION = {
                    Uploads._ID,
                    Uploads.URL,
                    Uploads.USERNAME,
                    Uploads.PASSWORD,
                    Uploads.ITEM_NAME,
                    Uploads.FILE_URI,
                    Uploads.STATUS,
                    Uploads.ITEM_PATH
            };

            int _ID = 0;
            int URL = 1;
            int USERNAME = 2;
            int PASSWORD = 3;
            int ITEM_NAME = 4;
            int FILE_URI = 5;
            int STATUS = 6;
            int ITEM_PATH = 7;

            public String ORDER_BY_STATUS = Uploads.STATUS + " desc";
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
        String ITEM_PATH = "item_path";
        String FILE_URI = "file_uri";
        String STATUS = "status";
    }
}
