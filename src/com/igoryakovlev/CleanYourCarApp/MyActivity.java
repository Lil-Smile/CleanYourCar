package com.igoryakovlev.CleanYourCarApp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.util.*;
import java.util.prefs.Preferences;

public class MyActivity extends Activity implements View.OnClickListener,AsyncResponce {
    /**
     * Called when the activity is first created.
     */

    WeatherAsyncTask weatherAsyncTask;

    TextView tvCleanOrNot;
    TextView tvDaysToForecast;
    Button bKnow;
    ProgressBar progressBar;
    Button buttonServiceUp;
    Button buttonServiceDown;
    SeekBar seekBarDaysToForecast;

    final static String APP_ID = "APPID=f1d392256e09a66fef69fd4117019dc4";
    final static String START_OF_THE_LINE = "http://api.openweathermap.org/data/2.5/forecast/daily?";
    final static String MODE = "mode=json";
    final static String UNITS = "units=metric";
    final static String CNT = "cnt=";
    final static String CITY = "q=";
    final static String BETWEEN="&";
    public static final String WEATHER="weather";
    public static final String KEY = "id";
    final static String SMTH_IS_WRONG="smth is wrong";
    final static String LAT="lat=";
    final static String LON="lon=";

    static String TRIGGER_SERVICE="TRIFFER_SERVICE";

    public static final String JSON_ARRAY_STRING = "list";

    public static int DAYS=5;

    float LONGITUDE_INT,ALTITUDE_INT;
    boolean triggerCoord=false;//не определены

    LocationManager locationManager;
    final static String LONGITUDE="longitude";
    final static String LATITUDE="laltitude";

    public static String CITY_NAME="";

    static AlarmManager alarmManager;
    static PendingIntent alarmIntent;


    final static Set<Integer> idSet = new HashSet<Integer>(); //bad id's list
    static
    {
        idSet.addAll(Arrays.asList(200, 201, 202, 210, 211, 212, 221, 230, 231, 232, //thunderstorm
                300, 301, 302, 310, 311, 312, 313, 314, 321,                        //drizzle
                500, 501, 502, 503, 504, 511, 520, 521, 522, 531,                    //rain
                900, 901, 902, 903, 904, 905, 906));                              //extreme
    }



    static SharedPreferences preferences;



    @Override
    public void onCreate(Bundle savedInstanceState) {

        preferences = getSharedPreferences("CleanYourCar", MODE_PRIVATE);
        DAYS = preferences.getInt("DAYS",5);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        tvCleanOrNot=(TextView)findViewById(R.id.tvClean);
        tvDaysToForecast=(TextView)findViewById(R.id.tvDaysToForecast);
        tvDaysToForecast.setText(getResources().getString(R.string.daysToForecast)+DAYS);
        bKnow=(Button)findViewById(R.id.bKnow);
        bKnow.setOnClickListener(this);
        progressBar=(ProgressBar)findViewById(R.id.progressBarGetting);
        buttonServiceUp=(Button)findViewById(R.id.buttonServiceUp);
        buttonServiceUp.setOnClickListener(this);
        buttonServiceDown=(Button)findViewById(R.id.buttonServiceDown);
        buttonServiceDown.setOnClickListener(this);
        seekBarDaysToForecast = (SeekBar)findViewById(R.id.seekBarForDays);
        seekBarDaysToForecast.setProgress(DAYS);
        seekBarDaysToForecast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DAYS=progress;
                preferences.edit().putInt("DAYS",progress).commit();
                tvDaysToForecast.setText(getResources().getString(R.string.daysToForecast) + DAYS);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if (preferences.getBoolean(TRIGGER_SERVICE,false))
        {
            buttonServiceUp.setEnabled(false);
            buttonServiceDown.setEnabled(true);
        }


        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        if (preferences.getFloat(LONGITUDE, 0f)==0f)
        {
            triggerCoord=false;
            Log.d("tagged","lon:"+preferences.getFloat(LONGITUDE,0f)+" lat:"+preferences.getFloat(LATITUDE,0f));
        }
        else
        {
            triggerCoord=true;
            LONGITUDE_INT=getPreferences(MODE_PRIVATE).getFloat(LONGITUDE,0f);
            ALTITUDE_INT=getPreferences(MODE_PRIVATE).getFloat(LATITUDE,0f);
            //Log.d("COORD", "long:" + LONGITUDE_INT + " alt:" + ALTITUDE_INT);
        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.bKnow:
            {
                if (!triggerCoord)
                {
                    //getting the coord
                    try {

                        Location location = getLastKnownLocation();
                        Log.d("COORD",location.toString() + "long:"+location.getLongitude()+" lat:"+location.getLatitude());
                        SharedPreferences sharedPreferences = getSharedPreferences("CleanYourCar", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putFloat(LONGITUDE,(float) location.getLongitude());
                        editor.putFloat(LATITUDE, (float) location.getLatitude());
                        editor.commit();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                }
                if(isNetworkOnline(this))
                {
                    weatherAsyncTask = new WeatherAsyncTask();
                    weatherAsyncTask.delegate=this;
                    String searchURL = START_OF_THE_LINE + LAT + preferences.getFloat(LATITUDE, 0f) +BETWEEN+LON+preferences.getFloat(LONGITUDE, 0f) + BETWEEN + MODE + BETWEEN + UNITS + BETWEEN + CNT +DAYS+ BETWEEN + APP_ID;
                    bKnow.setVisibility(View.INVISIBLE);
                    tvCleanOrNot.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    weatherAsyncTask.execute(searchURL);
                }
                else
                {
                   Toast.makeText(this,"Нет соединения",Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.buttonServiceUp:
            {
                startService(new Intent(MyActivity.this, CleanYourCarService.class));
                //start the clock
                preferences.edit().putBoolean(TRIGGER_SERVICE,true).commit();
                buttonServiceUp.setEnabled(false);
                buttonServiceDown.setEnabled(true);

                Intent intent = new Intent(getApplicationContext(),CleanYourCarService.class);
                alarmIntent = PendingIntent.getService(getApplicationContext(),0,intent,0);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, 6);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND,0);
                Calendar calendarToCheck = Calendar.getInstance();
                calendarToCheck.setTimeInMillis(System.currentTimeMillis());
                if (calendarToCheck.after(calendar))
                {
                    calendar.add(Calendar.DAY_OF_YEAR,1);
                    Log.d("DATE",calendar.getTime().toString());
                }
                //Log.d("TIME", calendar.getTime().toString());
                alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY,
                        //1000*60*1,
                        alarmIntent);
                Toast.makeText(getApplicationContext(),"Сервис запущен, ждите уведомлений",Toast.LENGTH_LONG).show();

                break;
            }
            case R.id.buttonServiceDown:
            {
                preferences.edit().putBoolean(TRIGGER_SERVICE,false).commit();
                //end the clock
                buttonServiceUp.setEnabled(true);
                buttonServiceDown.setEnabled(false);
                alarmManager.cancel(alarmIntent);
                break;
            }
        }
    }


    public boolean isNetworkOnline(Context context) {
        boolean status = false;
        //Log.d(TAG, "start");
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0); //mobile
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                status = true;
                //Log.d(TAG,"try mobile"+netInfo);
            } else {
                netInfo = cm.getNetworkInfo(1); //wi-fi
                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                    status = true;
                    //Log.d(TAG,"try wi-fi"+netInfo);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return status;

    }

    @Override
    public void processFinished(String[] output) {
        Toast.makeText(this,"Город:"+CITY_NAME+" Прогноз:"+algoritmToClean(output),Toast.LENGTH_LONG).show();
        progressBar.setVisibility(View.INVISIBLE);
        bKnow.setVisibility(View.VISIBLE);
        tvCleanOrNot.setVisibility(View.VISIBLE);
        weatherAsyncTask=null;
    }


    private String algoritmToClean(String params[])
    {
        if(params[0]=="Try Again")
        {
            return "Что-то пошло не так, попробуйте еще раз";
        }
        for (int i = 0; i<DAYS; i++)
        {
            if(idSet.contains(Integer.valueOf(params[i])))
            {
                return "Не мыть";
            }
        }


        return "Мыть";
    }

    private Location getLastKnownLocation() {
        locationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }
}
