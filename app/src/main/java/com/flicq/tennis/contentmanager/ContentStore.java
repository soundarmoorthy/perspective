package com.flicq.tennis.contentmanager;


import android.os.AsyncTask;
import android.text.format.Time;
import com.flicq.tennis.appengine.FlicqCloudRequestHandler;

/**
 * Created by soundararajan on 4/26/2015.
 */
public class ContentStore {
    public FlicqShot getShot() {
        synchronized (currentShotLock) {
            return currentShot;
        }
    }

    private ContentStore() {
    }

    private FlicqShot currentShot;
    private final Object currentShotLock = new Object();

    public void NewShot() {
        synchronized (currentShotLock) {
            long id = System.currentTimeMillis();
            Time time = new Time();
            time.setToNow();
            currentShot = new FlicqShot(time);
        }
    }



    public void Dump(final float[] values) {
        synchronized (currentShotLock) {
            currentShot.add(values);
        }
    }

    public void ShotDone()
    {
        FlicqShot cachedShot = currentShot;
        UploadAsync(cachedShot);
    }

    private static void UploadAsync(final FlicqShot shot) {
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                FlicqCloudRequestHandler handler = new FlicqCloudRequestHandler();
                handler.SendShot(shot);
                return null;
            }
        }.execute();
    }

    private static ContentStore instance;
    public static ContentStore Instance() {
        if(instance == null)
            instance = new ContentStore();
        return instance;
    }
}
