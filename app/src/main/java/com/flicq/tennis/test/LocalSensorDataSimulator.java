package com.flicq.tennis.test;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.flicq.tennis.framework.IActivityAdapter;
import com.flicq.tennis.opengl.ShotRenderer;

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
    ShotRenderer renderer;
    public LocalSensorDataSimulator(IActivityAdapter adapter, ShotRenderer renderer)
    {
        this.renderer = renderer;
        this.activityAdapter = adapter;
        handler = new LocalSensorDataHandler(adapter);
        Context context  = activityAdapter.GetApplicationContext();
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    public void Start()
    {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_NORMAL);
        activityAdapter.writeToUi("Simulator starting ...",false);
        activityAdapter.writeToUi("Setup acceleromenter and rotation vector from local android",false);
    }

    public synchronized float[] getSensorData()
    {
        return handler.getData();
    }

    @Override
    public synchronized void onSensorChanged(SensorEvent sensorEvent) {
        int type = sensorEvent.sensor.getType();
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER:
                handler.setAcc(sensorEvent.values);
                break;

            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getQuaternionFromVector(quaternion, sensorEvent.values);
                handler.setQuat(quaternion);
                count ++;
                break;
        }

        if(count++ >= 500) {
            renderer.Render(handler.getData());
            handler.Reset();
            count =0;
        }
    }
    int count = 0;

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
