package xposed.ayberkbaytok.sms_handler.loader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import xposed.ayberkbaytok.sms_handler.data.SmsMessageData;
import xposed.ayberkbaytok.sms_handler.provider.DatabaseContract;
import xposed.ayberkbaytok.sms_handler.utils.MapUtils;
import xposed.ayberkbaytok.sms_handler.widget.AutoContentLoader;
import xposed.ayberkbaytok.sms_handler.widget.CursorWrapper;

import static xposed.ayberkbaytok.sms_handler.provider.DatabaseContract.ReceivedMessages;

public class SentMessageLoader extends AutoContentLoader<SmsMessageData> {
    private static SentMessageLoader sInstance;

    public static SentMessageLoader get() {
        if (sInstance == null) {
            sInstance = new SentMessageLoader();
        }
        return sInstance;
    }

    private SentMessageLoader() {
        super(DatabaseContract.SentMessages.CONTENT_URI, DatabaseContract.SentMessages.ALL);
    }


    @Override
    protected SmsMessageData newData() {
        return new SmsMessageData();
    }

    @Override
    protected void clearData(SmsMessageData data) {
        data.reset();
    }

    @Override
    protected void bindData(Cursor cursor, int column, String columnName, SmsMessageData data) {
        switch (columnName) {
            case DatabaseContract.SentMessages._ID:
                data.setId(cursor.getLong(column));
                break;
            case DatabaseContract.SentMessages.SENDER:
                data.setSender(cursor.getString(column));
                break;
            case DatabaseContract.SentMessages.BODY:
                data.setBody(cursor.getString(column));
                break;
            case DatabaseContract.SentMessages.TIME_SENT:
                data.setTimeSent(cursor.getLong(column));
                break;
            case DatabaseContract.SentMessages.TIME_RECEIVED:
                data.setTimeReceived(cursor.getLong(column));
                break;
            case DatabaseContract.SentMessages.READ:
                data.setRead(cursor.getInt(column) != 0);
                break;
            case DatabaseContract.SentMessages.SEEN:
                data.setSeen(cursor.getInt(column) != 0);
                break;
            case DatabaseContract.SentMessages.SUB_ID:
                data.setSubId(cursor.getInt(column));
                break;
        }
    }

    @Override
    protected ContentValues serialize(SmsMessageData data) {
        ContentValues values = MapUtils.contentValuesForSize(8);
        if (data.getId() >= 0) {
            values.put(DatabaseContract.SentMessages._ID, data.getId());
        }
        values.put(DatabaseContract.SentMessages.SENDER, data.getSender());
        values.put(DatabaseContract.SentMessages.BODY, data.getBody());
        values.put(DatabaseContract.SentMessages.TIME_SENT, data.getTimeSent());
        values.put(DatabaseContract.SentMessages.TIME_RECEIVED, data.getTimeReceived());
        values.put(DatabaseContract.SentMessages.READ, data.isRead() ? 1 : 0);
        values.put(DatabaseContract.SentMessages.SEEN, data.isSeen() ? 1 : 0);
        values.put(DatabaseContract.SentMessages.SUB_ID, data.getSubId());
        return values;
    }

    public CursorWrapper<SmsMessageData> queryUnseen(Context context) {
        return queryAll(context, DatabaseContract.SentMessages.SEEN + "=?", new String[] {"0"}, DatabaseContract.SentMessages.TIME_SENT + " DESC");
    }

    public SmsMessageData queryAndDelete(Context context, long messageId) {
        return queryAndDelete(context, convertIdToUri(messageId));
    }

    public SmsMessageData queryAndDelete(Context context, Uri messageUri) {
        SmsMessageData messageData = query(context, messageUri);
        if (messageData != null) {
            delete(context, messageUri);
        }
        return messageData;
    }

    public boolean setReadStatus(Context context, long messageId, boolean read) {
        return setReadStatus(context, convertIdToUri(messageId), read);
    }

    public boolean setReadStatus(Context context, Uri messageUri, boolean read) {
        ContentValues values = MapUtils.contentValuesForSize(2);
        values.put(DatabaseContract.SentMessages.READ, read ? 1 : 0);
        if (read) {
            values.put(DatabaseContract.SentMessages.SEEN, 1);
        }
        return update(context, messageUri, values);
    }

    public boolean setSeenStatus(Context context, long messageId, boolean seen) {
        return setSeenStatus(context, convertIdToUri(messageId), seen);
    }

    public boolean setSeenStatus(Context context, Uri messageUri, boolean seen) {
        ContentValues values = MapUtils.contentValuesForSize(1);
        values.put(DatabaseContract.SentMessages.SEEN, seen ? 1 : 0);
        return update(context, messageUri, values);
    }

    public void markAllSeen(Context context) {
        ContentValues values = MapUtils.contentValuesForSize(1);
        values.put(DatabaseContract.SentMessages.SEEN, 1);
        updateAll(context, values, DatabaseContract.SentMessages.SEEN + "=?", new String[] {"0"});
    }
}

