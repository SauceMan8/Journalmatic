package com.sawyergehring.journalmatic;
/*************************************************
 * Title: NoteTakingApp
 * Author: MilindAmrutkar
 * Date: 08-11-2017
 * Availability: https://github.com/MilindAmrutkar/NoteTakingApp
 *************************************************/

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBOpenHelper extends SQLiteOpenHelper {

    //Constraints
    private static final String DATABASE_NAME = "journal.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_JOURNAL = "journal";
    public static final String ENTRY_ID = "_id";
    public static final String ENTRY_TEXT = "entryText";
    public static final String ENTRY_CREATED = "noteCreated";

    public static final String[] ALL_COLUMNS =
            {ENTRY_ID, ENTRY_TEXT, ENTRY_CREATED};

    //Creation SQL
    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_JOURNAL + " (" +
                    ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ENTRY_TEXT + " TEXT, " +
                    ENTRY_CREATED + " TEXT default CURRENT_TIMESTAMP" +
                    ")";

    public DBOpenHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_JOURNAL);
        onCreate(db);
    }
}
