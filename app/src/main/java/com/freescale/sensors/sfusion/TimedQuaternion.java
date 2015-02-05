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
    private long t = -1;  // sample time
    private long lastT = -1;
    private float axisScale = 1.0f;
    //public RateStatsCalculator rateStatsCalculator = new RateStatsCalculator();
    public float magnitude = 0.0f;
    public float angle = 0.0f;
    public float rate = 0.0f;
    public int maxSamples = 100;
    private String sensorName = new String("quaternion");
    private String sensorDescription = new String("Sensor Not Configured");
    private int numSamples = 0;
    private boolean statsOneShot = false;

    public boolean statsLoggingEnabled = false;
    private float timeScaleFactor = 1;

    public void setDescription(String desc) {
        this.sensorDescription = new String(desc);
    }

    public String getDescription() {
        return (this.sensorDescription);
    }

    public String getName() {
        return (this.sensorName);
    }

    TimedQuaternion() {
        super.setIdentity();
        this.t = -1;
    }

    TimedQuaternion(long time, float[] q) {
        this.set(time, q);
    }

    TimedQuaternion(long time, DemoQuaternion q) {
        this.set(time, q);
    }

    public synchronized void set(long time, DemoQuaternion q) {
        super.set(q);
        this.lastT = this.t;
        this.t = time;
    }

    public synchronized void set(long time, float[] q) {
        super.set(q);
        this.lastT = this.t;
        this.t = time;
    }

    public synchronized void snapshot(TimedQuaternion orig) {
        this.sensorDescription = orig.sensorDescription;
        this.sensorName = orig.sensorName;
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
        this.statsOneShot = orig.statsOneShot;
        this.statsLoggingEnabled = orig.statsLoggingEnabled;
    }

    public synchronized void setTimeScale(float scaleFactor) {
        timeScaleFactor = scaleFactor;
    }

    public synchronized void outdate() {
        t = -1; // time=-1 can never occur, so treat as "outdated value"
    }

    public boolean hasBeenSet() {
        return (t >= 0);
    }

    public synchronized float time() {
        return (((float) t) * timeScaleFactor);
    }

    public synchronized String toString() {
        String str;
        str = time() + " " + super.toString();
        return (str);
    }
}
