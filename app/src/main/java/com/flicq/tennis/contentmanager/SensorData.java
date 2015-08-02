package com.flicq.tennis.contentmanager;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by soundararajan on 7/8/2015.
 */

public class SensorData {
    private float[] acceleration;
    private float[] quaternion;

    private void init() {
        acceleration = new float[3];
        quaternion = new float[4];

    }

    ISensorDataBuilder sensorDataBuilder;

    public SensorData(ISensorDataBuilder sensorDataBuilder) {
        this.sensorDataBuilder = sensorDataBuilder;
        init();

        this.acceleration = this.sensorDataBuilder.getAcceleration();
        this.quaternion = this.sensorDataBuilder.getQuaternion();
    }

    public void set(float value, int index) {
        if (index < 3) //0,1,2 are for x,y,z
        {
            acceleration[index] = value;
        } else //3,4,5,6 are for q0,q1,q2,q3
        {
            quaternion[index - 3] = value;
        }
    }

    public float get(int index) {
        if (index < 3) //0,1,2 are for x,y,z
        {
            return acceleration[index];
        } else //3,4,5,6 are for q0,q1,q2,q3
        {
            return quaternion[index - 3];
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
        builder.append(sensorDataBuilder.dump());

        for (float a : acceleration)
            builder.append(String.valueOf(a)).append(",");

        for (float q : quaternion)
            builder.append(String.valueOf(q)).append(",");

        return builder.toString();
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

    public float[] asVector() {
        float[] f = new float[7];
        int index=0;
        for(int i=0;i<3;i++)
            f[index++] = acceleration[i];

        for(int i=0;i<4;i++)
            f[index++] = quaternion[i];

        return f;
    }
}
