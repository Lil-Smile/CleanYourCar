package com.igoryakovlev.CleanYourCarApp;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Smile on 13.08.15.
 */
public class WeatherAsyncTask extends AsyncTask<String,Void,String> {

    public AsyncResponce delegate=null;


    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection con = null ;
        InputStream is = null;

        try {
            con = (HttpURLConnection) ( new URL(params[0])).openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.connect();

            // Let's read the response
            StringBuffer buffer = new StringBuffer();
            is = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while (  (line = br.readLine()) != null )
            {
                buffer.append(line + "\r\n");
                Log.d("WEATGER", line);
            }
            is.close();
            con.disconnect();
            Thread.sleep(1000);
            return buffer.toString();
        }
        catch(Throwable t) {
            t.printStackTrace();
            //publishProgress("Smth is wrong");
        }
        finally {
            try { is.close(); } catch(Throwable t) {}
            try { con.disconnect(); } catch(Throwable t) {}
        }

        return null;
    }
    @Override
    protected void onPostExecute(String param)
    {
        if(param==null)
        {
            String[] nullInput= new String[MyActivity.DAYS];
            for (int i = 0; i<MyActivity.DAYS;i++)
            {
                nullInput[i]="Try Again";
            }
            delegate.processFinished(nullInput);
            return;
        }
        String[] strings = new String[MyActivity.DAYS];
        try {
            JSONObject jsonObject = new JSONObject(param);
            JSONObject jsonArrayForCity=jsonObject.getJSONObject("city");
            MyActivity.CITY_NAME=jsonArrayForCity.getString("name");
            //Log.d("CITY_NAME",CITY_NAME);
            JSONArray jsonArray = jsonObject.getJSONArray(MyActivity.JSON_ARRAY_STRING);
            for (int i = 0; i<jsonArray.length(); i++)
            {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                JSONArray jsonArray1 = jsonObject1.getJSONArray(MyActivity.WEATHER);
                JSONObject weatherObj = jsonArray1.getJSONObject(0);
                String weather = weatherObj.getString(MyActivity.KEY);
                Log.d("TAGGED",weather);
                strings[i]=weather;
            }

            delegate.processFinished(strings);

        } catch (JSONException e) {
            e.printStackTrace();

        }

        //tvCleanOrNot.setText("Answer:"+algoritmToClean(strings));
        //Toast.makeText(MyActivity.this, "Город:" + CITY_NAME + " Ответ:" + algoritmToClean(strings), Toast.LENGTH_LONG).show();
        //progressBar.setVisibility(View.INVISIBLE);
        //bKnow.setEnabled(true);
        //bKnow.setVisibility(View.VISIBLE);
        //tvCleanOrNot.setVisibility(View.VISIBLE);
    }
}
