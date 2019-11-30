package com.sawyergehring.journalmatic;

import android.provider.BaseColumns;

public class JournalContract {

    private JournalContract(){}

    public static final class JournalEntry implements BaseColumns {

        public static final String TABLE_NAME = "journal";
        public static final String COLUMN_TEXT = "entryText";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_DATE = "datetime";
    }

}
