package com.sawyergehring.journalmatic.Retrofit;

import com.sawyergehring.journalmatic.Common.Common;
import com.sawyergehring.journalmatic.Model.Weather;
import com.sawyergehring.journalmatic.Model.WeatherResult;


import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IOpenWeatherMap {

    @GET("weather")
    Observable<WeatherResult> getWeatherByLatLng(@Query("lat") String lat,
                                                 @Query("lon") String lon,
                                                 @Query("appid") String appid,
                                                 @Query("units") String units);

    WeatherResult getWeather(String lat, String lon, String units);


}
