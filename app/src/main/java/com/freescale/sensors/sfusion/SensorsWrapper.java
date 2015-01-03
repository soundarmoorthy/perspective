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
	public void dump_acc() {
		if (demo.guiState==A_FSL_Sensor_Demo.GuiState.LOGGING) {
			// the check above is a bit redundant.  Added to avoid string processing
			// unless it is absolutely necessary.
			A_FSL_Sensor_Demo.write(false, "A: " + acc.toString() + "\n");
		}
	}
	public void dump_mag() {
		if (demo.guiState==A_FSL_Sensor_Demo.GuiState.LOGGING) {
			// the check above is a bit redundant.  Added to avoid string processing
			// unless it is absolutely necessary.
			A_FSL_Sensor_Demo.write(false, "M: " + mag.toString() + "\n");
		}
	}
	public void dump_gyro() {
		if (demo.guiState==A_FSL_Sensor_Demo.GuiState.LOGGING) {
			// the check above is a bit redundant.  Added to avoid string processing
			// unless it is absolutely necessary.
			A_FSL_Sensor_Demo.write(false, "G: " + gyro.toString() + "\n");
		}
	}
	public void dump_acc_mag() {
		if (demo.guiState==A_FSL_Sensor_Demo.GuiState.LOGGING) {
			// the check above is a bit redundant.  Added to avoid string processing
			// unless it is absolutely necessary.
			A_FSL_Sensor_Demo.write(false, "A: " + acc.toString() + " ");
			A_FSL_Sensor_Demo.write(false, "M: " + mag.toString() + "\n");
		}
	}
	public void dump_mag_gyro() {
		if (demo.guiState==A_FSL_Sensor_Demo.GuiState.LOGGING) {
			// the check above is a bit redundant.  Added to avoid string processing
			// unless it is absolutely necessary.
			A_FSL_Sensor_Demo.write(false, "M: " + mag.toString() + " ");
			A_FSL_Sensor_Demo.write(false, "G: " + gyro.toString() + "\n");
		}
	}
	public void dump_9_axis() {
		if (demo.guiState==A_FSL_Sensor_Demo.GuiState.LOGGING) {
			// the check above is a bit redundant.  Added to avoid string processing
			// unless it is absolutely necessary.
			A_FSL_Sensor_Demo.write(false, "A: " + acc.toString() + " ");
			A_FSL_Sensor_Demo.write(false, "M: " + mag.toString() + " ");
			A_FSL_Sensor_Demo.write(false, "G: " + gyro.toString() + "\n");
		}
	}
	public void dump9AxisCsv() {
		if (demo.guiState==A_FSL_Sensor_Demo.GuiState.LOGGING) {
			// the check above is a bit redundant.  Added to avoid string processing
			// unless it is absolutely necessary.
			A_FSL_Sensor_Demo.write(false, acc.time() + ", " + acc.toCsvString() + ", " + mag.toCsvString() + ", " + gyro.toCsvString() + ", " + quaternion.toCsvString() + "\n");	
		}
	}
	public void dump6AxisCsv() {
		if (demo.guiState==A_FSL_Sensor_Demo.GuiState.LOGGING) {
			// the check above is a bit redundant.  Added to avoid string processing
			// unless it is absolutely necessary.
			A_FSL_Sensor_Demo.write(false, acc.time() + ", " + acc.toCsvString() + ", " + mag.toCsvString() + ", " + quaternion.toCsvString() + "\n");	
		}
	}
	public void dump_quaternion() {
		if (demo.guiState==A_FSL_Sensor_Demo.GuiState.LOGGING) {
			// the check above is a bit redundant.  Added to avoid string processing
			// unless it is absolutely necessary.
			A_FSL_Sensor_Demo.write(false, "Q: " + quaternion.toString() + "\n");
		}
	}
	public boolean statsReady() {
		return(acc.statsReady() && mag.statsReady() && ((!gyro.enabled) || gyro.statsReady()) && quaternion.statsReady());		
	}
	
	void dumpSensorStats(HtmlGenerator html, TimedTriad sen, String units) {
		if (sen.enabled) {
			String headers[] = {"Quantity", "Value", "Minimum", "Mean", "Maximum", "StdDev", "Units", "/rtHz"};
			html.h2(sen.getName());
			html.para();
			html.para("Sensor Description:");
			html.pre(sen.getDescription());
			html.write(String.format("Sample quantity used for stats gathering: %d (-1 for rate)", sen.maxSamples));
			html.para();
			html.start_table();
			html.thead(headers);
			html.start_tbody();
			float rtHz = (float) Math.sqrt(sen.rateStats.mean);
			html.row("X", sen.x(), sen.xStats.min, sen.xStats.mean, sen.xStats.max, sen.xStats.stddev, units, sen.xStats.stddev/rtHz);
			html.row("Y", sen.y(), sen.yStats.min, sen.yStats.mean, sen.yStats.max, sen.yStats.stddev, units, sen.yStats.stddev/rtHz);
			html.row("Z", sen.z(), sen.zStats.min, sen.zStats.mean, sen.zStats.max, sen.zStats.stddev, units, sen.zStats.stddev/rtHz);
			html.row("Magnitude", sen.magnitude(), sen.magStats.min, sen.magStats.mean, sen.magStats.max, sen.magStats.stddev, units, sen.magStats.stddev/rtHz);
			html.row("Rates", sen.rate, sen.rateStats.min, sen.rateStats.mean, sen.rateStats.max, sen.rateStats.stddev, "samples/sec", -1.0f);
			html.end_tbody(); html.end_table(); 		
		}
	}
	void dumpQuaternionStats(HtmlGenerator html, TimedQuaternion sen) {
		String headers[] = {"Quantity", "Value", "Minimum", "Mean", "Maximum", "StdDev", "Units", "/rtHz"};
		html.h2("Quaternion");
		html.para();
		html.write(String.format("Sample quantity used for stats gathering: %d", sen.maxSamples));
		html.para("Sensor Description:");
		html.pre(sen.getDescription());
		html.para();
		html.start_table();
		html.thead(headers);
		html.start_tbody();
		float rtHz = (float) Math.sqrt(sen.rateStats.mean);
		html.row("q0", sen.q0, sen.q0Stats.min, sen.q0Stats.mean, sen.q0Stats.max, sen.q0Stats.stddev, "cos(theta/2)", sen.q0Stats.stddev/rtHz);
		html.row("X", sen.q1, sen.q1Stats.min, sen.q1Stats.mean, sen.q1Stats.max, sen.q1Stats.stddev, "X sin(theta/2)", sen.q1Stats.stddev/rtHz);
		html.row("Y", sen.q2, sen.q2Stats.min, sen.q2Stats.mean, sen.q2Stats.max, sen.q2Stats.stddev, "Y sin(theta/2)", sen.q2Stats.stddev/rtHz);
		html.row("Z", sen.q3, sen.q3Stats.min, sen.q3Stats.mean, sen.q3Stats.max, sen.q3Stats.stddev, "Z sin(theta/2)", sen.q3Stats.stddev/rtHz);
		html.row("Magnitude", sen.magnitude, sen.magStats.min, sen.magStats.mean, sen.magStats.max, sen.magStats.stddev, "", sen.magStats.stddev/rtHz);
		html.row("Angle", sen.angle, sen.angleStats.min, sen.angleStats.mean, sen.angleStats.max, sen.angleStats.stddev, "radians", sen.angleStats.stddev/rtHz);
		html.row("Rates", sen.rate, sen.rateStats.min, sen.rateStats.mean, sen.rateStats.max, sen.rateStats.stddev, "samples/sec", -1.0f);
		html.end_tbody(); html.end_table(); 		
	}
	
	HtmlGenerator dumpStatsAsHtml() {
		HtmlGenerator html = new HtmlGenerator(demo);
		html.start("Xtrinsic Sensor Fusion Toolbox");
		html.h1("Xtrinsic Sensor Fusion Toolbox Statistics Report");
		html.hr();
		html.para("Copyright 2013 by Freescale Semiconductor");
		html.para("Program version: " + demo.appVersion());
		html.para("Application settings:");
		html.start_ul();
		html.li("Data source = " + demo.dataSource);
		html.li("Algoritm = " + demo.algorithm);
		html.end_ul();
		html.para();
		dumpSensorStats(html, acc, "gravity") ;
		dumpSensorStats(html, mag, "microTeslas") ;
		dumpSensorStats(html, gyro, "radians/sec") ;
		dumpQuaternionStats(html, quaternion);
    	DisplayMetrics metrics = new DisplayMetrics();
    	demo.getWindowManager().getDefaultDisplay().getMetrics(metrics);
    	html.h2("Android device parameters");
    	html.start_ul();
    	html.li("Board = " + Build.BOARD );
    	html.li("CPU_ABI = " + Build.CPU_ABI);
    	html.li("CPU_ABI2 = " + Build.CPU_ABI2 );
    	html.li("DEVICE = " + Build.DEVICE);
    	html.li("DISPLAY = " + Build.DISPLAY);
    	html.li("FINGERPRINT = " + Build.FINGERPRINT);
    	html.li("HARDWARE = " + Build.HARDWARE);
    	html.li("MANUFACTURER = " + Build.MANUFACTURER);
    	html.li("MODEL = " + Build.MODEL);
    	html.li("PRODUCT = " + Build.PRODUCT);
    	html.li("ANDROID_VERSION = " + Build.VERSION.RELEASE);
    	html.li("CODENAME = " + Build.VERSION.CODENAME);
    	html.li("Screen_Height = " + metrics.heightPixels);
    	html.li("Screen Width = " + metrics.widthPixels);
    	html.li("Screen xdpi = " + metrics.xdpi + " pixels/inch in X dimension");
    	html.li("Screen ydpi = " + metrics.xdpi + " pixels/inch in Y dimension");
    	html.li("Screen scaled density = " + metrics.scaledDensity);
    	html.li("Computed Screen X dimension = " + metrics.widthPixels/metrics.xdpi + " inches");
    	html.li("Computed Screen Y dimension = " + metrics.heightPixels/metrics.ydpi + " inches");
    	html.end_ul();
		html.end();
		html.close();
		return(html);
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
		acc.enableLogging(en, maxSamples, oneShot, resetStats);
		mag.enableLogging(en, maxSamples, oneShot, resetStats);
		gyro.enableLogging(en, maxSamples, oneShot, resetStats);
		quaternion.enableLogging(en, maxSamples, oneShot, resetStats);
	}

	public void setSensorDescriptions(String s) {
		acc.setDescription(s);
		mag.setDescription(s);
		gyro.setDescription(s);
		quaternion.setDescription(s);
	}
	public String getSensorDescription(SensorType type) {
		String str = null;
		switch (type) {
		case ACCEL:
			str = new String("The accelerometer description is not available.");
			break;
		case MAG:
			str = new String("The magnetometer description is not available.");
			break;
		case GYRO:
			str = new String("The gyroscope description is not available.");
			break;
		case QUAT:
			str = new String("The quaternion sensor description is not available.");
			break;
		}
		return str;
	}
	public void setSensorRateBySensorType(SensorType type, int rate) {
		// nothing is the default.  Will be Overridden in derived classes
	}
}    
