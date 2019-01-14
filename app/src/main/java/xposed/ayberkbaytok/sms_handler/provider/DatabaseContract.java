package xposed.ayberkbaytok.sms_handler.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;
import xposed.ayberkbaytok.sms_handler.BuildConfig;

public final class DatabaseContract {
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".database";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static class ReceivedMessages implements BaseColumns {
        public static final String TABLE = "received_messages";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(DatabaseContract.CONTENT_URI, TABLE);
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.ayberkbaytok.sms";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.ayberkbaytok.sms";

        public static final String SENDER = "sender";
        public static final String BODY = "body";
        public static final String TIME_SENT = "time_sent";
        public static final String TIME_RECEIVED = "time_received";
        public static final String READ = "read";
        public static final String SEEN = "seen";
        public static final String SUB_ID = "sub_id";
        public static final String[] ALL = {
                _ID,
                SENDER,
                BODY,
                TIME_SENT,
                TIME_RECEIVED,
                READ,
                SEEN,
                SUB_ID,
        };
    }

    public static class SentMessages implements BaseColumns {
        public static final String TABLE = "sent_messages";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(DatabaseContract.CONTENT_URI, TABLE);
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.ayberkbaytok.sms";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.ayberkbaytok.sms";

        public static final String SENDER = "sender";
        public static final String BODY = "body";
        public static final String TIME_SENT = "time_sent";
        public static final String TIME_RECEIVED = "time_received";
        public static final String READ = "read";
        public static final String SEEN = "seen";
        public static final String SUB_ID = "sub_id";
        public static final String[] ALL = {
                _ID,
                SENDER,
                BODY,
                TIME_SENT,
                TIME_RECEIVED,
                READ,
                SEEN,
                SUB_ID,
        };
    }
}

