package xposed.ayberkbaytok.sms_handler.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import xposed.ayberkbaytok.sms_handler.BuildConfig;


import static xposed.ayberkbaytok.sms_handler.provider.DatabaseContract.ReceivedMessages;
import static xposed.ayberkbaytok.sms_handler.provider.DatabaseContract.SentMessages;

/* package */ class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ayberkbaytok.db";
    private static final int DATABASE_VERSION = BuildConfig.DATABASE_VERSION;

    private static final String CREATE_RECEIVED_MESSAGES_TABLE =
            "CREATE TABLE " + ReceivedMessages.TABLE + "(" +
                    ReceivedMessages._ID                 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ReceivedMessages.SENDER              + " TEXT NOT NULL, " +
                    ReceivedMessages.BODY                + " TEXT NOT NULL, " +
                    ReceivedMessages.TIME_SENT           + " INTEGER NOT NULL, " +
                    ReceivedMessages.TIME_RECEIVED       + " INTEGER NOT NULL, " +
                    ReceivedMessages.READ                + " INTEGER NOT NULL, " +
                    ReceivedMessages.SEEN                + " INTEGER NOT NULL, " +
                    ReceivedMessages.SUB_ID              + " INTEGER NOT NULL" +
                    ");";

    private static final String CREATE_SENT_MESSAGES_TABLE =
            "CREATE TABLE " + SentMessages.TABLE + "(" +
                    SentMessages._ID                 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SentMessages.SENDER              + " TEXT NOT NULL, " +
                    SentMessages.BODY                + " TEXT NOT NULL, " +
                    SentMessages.TIME_SENT           + " INTEGER NOT NULL, " +
                    SentMessages.TIME_RECEIVED       + " INTEGER NOT NULL, " +
                    SentMessages.READ                + " INTEGER NOT NULL, " +
                    SentMessages.SEEN                + " INTEGER NOT NULL, " +
                    SentMessages.SUB_ID              + " INTEGER NOT NULL" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_RECEIVED_MESSAGES_TABLE);
        db.execSQL(CREATE_SENT_MESSAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
