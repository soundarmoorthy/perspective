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

import android.hardware.SensorManager;
import android.os.Build;
import android.util.DisplayMetrics;

/**
* Base class to define basic data which needs to be stored from sensors.  This includes x/y/z
* from each of accelometer, magnetometer and gyro, as well as quaternion.  All these are "timed"
* quantities - meaning each has a time stamp.  This class is used as the base class for both 
* local (to the Android device) and remote (via Bluetooth) sensors.
* @author Michael Stanley
*/
class SensorsWrapper  {
	public enum SensorType {  ACCEL, MAG, GYRO, QUAT }

    protected TimedTriad acc = null;
    protected TimedTriad mag = null;
    protected TimedTriad gyro = null;
    protected TimedQuaternion quaternion = null; 
	protected final float degreesPerRadian = (float) (180.0f/3.14159f);
	protected final float radiansPerDegree = (float) (3.14159f/180.0f);
	protected final double g = SensorManager.GRAVITY_EARTH;
	String LOG_TAG = null;
	A_FSL_Sensor_Demo demo;  // a back pointer to the master application
 
	public SensorsWrapper(A_FSL_Sensor_Demo demo) {
 		this.demo=demo;
 		acc = new TimedTriad();
 		mag = new TimedTriad();
 		gyro = new TimedTriad();
 		quaternion = new TimedQuaternion();
 		clear();
        LOG_TAG = demo.getString(R.string.log_tag);
    }
	public void setNoGyro() {
		gyro = null;
	}
	public boolean statsReady() {
		return(acc.statsReady() && mag.statsReady() && ((!gyro.enabled) || gyro.statsReady()) && quaternion.statsReady());		
	}
	
	synchronized public void enableLowPassFilters(boolean state) {
		acc.enableLpf(state);
		mag.enableLpf(state);
		gyro.enableLpf(state);		
	}
	synchronized public void setFilterCoef(float fc) {
		// fc should be between 0 and 1 (0=no filtering, 1=no change in output)
		acc.setFilterCoef(fc);
		mag.setFilterCoef(fc);
		gyro.setFilterCoef(fc);
	}
	public void setQuaternion(long time, float [] q) {
		assert(q.length==4);
		this.quaternion.set(time, q);
	}
	public TimedTriad mag() {
		return(mag);
	}
	public TimedTriad acc() {
		return(acc);
	}
	public TimedTriad gyro () {
		return(gyro);
	}
	public DemoQuaternion quaternion() {
		return(quaternion);
	}
	public synchronized void toDegreesRotation(RotationVector rv) {
		this.quaternion().toRotationVector(rv, MyUtils.AngleUnits.DEGREES);
	}
	public boolean valsHaveBeenSet() {
		return(acc.hasBeenSet() && mag.hasBeenSet() && gyro.hasBeenSet());
	}
	synchronized public void clear() {
		acc.zero();
		mag.zero();
		gyro.zero();
	}
	synchronized public void outdate() {
		acc.outdate();
		mag.outdate();
		gyro.outdate();
	}
	public synchronized void enableLogging(boolean en, int maxSamples, boolean oneShot, boolean resetStats) {
	}

	public void setSensorDescriptions(String s) {
		acc.setDescription(s);
		mag.setDescription(s);
		gyro.setDescription(s);
		quaternion.setDescription(s);
	}

	public void setSensorRateBySensorType(SensorType type, int rate) {
		// nothing is the default.  Will be Overridden in derived classes
	}
}    
