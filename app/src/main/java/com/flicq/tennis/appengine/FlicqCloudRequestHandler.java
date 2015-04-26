package com.flicq.tennis.appengine;

import android.util.Log;

import com.flicq.tennis.appengine.model.Shot;
import com.flicq.tennis.appengine.model.Shots;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


/**
 * Created by soundararajan on 4/5/2015.
 */
public final class FlicqCloudRequestHandler {
    final Flicq flicq;
    final Flicq.Builder builder;
    public FlicqCloudRequestHandler() {
        builder = new Flicq.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        flicq = builder.build();

        try {
            shot = new Shot();
            query = flicq.flicqEndpointService().shots().add(shot);
        }catch(Exception ioe)
        {
            ioe.printStackTrace();
        }
    }
    Flicq.FlicqEndpointService.Shots.Add query;

    public String getTimestamp()
    {
        String timeStamp = String.valueOf(Calendar.getInstance(Locale.getDefault()).getTimeInMillis());
        return timeStamp;
    }
    Shot shot;
    public void SendCurrentShot(float[] dataPoint) {
        try {
            shot.setAX(String.valueOf(dataPoint[0]));
            shot.setAY(String.valueOf(dataPoint[1]));
            shot.setAZ(String.valueOf(dataPoint[2]));

            shot.setQ0(String.valueOf(dataPoint[3]));
            shot.setQ1(String.valueOf(dataPoint[4]));
            shot.setQ2(String.valueOf(dataPoint[5]));
            shot.setQ3(String.valueOf(dataPoint[6]));
            String timeStamp = getTimestamp();
            shot.setCounter(timeStamp);
            query.execute();
            Log.i("Cloud Upload done : ", timeStamp);
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }

    public float[] GetShots()
    {
        try {
            Flicq.FlicqEndpointService.Shots.List query;
            Flicq.Builder builder = new Flicq.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            Flicq flicq = builder.build();
            query = flicq.flicqEndpointService().shots().list();
            Shots shots =  query.execute();
            return TransformShots(shots.getItems());
        }catch(IOException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    private float[] TransformShots(List<Shot> shots) {
        float[] f = new float[shots.size() * 7];
        int index = 0;

        Collections.sort(shots);
        for (int counter = 0; counter < shots.size(); counter++) {
            Shot shot = shots.get(counter);
            int aI = counter * 7; //array Index
            f[aI + 0] = Float.valueOf(shot.getAX());
            f[aI + 1] = Float.valueOf(shot.getAY());
            f[aI + 2] = Float.valueOf(shot.getAZ());
            f[aI + 3] = Float.valueOf(shot.getQ0());
            f[aI + 4] = Float.valueOf(shot.getQ1());
            f[aI + 5] = Float.valueOf(shot.getQ2());
            f[aI + 6] = Float.valueOf(shot.getQ3());
        }
        return f;
    }

    public String[] dataPoint = new String[7];
}
