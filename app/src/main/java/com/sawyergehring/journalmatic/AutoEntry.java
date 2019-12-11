package com.sawyergehring.journalmatic;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.location.LocationManagerCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.sawyergehring.journalmatic.Common.Common;
import com.sawyergehring.journalmatic.Model.WeatherResult;
import com.sawyergehring.journalmatic.Retrofit.IOpenWeatherMap;
import com.sawyergehring.journalmatic.Retrofit.RetrofitClient;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

import static androidx.core.content.ContextCompat.getSystemService;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class AutoEntry {


    private Context appContext;
    private IOpenWeatherMap weatherMap;
    private Location current_location;
    private CompositeDisposable compositeDisposable;
    private WeatherResult weatherResult;


    public AutoEntry(Context context) {
        appContext = context;
//        getLastLocation();
//        getWeather();
    }

    public void generate() {
        getLastLocation();
//        getWeather(String.valueOf(current_location.getLatitude()), String.valueOf(current_location.getLongitude()), "imperial");
    }


    public WeatherResult getWeather(String lat, String lon, String units) {
        WeatherResult result = null;
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat
                + "&lon=" + lon
                + "&appid=" + Common.WEATHER_API_KEY
                + "&units=" + units;
        JsonObjectRequest weatherApiRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Moshi moshi = new Moshi.Builder().build();
                JsonAdapter<WeatherResult> jsonAdapter = moshi.adapter(WeatherResult.class);

                try {
                    weatherResult = jsonAdapter.fromJson(response.toString());
                    String message = "The weather today in " + weatherResult.getName()
                            + " consisted of " + weatherResult.getWeatherFirst().getDescription()
                            + " and was " + weatherResult.getMain().getTemp() + " degrees Fahrenheit";
                    addEntry(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                System.out.println(weatherResult);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        RequestQueue queue = Volley.newRequestQueue(appContext);
        queue.add(weatherApiRequest);

        try {
            queue.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return weatherResult;
    }

    private void getWeather() {
        if (!Common.defaultPreferences.getBoolean("LocationEnabled", false) || !Common.defaultPreferences.getBoolean("weather", false)) {
            getCalendar("");
            return;
        }
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        weatherMap = retrofit.create(IOpenWeatherMap.class);
        compositeDisposable.add(weatherMap.getWeatherByLatLng(String.valueOf(current_location.getLatitude()), String.valueOf(current_location.getLongitude()), Common.WEATHER_API_KEY, "imperial")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherResult>() {
                    @Override
                    public void accept(WeatherResult weatherResult) throws Exception {

                        String weather_1 = Common.defaultPreferences.getString("weather_1", "The weather today in");
                        String weather_2 = Common.defaultPreferences.getString("weather_2", "consisted of");
                        String weather_3 = Common.defaultPreferences.getString("weather_3", "and was");

                        String message = weather_1 + " " + weatherResult.getName()
                                + " " + weather_2  + " " + weatherResult.getWeatherFirst().getDescription()
                                + " " + weather_3 + " " + weatherResult.getMain().getTemp() + " degrees Fahrenheit";
                        getCalendar(message);
                        //addEntry(message);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(appContext, throwable.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }));
//        Toast.makeText(appContext, weatherResult.getName(), Toast.LENGTH_LONG).show();
    }

    private void getCalendar(String string) {
        if (appContext.checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED || !Common.defaultPreferences.getBoolean("calendar", false)) {
            // TO.DO: Consider calling
            //    Activity#requestPermissions
            if (!string.isEmpty()) {
                addEntry(string);
            }
            return;
        }

        String message = "";


        if (!string.isEmpty()) {
            message += string + "\n\n\n\n";
        }
        String calendar_1 = Common.defaultPreferences.getString("calendar_1", "My Events Today consisted of:");
        String calendar_2 = Common.defaultPreferences.getString("calendar_2", "located at:");

        message += calendar_1 + " " + "\n";

        String[] selection = new String [] {CalendarContract.Events.TITLE,
                                CalendarContract.Events.EVENT_LOCATION,
                                CalendarContract.Events.DESCRIPTION,
                                CalendarContract.Events.DTSTART,
                                CalendarContract.Events.DTEND};

        Cursor cursor = appContext.getContentResolver().query(CalendarContract.Events.CONTENT_URI, selection, null, null, CalendarContract.Events.DTSTART + " ASC", null);

        while (cursor.moveToNext()) {
            if (cursor!=null) {
                int title_id = cursor.getColumnIndex(CalendarContract.Events.TITLE);
                int desc_id = cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION);
                int loc_id = cursor.getColumnIndex(CalendarContract.Events.EVENT_LOCATION);
                int date_id = cursor.getColumnIndex(CalendarContract.Events.DTSTART);
                int dateEnd_id = cursor.getColumnIndex(CalendarContract.Events.DTEND);

                String titleValue = cursor.getString(title_id);
                String descValue = cursor.getString(desc_id);
                String locValue = cursor.getString(loc_id);
                String dateValue = cursor.getString(date_id);
                String dateEndValue = cursor.getString(dateEnd_id);

                DateFormat dateTimeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(Long.valueOf(dateValue));

                String timeEnd = "";
                if (dateEndValue != null) {
                    Calendar calendarTimeEnd = Calendar.getInstance();
                    calendarTimeEnd.setTimeInMillis(Long.valueOf(dateEndValue));
                    timeEnd = dateTimeFormat.format(calendarTimeEnd.getTime());
                }


                long mDate = Long.valueOf(dateValue);
                long reminder_time = System.currentTimeMillis() - (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 1000 * 60 * 60);
                long reminder_time2 = System.currentTimeMillis() + ((24-Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) * 1000 * 60 * 60);

                String temp = "";
                if (reminder_time2 >= mDate && reminder_time <= mDate) {

                    if (!titleValue.isEmpty()) {
                        message += titleValue + "\n";
                    }
                    if (!descValue.isEmpty()) {
                        temp += descValue + "\n";
                    }
                    if (!locValue.isEmpty()) {
                        temp += "\n" + calendar_2 + "\n" + locValue + "\n";
                    }
                    if (!dateValue.isEmpty()) {
                        temp += "\t\nAt " + dateTimeFormat.format(calendar.getTime());
                    }
                    if (!dateEndValue.isEmpty()) {
                        temp += " to " + timeEnd;
                    }

                    message += temp.replaceAll("(?m)^", "\t\t") +"\n\n";
                }



            }
        }
        addEntry(message);
    }

    public void addEntry(String content) {

        ContentValues cv = new ContentValues();
        cv.put(JournalContract.JournalEntry.COLUMN_TEXT, content);
        cv.put(JournalContract.JournalEntry.COLUMN_DATE, getTodayDateAsString());
        cv.put(JournalContract.JournalEntry.COLUMN_TIMESTAMP, Common.dateSort);

        DBOpenHelper dbHelper = new DBOpenHelper(appContext);
        SQLiteDatabase mDatabase = dbHelper.getWritableDatabase();

        mDatabase.insert(JournalContract.JournalEntry.TABLE_NAME, null, cv);
    }

    public void getLastLocation() {
        if (!Common.defaultPreferences.getBoolean("LocationEnabled", false)) {
            return;
        }
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(appContext);

        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            current_location = location;
                            String msg = "Updated Location in AutoEntry: " +
                                    Double.toString(location.getLatitude()) + "," +
                                    Double.toString(location.getLongitude());
                            Toast.makeText(appContext, msg, Toast.LENGTH_SHORT).show();
                            getWeather();
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



    private String getTodayDateAsString() {
        DateFormat dateOutputFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        return dateOutputFormat.format(new Date(System.currentTimeMillis()));
    }
}
