package xposed.ayberkbaytok.sms_handler.loader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import xposed.ayberkbaytok.sms_handler.data.SmsMessageData;
import xposed.ayberkbaytok.sms_handler.utils.MapUtils;
import xposed.ayberkbaytok.sms_handler.widget.AutoContentLoader;
import xposed.ayberkbaytok.sms_handler.widget.CursorWrapper;

import static xposed.ayberkbaytok.sms_handler.provider.DatabaseContract.ReceivedMessages;

public class ReceivedMessageLoader extends AutoContentLoader<SmsMessageData> {
    private static ReceivedMessageLoader sInstance;

    public static ReceivedMessageLoader get() {
        if (sInstance == null) {
            sInstance = new ReceivedMessageLoader();
        }
        return sInstance;
    }

    private ReceivedMessageLoader() {
        super(ReceivedMessages.CONTENT_URI, ReceivedMessages.ALL);
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
            case ReceivedMessages._ID:
                data.setId(cursor.getLong(column));
                break;
            case ReceivedMessages.SENDER:
                data.setSender(cursor.getString(column));
                break;
            case ReceivedMessages.BODY:
                data.setBody(cursor.getString(column));
                break;
            case ReceivedMessages.TIME_SENT:
                data.setTimeSent(cursor.getLong(column));
                break;
            case ReceivedMessages.TIME_RECEIVED:
                data.setTimeReceived(cursor.getLong(column));
                break;
            case ReceivedMessages.READ:
                data.setRead(cursor.getInt(column) != 0);
                break;
            case ReceivedMessages.SEEN:
                data.setSeen(cursor.getInt(column) != 0);
                break;
            case ReceivedMessages.SUB_ID:
                data.setSubId(cursor.getInt(column));
                break;
        }
    }

    @Override
    protected ContentValues serialize(SmsMessageData data) {
        ContentValues values = MapUtils.contentValuesForSize(8);
        if (data.getId() >= 0) {
            values.put(ReceivedMessages._ID, data.getId());
        }
        values.put(ReceivedMessages.SENDER, data.getSender());
        values.put(ReceivedMessages.BODY, data.getBody());
        values.put(ReceivedMessages.TIME_SENT, data.getTimeSent());
        values.put(ReceivedMessages.TIME_RECEIVED, data.getTimeReceived());
        values.put(ReceivedMessages.READ, data.isRead() ? 1 : 0);
        values.put(ReceivedMessages.SEEN, data.isSeen() ? 1 : 0);
        values.put(ReceivedMessages.SUB_ID, data.getSubId());
        return values;
    }

    public CursorWrapper<SmsMessageData> queryUnseen(Context context) {
        return queryAll(context, ReceivedMessages.SEEN + "=?", new String[] {"0"}, ReceivedMessages.TIME_SENT + " DESC");
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
        values.put(ReceivedMessages.READ, read ? 1 : 0);
        if (read) {
            values.put(ReceivedMessages.SEEN, 1);
        }
        return update(context, messageUri, values);
    }

    public boolean setSeenStatus(Context context, long messageId, boolean seen) {
        return setSeenStatus(context, convertIdToUri(messageId), seen);
    }

    public boolean setSeenStatus(Context context, Uri messageUri, boolean seen) {
        ContentValues values = MapUtils.contentValuesForSize(1);
        values.put(ReceivedMessages.SEEN, seen ? 1 : 0);
        return update(context, messageUri, values);
    }

    public void markAllSeen(Context context) {
        ContentValues values = MapUtils.contentValuesForSize(1);
        values.put(ReceivedMessages.SEEN, 1);
        updateAll(context, values, ReceivedMessages.SEEN + "=?", new String[] {"0"});
    }
}

