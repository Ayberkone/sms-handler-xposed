package xposed.ayberkbaytok.sms_handler.consts;

import xposed.ayberkbaytok.sms_handler.BuildConfig;

public final class PreferenceConsts {
    private static final String SMS_HANDLER_PACKAGE = BuildConfig.APPLICATION_ID;
    public static final String FILE_MAIN = SMS_HANDLER_PACKAGE + "_preferences";
    public static final String FILE_INTERNAL = "internal_preferences";
    public static final String REMOTE_PREFS_AUTHORITY = SMS_HANDLER_PACKAGE + ".preferences";

    public static final String KEY_ENABLE = "pref_enable";
    public static final boolean KEY_ENABLE_DEFAULT = true;
    public static final String KEY_CONTACTS = "pref_whitelist_contacts";
    public static final boolean KEY_CONTACTS_DEFAULT = false;
    public static final String KEY_NOTIFICATIONS_ENABLE = "pref_notifications_enable";
    public static final boolean KEY_NOTIFICATIONS_ENABLE_DEFAULT = false;
    public static final String KEY_VERBOSE_LOGGING = "pref_verbose_logging";
    public static final boolean KEY_VERBOSE_LOGGING_DEFAULT = false;
    public static final String KEY_NOTIFICATIONS_PRIORITY = "pref_notifications_priority";
    public static final String KEY_NOTIFICATIONS_PRIORITY_DEFAULT = "0";
    public static final String KEY_NOTIFICATIONS_OPEN_SETTINGS = "pref_notifications_open_settings";

    public static final String KEY_APP_VERSION = "pref_app_version";
    public static final String KEY_SELECTED_SECTION = "pref_selected_section";
    public static final String KEY_KNOWN_TASK_KILLERS = "pref_known_task_killers";

    private PreferenceConsts() { }
}
