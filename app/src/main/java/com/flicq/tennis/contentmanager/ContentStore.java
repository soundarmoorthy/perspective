package com.flicq.tennis.contentmanager;


import android.os.AsyncTask;
import android.text.format.Time;
import com.flicq.tennis.appengine.FlicqCloudRequestHandler;
import com.flicq.tennis.framework.IActivityAdapter;

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
            float quaternionTimeScale = 30000f;
            values[0] = values[0] / quaternionTimeScale;
            values[1] = values[1] / quaternionTimeScale;
            values[2] = values[2] / quaternionTimeScale;
            values[3] = values[3] / quaternionTimeScale;
            double normalize = (values[0] * values[0])
                             + (values[1] * values[1])
                             + (values[2] * values[2])
                             + (values[3] * values[3]);
            normalize = Math.sqrt(normalize);
            values[0] /= normalize;
            values[1] /= normalize;
            values[2] /= normalize;
            values[3] /= normalize;

            float q1 = values[2];
            float q2 = values[1];
            float q3 = -values[3];

            float ax = values[4];
            float ay = values[5];
            float az = values[6];

            values[1] = q1;
            values[2] = q2;
            values[3] = q3;

            values[4] = ax * 0.00012207f;
            values[5] = ay * 0.00012207f;
            values[6] = az * 0.00012207f;
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
