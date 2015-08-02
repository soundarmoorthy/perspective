package com.flicq.tennis.contentmanager;

import android.text.format.Time;
import java.util.ArrayList;
import java.util.List;
import static java.util.Arrays.sort;

public class FlicqShot {
    private final Object valuesLock = new Object();
    private final ArrayList<SensorData> values;

    private final Time time;

    public FlicqShot(Time time) {
        this.time = time;
        values = new ArrayList<>();
    }

    int size;
    public List<SensorData> getDataForRendering() {
        if (values == null || values.isEmpty()) return null; synchronized (valuesLock) {
            if (dirty)
                medianFilter(values);
            dirty = false;
            return values;
        }
    }

    private static void medianFilter(final List<SensorData> values) {
        float[] w = new float[3];
        for (int i = 1; i < values.size() - 1; i++) {
            for (int l = 0; l < 7; l++) {
                w[0] = values.get(i-1) .get(l);
                w[1] = values.get(i)   .get(l);
                w[2] = values.get(i+1) .get(l);
                sort(w);
                values.get(i).set(w[1], l);
            }
        }
    }

    public List<Float> getDataForUpload() {
        ArrayList<Float> f = new ArrayList<Float>();
        synchronized (valuesLock) {
            for (SensorData value : values) {
                f.addAll(value.asList());
            }
        }
        return f;
    }


    boolean dirty;
    public void add(final SensorData data)
    {
        //After processing the data we need to make sure that we
        //get them in the order we want, i.e ax,ay,az, q0,q1,q2,q3
        synchronized (valuesLock) {
                this.values.add(data);
                dirty = true;
        }
    }
}
