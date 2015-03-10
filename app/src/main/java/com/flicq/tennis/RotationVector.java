package com.flicq.tennis;

import com.flicq.tennis.FlicqUtils.AngleUnits;

public class RotationVector {
    // a = angle in radians OR degrees (this is up to the programmer, NOT enforced here
    // x, y, z = coordinates of vector about which we will do the rotation.
    public AngleUnits units;
    public float a, x, y, z;

    RotationVector() {
        this.setIdentity();
    }

    RotationVector(AngleUnits units, float[] rv) {
        this.set(units, rv);
    }

    RotationVector(RotationVector rv) {
        this.set(rv);
    }

    RotationVector(AngleUnits units, float a, float x, float y, float z) {
        this.set(units, a, x, y, z);
    }

    synchronized void set(AngleUnits units, float a, float x, float y, float z) {
        this.units = units;
        this.a = a;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    synchronized void set(AngleUnits units, float[] rv) {
        this.units = units;
        a = rv[0];
        x = rv[1];
        y = rv[2];
        z = rv[3];
    }

    public synchronized void computeFromQuaternion(FlicqQuaternion q, AngleUnits units) {
        float theta = (float) Math.acos(q.q0);
        a = 2 * theta;
        if (q.q0 == 1) {
            x = y = z = (float) 0.0;
        } else {
            float sinTheta = (float) Math.sin(theta);
            x = q.q1 / sinTheta;
            y = q.q2 / sinTheta;
            z = q.q3 / sinTheta;
        }
        this.units = AngleUnits.RADIANS;
        convert(units); // only changes if needed
    }

    synchronized void convert(AngleUnits units) {
        if ((this.units == AngleUnits.RADIANS) && (units == AngleUnits.DEGREES)) {
            this.units = units;
            this.a = this.a * FlicqUtils.degreesPerRadian;
        }
    }

    synchronized void set(RotationVector rv) {
        this.units = rv.units;
        this.a = rv.a;
        this.x = rv.x;
        this.y = rv.y;
        this.z = rv.z;
    }

    synchronized void setIdentity() {
        units = AngleUnits.RADIANS;
        a = 0;
        x = 0;
        y = 0;
        z = 1;
    }

}
