package com.sawyergehring.journalmatic.Common;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.sawyergehring.journalmatic.Model.WeatherResult;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.json.JSONObject;

import java.io.IOException;

public class Common {

    public static final String WEATHER_API_KEY = "c2f533c20113f7abe00b353a5f1331fb";
    public static Location current_location = null;
    public static final int GET_LOCATION_PERMISSIONS_REQUEST = 1;
    public static final int GET_CALENDAR_PERMISSIONS_REQUEST = 2;
    public static SharedPreferences defaultPreferences;// Identifier for the permission request

}
