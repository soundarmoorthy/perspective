/*
Copyright (c) 2013, 2014, Freescale Semiconductor, Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
 * Neither the name of Freescale Semiconductor, Inc. nor the
   names of its contributors may be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL FREESCALE SEMICONDUCTOR, INC. BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.freescale.sensors.sfusion;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.freescale.sensors.sfusion.FlicqActivity.Algorithm;


/**
 * Wrapper for control of local (on Android device) sensors.
 * This class extends the more generic SensorsWrapper class, which is also used as a base class
 * for the remote FlicqDevice.
 *
 * @author Michael Stanley
 */
class LocalSensors extends SensorsWrapper {
    private SensorManager localSensorManager;
    private Sensor localAcc, localMag, localGyro, localRotationVectorSensor;
    private boolean sensorsEnabled = false;
    float[] workingQuaternion = null;

    public LocalSensors(FlicqActivity activity) {
        super(activity);
        this.activity = activity;
        workingQuaternion = new float[4];
        acc.setTimeScale(1e-9f);
        mag.setTimeScale(1e-9f);
        gyro.setTimeScale(1e-9f);
        quaternion.setTimeScale(1e-9f);
        acc.setName("Accelerometer");
        mag.setName("Magnetometer");
        gyro.setName("Gyroscope");
        localSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
    }


    public synchronized void updateRotation(long time, float[] values) {
        SensorManager.getQuaternionFromVector(workingQuaternion, values);
        this.quaternion.set(time, workingQuaternion);
    }

    void enable() {
        sensorsEnabled = true;
    }

    void disable() {
        sensorsEnabled = false;
    }

    void computeQuaternion(FlicqQuaternion result, Algorithm algorithm) {
        assert (hasLocalGyro());
        result.set(super.quaternion());
        result.reverse();
    }

    public boolean valsHaveBeenSet() {
        return (acc.hasBeenSet() && mag.hasBeenSet() && gyro.hasBeenSet());
    }

    public boolean hasLocalGyro() {
        if (localGyro == null) {
            localGyro = localSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
        return (localGyro != null);
    }

    public synchronized void run() {
        run(true);
    }

    public void setSensorRate(Sensor sensor, int rate) {
        localSensorManager.unregisterListener(localSensorListener, sensor);
        if (rate < 4) {
            localSensorManager.registerListener(localSensorListener, sensor, rate);
        }
    }



    public synchronized void run(boolean register_listeners) {
        boolean writeRates = !register_listeners;
        // Now let's access local sensors
        if (sensorsEnabled == false) {
            localAcc = localSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            localGyro = localSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            localMag = localSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            localRotationVectorSensor = localSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            if (register_listeners) {
                int rate = SensorManager.SENSOR_DELAY_UI;
                setSensorRate(localAcc, rate);
                setSensorRate(localMag, rate);
                setSensorRate(localGyro,rate);
                setSensorRate(localRotationVectorSensor, rate);
            }
        }
        if (register_listeners) {
            sensorsEnabled = true;
            clear();
        }
    }

    public void stop() {
        if (sensorsEnabled == true) {
            localSensorManager.unregisterListener(localSensorListener);
        }
        sensorsEnabled = false;
    }

    private final SensorEventListener localSensorListener = new SensorEventListener() {
        @Override
        synchronized public void onSensorChanged(SensorEvent event) {
            int sensorType = event.sensor.getType();
            switch (sensorType) {
                case Sensor.TYPE_ACCELEROMETER:
                    acc.update(event.timestamp,
                            event.values[0] / (float) g,
                            event.values[1] / (float) g,
                            event.values[2] / (float) g);
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    gyro.update(event.timestamp,
                            event.values[0],
                            event.values[1],
                            event.values[2]);
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mag.update(event.timestamp,
                            event.values[0],
                            event.values[1],
                            event.values[2]);
                    break;
                case Sensor.TYPE_ROTATION_VECTOR:
                    updateRotation(event.timestamp, event.values);
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Not needed for this application
        }
    };
}
