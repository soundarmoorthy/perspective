package com.flicq.tennis.contentmanager;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TimeZone;

/**
 * Created by soundararajan on 4/26/2015.
 */
public class ContentStore {
    //private LinkedList<UnprocessedShot> shots;
    boolean stopped;

    public UnprocessedShot getShot()
    {
        return currentShot;
    }

    private ContentStore()
    {
        //shots = new LinkedList<UnprocessedShot>();
        stopped = false;
    }

    private UnprocessedShot currentShot;
    private Object currentShotLock = new Object();
    public void NewShot() {
        synchronized (currentShotLock) {
            long id = System.currentTimeMillis();
            currentShot = new UnprocessedShot(id);
            //shots.add(currentShot);
        }
    }

    public void Dump(float[] values)
    {
        if(stopped)
            return;
        synchronized (currentShotLock) {
                currentShot.add(values);
        }
    }

    public void Stop()
    {
        stopped = true;
    }

    //Should we expose this as android service
    private static ContentStore store;
    public static ContentStore Instance()
    {
        if(store == null)
            store = new ContentStore();
        return store;
    }

    public void Upload()
    {

    }
}
