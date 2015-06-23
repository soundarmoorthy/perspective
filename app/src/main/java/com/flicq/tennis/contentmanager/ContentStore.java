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
            //Android (a x=East, y=North, z=Up or ENU standard.

            float[] values = new float[content.length];
            final float acc_lsb = 0.00012207f;
            values[0] = content[0] * acc_lsb;
            values[1] = content[1] * acc_lsb;
            values[2] = content[2] * acc_lsb;
            final float quaternionTimeScale = 30000f;
            for(int i=3;i<7;i++)
            values[i] = content[i] / quaternionTimeScale;

            double normalize = 0.0;
            for(int i=3;i<7;i++)
                normalize = normalize + (values[i] * values[i]);
            normalize = Math.sqrt(normalize);
            for(int i=3;i<7;i++)
                values[i] /= normalize;

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
