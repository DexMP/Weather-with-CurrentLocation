package xyz.rpka.Weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Api {
    //55.8089,38.4212
    @GET("47c6ed136de7aa4073f17f6c69a5d308/{lat},{lon}")
    Call<WearherData> getDataWeather(@Path("lat") String lat, @Path("lon") String lon);
}
