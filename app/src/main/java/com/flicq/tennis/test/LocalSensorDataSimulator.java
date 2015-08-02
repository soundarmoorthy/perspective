package com.flicq.tennis.test;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.flicq.tennis.contentmanager.SensorData;
import com.flicq.tennis.framework.IActivityAdapter;
import com.flicq.tennis.opengl.ShotRenderer;

import java.util.List;

/**
 * Created by soundararajan on 5/31/2015.
 */
public class LocalSensorDataSimulator implements SensorEventListener {
    private final IActivityAdapter activityAdapter;
    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final Sensor rotationVector;
    private final float[] quaternion = new float[4];
    private final LocalSensorDataHandler handler;
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
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_NORMAL);
        activityAdapter.writeToUi("Simulator starting ...");
        activityAdapter.writeToUi("Setup acceleromenter and rotation vector from local android");
    }

    public synchronized List<SensorData> getSensorData()
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
            //renderer.Render(handler.getData());
            handler.Reset();
            count =0;
        }
    }
    private int count = 0;

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
