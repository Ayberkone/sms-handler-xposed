package xposed.ayberkbaytok.sms_handler.xposed;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Telephony;


import xposed.ayberkbaytok.sms_handler.BuildConfig;
import xposed.ayberkbaytok.sms_handler.consts.BroadcastConsts;
import xposed.ayberkbaytok.sms_handler.consts.PreferenceConsts;
import xposed.ayberkbaytok.sms_handler.data.SmsMessageData;
import xposed.ayberkbaytok.sms_handler.utils.*;


import com.crossbowffs.remotepreferences.RemotePreferenceAccessException;
import com.crossbowffs.remotepreferences.RemotePreferences;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.lang.reflect.Method;

public class SmsHandlerHook implements IXposedHookLoadPackage {

    private static final String SMS_HANDLER_PACKAGE = BuildConfig.APPLICATION_ID;
    private static final String TELEPHONY_PACKAGE = "com.android.internal.telephony";
    private static final String SMS_HANDLER_CLASS = TELEPHONY_PACKAGE + ".InboundSmsHandler";
    private static final int EVENT_BROADCAST_COMPLETE = 3;

    private Context mContext;
    private RemotePreferences mPreferences;

    private class ConstructorHook extends XC_MethodHook {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            try {
                afterConstructorHandler(param);
            } catch (Throwable e) {
                Xlog.e("Error occurred in constructor hook", e);
                throw e;
            }
        }
    }

    private static Object callDeclaredMethod(String clsName, Object obj, String methodName, Object... args) {

        Class<?> cls = XposedHelpers.findClass(clsName, obj.getClass().getClassLoader());
        Method method = XposedHelpers.findMethodBestMatch(cls, methodName, args);
        return ReflectionUtils.invoke(method, obj, args);

    }


    private void sendBroadcastComplete(Object smsHandler) {
        Xlog.i("Notifying completion of SMS broadcast");
        XposedHelpers.callMethod(smsHandler, "sendMessage",
                /* what */ EVENT_BROADCAST_COMPLETE);
    }

    private void finishSmsBroadcast(Object smsHandler, Object smsReceiver) {
        // This is required to write to the SMS database.
        long token = Binder.clearCallingIdentity();
        Binder.restoreCallingIdentity(token);
        sendBroadcastComplete(smsHandler);
    }

    private void broadcastSms(Uri messageUri) {
        // The provider requires permissions to read the actual message contents.
        Intent intent = new Intent(BroadcastConsts.ACTION_RECEIVE_SMS);
        intent.setComponent(new ComponentName(SMS_HANDLER_PACKAGE, BroadcastConsts.RECEIVER_NAME));
        intent.putExtra(BroadcastConsts.EXTRA_MESSAGE, messageUri);
        mContext.sendBroadcast(intent);
    }

    private boolean getBooleanPref(String key, boolean defValue) {
        try {
            return mPreferences.getBoolean(key, defValue);
        } catch (RemotePreferenceAccessException e) {
            Xlog.e("Failed to read preference: %s", key, e);
            return defValue;
        }
    }

    private void grantWriteSmsPermissions(Context context) {
        /*  Mesajlari tekrar default inboxa depolamak için
        grant write permissiona ihtiyacimiz var.        */

        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(SMS_HANDLER_PACKAGE, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Xlog.e("App package not found, ignoring", e);
            return;
        }

        int uid = packageInfo.applicationInfo.uid;
        try {
            Xlog.i("Checking if we have OP_WRITE_SMS permission");
            if (AppUtils.checkOp(context, AppUtils.OP_WRITE_SMS, uid, SMS_HANDLER_PACKAGE)) {
                Xlog.i("Already have OP_WRITE_SMS permission");
            } else {
                Xlog.i("Giving our package OP_WRITE_SMS permission");
                AppUtils.allowOp(context, AppUtils.OP_WRITE_SMS, uid, SMS_HANDLER_PACKAGE);
            }
        } catch (Exception e) {
            // This isn't a fatal error - the user just won't
            // be able to restore messages to the inbox.
            Xlog.e("Failed to grant OP_WRITE_SMS permission", e);
        }
    }

    private void afterConstructorHandler(XC_MethodHook.MethodHookParam param) {
        Context context = (Context)param.args[1];
        if (mContext == null) {
            mContext = context;
            mPreferences = new RemotePreferences(context,
                    PreferenceConsts.REMOTE_PREFS_AUTHORITY,
                    PreferenceConsts.FILE_MAIN,
                    true);
            grantWriteSmsPermissions(context);
        }
    }

    private void beforeDispatchIntentHandler(XC_MethodHook.MethodHookParam param, int receiverIndex) {
        Intent intent = (Intent)param.args[0];
        String action = intent.getAction();

        if (!Telephony.Sms.Intents.SMS_DELIVER_ACTION.equals(action)) {
            return;
        }

        SmsMessageData message = SmsMessageData.fromIntent(intent);
        String sender = message.getSender();
        String body = message.getBody();
        Xlog.i("Received a new SMS message");
        Xlog.i("Sender: %s", sender);
        Xlog.i("Body: %s", body);

        finishSmsBroadcast(param.thisObject, param.args[receiverIndex]);
        param.setResult(null);
    }

    private void hookConstructor(XC_LoadPackage.LoadPackageParam lpparam) {
        Xlog.i("Hooking InboundSmsHandler constructor for Android v27+");
        XposedHelpers.findAndHookConstructor(SMS_HANDLER_CLASS, lpparam.classLoader,
                /*                 name */ String.class,
                /*              context */ Context.class,
                /*       storageMonitor */ TELEPHONY_PACKAGE + ".SmsStorageMonitor",
                /*                phone */ TELEPHONY_PACKAGE + ".Phone",
                /* cellBroadcastHandler */ TELEPHONY_PACKAGE + ".CellBroadcastHandler",
                new ConstructorHook());
    }


    private void hookSmsHandler(XC_LoadPackage.LoadPackageParam lpparam) {
        hookConstructor(lpparam);
    }


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if ("com.android.phone".equals(lpparam.packageName)) {
            Xlog.i("SMS Handler initializing...");
            try {
                hookSmsHandler(lpparam);
            } catch (Throwable e) {
                Xlog.e("Failed to hook SMS handler", e);
                throw e;
            }
            Xlog.i("SMS Handler initialization complete!");
        }
    }
}

