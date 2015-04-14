package com.flicq.tennis.appengine;

import com.flicq.tennis.appengine.model.Shot;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;


/**
 * Created by soundararajan on 4/5/2015.
 */
public final class FlicqCloud {
    Flicq flicq;
    Flicq.Builder builder;
    public FlicqCloud() {
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

    Shot shot;
    public void SendCurrentShot(String data, String id) {
        try {
            //Actual data from sensor
            dataPoint = data.split(";");
            shot.setAX(dataPoint[0]);
            shot.setAY(dataPoint[1]);
            shot.setAZ(dataPoint[2]);

            shot.setQ0(dataPoint[3]);
            shot.setQ1(dataPoint[4]);
            shot.setQ2(dataPoint[5]);
            shot.setQ3(dataPoint[6]);
            shot.setCounter(id);

            query.execute();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }
    public String[] dataPoint = new String[7];
}
