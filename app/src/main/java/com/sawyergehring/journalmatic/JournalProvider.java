package com.sawyergehring.journalmatic;

/*************************************************
 * Title: NoteTakingApp
 * Author: MilindAmrutkar
 * Date: 08-11-2017
 * Availability: https://github.com/MilindAmrutkar/NoteTakingApp
 *************************************************/

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class JournalProvider extends ContentProvider {


    private static final String AUTHORITY = "com.sawyergehring.journalmatic.journalprovider";

    private static final String BASE_PATH = "journal";

    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    private static final int JOURNAL = 1;
    private static final int JOURNAL_ID = 2;

    private static final UriMatcher uriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);
    public static final String CONTENT_ITEM_TYPE = "Note";

    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, JOURNAL);
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", JOURNAL_ID);
    }

    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {
        DBOpenHelper helper = new DBOpenHelper(getContext());
        database = helper.getWritableDatabase();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        if(uriMatcher.match(uri) == JOURNAL_ID) {
            selection = DBOpenHelper.ENTRY_ID + "=" + uri.getLastPathSegment();
        }

        return database.query(DBOpenHelper.TABLE_JOURNAL, DBOpenHelper.ALL_COLUMNS,
                selection, null, null, null,
                DBOpenHelper.ENTRY_CREATED + " DESC");
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        long id = database.insert(DBOpenHelper.TABLE_JOURNAL,
                null, values);

        return Uri.parse(BASE_PATH + "/" + id);

    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return database.delete(DBOpenHelper.TABLE_JOURNAL, selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return database.update(DBOpenHelper.TABLE_JOURNAL, values, selection, selectionArgs);
    }
}
