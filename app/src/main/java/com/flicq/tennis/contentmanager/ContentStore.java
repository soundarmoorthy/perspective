package com.flicq.tennis.contentmanager;


import com.flicq.tennis.opengl.ShotRenderer;

/**
 * Created by soundararajan on 4/26/2015.
 */
public class ContentStore {
    //private LinkedList<UnprocessedShot> shots;
    boolean stopped;

    public UnprocessedShot getShot() {
        return currentShot;
    }

    private ContentStore() {
        //shots = new LinkedList<UnprocessedShot>();
        stopped = false;
    }

    private UnprocessedShot currentShot;
    private final Object currentShotLock = new Object();

    public void NewShot() {

        synchronized (currentShotLock) {
            long id = System.currentTimeMillis();
            currentShot = new UnprocessedShot(id);
        }
    }

    public void Dump(float[] values) {
        if (stopped)
            return;
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

    public void Stop() {
        stopped = true;
    }

    //Should we expose this as android service
    private static ContentStore store;

    public static ContentStore Instance() {
        if (store == null)
            store = new ContentStore();
        return store;
    }

    public void Upload() {

    }
}
