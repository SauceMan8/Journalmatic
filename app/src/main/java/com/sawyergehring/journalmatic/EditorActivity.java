package com.sawyergehring.journalmatic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

/*************************************************
 * Title: NoteTakingApp
 * Author: MilindAmrutkar
 * Date: 08-11-2017
 * Availability: https://github.com/MilindAmrutkar/NoteTakingApp
 *************************************************/

public class EditorActivity extends AppCompatActivity {

    private String action;
    private EditText editor;
    private String noteFilter;
    private String oldText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editor = findViewById(R.id.editText2);

        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra(JournalProvider.CONTENT_ITEM_TYPE);

        if(uri == null) {
            action = Intent.ACTION_INSERT;
            setTitle(getString(R.string.new_entry));
        } else {
            action = Intent.ACTION_EDIT;
            noteFilter = DBOpenHelper.ENTRY_ID + "=" + uri.getLastPathSegment();

            Cursor cursor = getContentResolver().query(uri,
                    DBOpenHelper.ALL_COLUMNS,
                    noteFilter,
                    null,
                    null);

            cursor.moveToFirst();
            oldText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.ENTRY_TEXT));

            editor.setText(oldText);
            editor.requestFocus();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(action.equals(Intent.ACTION_EDIT)) {
            getMenuInflater().inflate(R.menu.menu_editor, menu);
        }
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
        getContentResolver().delete(JournalProvider.CONTENT_URI,
                noteFilter, null);
        Toast.makeText(this, R.string.entry_deleted, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void finishEditing() {
        String newText = editor.getText().toString().trim();

        switch (action) {
            case Intent.ACTION_INSERT:
                if(newText.length() == 0) {
                    setResult(RESULT_CANCELED);
                } else {
                    insertEntry(newText);
                }
                break;
            case Intent.ACTION_EDIT:
                if(newText.length() == 0) {
                    deleteEntry();
                } else if(oldText.equals(newText)) {
                    setResult(RESULT_CANCELED);
                } else {
                    updateEntry(newText);
                }
        }

        finish();
    }

    private void updateEntry(String noteText) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.ENTRY_TEXT, noteText);
        getContentResolver().update(JournalProvider.CONTENT_URI, values, noteFilter, null);
        Toast.makeText(this, R.string.entry_updated, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);

    }

    private void insertEntry(String noteText) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.ENTRY_TEXT, noteText);
        getContentResolver().insert(JournalProvider.CONTENT_URI, values);
        setResult(RESULT_OK);
    }

    @Override
    public void onBackPressed() {
        finishEditing();
    }
}
