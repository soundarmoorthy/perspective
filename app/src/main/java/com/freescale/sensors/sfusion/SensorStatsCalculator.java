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

/**
 * Defines utility functions required for the Stats view
 * @author Michael Stanley
 */
package com.freescale.sensors.sfusion;

public class SensorStatsCalculator {
	private float mn = 9999;
	public float mean = 0;
	private float mx = -9999;
	public float min=mn;
	public float max=mx;
	public float variance = 0;
	public float stddev = 0;
	private float sumX = 0;
	private float sumXX = 0;
	private int n=0;	
	public boolean dataReady = false;
	
	// Call tick() at every sample
	public synchronized void tick(float newValue, float newValueSquared) {
		if (newValue<mn) mn=newValue;
		if (newValue>mx) mx=newValue;
		sumX += newValue;
		sumXX += newValueSquared;
		n += 1;
	}
	public void clear() {
		clear(true);
	}
	/**
	 * The SensorStatsCalculator four states:
	 * 1) Not calculating (statsLoggingEnable at the sensor level = 0)
	 * 2) calculating (statsLoggingEnable && (numSamples<maxSamples))
	 * 3) stats available and continuously updating (dataReady=true AND states of (2)
	 * 4) stats available and NOT updating (dataReady=true AND (statsLoggingEnable=false OR (numSamples<maxSamples))
	 * clear() only clears the variables used to compute stats, not the statistical results themselves
	 * 
	 * @param all set to true to clear dataReady
	 */
	public synchronized void clear(boolean all) {
		mn = 9999;
		mx = -9999;
		sumX = 0;
		sumXX = 0;
		n=0;	
		if (all) dataReady = false;
	}

	// Call compute after the tick on the last sample of a sequence
	public synchronized void compute() {
		mean = sumX/(float) n;
		double temp = (n*sumXX - sumX*sumX)/(float) (n*(n-1));
		variance = (float) temp;
		stddev = (float) Math.sqrt(temp);
		min=mn;
		max=mx;
		clear(false);
		dataReady=true;
	}
	public synchronized void snapshot(SensorStatsCalculator old) {
		this.mean = old.mean;
		this.min = old.min;
		this.max = old.max;
		this.variance = old.variance;
		this.stddev = old.stddev;
		this.dataReady = old.dataReady;
	}
}
