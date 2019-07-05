package xyz.rpka.Weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Api {
    @GET("/weather")
    Call<WearherData> getDataWeather(@Query("lat") Character lat,
                                     @Query("lon") Character lon,
                                     @Query("appid") String appid);
}
