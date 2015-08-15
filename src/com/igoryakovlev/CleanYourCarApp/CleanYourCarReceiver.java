package com.igoryakovlev.CleanYourCarApp;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by Smile on 13.08.15.
 */
public class CleanYourCarReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //add clock
        SharedPreferences sharedPreferences = context.getSharedPreferences("CleanYourCar",Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(MyActivity.TRIGGER_SERVICE,false))
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 6);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND,0);
            Calendar calendarToCheck = Calendar.getInstance();
            calendarToCheck.setTimeInMillis(System.currentTimeMillis());
            if (calendarToCheck.after(calendar))
            {
                calendar.add(Calendar.DAY_OF_YEAR, 1);

            }
            MyActivity.alarmManager.setInexactRepeating(AlarmManager.RTC,calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    //1000*60*3,
                    MyActivity.alarmIntent);
        } else
        {
            //MyActivity.alarmManager.cancel(MyActivity.alarmIntent);
        }
    }
}
