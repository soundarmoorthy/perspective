package com.flicq.tennis.contentmanager;

import android.hardware.Sensor;
import android.text.format.Time;

import junit.framework.Assert;

import java.util.ArrayList;
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

//    private float[] accelerationToXYZ(float[] floats) {
//        static final float NS2S = 1.0f / 1000000000.0f;
//        float[] last_values = null;
//        float[] velocity = null;
//        float[] position = null;
//        long last_timestamp = 0;
//
//            if(last_values != null){
//                float dt = (event.timestamp - last_timestamp) * NS2S;
//
//                for(int index = 0; index < 3;++index){
//                    velocity[index] += (event.values[index] + last_values[index])/2 * dt;
//                    position[index] += velocity[index] * dt;
//                }
//            }
//            else{
//                last_values = new float[3];
//                velocity = new float[3];
//                position = new float[3];
//                velocity[0] = velocity[1] = velocity[2] = 0f;
//                position[0] = position[1] = position[2] = 0f;
//            }
//            System.arraycopy(event.values, 0, last_values, 0, 3);
//            last_timestamp = event.timestamp;
//        }
//    }

    private static List<SensorData> medianFilter(final List<SensorData> values) {

        List<Float> p, c, n;
        float[] w = new float[3];
        float[] t = new float[7];
        List<SensorData> r = new ArrayList<SensorData>();
        p = values.get(0).asVector();
        for(int i=1;i<values.size() - 1;i++) {
            c = values.get(i).asVector();
            n = values.get(i + 1).asVector();

            for (int l = 0; l < 7; l++)
            {
                w[0] = p.get(l);
                w[1] = c.get(l);
                w[2] = n.get(l);
                sort(w);
                t[l] = w[1];
            }
            r.add(new SensorData(t));
            p = c;
        }
        return r;
    }

    public List<Float> getDataForUpload() {
        ArrayList<Float> f = new ArrayList<Float>();
        synchronized (valuesLock) {
            for (SensorData value: values){
                f.addAll(value.asVector());
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
