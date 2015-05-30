package com.flicq.tennis.appengine;

import android.util.Log;

import com.flicq.tennis.appengine.model.Shot;
import com.flicq.tennis.appengine.model.Shots;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.util.Calendar;
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

    public com.google.api.client.util.DateTime getTimestamp()
    {
        return new com.google.api.client.util.DateTime(new java.util.Date().getTime());
    }
    Shot shot;
    public void SendCurrentShot(List<Float> shotData) {
        try {
            shot.setTime(getTimestamp());
            shot.setID(System.getProperty("user.name"));
            shot.setItems(shotData);
            query.execute();
            Log.i("Cloud Upload done : ", "soundar");
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }

    public Flicq.FlicqEndpointService.Shots GetShots()
    {
        try {
            Flicq.FlicqEndpointService.Shots.List query;
            Flicq.Builder builder = new Flicq.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            Flicq flicq = builder.build();
            query = flicq.flicqEndpointService().shots().list();
            Flicq.FlicqEndpointService.Shots shots = query.execute();
            return shots;
        }catch(IOException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
}
