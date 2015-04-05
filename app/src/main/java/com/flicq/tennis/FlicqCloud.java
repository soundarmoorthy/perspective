package com.flicq.tennis;

import com.flicq.tennis.appengine.Flicq;
import com.flicq.tennis.appengine.model.Shot;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;

/**
 * Created by soundararajan on 4/5/2015.
 */
public final class FlicqCloud {
    Flicq flicqEndPointService;
    Flicq.Builder builder;
    public FlicqCloud() {
        builder = new Flicq.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        flicqEndPointService = builder.build();
    }

    public void Send() throws IOException {
        Shot shot;
        shot = new Shot().setX("100").setY("200").setZ("300").setK("400");
        flicqEndPointService.flicqEndpointService().shots().add(shot);
    }
}
