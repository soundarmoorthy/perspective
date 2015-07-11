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
                return medianFilter(accelerationToXYZ(values));
        }
    }

    private static List<SensorData> accelerationToXYZ(final List<SensorData> sensorData) {
        SensorData previous;
        try {
            final float NS2S = 1.0f / 1000000000.0f;
            float v[], p[];
            v = new float[3];
            p = new float[3];
            //for us dt is constant, assuming we get 25 packets per second.
            float dt = ((1 * 1000) / 25) * NS2S;
            previous = sensorData.get(0);
            for (int i = 1; i < sensorData.size(); i++) {
                SensorData current = sensorData.get(i);

                float[] c_acc = current.getAcceleration();
                float[] p_acc = previous.getAcceleration();
                for (int k = 0; k < 3; k++) {
                    v[k] += (c_acc[k] + p_acc[k]) / 2 * dt;
                    p[k] += v[k] * dt;
                    current.set(p[k], k);
                }
                previous = current;
            }
        } catch (Exception ex) {
            Log.e("async", ex.toString());
            ex.printStackTrace();
        }
        return sensorData;
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
