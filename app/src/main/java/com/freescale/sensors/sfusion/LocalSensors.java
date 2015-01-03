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
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import com.freescale.sensors.sfusion.A_FSL_Sensor_Demo.Algorithm;
import com.freescale.sensors.sfusion.A_FSL_Sensor_Demo.DataSource;


/**
* Wrapper for control of local (on Android device) sensors.
* This class extends the more generic SensorsWrapper class, which is also used as a base class
* for the remote IMU.
* @author Michael Stanley
*/
class LocalSensors extends SensorsWrapper {
	private SensorManager localSensorManager;
    private Sensor localAcc, localMag, localGyro, localPressure, localRotationVectorSensor;
    private Sensor localLight, localProximity, localTemperature;
    private Sensor localLinearAccelerometer, localOrientation;
	private boolean sensorsEnabled=false;
	float[] workingQuaternion = null;

	public LocalSensors(A_FSL_Sensor_Demo demo) {
		super(demo);
 		this.demo=demo;
		workingQuaternion = new float[4];
 		acc.setTimeScale(1e-9f);
 		mag.setTimeScale(1e-9f);
 		gyro.setTimeScale(1e-9f);
 		quaternion.setTimeScale(1e-9f);
 		acc.setName("Accelerometer");
 		mag.setName("Magnetometer");
 		gyro.setName("Gyroscope");
  		localSensorManager = (SensorManager) demo.getSystemService(Context.SENSOR_SERVICE);
 		setDescriptions();
    }

	public synchronized void updateRotation(long time, float[] values) {
		SensorManager.getQuaternionFromVector(workingQuaternion, values);
		setQuaternion(time, workingQuaternion);
	}
	void enable() {
		sensorsEnabled=true;
	}
	void disable() {
		sensorsEnabled=false;
	}

	void computeQuaternion(DemoQuaternion result, Algorithm algorithm) {
		switch (algorithm) {
		case ACC_ONLY:
			float[] a = acc().array();
			float[] axis = new float[3];
			float[] baseline_axis = {0f, 0f, 1.0f}; 
			float angle = MyUtils.axisAngle(axis, baseline_axis, a);
			result.computeFromAxisAngle(angle, axis);			
			break;
		case ACC_MAG:
			float[] rotationMatrix = new float[9];
			SensorManager.getRotationMatrix(rotationMatrix, null, acc().array(), mag().array());
			result.computeFromRM(rotationMatrix);
			break;
		case NINE_AXIS:
			assert(hasLocalGyro());
			result.set(super.quaternion());
			result.reverse();
			break;
		case NONE:
		default:
			Log.i(A_FSL_Sensor_Demo.LOG_TAG, "Quaternion from UNKNOWN\n");
		}
	}

	public boolean valsHaveBeenSet() {
		return(acc.hasBeenSet() && mag.hasBeenSet() && gyro.hasBeenSet());
	}
	public boolean hasLocalGyro() {
		if (localGyro==null) {
			localGyro = localSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		}
		return (localGyro!=null);
	}
	
	private void checkGyroAvailability(Sensor local_gyro) {
		String msg;
		if ((local_gyro==null) && (demo.dataSource==DataSource.LOCAL)) {
				String title = "Warning!";
				msg = "This Android device does not include a gyroscope.";
				MyUtils.popupAlert(title, msg);	
				demo.setSts("Local gyro not present");
		}
	}
	public synchronized void run() {
		run(true);
	}
	private int rateLookup(String lbl, int idx, boolean writeRates) {
		int retVal = SensorManager.SENSOR_DELAY_NORMAL;
		String rateString = new String();
        switch (idx) { // This order needs to match defined in strings.xml
        case 0:
        	retVal = SensorManager.SENSOR_DELAY_NORMAL;
        	rateString = " sampling at SENSOR_DELAY_NORMAL\n";
        	break;
        case 1:
        	retVal = SensorManager.SENSOR_DELAY_UI;
        	rateString = " sampling at SENSOR_DELAY_UI\n";
            break;
        case 2:
        	retVal = SensorManager.SENSOR_DELAY_GAME;
        	rateString = " sampling at SENSOR_DELAY_GAME\n";
            break;
        case 3:
         	retVal = SensorManager.SENSOR_DELAY_FASTEST;
        	rateString = " sampling at SENSOR_DELAY_FASTEST\n";
        	break;
        case 4:
         	retVal = -1;
        	rateString = " sampling OFF\n";
         }
        if (writeRates) A_FSL_Sensor_Demo.write(true, "Android " + lbl + rateString);
		return(retVal);
	}
	public void setSensorRate(Sensor sensor, int rate) {
		localSensorManager.unregisterListener(localSensorListener, sensor);
		if (rate<4) {
			localSensorManager.registerListener(localSensorListener, sensor, rate);
		}
	}
    @Override
	public void setSensorRateBySensorType(SensorType type, int rate) {
		switch (type) {
		case ACCEL:
			setSensorRate(localAcc, rate);
			break;
		case MAG:
			setSensorRate(localMag, rate);
			break;
		case GYRO:
			setSensorRate(localGyro, rate);
			break;
		case QUAT:
			setSensorRate(localRotationVectorSensor, rate);
			break;
		}
	}

	public synchronized void run(boolean register_listeners) {
		checkGyroAvailability(localGyro);  // force 6-axis fusion if no gyro.  Also sets sts field on button row
        int accRateIndex = demo.myPrefs.getInt("acc_sample_rate", SensorManager.SENSOR_DELAY_NORMAL);
        int magRateIndex = demo.myPrefs.getInt("mag_sample_rate", SensorManager.SENSOR_DELAY_NORMAL);
        int gyroRateIndex = demo.myPrefs.getInt("gyro_sample_rate", SensorManager.SENSOR_DELAY_NORMAL);
        int rotationVectorRateIndex = demo.myPrefs.getInt("rotation_vector_sample_rate", SensorManager.SENSOR_DELAY_NORMAL);
        boolean writeRates = ! register_listeners;
        int accSampleRate = rateLookup("accelerometer", accRateIndex, writeRates);
        int magSampleRate = rateLookup("magnetometer", magRateIndex, writeRates);
        int gyroSampleRate = rateLookup("gyro", gyroRateIndex, writeRates);
        int rotationVectorSampleRate = rateLookup("rotation vector sensor", rotationVectorRateIndex, writeRates);

		// Now let's access local sensors
		if (sensorsEnabled == false)  {
			localAcc = localSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			localGyro = localSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
			localMag = localSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			localPressure = localSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
			localLight = localSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
			localProximity = localSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
			localTemperature = localSensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
			localRotationVectorSensor = localSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
			localLinearAccelerometer = localSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
			localOrientation = localSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
			checkGyroAvailability(localGyro);  
			if (register_listeners) {
				setSensorRate(localAcc, accSampleRate);
				setSensorRate(localMag, magSampleRate);
				setSensorRate(localGyro, gyroSampleRate);
				setSensorRate(localRotationVectorSensor, rotationVectorSampleRate);
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
		demo.setSts("");  // clear previous status
	}
	synchronized String getRecord() {
		String str1, str2, str3;
		// format statement converts acceleration from m/s^2 to g's
		str1 = String.format("%10.4f, %5.3f, %5.3f, %5.3f, ", acc.time(),  acc.x(),  acc.y(),  acc.z());
		str2 = String.format("%10.4f, %5.3f, %5.3f, %5.3f, ", mag.time(),  mag.x(),  mag.y(),  mag.z());
		str3 = String.format("%10.4f, %5.2f, %5.2f, %5.2f\n", gyro.time(), gyro.x(), gyro.y(), gyro.z());
		return(str1 + str2 + str3);
	}

	private String getSensorDescription(Sensor sensor, String type, String units1, String units2, float s) {
		String str = new String("");
        if (sensor != null) {
            str += "Local " + type + " vendor = " + sensor.getVendor() + ".\n";
            str +=  "Local " + type + " name = " + sensor.getName() + ".\n";
            str +=  "Local " + type + " resolution = " + sensor.getResolution() + " " + units1 + "/LSB.\n";
            if (units2!=null) {
            	str += "Local " + type + " resolution = " + sensor.getResolution()/s + " " + units2 + "/LSB.\n";            	
            }
            str += "Local " + type + " range = +/-" + sensor.getMaximumRange() + " " + units1 + "\n";
            if (units2!=null) {
            	str += "Local " + type + " range = +/-" + sensor.getMaximumRange()/s + " " + units2 + "\n";
            }
            str += "Local " + type + " current draw = " + sensor.getPower() + " mA.";
        } else {
        	str += "This device does not include a local " + type + " sensor.";
        }       
        return(str);
	}
	private void setDescriptions() {
		localAcc = localSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		localGyro = localSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		localMag = localSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		localRotationVectorSensor = localSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		acc.setDescription(getSensorDescription(localAcc, "accelerometer", "m/s^2", "g", (float) g));
		gyro.setDescription(getSensorDescription(localGyro, "gyro", "radians/sec", "dps", (float) radiansPerDegree));
		mag.setDescription(getSensorDescription(localMag, "magnetometer", "micro-Teslas", null, 1));
		quaternion.setDescription(getSensorDescription(localRotationVectorSensor, "rotation vector sensor", "degrees", null, 1));
	}
	private void dump_sensor(Sensor sensor, String type, String units1, String units2, float s) {
		A_FSL_Sensor_Demo.write(true, getSensorDescription(sensor, type,units1, units2, s));
		A_FSL_Sensor_Demo.write(true, "\n\n");
	}
    public void dump () {
    	A_FSL_Sensor_Demo.write(true, demo.prompt());
    	run(false);
    	DisplayMetrics metrics = new DisplayMetrics();
    	demo.getWindowManager().getDefaultDisplay().getMetrics(metrics);
    	A_FSL_Sensor_Demo.write(true, "\nBoard = " + Build.BOARD + ".\n");
    	A_FSL_Sensor_Demo.write(true, "CPU_ABI = " + Build.CPU_ABI + ".\n");
    	A_FSL_Sensor_Demo.write(true, "CPU_ABI2 = " + Build.CPU_ABI2 + ".\n");
    	A_FSL_Sensor_Demo.write(true, "DEVICE = " + Build.DEVICE + ".\n");
    	A_FSL_Sensor_Demo.write(true, "DISPLAY = " + Build.DISPLAY + ".\n");
    	A_FSL_Sensor_Demo.write(true, "FINGERPRINT = " + Build.FINGERPRINT + ".\n");
    	A_FSL_Sensor_Demo.write(true, "HARDWARE = " + Build.HARDWARE + ".\n");
    	A_FSL_Sensor_Demo.write(true, "MANUFACTURER = " + Build.MANUFACTURER + ".\n");
    	A_FSL_Sensor_Demo.write(true, "MODEL = " + Build.MODEL + ".\n");
    	A_FSL_Sensor_Demo.write(true, "PRODUCT = " + Build.PRODUCT+ ".\n");
    	A_FSL_Sensor_Demo.write(true, "ANDROID_VERSION = " + Build.VERSION.RELEASE + ".\n");
    	A_FSL_Sensor_Demo.write(true, "CODENAME = " + Build.VERSION.CODENAME + ".\n\n");
    	A_FSL_Sensor_Demo.write(true, "Screen_Height = " + metrics.heightPixels + " pixels\n");
    	A_FSL_Sensor_Demo.write(true, "Screen Width = " + metrics.widthPixels + " pixels\n");
    	A_FSL_Sensor_Demo.write(true, "Screen xdpi = " + metrics.xdpi + " pixels/inch in X dimension\n");
    	A_FSL_Sensor_Demo.write(true, "Screen ydpi = " + metrics.xdpi + " pixels/inch in Y dimension\n");
    	A_FSL_Sensor_Demo.write(true, "Screen scaled density = " + metrics.scaledDensity + "\n");
    	A_FSL_Sensor_Demo.write(true, "Computed Screen X dimension = " + metrics.widthPixels/metrics.xdpi + " inches\n");
    	A_FSL_Sensor_Demo.write(true, "Computed Screen Y dimension = " + metrics.heightPixels/metrics.ydpi + " inches\n\n");
    	dump_sensor(localAcc, "accelerometer", "m/s^2", "g", (float) g);
    	dump_sensor(localGyro, "gyro", "radians/sec", "dps", (float) radiansPerDegree);
    	dump_sensor(localMag, "magnetometer", "micro-Teslas", null, 1);
    	dump_sensor(localPressure, "pressure", "hPa", "millibar", 1);
    	dump_sensor(localLight, "light", "lux", null, 1);
    	dump_sensor(localProximity, "proximity", "cm", null, 1);
    	dump_sensor(localTemperature, "temperature", "C", null, 1);
    	dump_sensor(localRotationVectorSensor, "rotation vector sensor", "degrees", null, 1);
    	dump_sensor(localOrientation, "orientation sensor", "degrees", null, 1);
    	dump_sensor(localLinearAccelerometer, "linear acceleration sensor", "m/s^2", "g", (float) g);
    }
    private final SensorEventListener localSensorListener = new SensorEventListener() {
    	@Override
    	synchronized public void onSensorChanged(SensorEvent event) {
     		int sensorType = event.sensor.getType();
    		switch (sensorType) {
    		case Sensor.TYPE_ACCELEROMETER:
    			acc.update(	event.timestamp, 
    					event.values[0]/(float) g, 
    					event.values[1]/(float) g, 
    					event.values[2]/(float) g);
    			if (demo.legacyDumpEnabled) dump_acc();
     			break;
    		case Sensor.TYPE_GYROSCOPE:
    			gyro.update(	event.timestamp, 
    					event.values[0], 
    					event.values[1], 
    					event.values[2]);
    			if (demo.legacyDumpEnabled) dump_gyro();
    			break;
    		case Sensor.TYPE_MAGNETIC_FIELD:
    			mag.update(	event.timestamp, 
    					event.values[0], 
    					event.values[1], 
    					event.values[2]);
    			if (demo.legacyDumpEnabled) dump_mag();
    			break;
    		case Sensor.TYPE_ROTATION_VECTOR:
    			updateRotation(event.timestamp, event.values);
    			if (demo.legacyDumpEnabled) dump_quaternion();
    			break;
    		}
    	}
      	@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// Not needed for this application
		}
    };
    @Override
	public String getSensorDescription(SensorType type) {
		String str = null;
		switch (type) {
		case ACCEL:
			str = getSensorDescription(localAcc, "accelerometer", "m/s^2", "g", (float) g);
			break;
		case MAG:
			str = getSensorDescription(localMag, "magnetometer", "micro-Teslas", null, 1);
			break;
		case GYRO:
			str = getSensorDescription(localGyro, "gyro", "radians/sec", "dps", (float) radiansPerDegree);
			break;
		case QUAT:
			str = getSensorDescription(localRotationVectorSensor, "rotation vector sensor", "degrees", null, 1);
			break;
		}
		return str;
	}

}    
