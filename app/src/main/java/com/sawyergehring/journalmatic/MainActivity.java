package com.sawyergehring.journalmatic;

/*************************************************
 * Title: NoteTakingApp
 * Author: MilindAmrutkar
 * Date: 08-11-2017
 * Availability: https://github.com/MilindAmrutkar/NoteTakingApp
 *************************************************/

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int EDITOR_REQUEST_CODE = 1001;
    private CursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        openEditorNewNote();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEditorNewNote();
            }
        });

        cursorAdapter = new JournalCursorAdapter(this,
                null, 0);

        ListView list = findViewById(android.R.id.list);
        list.setAdapter(cursorAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                Uri uri = Uri.parse(JournalProvider.CONTENT_URI + "/" + id);
                intent.putExtra(JournalProvider.CONTENT_ITEM_TYPE, uri);
                startActivityForResult(intent, EDITOR_REQUEST_CODE);
            }
        });

        getLoaderManager().initLoader(0, null, (LoaderManager.LoaderCallbacks<Object>) this);


    }

    private void openEditorNewNote() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplication(), EditorActivity.class);
                startActivityForResult(intent, EDITOR_REQUEST_CODE);
            }
        });
    }

    private void insertEntry(String EntryText) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.ENTRY_TEXT, EntryText);

        Uri journalUri = getContentResolver().insert(JournalProvider.CONTENT_URI, values);

        Log.d(TAG, "Inserted Entry: " +journalUri.getLastPathSegment());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void insertSampleData() {
        insertEntry("Simple note");
        insertEntry("Mutli-line\nnote");
        insertEntry("Very long note with a lot of text that exceeds the width of the screen");
        //each time you change the data in the database, you need to tell your loader object that it needs
        //to restart, that it needs to re-read the data from the back-end database
        restartLoader();
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(0,null, (LoaderManager.LoaderCallbacks<Object>) this);
    }

    private void deleteAllNotes() {
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if(button == DialogInterface.BUTTON_POSITIVE) {
                            //Insert Data management code here
                            getContentResolver().delete(
                                    JournalProvider.CONTENT_URI,
                                    null, //delete everything
                                    null
                            );

                            restartLoader(); //to refresh activity

                            Toast.makeText(MainActivity.this,
                                    R.string.all_deleted,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.are_you_sure)
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, JournalProvider.CONTENT_URI,
                null, null, null, null);
        //here we set the projection, that's the list of columns, to null becoz that's already coded in the provider
        //selection to null, means that I want all the data
        //and we're not using selectionArgs and sortOrder
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Notice that onLoadFinished is receiving a cursor object. When you create the cursor loader object
        //it executes the Query method on the background thread
        //And when the data comes back, onLoadFinised is called. Our job is to take the data represented
        //by the cursor object, named data and pass it to the cursor adaptor. We do that with
        //cursorAdaptor.swapCursor
        cursorAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        //This is called whenever the data needs to be wiped out
        cursorAdapter.swapCursor(null);
    }
}
