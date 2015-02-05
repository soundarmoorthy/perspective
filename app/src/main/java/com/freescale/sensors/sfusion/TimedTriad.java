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

import android.util.Log;


/**
 * Extends the Triad class by adding timestamps.  This class also instantiates multiple
 * copies of the SensorStatsCalculator to handle computation of numbers for the
 * Statistics view of the application.
 *
 * @author Michael Stanley
 */
public class TimedTriad extends Triad {
    private long t = -1;  // sample time
    private long lastT = -1;
    public float rate = 0.0f;
    private float timeScaleFactor = 1;
    private float magnitude = 0.0f;
    private boolean lowPassFilterEnabled = false;
    private float filterCoefficient = 0.0f;
    public boolean enabled = true;
    public int maxSamples = 100;
    private String sensorName = new String("Sensor Not Configured");
    private String sensorDescription = new String("Sensor Not Configured");

    public void setDisabled() {
        enabled = false;
        sensorName = new String("Sensor Not Configured");
        sensorDescription = new String("Sensor Not Configured");
    }

    public TimedTriad() {
        this.zero();
    }

    public TimedTriad(long t, float x, float y, float z) {
        this.set(t, x, y, z);
    }

    public TimedTriad(TimedTriad old) {
        this.set(old);
    }

    public void setName(String name) {
        this.sensorName = new String(name);
    }

    public String getName() {
        return (this.sensorName);
    }

    public void setDescription(String desc) {
        this.sensorDescription = new String(desc);
    }

    public String getDescription() {
        return (this.sensorDescription);
    }

    public synchronized void set(TimedTriad old) {
        this.lastT = this.t;
        this.t = old.t;
        super.set(old);
    }

    public synchronized void set(long t, float x, float y, float z) {
        this.lastT = this.t;
        this.t = t;
        super.set(x, y, z);
    }

    public synchronized void snapshot(TimedTriad orig) {
        this.t = orig.t;
        this.enabled = orig.enabled;
        this.lastT = orig.lastT;
        this.sensorDescription = orig.sensorDescription;
        this.sensorName = orig.sensorName;
        this.x = orig.x;
        this.y = orig.y;
        this.z = orig.z;
        this.rate = orig.rate;
        this.maxSamples = orig.maxSamples;
        this.magnitude = orig.magnitude;
        this.timeScaleFactor = orig.timeScaleFactor;
        this.lowPassFilterEnabled = orig.lowPassFilterEnabled;
        this.filterCoefficient = orig.filterCoefficient;
    }

    public synchronized void setTimeScale(float scaleFactor) {
        timeScaleFactor = scaleFactor;
    }

    public synchronized void setFilterCoef(float fc) {
        filterCoefficient = fc;
    }

    public synchronized void enableLpf(boolean set) {
        lowPassFilterEnabled = set;
    }

    public synchronized void outdate() {
        t = -1; // time=-1 can never occur, so treat as "outdated value"
    }

    public synchronized void zero() {
        super.zero();
        t = -1; // time=-1 can never occur, so treat as "outdated value"
    }

    public synchronized void scale(float m) {
        super.scale(m);
        // no change in t
    }

    public boolean hasBeenSet() {
        return (t >= 0);
    }

    public synchronized void update(long t, float x, float y, float z) {
        this.lastT = this.t;
        if (lowPassFilterEnabled && hasBeenSet()) {
            this.x = (1 - filterCoefficient) * x + filterCoefficient * this.x;
            this.y = (1 - filterCoefficient) * y + filterCoefficient * this.y;
            this.z = (1 - filterCoefficient) * z + filterCoefficient * this.z;
            this.t = t;
        } else {
            this.t = t;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public synchronized float time() {
        return (((float) t) * timeScaleFactor);
    }

    public synchronized float x() {
        return (super.x);
    }

    public synchronized float y() {
        return (super.y);
    }

    public synchronized float z() {
        return (super.z);
    }

    public synchronized float magnitude() {
        return (this.magnitude);
    }

    public synchronized String toString() {
        String str;
        str = time() + " " + super.toString();
        return (str);
    }
}
