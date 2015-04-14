package com.flicq.tennis.ble;

import com.flicq.tennis.appengine.FlicqCloud;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by soundararajan on 4/13/2015.
 */
public class FlicqSession {
    String timeStamp;
    final FlicqCloud cloud;
    public FlicqSession() {
        timeStamp = String.valueOf(Calendar.getInstance(Locale.getDefault()).getTimeInMillis());
        cloud = new FlicqCloud();
    }

    public FlicqCloud getCloudManager()
    {
        return cloud;
    }

    public String getTimestamp()
    {
        return timeStamp;
    }
}


