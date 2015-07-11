package com.flicq.tennis.contentmanager;

import android.hardware.Sensor;
import android.text.format.Time;
import android.util.Log;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.*;

public class FlicqShot {
    private final Object valuesLock = new Object();
    private final ArrayList<SensorData> values;

    private final Time time;

    public FlicqShot(Time time) {
        this.time = time;
        values = new ArrayList<SensorData>();
    }

    public List<SensorData> getDataForRendering() {
        if(values == null)
            return null;
        if(values.size() < 1)
            return null;
        synchronized (valuesLock) {
                return medianFilter(values);
        }
    }


    private static List<SensorData> medianFilter(final List<SensorData> values) {

        float[] p, c, n;
        p = new float[7];
        c = new float[7];
        n = new float[7];
        float[] w = new float[3];
        p = values.get(0).asVector();
        for(int i=1;i<values.size() - 1;i++) {
            c = values.get(i).asVector();
            n = values.get(i + 1).asVector();

            for (int l = 0; l < 7; l++) {
                w[0] = p[l];
                w[1] = c[l];
                w[2] = n[l];
                sort(w);
                values.get(i).set(w[1], l);
            }
            p = c;
        }
        return values;
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


    public void add(final SensorData data)
    {
        //After processing the data we need to make sure that we
        //get them in the order we want, i.e ax,ay,az, q0,q1,q2,q3
        synchronized (valuesLock) {
                this.values.add(data);
        }
    }
}
