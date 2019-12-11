package com.sawyergehring.journalmatic;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditorActivity extends AppCompatActivity {

    private EditText editor;
    private SQLiteDatabase mDatabase;
    private String selectedDate;
    private String dateSort;
    private String entryId;
    private Cursor mCursor;
    private Boolean isEdit = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        selectedDate = intent.getStringExtra("selectedDate");
        dateSort = intent.getStringExtra("dateSort");
        entryId = intent.getStringExtra("entryId");

        DBOpenHelper dbHelper = new DBOpenHelper(this);
        mDatabase = dbHelper.getWritableDatabase();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editor = findViewById(R.id.editText2);

        if (entryId != null && entryId.trim().length() != 0)
        {
            isEdit = true;
            mCursor = getItemsById(entryId);
            if (mCursor.moveToFirst()){
                String content = mCursor.getString(mCursor.getColumnIndex(JournalContract.JournalEntry.COLUMN_TEXT));
                editor.setText(content);
            }
        }

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
                onBackPressed();
                break;

            case R.id.action_save:
                String newText = editor.getText().toString().trim();
                if (isEdit) {
                    updateEntry(newText);
                }
                else {
                    entryId = String.valueOf(addEntry(newText));
                    isEdit = true;
                }
                break;

            case R.id.action_delete:
                deleteEntry();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finishEditing();
    }

    private void deleteEntry() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this entry?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isEdit)
                            mDatabase.delete(JournalContract.JournalEntry.TABLE_NAME, "_id=?" , new String[]{entryId});

                        Toast.makeText(EditorActivity.this, "Entry deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void finishEditing() {
        String newText = editor.getText().toString().trim();
        save(newText);
        finish();
    }

    private long save(String newText) {
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        if (isEdit) {
            updateEntry(newText);
            return -1;
        }
        else {
            return addEntry(newText);
        }
    }


    private void updateEntry(String content) {
        if (content.trim().length() == 0) {
            return;
        }

        ContentValues cv = new ContentValues();
        cv.put(JournalContract.JournalEntry.COLUMN_TEXT, content);

        mDatabase.update(JournalContract.JournalEntry.TABLE_NAME, cv, "_id=?" , new String[]{entryId});

        return;

    }


    private long addEntry(String content) {
        if (content.trim().length() == 0) {
            return -1;
        }

        ContentValues cv = new ContentValues();
        cv.put(JournalContract.JournalEntry.COLUMN_TEXT, content);
        cv.put(JournalContract.JournalEntry.COLUMN_DATE, selectedDate);
        cv.put(JournalContract.JournalEntry.COLUMN_TIMESTAMP, dateSort);

        return mDatabase.insert(JournalContract.JournalEntry.TABLE_NAME, null, cv);
    }

    private Cursor getItemsById(String entryId) {

        return mDatabase.query(
                JournalContract.JournalEntry.TABLE_NAME,
                null,
                JournalContract.JournalEntry._ID + " = " + entryId,
                null,
                null,
                null,
                null
        );
    }
}
