package com.sawyergehring.journalmatic;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditorActivity extends AppCompatActivity {

    private String action;
    private EditText editor;
    private String noteFilter;
    private String oldText;
    private SQLiteDatabase mDatabase;
    private String selectedDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        selectedDate = intent.getStringExtra("selectedDate");

        DBOpenHelper dbHelper = new DBOpenHelper(this);
        mDatabase = dbHelper.getWritableDatabase();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editor = findViewById(R.id.editText2);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finishEditing();
                break;

            case R.id.action_delete:
                deleteEntry();
                break;
        }
        return true;
    }

    private void deleteEntry() {
        Toast.makeText(this, R.string.all_deleted, Toast.LENGTH_SHORT).show();
    }

    private void finishEditing() {
        String newText = editor.getText().toString().trim();
        addEntry(newText);
//        switch (action) {
//            case Intent.ACTION_INSERT:
//                if(newText.length() == 0) {
//                    setResult(RESULT_CANCELED);
//                } else {
//                    addEntry(newText);
//                }
//                break;
//            case Intent.ACTION_EDIT:
//                if(newText.length() == 0) {
//                    deleteEntry();
//                } else if(oldText.equals(newText)) {
//                    setResult(RESULT_CANCELED);
//                } else {
//                    updateEntry(newText);
//                }
//        }
        finish();
    }

    private void updateEntry(String noteText) {
        addEntry(noteText);

    }


    private void addEntry(String content) {
        if (content.trim().length() == 0) {
            return;
        }

        ContentValues cv = new ContentValues();
        cv.put(JournalContract.JournalEntry.COLUMN_TEXT, content);
        cv.put(JournalContract.JournalEntry.COLUMN_DATE, selectedDate);

        mDatabase.insert(JournalContract.JournalEntry.TABLE_NAME, null, cv);

        return;
    }

    @Override
    public void onBackPressed() {
        finishEditing();
    }
}
