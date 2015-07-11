package com.flicq.tennis.test;

import android.hardware.SensorManager;

import com.flicq.tennis.contentmanager.SensorData;
import com.flicq.tennis.framework.IActivityAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by soundararajan on 6/20/2015.
 */
class LocalSensorDataHandler {

    private boolean busy;
    private final List<SensorData> sensorData;
    float[] acc,quat;
    private final IActivityAdapter adapter;
    private final float gravity = (float) SensorManager.GRAVITY_EARTH;
    public LocalSensorDataHandler(IActivityAdapter adapter)
    {
        this.adapter = adapter;
        sensorData = new ArrayList<SensorData>();
        acc = new float[3];
        quat = new float[4];
    }

    public synchronized void setAcc(float[] values) {
        if (busy)
            return;
        for (int i = 0; i < 3; i++)
            acc[i] = values[i] / gravity;

    }

    public synchronized void setQuat(float[] values) {
        synchronized (sensorData) {
            for (int i = 0; i < 4; i++)
                quat[i] = values[i];
            busy = true;
            sensorData.add(new SensorData(acc,quat));
            busy = false;
        }
    }

    public List<SensorData> getData() {
        synchronized (sensorData) {
            return sensorData;
        }
    }

    public void Reset() {
        this.busy = false;
        this.sensorData.clear();
    }
}
