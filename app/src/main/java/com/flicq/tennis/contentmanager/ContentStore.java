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

    public void Dump(final short[] content) {
        synchronized (currentShotLock) {

        /*  Remember, 2 bytes each
            ------------------------------------------------------------------
           | ax.2 | ay.2 | az.2 | q0.2 | q1.2 | q2.2 | q3.2 | seqNo.1 | n/a |
           ------------------------------------------------------------------ */
            final float quaternionTimeScale = 30000f;
            float[] values = new float[content.length];
            for(int i=3;i<7;i++)
            values[i] = content[i] / quaternionTimeScale;

            float ax = content[0];
            float ay = content[1];
            float az = content[2];

            double normalize = 0.0;
            for(int i=3;i<7;i++)
                normalize = normalize + (values[i] * values[i]);
            normalize = Math.sqrt(normalize);
            for(int i=3;i<7;i++)
                values[i] /= normalize;

            // Incoming quaternion is NED.
            // We need to do a minor translation on the quaternion axis,
            // as the app needs it in Android form
            float q1 = values[5];
            float q2 = values[4];
            float q3 = -values[6];

            values[4] = q1;
            values[5] = q2;
            values[6] = q3;

            final float acc_lsb = 0.00012207f;
            values[0] = ax * acc_lsb;
            values[1] = ay * acc_lsb;
            values[2] = az * acc_lsb;
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
