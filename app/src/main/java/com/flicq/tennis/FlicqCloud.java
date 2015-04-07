package com.flicq.tennis;

import android.os.AsyncTask;

import com.flicq.tennis.appengine.Flicq;
import com.flicq.tennis.appengine.model.Shot;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;

/**
 * Created by soundararajan on 4/5/2015.
 */
public final class FlicqCloud {
    Flicq flicq;
    Flicq.Builder builder;
    public FlicqCloud() {
        builder = new Flicq.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        flicq = builder.build();
    }

    public void Send() {
        try {
            AsyncTask<Void,Void,Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids){
                    try {

                        Shot shot;
                        shot = new Shot().setX("3.222").setY("3.333").setZ("3.5").setK("1.2");
                        Flicq.FlicqEndpointService.Shots.Add query = flicq.flicqEndpointService().shots().add(shot);
                        query.execute();
                        return Void.TYPE.newInstance();

                    } catch (Exception ioe) {
                        ioe.printStackTrace();
                        return null;
                    }
                }
            };
                    task.execute();
        }catch  (Exception ex){}
    }
}
