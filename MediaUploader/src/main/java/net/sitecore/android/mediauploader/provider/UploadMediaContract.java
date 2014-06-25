package net.sitecore.android.mediauploader.provider;

import android.net.Uri;
import android.provider.BaseColumns;

import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploaderMediaDatabase.Tables;

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
                    Instances.ROOT_FOLDER,
                    Instances.DATABASE,
                    Instances.SITE,
                    Instances.PUBLIC_KEY,
                    Instances.SELECTED
            };

            int _ID = 0;
            int URL = 1;
            int LOGIN = 2;
            int PASSWORD = 3;
            int ROOT_FOLDER = 4;
            int DATABASE = 5;
            int SITE = 6;
            int PUBLIC_KEY = 7;
            int SELECTED = 8;
        }
    }

    public static final class Uploads implements UploadColumns, BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath("uploads").build();

        public static final Uri UPLOADS_JOIN_INSTANCE_URI = CONTENT_URI.buildUpon().appendPath("instances").build();

        public static final String CONTENT_TYPE = CURSOR_DIR_BASE_TYPE + "/vnd.sitecore.uploads";
        public static final String CONTENT_ITEM_TYPE = CURSOR_ITEM_BASE_TYPE + "/vnd.sitecore.upload";

        public static String getUploadId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static Uri buildUploadUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static Uri buildUploadWithInstanceUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).appendPath("instance").build();
        }

        public interface Query {
            public String[] PROJECTION = {
                    Uploads._ID,
                    Uploads.INSTANCE_ID,
                    Uploads.ITEM_NAME,
                    Uploads.FILE_URI,
                    Uploads.STATUS,
                    Uploads.IMAGE_SIZE,
                    Uploads.FAIL_MESSAGE,
                    Uploads.ADDRESS_NAME,
                    Uploads.COUNTRY_CODE,
                    Uploads.ZIP_CODE,
                    Uploads.LATITUDE,
                    Uploads.LONGITUDE,
                    Uploads.IS_IMAGE
            };

            int _ID = 0;
            int INSTANCE_ID = 1;
            int ITEM_NAME = 2;
            int FILE_URI = 3;
            int STATUS = 4;
            int IMAGE_SIZE = 5;
            int FAIL_MESSAGE = 6;
            int ADDRESS_NAME = 7;
            int COUNTRY_CODE = 8;
            int ZIP_CODE = 9;
            int LATITUDE = 10;
            int LONGITUDE = 11;
            int IS_IMAGE = 12;

            public String SELECTION_COMPLETED_UPLOADS = Uploads.STATUS + "='" + UploadStatus.DONE + "'";
            public String SELECTION_NOT_COMPLETED_UPLOADS = Uploads.STATUS + "!='" + UploadStatus.DONE + "'";

            public String ORDER_BY_STATUS = Uploads.STATUS + " desc";
            public String ORDER_BY_TIME_ADDED = Tables.UPLOADS + "." + Uploads._ID + " desc";
        }

        public interface UploadWithInstanceQuery {

            public String[] PROJECTION = {
                    Uploads._ID,
                    Uploads.INSTANCE_ID,
                    Uploads.ITEM_NAME,
                    Uploads.FILE_URI,
                    Uploads.IS_IMAGE,
                    Uploads.STATUS,
                    Uploads.IMAGE_SIZE,
                    Uploads.FAIL_MESSAGE,
                    Uploads.ADDRESS_NAME,
                    Uploads.COUNTRY_CODE,
                    Uploads.ZIP_CODE,
                    Uploads.LATITUDE,
                    Uploads.LONGITUDE,
                    Instances.URL,
                    Instances.LOGIN,
                    Instances.PASSWORD,
                    Instances.ROOT_FOLDER,
                    Instances.DATABASE,
                    Instances.SITE,
                    Instances.PUBLIC_KEY

            };

            int _ID = 0;
            int INSTANCE_ID = 1;
            int ITEM_NAME = 2;
            int FILE_URI = 3;
            int IS_IMAGE = 4;
            int STATUS = 5;
            int IMAGE_SIZE = 6;
            int FAIL_MESSAGE = 7;
            int ADDRESS_NAME = 8;
            int COUNTRY_CODE = 9;
            int ZIP_CODE = 10;
            int LATITUDE = 11;
            int LONGITUDE = 12;

            int URL = 13;
            int LOGIN = 14;
            int PASSWORD = 15;
            int ROOT_FOLDER = 16;
            int DATABASE = 17;
            int SITE = 18;
            int PUBLIC_KEY = 19;
        }

    }

    interface InstancesColumns {
        String URL = "url";
        String LOGIN = "login";
        String PASSWORD = "password";
        String ROOT_FOLDER = "root_folder";
        String DATABASE = "database";
        String SITE = "site";
        String PUBLIC_KEY = "public_key";
        String SELECTED = "selected";
    }

    interface UploadColumns {
        String INSTANCE_ID = "instance_id";
        String ITEM_NAME = "item_name";
        String FILE_URI = "file_uri";
        String IS_IMAGE = "is_image";
        String STATUS = "status";
        String IMAGE_SIZE = "image_size";
        String FAIL_MESSAGE = "message";
        String ADDRESS_NAME = "addressName";
        String COUNTRY_CODE = "countryCode";
        String ZIP_CODE = "zipCode";
        String LATITUDE = "latitude";
        String LONGITUDE = "longitude";
    }
}
