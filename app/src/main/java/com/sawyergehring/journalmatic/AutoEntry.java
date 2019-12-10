package com.sawyergehring.journalmatic;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import java.util.Date;

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


    public WeatherResult getWeather(String lat, String lon, String units){
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
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        weatherMap = retrofit.create(IOpenWeatherMap.class);
        compositeDisposable.add(weatherMap.getWeatherByLatLng(String.valueOf(current_location.getLatitude()), String.valueOf(current_location.getLongitude()), Common.WEATHER_API_KEY, "imperial")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherResult>() {
                    @Override
                    public void accept(WeatherResult weatherResult) throws Exception {
                        String message = "The weather today in " + weatherResult.getName()
                                + " consisted of " + weatherResult.getWeatherFirst().getDescription()
                                + " and was " + weatherResult.getMain().getTemp() + " degrees Fahrenheit";
                        addEntry(message);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(appContext, throwable.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }));
//        Toast.makeText(appContext, weatherResult.getName(), Toast.LENGTH_LONG).show();
    }


    public void addEntry() {addEntry("This is an auto Generated entry");}
    public void addEntry(String content) {

        ContentValues cv = new ContentValues();
        cv.put(JournalContract.JournalEntry.COLUMN_TEXT, content);
        cv.put(JournalContract.JournalEntry.COLUMN_DATE, getTodayDateAsString());

        DBOpenHelper dbHelper = new DBOpenHelper(appContext);
        SQLiteDatabase mDatabase = dbHelper.getWritableDatabase();

        mDatabase.insert(JournalContract.JournalEntry.TABLE_NAME, null, cv);
    }

    public void getLastLocation() {
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
