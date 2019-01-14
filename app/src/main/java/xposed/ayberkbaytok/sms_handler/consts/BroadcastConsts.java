package xposed.ayberkbaytok.sms_handler.consts;
import xposed.ayberkbaytok.sms_handler.BuildConfig;


public final class BroadcastConsts {
    private static final String SMS_HANDLER_PACKAGE = BuildConfig.APPLICATION_ID;
    public static final String RECEIVER_NAME = SMS_HANDLER_PACKAGE + ".app.SmsReceiver";
    public static final String ACTION_RECEIVE_SMS = SMS_HANDLER_PACKAGE + ".action.RECEIVE_SMS";
    public static final String ACTION_DELETE_SMS = SMS_HANDLER_PACKAGE + ".action.DELETE_BLOCKED_SMS";
    public static final String ACTION_RESTORE_SMS = SMS_HANDLER_PACKAGE + ".action.RESTORE_BLOCKED_SMS";
    public static final String ACTION_DISMISS_NOTIFICATION = SMS_HANDLER_PACKAGE + ".action.DISMISS_NOTIFICATION";
    public static final String EXTRA_MESSAGE = "message";

    private BroadcastConsts() { }
}