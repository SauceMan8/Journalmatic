package com.sawyergehring.journalmatic;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sawyergehring.journalmatic.Common.Common;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static com.sawyergehring.journalmatic.Journalmatic.Reminder_Channel;

public class MainActivity extends AppCompatActivity {

    private NotificationManagerCompat notificationManager;

    private SQLiteDatabase mDatabase;
    private JournalAdapter mAdapter;
    private String selectedDate;
    private DateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:MM:SS.SSS", Locale.US);
    private DateFormat dateInputFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
    private DateFormat dateOutputFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
    private AlarmManager am;
    private PendingIntent pendingIntent;


    private LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DBOpenHelper dbHelper = new DBOpenHelper(this);
        mDatabase = dbHelper.getWritableDatabase();
        Common.mDatabase = dbHelper.getWritableDatabase();

        getPermissionToReadLocation();
        getPermissionToReadCalendar();

        selectedDate = getTodayDateAsString();
        buildRecycleView();

        //getLocation();
        //startLocationUpdates();
//        AutoEntry autoEntry = new AutoEntry(this);
//        autoEntry.generate();

//        Toast.makeText(this, autoEntry.getWeather("43.81", "-111.79", "imperial").getName(), Toast.LENGTH_LONG).show();


        notificationManager = NotificationManagerCompat.from(this);
//        scheduleAlarm();

        am = (AlarmManager) this.getSystemService(ALARM_SERVICE);

        //setupNotification();

//        insertSampleData();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LaunchNew();
            }
        });

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReminderNotification();
                AutoEntry autoEntry = new AutoEntry(MainActivity.this);
                autoEntry.generate();

            }
        });


        final CalendarView calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Date date = null;
                try {
                    date = dateInputFormat.parse(year + "/" + (month+1) + "/" + dayOfMonth);
                    Common.dateSort = year + "/" + (month+1) + "/" + dayOfMonth + "-" + System.currentTimeMillis();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (date != null) {
                    selectedDate = dateOutputFormat.format(date);
                }
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

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    }


    @Override
    protected void onStart() {
        super.onStart();

        LocationManager locationManager = (LocationManager) getSystemService((Context.LOCATION_SERVICE));
        final boolean networkLocationEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        SharedPreferences.Editor editor = Common.defaultPreferences.edit();

        editor.putBoolean("networkLocationEnabled", networkLocationEnabled);
        editor.apply();
    }

    // Trigger new location updates at interval
    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                        Common.current_location = locationResult.getLastLocation();
                    }
                },
                Looper.myLooper());
    }

    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
//        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }

    public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    public void launchTestService() {
        // Construct our Intent specifying the Service
        Intent i = new Intent(this, SetupNotificationsService.class);
        // Add extras to the bundle
        i.putExtra("foo", "bar");
        // Start the service
        startService(i);
    }

    private void ScheduleJob() {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        JobInfo jobInfo = new JobInfo.Builder(11, new ComponentName(this, MyJobService.class))
                .setPeriodic((1000 * 60 * 60 * 24 * 7), (1000 * 60 * 7))
                // only add if network access is required
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build();

        jobScheduler.schedule(jobInfo);
    }

    public void scheduleAlarm() {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, NotificationReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every every half hour from this point onwards
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pIntent);
    }

    public void createAlarm() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("reminder_time", "10")));
        //Toast.makeText(this, "Alarm created2" , Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, NotificationReceiver.class);

        am = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        am.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent);

    }

    public void getPermissionToReadCalendar() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_CALENDAR)) {
                Toast.makeText(this,"Location is used for getting weather data", Toast.LENGTH_LONG);

            }

            requestPermissions(new String[]{Manifest.permission.READ_CALENDAR}, Common.GET_CALENDAR_PERMISSIONS_REQUEST);
        }
    }

    public void getPermissionToReadLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(this,"Location is used for getting weather data", Toast.LENGTH_LONG);

            }

            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Common.GET_LOCATION_PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == Common.GET_LOCATION_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Locations permission granted", Toast.LENGTH_SHORT).show();
                Common.defaultPreferences.edit().putBoolean("LocationEnabled", true).apply();
            } else {
                // showRationale = false if user clicks Never Ask Again, otherwise true
                boolean showRationale = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION);

                if (showRationale) {
                    Common.defaultPreferences.edit().putBoolean("LocationEnabled", false).apply();
                } else {
                    Toast.makeText(this, "Read Contacts permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == Common.GET_CALENDAR_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Calendar permission granted", Toast.LENGTH_SHORT).show();
                Common.defaultPreferences.edit().putBoolean("CalendarEnabled", true).apply();
            } else {
                // showRationale = false if user clicks Never Ask Again, otherwise true
                boolean showRationale = shouldShowRequestPermissionRationale(Manifest.permission.READ_CALENDAR);

                if (showRationale) {
                    Common.defaultPreferences.edit().putBoolean("CalendarEnabled", false).apply();
                } else {
                    Toast.makeText(this, "Read Calendar permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void setupNotification() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 50);
        calendar.set(Calendar.SECOND, 0);
        Intent intent1 = new Intent(MainActivity.this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0,intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        if (am != null) {
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
        }
    }

    private void setCalendarToday(CalendarView calendarView) {
        calendarView.setDate(System.currentTimeMillis(),true, true);

        selectedDate = getTodayDateAsString();
        mAdapter.swapCursor(getItemsByDate(selectedDate));
    }

    private String getTodayDateAsString() {
        return dateOutputFormat.format(new Date(System.currentTimeMillis()));
    }

    public String getTodayDateForSort() {
        return Common.dateSort;
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
        intent.putExtra("dateSort", Common.dateSort);
        intent.putExtra("entryId", tag.toString());
        startActivity(intent);
    }

    public void LaunchNew() {
        Intent intent = new Intent(this, EditorActivity.class);
        intent.putExtra("selectedDate", selectedDate);
        intent.putExtra("dateSort", Common.dateSort);
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
                deleteAllEntries();
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.actions_export:
                startActivity(new Intent(this, SaveJournalInRange.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


    private void addEntry(String content) {
        if (content.trim().length() == 0) {
            return;
        }

        ContentValues cv = new ContentValues();
        cv.put(JournalContract.JournalEntry.COLUMN_TEXT, content);
        cv.put(JournalContract.JournalEntry.COLUMN_DATE, selectedDate);
        cv.put(JournalContract.JournalEntry.COLUMN_TIMESTAMP, Common.dateSort);

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

    private void deleteAllEntries() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this entry?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabase.delete(JournalContract.JournalEntry.TABLE_NAME, "datetime=?" , new String[]{selectedDate});

                        Toast.makeText(MainActivity.this, "Today's entries deleted", Toast.LENGTH_SHORT).show();
                        mAdapter.swapCursor(getItemsByDate(selectedDate));
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

//    public void createAlarm() {
//        Intent intent = new Intent(this, NotificationReceiver.class);
//
//        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
//
//        am.setInexactRepeating(
//                AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                SystemClock.elapsedRealtime() + (5*1000),
//                (10*1000),
//                pendingIntent);
//
//    }

    public void sendReminderNotification() {

        Intent notificationIntent = new Intent(this, EditorActivity.class);
        notificationIntent.putExtra("selectedDate", getTodayDateAsString());
        notificationIntent.putExtra("dateSort", Common.dateSort);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, Reminder_Channel)
                .setSmallIcon(R.drawable.ic_main_icon_foreground)
                .setContentTitle("Journal Reminder")
                .setContentText("Reminder to write in your journal today!")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(1, notification);
    }
//
//    public void sendErrorNotification(View v) {
//        Notification notification = new NotificationCompat.Builder(this, Reminder_Channel)
//                .setSmallIcon(R.drawable.ic_main_icon_foreground)
//                .setContentTitle("Error Title")
//                .setContentText("Error Content")
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .build();
//
//        notificationManager.notify(2, notification);
//    }


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
