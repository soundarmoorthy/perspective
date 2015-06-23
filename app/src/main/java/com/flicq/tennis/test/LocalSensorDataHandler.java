package com.flicq.tennis.test;

import android.hardware.SensorManager;

import com.flicq.tennis.framework.IActivityAdapter;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by soundararajan on 6/20/2015.
 */
public class LocalSensorDataHandler {

    private float[] values;
    private boolean busy;

    private ArrayList<float[]> sensorData;
    private float ax,ay,az;
    IActivityAdapter adapter;
    protected final float gravity = (float) SensorManager.GRAVITY_EARTH;
    public LocalSensorDataHandler(IActivityAdapter adapter)
    {
        this.adapter = adapter;
        sensorData = new ArrayList<float[]>();
        values = new float[7];
    }

    public synchronized void setAcc(float[] values)
    {
        if(busy)
            return;
        ax = values[0];
        ay = values[1];
        az = values[2];
    }

    public synchronized void setQuat(float[] values) {
        busy = true;
        this.values[0] = ax / gravity;
        this.values[1] = ay / gravity;
        this.values[2] = az / gravity;
        busy = false;
        this.values[3] = values[0];
        this.values[4] = values[1];
        this.values[5] = values[2];
        this.values[6] = values[3];
        sensorData.add(this.values);
    }

    public float[] getData() {
        int count = 0;
        StringBuilder  builder = new StringBuilder();
        float[] data = new float[sensorData.size() * 7];
        for (int i = 0; i < sensorData.size(); i++) {
            float[] row = sensorData.get(i);
            for (int j = 0; j < 7; j++) {
                data[count++] = row[j];
                builder.append(row[j]);
            }
            adapter.writeToUi(builder.toString(), false);
            builder.delete(0,builder.length());
        }
        return data;
    }

    public void Reset() {
        for(int i=0;i<7;i++)
            this.values[i] = 0.0f;
        this.ax = this.ay = this.az = 0.0f;
        this.busy = false;
        this.sensorData.clear();
    }
}