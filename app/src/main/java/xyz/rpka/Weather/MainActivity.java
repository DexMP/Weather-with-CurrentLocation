package xyz.rpka.Weather;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /* Constants */
    public static final long FASTEST_UPDATE_INTERVAL    = 60000; /* In milliseconds*/
    public static final long UPDATE_INTERVAL            = 60000;  /* In milliseconds*/
    public static final int LAUNCH_SOURCE_ON_RESUME     = 1;
    public static final int LAUNCH_SOURCE_START         = 0;
    public static final int REQUEST_LOCATION            = 199;
    public static final int ERROR_GPS_OFF               = 10;
    public static final int ERROR_PERMISSION_DENIED     = 20;
    public static final int ERROR_DENIED_PERMANENTLY    = 30;
    public static final int ERROR_CONNECTION_FAILED     = 40;
    public static final int ERROR_INTERNET_UNAVAILABLE  = 50;
    private static final String baseURL                 = "http://email.yaxroma.org/data.php";
    public static final String WEATHER_KEY = "14694abc40a51e1b28c053fdea1faa06";

    /* Widgets */
    private TextView currentWeatherView;
    private TextView currentLocationView;
    private TextView lastUpdateView;
    private View mainContent;
    private TextView errorTitleView;
    private ImageView errorIconView;
    private Button buttonResolver;

    /* Variables */
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private SettingsClient mSettingsClient;

    private ConnectivityManager connectivityManager;
    private boolean userDeniedSwitchingOnGPS = false;
    private boolean isPausedEarlier = false;
    private boolean updatesLaunched = false;
    ResolvableApiException exceptionRAE;
    private int currentErrorState = 0;

    private Location currentLocation;
    private String lastUpdateTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initLocationServices();
        if(!isPermissionGranted()) {
            askForPermission();
        } else {
            beginLocationUpdates(LAUNCH_SOURCE_START);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!updatesLaunched && isPausedEarlier) {
            beginLocationUpdates(LAUNCH_SOURCE_ON_RESUME);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(!updatesLaunched && isPermissionGranted()) {
            beginLocationUpdates(LAUNCH_SOURCE_START);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(updatesLaunched) {
            stopLocationUpdates();
            Toast.makeText(getApplicationContext(), "Отправка местоположения приостановлена!", Toast.LENGTH_SHORT).show();
            isPausedEarlier = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(updatesLaunched) {
            stopLocationUpdates();
            Toast.makeText(getApplicationContext(), "Отправка местоположения завершена!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOCATION) {
            switch (resultCode) {
                case Activity.RESULT_OK: {
                    userDeniedSwitchingOnGPS = false;
                    break;
                }
                case Activity.RESULT_CANCELED: {
                    userDeniedSwitchingOnGPS = true;
                    break;
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnResolveError) {
            switch(currentErrorState){
                case ERROR_GPS_OFF:
                    try {
                        exceptionRAE.startResolutionForResult(MainActivity.this, REQUEST_LOCATION);
                    } catch (IntentSender.SendIntentException e) {
                        Toast.makeText(getApplicationContext(), "Невозможно выполнить запрос на получение разрешения!", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ERROR_DENIED_PERMANENTLY:
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                    intent.setData(uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    break;
                case ERROR_PERMISSION_DENIED:
                    askForPermission();
                    break;
                case ERROR_INTERNET_UNAVAILABLE:
                    if(isNetworkUnavailable()) {
                        Snackbar.make(mainContent, "Отсутствует подключение к интернету", Snackbar.LENGTH_SHORT).show();
                    } else {
                        if(isPermissionGranted()) {
                            beginLocationUpdates(LAUNCH_SOURCE_START);
                        }
                    }
                    break;
            }
        }
    }


    private void initData() {
        connectivityManager     = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        currentLocationView     = findViewById(R.id.currentLocation);
        lastUpdateView          = findViewById(R.id.lastUpdate);
        errorTitleView          = findViewById(R.id.errorTitle);
        buttonResolver          = findViewById(R.id.btnResolveError);
        errorIconView           = findViewById(R.id.errorIcon);
        mainContent             = findViewById(R.id.root);
        currentWeatherView      = findViewById(R.id.currentWeather);

        buttonResolver.setOnClickListener(this);
    }

    private void initLocationServices() {
        mFusedLocationClient    = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient         = LocationServices.getSettingsClient(this);
        mLocationRequest        = new LocationRequest();

        mLocationCallback       = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
                lastUpdateTime = DateFormat.getTimeInstance().format(new Date());

                updateUI();
                sendLocation(currentLocation);
            }
        };

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setInterval(UPDATE_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    public void beginLocationUpdates(final int _source) {
        if(isNetworkUnavailable()) {
            showErrorState(ERROR_INTERNET_UNAVAILABLE);
            return;
        }
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            showErrorState(ERROR_PERMISSION_DENIED);
                            return;
                        }

                        hideErrorState();

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        if(_source == LAUNCH_SOURCE_START) {
                            Snackbar.make(mainContent, "Отправка местоположения запущена", Snackbar.LENGTH_SHORT).show();
                        } else if(isPausedEarlier) {
                            Snackbar.make(mainContent, "Отправка местоположения возобновлена", Snackbar.LENGTH_SHORT).show();
                        }

                        updatesLaunched = true;
                        updateUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED: {
                                if(!userDeniedSwitchingOnGPS) {
                                    try {
                                        exceptionRAE = (ResolvableApiException) e;
                                        exceptionRAE.startResolutionForResult(MainActivity.this, REQUEST_LOCATION);
                                    } catch (IntentSender.SendIntentException sie) {
                                        Toast.makeText(getApplicationContext(), "Невозможно выполнить запрос на получение разрешения!", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                }
                            }
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                showErrorState(ERROR_GPS_OFF);
                                updatesLaunched = false;
                                return;
                        }

                        updatesLaunched = false;
                        updateUI();
                    }
                });
    }

    private void stopLocationUpdates() {
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updatesLaunched = false;
                        hideUI();
                    }
                });
    }

    private void updateUI() {
        if (currentLocation != null) {
            char lat = (char) currentLocation.getLatitude();
            char lon = (char) currentLocation.getLongitude();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://samples.openweathermap.org/data/2.5")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            Api api = retrofit.create(Api.class);
            Call<WearherData> call = api.getDataWeather(lat, lon, WEATHER_KEY);
            call.enqueue(new Callback<WearherData>() {
                @Override
                public void onResponse(Call<WearherData> call, retrofit2.Response<WearherData> response) {
                    WearherData wearherData = response.body();

                    currentLocationView.setText("Широта: " + currentLocation.getLatitude() + "\n" + "Долгота: " + currentLocation.getLongitude());
                    lastUpdateView.setText("Последняя отправка: " + lastUpdateTime);
                    currentLocationView.setText(wearherData.main.getTemp() + "C°");
                }

                @Override
                public void onFailure(Call<WearherData> call, Throwable t) {

                }
            });

            currentWeatherView.setVisibility(View.VISIBLE);
            currentLocationView.setVisibility(View.VISIBLE);
            lastUpdateView.setVisibility(View.VISIBLE);
        } else {
            hideUI();
        }
    }

    private void sendLocation(Location _curLocation) {
        if(isNetworkUnavailable()){
            stopLocationUpdates();
            showErrorState(ERROR_INTERNET_UNAVAILABLE);
        } else {
            RequestQueue LocationSendRequestQueue = Volley.newRequestQueue(this);
            LocationSendRequestQueue.add(new StringRequest(Request.Method.GET, baseURL + getRequest(_curLocation), responseListener, responseErrorListener));
        }
    }

    public void showErrorState(int state) {
        hideUI();
        hideErrorState();

        switch(state) {
            case ERROR_GPS_OFF:
                errorTitleView.setText(R.string.error_gps_on_denied);
                buttonResolver.setText(R.string.turn_on_gps);
                errorIconView.setImageResource(R.drawable.ic_gps_off);
                break;
            case ERROR_PERMISSION_DENIED:
                errorTitleView.setText(R.string.error_gps_permission_denied);
                buttonResolver.setText(R.string.grant_permission);
                errorIconView.setImageResource(R.drawable.ic_warning);
                break;
            case ERROR_DENIED_PERMANENTLY:
                errorTitleView.setText(R.string.error_denied_permanently);
                buttonResolver.setText(R.string.btn_open_setting);
                errorIconView.setImageResource(R.drawable.ic_error);
                break;
            case ERROR_INTERNET_UNAVAILABLE:
                final Handler handler = new Handler();
                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        if (!isNetworkUnavailable()) {
                            handler.removeCallbacksAndMessages(null);
                            beginLocationUpdates(LAUNCH_SOURCE_START);
                        } else {
                            handler.postDelayed(this, 1000);
                        }
                    }
                };
                handler.post(run);
                errorTitleView.setText(R.string.error_internet_unavailable);
                buttonResolver.setText(R.string.btn_check_connection);
                errorIconView.setImageResource(R.drawable.ic_internet_failed);
                break;
            case ERROR_CONNECTION_FAILED:
                errorTitleView.setText(R.string.error_conn_failed);
                buttonResolver.setText(R.string.btn_check_connection);
                errorIconView.setImageResource(R.drawable.ic_internet_failed);
                break;
        }

        currentErrorState = state;

        errorTitleView.setVisibility(View.VISIBLE);
        buttonResolver.setVisibility(View.VISIBLE);
        errorIconView.setVisibility(View.VISIBLE);
    }

    public void hideErrorState() {
        errorTitleView.setVisibility(View.GONE);
        buttonResolver.setVisibility(View.GONE);
        errorIconView.setVisibility(View.GONE);
    }

    private boolean isPermissionGranted() {
        return ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void askForPermission() {
        Dexter
            .withActivity(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(new OwnPermissionListener(this))
            .check();
    }

    public void hideUI() {
        currentWeatherView.setVisibility(View.GONE);
        currentLocationView.setVisibility(View.GONE);
        lastUpdateView.setVisibility(View.GONE);
    }

    boolean isNetworkUnavailable() {
        return connectivityManager.getActiveNetworkInfo() == null || !connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private String getRequest(Location _location) {
        Date dateTime   = new Date();
        String ccKey    = "sample_key";

        StringBuilder builder = new StringBuilder();
        builder
            .append("?device=")
            .append(android.os.Build.MODEL)
            .append("&date=")
            .append(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(dateTime))
            .append("&time=")
            .append(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(dateTime))
            .append("&geo=(")
            .append(_location.getLatitude())
            .append(" , ")
            .append(_location.getLongitude())
            .append(")")
            .append("&cc_key=")
            .append(ccKey);

        return builder.toString();
    }

    Response.Listener<String> responseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            Toast.makeText(getApplicationContext(), "Местоположение отправлено!", Toast.LENGTH_SHORT).show();
        }
    };

    Response.ErrorListener responseErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            stopLocationUpdates();
            showErrorState(ERROR_CONNECTION_FAILED);
        }
    };

}
