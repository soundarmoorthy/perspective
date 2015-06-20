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
public class LocalSensorDataSimulator implements SensorEventListener {
    IActivityAdapter activityAdapter;
    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final Sensor rotationVector;
    float[] quaternion = new float[4];
    LocalSensorDataHandler handler;
    public LocalSensorDataSimulator(IActivityAdapter adapter)
    {
        this.activityAdapter = adapter;
        handler = new LocalSensorDataHandler(adapter);
        Context context  = activityAdapter.GetApplicationContext();
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    public void Start()
    {
        handler.Reset();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_NORMAL);
        activityAdapter.writeToUi("Simulator starting ...",false);
        activityAdapter.writeToUi("Setup acceleromenter and rotation vector from local android",false);
    }

    public float[] getSensorData()
    {
        return handler.getData();
    }

    public void Stop()
    {
        sensorManager.unregisterListener(this, rotationVector);
        sensorManager.unregisterListener(this, accelerometer);
        activityAdapter.writeToUi("Stopped simulator",false);
    }

    @Override
    public synchronized void onSensorChanged(SensorEvent sensorEvent) {
        int type = sensorEvent.sensor.getType();
        switch (type)
        {
            case Sensor.TYPE_ACCELEROMETER:
                handler.setAcc(sensorEvent.values);
                break;

            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getQuaternionFromVector(quaternion, sensorEvent.values);
                handler.setQuat(quaternion);
                activityAdapter.writeToUi("Acquiring data : " + String.valueOf(count), false);
                if(count++ == 50)
                    Stop();
                break;
        }
    }
    private int count;

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
