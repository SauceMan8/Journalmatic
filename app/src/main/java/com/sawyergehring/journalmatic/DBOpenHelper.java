package com.sawyergehring.journalmatic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.sawyergehring.journalmatic.JournalContract.*;

import androidx.annotation.Nullable;

public class DBOpenHelper extends SQLiteOpenHelper {

    //Constraints
    private static final String DATABASE_NAME = "journal.db";
    private static final int DATABASE_VERSION = 1;

    public DBOpenHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_GROCERYLIST_TABLE = " CREATE TABLE " +
                JournalEntry.TABLE_NAME + " (" +
                JournalEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                JournalEntry.COLUMN_TEXT + " TEXT NOT NULL, " +
                JournalEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        db.execSQL(SQL_CREATE_GROCERYLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + JournalEntry.TABLE_NAME);
        onCreate(db);
    }
}
