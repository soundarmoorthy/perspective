// Copyright 2013 by Freescale Semiconductor
package com.freescale.sensors.sfusion;

import android.util.Log;


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

* This class is an extension to the DemoQuaternion class which adds a timestamp.
* @author Michael Stanley
*/
public class TimedQuaternion extends DemoQuaternion {
	private long t=-1;  // sample time
	private long lastT=-1;
	private float axisScale=1.0f;
	//public RateStatsCalculator rateStatsCalculator = new RateStatsCalculator();
	public SensorStatsCalculator q0Stats = null;
	public SensorStatsCalculator q1Stats = null;
	public SensorStatsCalculator q2Stats = null;
	public SensorStatsCalculator q3Stats = null;
	public SensorStatsCalculator magStats = null;
	public SensorStatsCalculator angleStats = null;
	public SensorStatsCalculator rateStats = null;
	public float magnitude=0.0f;
	public float angle = 0.0f;
	public float rate = 0.0f;
	public int maxSamples=100;
	private String sensorName = new String("quaternion");
	private String sensorDescription = new String("Sensor Not Configured");
	private int numSamples=0;
	private boolean statsOneShot = false;

	public boolean statsLoggingEnabled = false;
	private float timeScaleFactor=1;	
		
	public boolean statsReady() {
		return(q0Stats.dataReady && q1Stats.dataReady && q2Stats.dataReady && 
				q3Stats.dataReady && magStats.dataReady && angleStats.dataReady && rateStats.dataReady);		
	}
	public float percentDone() {
		// Compute how many samples are still required to fill desired sample size for stats gathering
		// returns number between 0 and 1 (inclusive)
		if (statsReady()) {
			return(1.0f);
		} else {
			return(((float) numSamples)/ ((float) maxSamples));
		}
	}
	public void setDescription(String desc) {
		this.sensorDescription=new String(desc);  
	}
	public String getDescription() {
		return(this.sensorDescription);
	}
	public String getName() {
		return(this.sensorName);
	}

	private void populateStats() {
		q0Stats = new SensorStatsCalculator();
		q1Stats = new SensorStatsCalculator();
		q2Stats = new SensorStatsCalculator();
		q3Stats = new SensorStatsCalculator();
		magStats = new SensorStatsCalculator();	
		angleStats = new SensorStatsCalculator();
		rateStats = new SensorStatsCalculator();	
	}

	TimedQuaternion() {
		super.setIdentity();
		this.t=-1;
		populateStats();
	}
	TimedQuaternion(long time, float[] q) {
		this.set(time, q);
		populateStats();
	}
	TimedQuaternion(long time, DemoQuaternion q) {
		this.set(time, q);
		populateStats();
	}
	public synchronized void set(long time, DemoQuaternion q) {
		super.set(q);
		this.lastT = this.t;
		this.t = time;
		optionalStatsUpdate();
	}
	public synchronized void set(long time, float[] q) {
		super.set(q);
		this.lastT = this.t;
		this.t = time;
		optionalStatsUpdate();
	}

	public synchronized void snapshot(TimedQuaternion orig) {
		this.sensorDescription=orig.sensorDescription;
		this.sensorName=orig.sensorName;
		this.t = orig.t;
		this.lastT = orig.lastT;
		this.q0 = orig.q0;
		this.q1 = orig.q1;
		this.q2 = orig.q2;
		this.q3 = orig.q3;
		this.rate = orig.rate;
		this.angle = orig.angle;
		this.timeScaleFactor = orig.timeScaleFactor;
		this.magnitude = orig.magnitude;
		this.maxSamples = orig.maxSamples;
		this.axisScale = orig.axisScale;
		this.numSamples = orig.numSamples;
		this.rateStats.snapshot(orig.rateStats);
		this.statsOneShot = orig.statsOneShot;
		this.statsLoggingEnabled = orig.statsLoggingEnabled;
		this.q0Stats.snapshot(orig.q0Stats);
		this.q1Stats.snapshot(orig.q1Stats);
		this.q2Stats.snapshot(orig.q2Stats);
		this.q3Stats.snapshot(orig.q3Stats);
		this.magStats.snapshot(orig.magStats);
		this.angleStats.snapshot(orig.angleStats);
	}
	
	public synchronized void enableLogging(boolean en, int maxSamples, boolean oneShot, boolean resetStats) {
		// we will reset basic stats variables if ANYTHING changes except en
		statsLoggingEnabled = en;
		if (this.statsOneShot != oneShot) {
			this.statsOneShot = oneShot;
			resetStats=true;
		}
		if (this.maxSamples != maxSamples) {
			this.maxSamples = maxSamples;
			resetStats=true;
		}
		if (resetStats) {
			q0Stats.clear(resetStats);
			q1Stats.clear(resetStats);
			q2Stats.clear(resetStats);
			q3Stats.clear(resetStats);
			magStats.clear(resetStats);
			angleStats.clear(resetStats);
			rateStats.clear(resetStats);
			numSamples=0;
		}
	}
	// call AFTER set();
	private void optionalStatsUpdate() {
		if (statsLoggingEnabled && (numSamples<maxSamples)) {
			numSamples += 1;
			float time_increment = this.t-this.lastT;
			if (time_increment > 0) {
				rate =  1.0f/(this.timeScaleFactor*time_increment);
			} else {
				Log.e("TEST", "ERROR! zero time increment spotted.");
			}
			float q0q0 = q0*q0;
			float q1q1 = q1*q1;
			float q2q2 = q2*q2;
			float q3q3 = q3*q3;
			angle = 2.0f * (float) Math.acos((double) q0);
			float mm = q0q0 + q1q1 + q2q2 + q3q3;
			this.magnitude = (float) Math.sqrt(mm);
			q0Stats.tick(q0,  q0q0);
			q1Stats.tick(q1,  q1q1);
			q2Stats.tick(q2,  q2q2);
			q3Stats.tick(q3,  q3q3);
			magStats.tick(this.magnitude,  mm);
			angleStats.tick(angle,  angle*angle);
			if (numSamples>1) rateStats.tick(rate, rate*rate);
			if (numSamples>=maxSamples) {
				q0Stats.compute();
				q1Stats.compute();
				q2Stats.compute();
				q3Stats.compute();
				magStats.compute();
				angleStats.compute();
				rateStats.compute();
				if (statsOneShot) {
					MyUtils.beep();
				}
				// status period is complete.  Finish out the calculations
			} 
		}	
		if (numSamples>=maxSamples) {
			if (!statsOneShot) {
				numSamples=0;
			}
		}
	}

	public synchronized void setTimeScale(float scaleFactor) {
		timeScaleFactor = scaleFactor;
	}
	public synchronized void outdate() {
		t=-1; // time=-1 can never occur, so treat as "outdated value"
	}
	public boolean hasBeenSet() {
		return(t>=0);
	}
	public synchronized float time() {
		return(((float) t)*timeScaleFactor);
	}
	public synchronized String toString()  {
		String str;
		str = time() + " " + super.toString();
		return(str);
	}
}
