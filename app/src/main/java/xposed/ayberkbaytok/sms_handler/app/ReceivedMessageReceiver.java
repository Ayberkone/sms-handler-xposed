package xposed.ayberkbaytok.sms_handler.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import xposed.ayberkbaytok.sms_handler.R;
import xposed.ayberkbaytok.sms_handler.consts.BroadcastConsts;
import xposed.ayberkbaytok.sms_handler.data.SmsMessageData;
import xposed.ayberkbaytok.sms_handler.loader.ReceivedMessageLoader;
import xposed.ayberkbaytok.sms_handler.loader.DatabaseException;
import xposed.ayberkbaytok.sms_handler.loader.InboxSmsLoader;
import xposed.ayberkbaytok.sms_handler.utils.Xlog;

public class ReceivedMessageReceiver extends BroadcastReceiver {
    private void onReceiveSms(Context context, Intent intent) {
        Uri messageUri = intent.getParcelableExtra(BroadcastConsts.EXTRA_MESSAGE);
        if (messageUri == null) {
            return;
        }

        NotificationHelper.displayNotification(context, messageUri);
    }

    private void onDeleteSms(Context context, Intent intent) {
        Uri messageUri = intent.getData();
        NotificationHelper.cancelNotification(context, messageUri);

        boolean deleted = ReceivedMessageLoader.get().delete(context, messageUri);
        if (!deleted) {
            Xlog.e("Failed to delete message: could not load data");
            Toast.makeText(context, R.string.load_message_failed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.message_deleted, Toast.LENGTH_SHORT).show();
        }
    }

    private void onRestoreSms(Context context, Intent intent) {
        Uri messageUri = intent.getData();
        NotificationHelper.cancelNotification(context, messageUri);

        ReceivedMessageLoader.get().setSeenStatus(context, messageUri, true);

        SmsMessageData messageToRestore = ReceivedMessageLoader.get().query(context, messageUri);
        if (messageToRestore == null) {
            Xlog.e("Failed to restore message: could not load data");
            Toast.makeText(context, R.string.load_message_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            InboxSmsLoader.writeMessage(context, messageToRestore);
        } catch (SecurityException e) {
            Xlog.e("Do not have permissions to write SMS");
            Toast.makeText(context, R.string.must_enable_xposed_module, Toast.LENGTH_SHORT).show();
            return;
        } catch (DatabaseException e) {
            Xlog.e("Failed to restore message: could not write to SMS inbox");
            Toast.makeText(context, R.string.message_restore_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        ReceivedMessageLoader.get().delete(context, messageUri);
        Toast.makeText(context, R.string.message_restored, Toast.LENGTH_SHORT).show();
    }

    private void onDismissNotification(Context context, Intent intent) {
        Uri messageUri = intent.getData();
        ReceivedMessageLoader.get().setSeenStatus(context, messageUri, true);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case BroadcastConsts.ACTION_RECEIVE_SMS:
                onReceiveSms(context, intent);
                break;
            case BroadcastConsts.ACTION_DELETE_SMS:
                onDeleteSms(context, intent);
                break;
            case BroadcastConsts.ACTION_RESTORE_SMS:
                onRestoreSms(context, intent);
                break;
            case BroadcastConsts.ACTION_DISMISS_NOTIFICATION:
                onDismissNotification(context, intent);
                break;
        }
    }


}

