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

import android.app.AlertDialog;

/**
 * Basically a set of utility functions, many of which need a pointer to the main application instance.
 *
 * @author Michael Stanley
 */
public class FlicqUtils {
    static public final float degreesPerRadian = (float) (180.0f / 3.14159f);
    static public final float radiansPerDegree = (float) (3.14159f / 180.0f);

    public enum AngleUnits {DEGREES, RADIANS}

    static public float unitScaler = 32768.0f;

    FlicqUtils() {
    }

    static public float dotProduct(float[] a, float[] b) {
        // a and b are assumed to be the same length
        assert (a.length == b.length);
        float dp = 0;
        for (int i = 0; i < b.length; i++) {
            dp = dp + a[i] * b[i];
        }
        return (dp);
    }

    static public void computeCrossProduct(float[] cp, float[] a, float[] b) {
        // a and b are assumed to be 3x1 vectors
        assert (a.length == 3);
        assert (b.length == 3);
        cp[0] = a[1] * b[2] - a[2] * b[1];
        cp[1] = a[2] * b[0] - a[0] * b[2];
        cp[2] = a[0] * b[1] - a[1] * b[0];
    }

    static public float norm(float[] v) {
        int n = v.length;
        float norm = 0;
        for (int i = 0; i < n; i++) {
            norm = norm + v[i] * v[i];
        }
        norm = (float) Math.sqrt(norm);
        return (norm);
    }

    static public void normalize(float[] vout, float[] vin) {
        scalarMult(vout, vin, 1 / norm(vin));
    }

    static public void scalarMult(float[] vout, float[] vin, float s) {
        assert (vin.length == vout.length);
        for (int i = 0; i < vout.length; i++) {
            vout[i] = s * vin[i];
        }
    }

    static public float axisAngle(float[] axis, float[] v1, float[] v2) {
        assert (v1.length == 3);
        assert (v2.length == 3);
        float[] cp = new float[3]; // cross product
        float[] nv1 = new float[3]; // normalized version of v1
        float[] nv2 = new float[3]; // normalized version of v2
        normalize(nv1, v1);
        normalize(nv2, v2);
        float cosAngle = dotProduct(nv1, nv2);
        float angle = (float) Math.acos(cosAngle);
        computeCrossProduct(cp, nv1, nv2);
        normalize(axis, cp);
        return (angle); // Angle is in radians
    }

    static public void waitALittle(int delay) {
        try {
            Thread.sleep(delay);
        } catch (Throwable t) {
        }
    }
}
