package com.igoryakovlev.CleanYourCarApp;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;
import java.util.prefs.Preferences;

/**
 * Created by Smile on 13.08.15.
 */
public class CleanYourCarService extends Service implements AsyncResponce{

    public boolean trigger = false;//gave the forecast or not
    public static SharedPreferences preferences;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        if(isNetworkOnline(this)){
            trigger=true;
            preferences = getSharedPreferences("CleanYourCar",MODE_PRIVATE);
            WeatherAsyncTask weatherAsyncTask = new WeatherAsyncTask();
            weatherAsyncTask.delegate=this;
            String searchURL = MyActivity.START_OF_THE_LINE + MyActivity.LAT +
                    //MyActivity.preferences.getFloat(MyActivity.LATITUDE,0f) +
                    preferences.getFloat(MyActivity.LATITUDE,0f) +
                    MyActivity.BETWEEN +
                    MyActivity.LON+
                    //MyActivity.preferences.getFloat(MyActivity.LONGITUDE, 0f) +
                    preferences.getFloat(MyActivity.LONGITUDE,0f) +
                    MyActivity.BETWEEN + MyActivity.MODE + MyActivity.BETWEEN + MyActivity.UNITS + MyActivity.BETWEEN + MyActivity.CNT + MyActivity.DAYS + MyActivity.BETWEEN + MyActivity.APP_ID;
            weatherAsyncTask.execute(searchURL);
            Log.d("TAGGED","executed:"+searchURL);

        } else
        {
            Log.d("tagged","no internet");
            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(),0,intent,0);
            Notification.Builder builder = new Notification.Builder(this).setContentTitle(getResources().getString(R.string.app_name)).setContentText("Похоже, нет сети, попробуйте еще раз").setContentIntent(pendingIntent).setSmallIcon(R.drawable.app_icon).setAutoCancel(true);
            Notification notification = builder.build();
            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0,notification);
            /*//i hope it's clear
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000*60*10);
                        startService(new Intent(getApplicationContext(),CleanYourCarService.class));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });*/
        }


        return START_NOT_STICKY;
    }

    @Override
    public void processFinished(String[] output) {
        Notification.Builder notificationBuilder = new  Notification.Builder(this).setContentTitle(getResources().getString(R.string.app_name)).setContentText("Город:"+MyActivity.CITY_NAME+" прогноз:"+algoritmToClean(output)).setSmallIcon(R.drawable.app_icon);
        Notification notification = new Notification.BigTextStyle(notificationBuilder).bigText("Город:"+MyActivity.CITY_NAME+" прогноз:"+algoritmToClean(output)).build();
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1,notification);
        Log.d("TAGGED","notified");
    }


    private String algoritmToClean(String params[])
    {
        if (params[0]=="Try Again")
        {
            return "Что-то пошло не так, попробуйте еще раз";
        }
        for (int i = 0; i<MyActivity.DAYS; i++)
        {
            if(MyActivity.idSet.contains(Integer.valueOf(params[i])))
            {
                return "Не мыть";
            }
        }


        return "Мыть";
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
}
