package com.sawyergehring.journalmatic;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.sawyergehring.journalmatic.Journalmatic.CHANNEL_1_ID;

public class MainActivity extends AppCompatActivity {

    private NotificationManagerCompat notificationManager;

    private SQLiteDatabase mDatabase;
    private JournalAdapter mAdapter;
    private String selectedDate;
    private DateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:MM:SS.SSS", Locale.US);
    private DateFormat dateInputFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
    private DateFormat dateOutputFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DBOpenHelper dbHelper = new DBOpenHelper(this);
        mDatabase = dbHelper.getWritableDatabase();

        selectedDate = getTodayDateAsString();
        buildRecycleView();


        notificationManager = NotificationManagerCompat.from(this);



//        insertSampleData();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LaunchNew();
            }
        });

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReminderNotification(v);
                sendErrorNotification(v);
            }
        });

        final CalendarView calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Date date = null;
                try {
                    date = dateInputFormat.parse(year + "/" + (month+1) + "/" + dayOfMonth);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                selectedDate = dateOutputFormat.format(date);
                mAdapter.swapCursor(getItemsByDate(selectedDate));
                Toast.makeText(MainActivity.this, selectedDate, Toast.LENGTH_SHORT).show();
            }
        });

        Button todayButton = findViewById(R.id.today_button);
        todayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCalendarToday(calendarView);
            }
        });
    }

    private void setCalendarToday(CalendarView calendarView) {
        calendarView.setDate(System.currentTimeMillis(),true, true);

        selectedDate = getTodayDateAsString();
        mAdapter.swapCursor(getItemsByDate(selectedDate));
    }

    private String getTodayDateAsString() {
        return dateOutputFormat.format(new Date(System.currentTimeMillis()));
    }

    private void buildRecycleView() {
        RecyclerView recyclerView = findViewById(R.id.entry_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL));
        mAdapter = new JournalAdapter(this, getItemsByDate(selectedDate), new CustomItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                LaunchEdit(v.getContext(), v.getTag());
            }
        });
        recyclerView.setAdapter(mAdapter);
        mAdapter.swapCursor(getItemsByDate(selectedDate));
    }

    private void LaunchEdit(Context context, Object tag) {
        Toast.makeText(context, "onClick: " + tag.toString(), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, EditorActivity.class);
        intent.putExtra("selectedDate", selectedDate);
        intent.putExtra("entryId", tag.toString());
        startActivity(intent);
    }

    public void LaunchNew() {
        Intent intent = new Intent(this, EditorActivity.class);
        intent.putExtra("selectedDate", selectedDate);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_create_sample:
                insertSampleData();
                break;
            case R.id.action_delete_all:
                Toast.makeText(this, "Delete", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
                default:
                    return super.onOptionsItemSelected(item);
        }
        return true;
    }

//    private void addEntry(String content, String date) {
//        if (content.trim().length() == 0) {
//            return;
//        }
//
//
//        ContentValues cv = new ContentValues();
//        cv.put(JournalContract.JournalEntry.COLUMN_TEXT, content);
//        cv.put(JournalContract.JournalEntry.COLUMN_DATE, date);
//
//        mDatabase.insert(JournalContract.JournalEntry.TABLE_NAME, null, cv);
//        mAdapter.swapCursor(getItemsByDate(selectedDate));
//        return;
//    }

    private void addEntry(String content) {
        if (content.trim().length() == 0) {
            return;
        }

        ContentValues cv = new ContentValues();
        cv.put(JournalContract.JournalEntry.COLUMN_TEXT, content);
        cv.put(JournalContract.JournalEntry.COLUMN_DATE, selectedDate);

        mDatabase.insert(JournalContract.JournalEntry.TABLE_NAME, null, cv);
        mAdapter.swapCursor(getItemsByDate(selectedDate));
    }

    private Cursor getAllItems() {
        return mDatabase.query(
                JournalContract.JournalEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                JournalContract.JournalEntry.COLUMN_TIMESTAMP + " DESC"
        );
    }

    private Cursor getItemsByDate(String date) {

        return mDatabase.query(
                JournalContract.JournalEntry.TABLE_NAME,
                null,
                JournalContract.JournalEntry.COLUMN_DATE + " = \"" + date + "\"",
                null,
                null,
                null,
                JournalContract.JournalEntry.COLUMN_TIMESTAMP + " DESC"
        );
    }

    private void insertSampleData() {
        addEntry("Simple note");
        addEntry("Mutli-line\nnote");
        addEntry("Very long note with a lot of text that exceeds the width of the screen");
        // reset
    }

    @Override
    public void onResume(){
        super.onResume();
        mAdapter.swapCursor(getItemsByDate(selectedDate));
    }

    public void sendReminderNotification(View v) {

        Intent notificationIntent = new Intent(this, EditorActivity.class);
        notificationIntent.putExtra("selectedDate", getTodayDateAsString());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_main_icon_foreground)
                .setContentTitle("Journal Reminder")
                .setContentText("Reminder to write in your journal today!")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(1, notification);
    }

    public void sendErrorNotification(View v) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_main_icon_foreground)
                .setContentTitle("Error Title")
                .setContentText("Error Content")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        notificationManager.notify(2, notification);
    }

//    private static final String TAG = MainActivity.class.getSimpleName();
//    private static final int EDITOR_REQUEST_CODE = 1001;
//    private CursorAdapter cursorAdapter;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        openEditorNewNote();
//
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                openEditorNewNote();
//            }
//        });
//
//        cursorAdapter = new JournalCursorAdapter(this,
//                null, 0);
//
//        ListView list = findViewById(R.id.JournalList);
//        list.setAdapter(cursorAdapter);
//
//        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
//                Uri uri = Uri.parse(JournalProvider.CONTENT_URI + "/" + id);
//                intent.putExtra(JournalProvider.CONTENT_ITEM_TYPE, uri);
//                startActivityForResult(intent, EDITOR_REQUEST_CODE);
//            }
//        });
//
//
//
//
//    }
//
//    private void openEditorNewNote() {
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getApplication(), EditorActivity.class);
//                startActivityForResult(intent, EDITOR_REQUEST_CODE);
//            }
//        });
//    }
//
//    private void insertEntry(String EntryText) {
//        ContentValues values = new ContentValues();
//        values.put(DBOpenHelper.ENTRY_TEXT, EntryText);
//
//        Uri journalUri = getContentResolver().insert(JournalProvider.CONTENT_URI, values);
//
//        Log.d(TAG, "Inserted Entry: " +journalUri.getLastPathSegment());
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        switch (id) {
//            case R.id.action_create_sample:
//                insertSampleData();
//                break;
//
//            case R.id.action_delete_all:
//                deleteAllNotes();
//                break;
//
//            case R.id.action_settings:
//                Toast.makeText(this, "Open Settings...", Toast.LENGTH_SHORT).show();
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void insertSampleData() {
//        insertEntry("Simple note");
//        insertEntry("Mutli-line\nnote");
//        insertEntry("Very long note with a lot of text that exceeds the width of the screen");
//
//
//        // reset
//    }
//
//    private void deleteAllNotes() {
//        DialogInterface.OnClickListener dialogClickListener =
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int button) {
//                        if(button == DialogInterface.BUTTON_POSITIVE) {
//                            //Insert Data management code here
//                            getContentResolver().delete(
//                                    JournalProvider.CONTENT_URI,
//                                    null, //delete everything
//                                    null
//                            );
//
//                             //refresh activity
//
//                            Toast.makeText(MainActivity.this,
//                                    R.string.all_deleted,
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                };
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage(R.string.are_you_sure)
//                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
//                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
//                .show();
//    }


}
