package xposed.ayberkbaytok.sms_handler.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;

import static xposed.ayberkbaytok.sms_handler.provider.DatabaseContract.ReceivedMessages;
import static xposed.ayberkbaytok.sms_handler.provider.DatabaseContract.SentMessages;

public class DatabaseProvider extends AutoContentProvider {
    public DatabaseProvider() {
        super(DatabaseContract.AUTHORITY, new ProviderTable[] {
                new ProviderTable(ReceivedMessages.TABLE, ReceivedMessages.CONTENT_ITEM_TYPE, ReceivedMessages.CONTENT_TYPE),
                new ProviderTable(SentMessages.TABLE, SentMessages.CONTENT_ITEM_TYPE, SentMessages.CONTENT_TYPE)
        });
    }

    @Override
    protected SQLiteOpenHelper createDatabaseHelper(Context context) {
        return new DatabaseHelper(context);
    }

    /*
     * Below is an ugly workaround for Android 8.0+. Since the
     * com.android.phone package no longer has SMS permissions,
     * we can't use android:{read,write}Permissions in AndroidManifest.xml.
     * Instead, we just check the calling package via code.
     *
     * TODO: Migrate to new architecture and delete this hack
     */

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        checkAccess();
        return super.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public String getType(@NonNull Uri uri) {
        checkAccess();
        return super.getType(uri);
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        checkAccess();
        return super.insert(uri, values);
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] bulkValues) {
        checkAccess();
        return super.bulkInsert(uri, bulkValues);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        checkAccess();
        return super.delete(uri, selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        checkAccess();
        return super.update(uri, values, selection, selectionArgs);
    }

    private void checkAccess() {
        String caller = getCallingPackage();
        if (caller != null && !"com.android.phone".equals(caller) && !"xposed.ayberkbaytok.sms_handler".equals(caller)) {
            throw new SecurityException("Cannot access this database, go away!");
        }
    }
}
