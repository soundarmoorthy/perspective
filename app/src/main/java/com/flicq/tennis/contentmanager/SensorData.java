package com.flicq.tennis.contentmanager;

import android.hardware.Sensor;
import android.util.FloatMath;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by soundararajan on 7/8/2015.
 */
public class SensorData {
    private float[] acceleration;
    private float[] quaternion;
    private float[] xyz;
    SensorData previous;

    private void init()
    {
        acceleration = new float[3];
        xyz = new float[3];
        quaternion = new float[4];
        accelerationLock = new Object();
        quaternionLock = new Object();
    }

    public SensorData(float[] xyz, float[] quaternion)
    {
        init();
        this.previous = null;

        for (int i = 0; i < 3; i++) {
            this.xyz[i] = xyz[i]; //x
        }

        for (int i = 0; i < 4; i++) {
            this.quaternion[i] = quaternion[i]; //q0, q1,q2,q3
        }
    }

    private void process(float[] data) {
        Assert.assertEquals(7, data.length);
        for (int i = 0; i < 3; i++) {
            this.acceleration[i] = data[i]; //Raw acceleration
            this.xyz[i] = data[i]; //currently no processing;
        }

        for (int i = 0, k = 3; i < 4; i++, k++) {
            this.quaternion[i] = data[k]; //q0, q1,q2,q3
        }
    }

    public SensorData(short[] content, SensorData previous) {
        init();
        this.previous = previous;
        /*  Remember, 2 bytes each
            ------------------------------------------------------------------
           | ax.2 | ay.2 | az.2 | q0.2 | q1.2 | q2.2 | q3.2 | seqNo.1 | n/a |
           ------------------------------------------------------------------ */
        //Android (a x=East, y=North, z=Up or ENU standard. Currently
        //not applicable
        final float[] curr_vector = new float[content.length];
        final float acc_lsb = 0.00012207f;
        curr_vector[0] = (content[0] * acc_lsb);
        curr_vector[1] = (content[1] * acc_lsb);
        curr_vector[2] = (content[2] * acc_lsb);
        final float quaternionTimeScale = 30000f;
        for (int i = 3; i < 7; i++)
            curr_vector[i] = content[i] / quaternionTimeScale;

        float normalize = 0.0f;
        for (int i = 3; i < 7; i++)
            normalize = normalize + (curr_vector[i] * curr_vector[i]);
        normalize = FloatMath.sqrt(normalize);
        for (int i = 3; i < 7; i++)
            curr_vector[i] /= normalize;

        process(curr_vector);
    }

    private Object quaternionLock, accelerationLock;

    public float[] getAcceleration() {
        synchronized (accelerationLock) {
            return acceleration;
        }
    }

    public void set(float value, int index) {
        if (index < 3) //0,1,2 are for x,y,z
            acceleration[index] = value;
        else //3,4,5,6 are for q0,q1,q2,q3
        {
            quaternion[index - 3] = value;
        }
    }

    public float getZ() {
        return acceleration[2];
    }

    public float getY() {
        return acceleration[1];
    }

    public float getX() {
        return acceleration[0];
    }

    public float getQ0() {
        return quaternion[0];
    }

    public float getQ1() {
        return quaternion[1];
    }

    public float getQ2() {
        return quaternion[2];
    }

    public float getQ3() {
        return quaternion[3];
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 3; i++)
            builder.append(String.valueOf(xyz[i])).append(",");

        for (int i = 0; i < 4; i++)
            builder.append(String.valueOf(quaternion[i])).append(",");
        return builder.toString();
    }

    public float[] asVector() {
        float[] f = new float[7];
        int i = 0;
        for (float v : xyz)
            f[i++] = v;
        for (float q : quaternion)
            f[i++] = q;
        return f;
    }

    public List<Float> asList() {
        ArrayList<Float> f = new ArrayList<Float>();
        int i = 0;
        for (float v : xyz)
            f.add(v);
        for (float q : quaternion)
            f.add(q);
        return f;
    }

//    final float NS2S = 1.0f / 1000000000.0f;
//    private void accelerationToXYZ() {
//        SensorData previous;
//        try {
//            float v[], p[];
//            v = new float[3];
//            p = new float[3];
//            //for us dt is constant, assuming we get 25 packets per second.
//            float dt = ((1 * 1000) / 25) * NS2S;
//
//                float[] c_acc = this.getAcceleration();
//                float[] p_acc = previous.getAcceleration();
//                for (int k = 0; k < 3; k++) {
//                    v[k] += (c_acc[k] + p_acc[k]) / 2 * dt;
//                    p[k] += v[k] * dt;
//                    current.set(p[k], k);
//                }
//                previous = current;
//            }
//        } catch (Exception ex) {
//            Log.e("async", ex.toString());
//            ex.printStackTrace();
//        }
//        return sensorData;
//    }
}
