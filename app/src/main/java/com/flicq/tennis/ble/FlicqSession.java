package com.flicq.tennis.ble;

import com.flicq.tennis.appengine.FlicqCloudRequestHandler;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by soundararajan on 4/13/2015.
 */
public class FlicqSession {
    String timeStamp;
    public FlicqSession() {
    }

    public FlicqCloudRequestHandler getCloudManager()
    {

        FlicqCloudRequestHandler cloud;
        cloud = new FlicqCloudRequestHandler();
        return cloud;
    }

    public String getTimestamp()
    {
        return timeStamp;
    }
}


