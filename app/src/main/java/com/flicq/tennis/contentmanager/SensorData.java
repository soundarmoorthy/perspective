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
    final SensorData previous;

    private void init()
    {
        acceleration = new float[3];
        quaternion = new float[4];
    }

    //This constructor is used for Android local sensor data
    public SensorData(float[] acceleration, float[] quaternion)
    {
        init();
        this.previous = null;


        for (int i = 0; i < 3; i++) {
            this.acceleration[i] = acceleration[i];
        }

        for (int i = 0; i < 4; i++) {
            this.quaternion[i] = quaternion[i]; //q0, q1,q2,q3
        }
    }

    private static final float acc_lsb = 0.00012207f;
    private static final float quaternionTimeScale = 30000f;
    public SensorData(short[] content, SensorData previous) {
        init();
        this.previous = previous;
        Assert.assertEquals(content.length, 7);
        /*  Remember, 2 bytes each
            ------------------------------------------------------------------
           | ax.2 | ay.2 | az.2 | q0.2 | q1.2 | q2.2 | q3.2 | seqNo.1 | n/a |
           ------------------------------------------------------------------ */
        //Android (a x=East, y=North, z=Up or ENU standard. Currently not applicable
        acceleration[0] = (content[0] * acc_lsb);
        acceleration[1] = (content[1] * acc_lsb);
        acceleration[2] = (content[2] * acc_lsb);

        float normalize = 0.0f;
        for (int i = 3, k=0; i < 7; i++, k++) {
            quaternion[k] = content[i] / quaternionTimeScale;
            normalize = normalize + (quaternion[k] * quaternion[k]);
        }
        normalize = FloatMath.sqrt(normalize);

        for (int k = 0; k < 4; k++)
            quaternion[k] /= normalize;
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
            builder.append(String.valueOf(acceleration[i])).append(",");

        for (int i = 0; i < 4; i++)
            builder.append(String.valueOf(quaternion[i])).append(",");
        return builder.toString();
    }

    public float[] asVector() {
        float[] f = new float[7];
        int i = 0;
        for (float v : acceleration)
            f[i++] = v;
        for (float q : quaternion)
            f[i++] = q;
        return f;
    }

    public List<Float> asList() {
        ArrayList<Float> f = new ArrayList<Float>();
        int i = 0;
        for (float v : acceleration)
            f.add(v);
        for (float q : quaternion)
            f.add(q);
        return f;
    }
}
