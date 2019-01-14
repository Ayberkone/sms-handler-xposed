package xposed.ayberkbaytok.sms_handler.loader;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import xposed.ayberkbaytok.sms_handler.data.SmsMessageData;
import xposed.ayberkbaytok.sms_handler.utils.AppUtils;
import xposed.ayberkbaytok.sms_handler.utils.MapUtils;
import xposed.ayberkbaytok.sms_handler.utils.Xlog;

public final class InboxSmsLoader {
    private InboxSmsLoader() { }

    private static ContentValues serializeMessage(SmsMessageData messageData) {
        ContentValues values = MapUtils.contentValuesForSize(7);
        values.put(Telephony.Sms.ADDRESS, messageData.getSender());
        values.put(Telephony.Sms.BODY, messageData.getBody());
        values.put(Telephony.Sms.DATE, messageData.getTimeReceived());
        values.put(Telephony.Sms.DATE_SENT, messageData.getTimeSent());
        values.put(Telephony.Sms.READ, messageData.isRead() ? 1 : 0);
        values.put(Telephony.Sms.SEEN, 1); // Always mark messages as seen

        // Also write subscription ID (aka SIM card number) on Android 5.1+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            values.put(Telephony.Sms.SUBSCRIPTION_ID, messageData.getSubId());
        }

        return values;
    }

    public static Uri writeMessage(Context context, SmsMessageData messageData) {
        if (!AppUtils.noteOp(context, AppUtils.OP_WRITE_SMS)) {
            throw new SecurityException("Do not have permissions to write SMS");
        }

        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = contentResolver.insert(Telephony.Sms.CONTENT_URI, serializeMessage(messageData));
        long id = -1;
        if (uri != null) {
            id = ContentUris.parseId(uri);
        }

        if (id == 0) {
            Xlog.w("Writing to SMS inbox returned row 0");
            return uri;
        } else if (id < 0) {
            Xlog.e("Writing to SMS inbox failed (unknown reason)");
            throw new DatabaseException("Failed to write message to SMS inbox");
        } else {
            return uri;
        }
    }

    public static void deleteMessage(Context context, Uri messageUri) {
        ContentResolver contentResolver = context.getContentResolver();
        int deletedRows = contentResolver.delete(messageUri, null, null);
        if (deletedRows == 0) {
            Xlog.e("URI does not match any message in SMS inbox");
        }
    }
}
