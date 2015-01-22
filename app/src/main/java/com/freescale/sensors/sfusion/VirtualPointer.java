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
 * This class implements virtual pointer/air mouse functions based on the standard
 * quaternion output from the sensor fusion routines.  These computations could be
 * in an embedded environment.  See the "Canvas View" page of the in-app documentation
 * for details of the algorithm.
 *
 * @author Michael Stanley
 */
public class VirtualPointer {
    public int x; // X screen pointer
    public int y; // Y screen pointer
    public int maxX; // requested maximum X screen pointer
    public int maxY; // requested maximum Y screen pointer
    public double hSin = 0; // sin & cos used to rotate measured heading back by baseline amount
    public double hCos = 1;
    public double headingBaseline = 0; // baseline compass heading
    public double inclinationBaseline = 0; // baseline inclination from horizontal
    public double inclinationCurrent = 0; // current inclination from horizontal
    public Triad px = null; // [1,0,0] as observed by the PCB
    public Triad pz = null; // [0,0,1] as observed by the PCB
    private Triad unityPointerX = null; // reference vector used for horizontal heading
    private Triad unityPointerZ = null; // reference vector used for vertical inclination
    private double sensitivity; // computed scale factor used to scale frustum range to desired graphic window
    private double frustumLimits = Math.PI / 6; // +/- 30 dgree variation are natural pointer limits
    DemoQuaternion quaternion = null;

    public synchronized void update(DemoQuaternion q, boolean mouseMode) {
        // q = current rotation quaternion
        // mouseMode = true if we are operating in relative (mouse) mode
        // mouseMode = false if we are operating as an absolute pointer
        px.set(q.rotateVector(unityPointerX));
        pz.set(q.rotateVector(unityPointerZ));
        double xeff = hCos * px.x - hSin * px.y;  // X component rotated back based on baseline orientatino
        double yeff = hSin * px.x + hCos * px.y;  // Y component rotated back based on baseline orientation
        double headingDelta = Math.atan2(yeff, xeff);  // compute the change in compass heading from baseline
        inclinationCurrent = Math.asin(pz.y);  // compute the current inclination based on [0,0,1] as observed by the PCB
        double yEst = sensitivity * Math.tan(inclinationCurrent - inclinationBaseline);
        if (mouseMode) {
            center(q);  // note that center() sets x=y=0, but that will get overwritten below
        } else {
            headingDelta = MyUtils.limitD(headingDelta, frustumLimits);
        }
        double xEst = sensitivity * Math.tan(headingDelta);
        if (mouseMode) {
            x = (int) xEst;
            y = (int) yEst;
        } else {
            x = (int) Math.round((xEst + x) / 2); // simple filter and rounding in this operation
            y = (int) Math.round((yEst + y) / 2); // simple filter and rounding in this operation
            x = MyUtils.limitI(x, maxX);
            y = MyUtils.limitI(y, maxY);
        }
    }

    public VirtualPointer() {
        quaternion = new DemoQuaternion();
        px = new Triad(1, 0, 0);
        pz = new Triad(1, 0, 0);
        unityPointerX = new Triad(1, 0, 0);
        unityPointerZ = new Triad(0, 0, 1);
    }

    public synchronized void center(DemoQuaternion q) {
        Log.v(A_FSL_Sensor_Demo.LOG_TAG, "centering virtual pointer");
        px.set(q.rotateVector(unityPointerX)); // compute horizontal pointer
        pz.set(q.rotateVector(unityPointerZ)); // compute vertical pointer
        headingBaseline = Math.atan2(px.y, px.x);  // compute the baseline heading
        hSin = Math.sin(-headingBaseline); // store away sin and cosine for baseline heading for later rotations of horizontal pointers
        hCos = Math.cos(-headingBaseline);
        inclinationBaseline = Math.asin(pz.y); // compute the vertical inclination
        x = 0;
        y = 0;
    }

    public synchronized void updateSensitivity(int maxX, int maxY) {
        // we assume we use center +/- 45 degrees of rotation for pointer
        // we assume a standard X/Y frame of reference centered at [0,0] with X ranging from -maxX to +maxX
        // and Y ranging from -maxY to +maxY;
        this.maxX = maxX;
        this.maxY = maxY;
        if (maxX > maxY) {
            this.sensitivity = ((double) maxX) / Math.sin(frustumLimits);
        } else {
            this.sensitivity = ((double) maxY) / Math.sin(frustumLimits);
        }
    }


}
