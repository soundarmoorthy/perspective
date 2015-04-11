package com.flicq.tennis;

import android.os.AsyncTask;

import com.flicq.tennis.appengine.Flicq;
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
    }

    public void Send() {
        try {
            AsyncTask<Void,Void,Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids){
                    try {

                        Shot shot;
                        shot = new Shot().setAX("1.0").setAY("2.0").setAZ("3.0").setQ0("4.0").setQ1("5.0")
                                .setQ2("6.0").setQ3("7.0");
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
