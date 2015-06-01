package com.flicq.tennis.test;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.flicq.tennis.framework.IActivityAdapter;

/**
 * Created by soundararajan on 5/31/2015.
 */
public class SimulateBLEData implements SensorEventListener {
    IActivityAdapter activityAdapter;
    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    public SimulateBLEData(IActivityAdapter adapter)
    {
        this.activityAdapter = adapter;
        Context context  = activityAdapter.GetApplicationContext();
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void Start()
    {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void Stop()
    {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
