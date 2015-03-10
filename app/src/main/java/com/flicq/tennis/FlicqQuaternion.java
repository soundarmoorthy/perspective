package com.flicq.tennis;

import android.opengl.Matrix;

public class FlicqQuaternion {
    public float q0, q1, q2, q3;
    float[] pm = null; // p Matrix
    float[] qv = null;  // q Vector
    float[] rv = null;  // r Vector

    public synchronized String toString() {
        String str; // scratch string
        str = q0 + " " + q1 + " " + q2 + " " + q3;
        return (str);
    }

    public synchronized String toCsvString() {
        String str; // scratch string
        str = q0 + ", " + q1 + ", " + q2 + ", " + q3;
        return (str);
    }

    FlicqQuaternion() {
        initialize();
        this.setIdentity();
    }

    FlicqQuaternion(float[] q) {
        initialize();
        this.set(q);
    }

    public float X,Y,Z;
    public void setXYZ(float x, float y, float z)
    {
        X =x; Y=y;Z=z;
    }

    FlicqQuaternion(FlicqQuaternion q) {
        initialize();
        this.set(q);
    }

    void initialize() {
        pm = new float[16]; // p Matrix
        qv = new float[4];  // q Vector
        rv = new float[4];  // r Vector
    }

    void computeFromAxisAngle(float angle, float[] axis) {
        assert (axis.length == 3);
        q0 = (float) Math.cos(angle / 2);
        float sao2 = (float) Math.sin(angle / 2);
        q1 = sao2 * axis[0];
        q2 = sao2 * axis[1];
        q3 = sao2 * axis[2];
    }

    synchronized void toVector(float[] result) {
        result[0] = q0;
        result[1] = q1;
        result[2] = q2;
        result[3] = q3;
    }

    void computeFromRM(float[] rm) {
        // rm is a 3x3 rotation matrix
        assert (rm.length == 9); // rm is assumed to be 3x3 matrix of form
        // rm is assumed to be 3x3 matrix of form
        // rm0 rm1 rm2
        // rm3 rm4 rm5
        // rm6 rm7 rm8
        // From p169 of Quaternionjs and Rotation Sequences by Jack B. Kuipers, we have
        q0 = 0.5f * (float) Math.sqrt((double) (rm[0] + rm[4] + rm[8] + 1));
        q1 = (rm[5] - rm[7]) / (4 * q0);
        q2 = (rm[6] - rm[2]) / (4 * q0);
        q3 = (rm[1] - rm[3]) / (4 * q0);
    }

    synchronized void set(float[] q) {
        assert (q.length == 4);
        q0 = q[0];
        q1 = q[1];
        q2 = q[2];
        q3 = q[3];
    }

    synchronized void set(FlicqQuaternion q) {
        this.q0 = q.q0;
        this.q1 = q.q1;
        this.q2 = q.q2;
        this.q3 = q.q3;
        this.X = q.X;
        this.Y  = q.Y;
        this.Z = q.Z;
    }

    synchronized void setIdentity() {
        // set to identity quaternion
        q0 = 1;
        q1 = 0;
        q2 = 0;
        q3 = 0;
    }

    void eqPxQ(FlicqQuaternion p, FlicqQuaternion q) {
        // This is a direct coding of equation 5.3 on page 109 of Quaternions and Rotation Sequences
        q.toVector(qv);  // this works
        //sQ[0]=q0; sQ[1]=q1; sQ[2]=q2; sQ[3]=q3;  // this does not

        // Matrix form is column-major as defined at http://developer.android.com/reference/android/opengl/Matrix.html
        pm[0] = pm[5] = pm[10] = pm[15] = p.q0;
        pm[1] = pm[11] = p.q1;
        pm[2] = pm[13] = p.q2;
        pm[3] = pm[6] = p.q3;
        pm[4] = pm[14] = -p.q1;
        pm[8] = pm[7] = -p.q2;
        pm[9] = pm[12] = -p.q3;
        Matrix.multiplyMV(rv, 0, pm, 0, qv, 0);
        this.set(rv);
    }

    void reverse() {
        q0 = -q0;
    }

    void toRotationVector(RotationVector rv, FlicqUtils.AngleUnits unit) {
        float theta = (float) Math.acos(q0);
        float x, y, z, sinTheta;
        float scale_factor = 1;
        if (q0 == 1) {
            x = y = z = (float) 0.0;
        } else {
            sinTheta = (float) Math.sin(theta);
            x = q1 / sinTheta;
            y = q2 / sinTheta;
            z = q3 / sinTheta;
        }
        if (unit == FlicqUtils.AngleUnits.DEGREES) {
            scale_factor = FlicqUtils.degreesPerRadian;
        }
        rv.set(unit, 2 * theta * scale_factor, x, y, z);
    }

    /**
     * This function uses the quaternion to rotate a Vector=[0, 0, 1] to a mapped value.
     * Because X and Y of the input vector are zero, we can simplify the usual quaternion
     * multiplication (See p 158 of Jack Kuipers "Quaternion and Rotation Sequences") by
     * a factor of 2/3.
     *
     * @return Triad vector (x, y, z)
     */
    synchronized Triad mappedZ() {
        float x = 2 * q1 * q3 - 2 * q0 * q2;
        float y = 2 * q2 * q3 + 2 * q0 * q1;
        float z = 2 * q0 * q0 - 1 + 2 * q3 * q3;
        Triad t = new Triad(x, y, z);
        return (t);
    }

    // similar function, but rotates [0, 1, 0]
    synchronized Triad mappedY() {
        float x = 2 * q1 * q2 + 2 * q0 * q3;
        float y = 2 * q0 * q0 - 1 + 2 * q2 * q2;
        float z = 2 * q2 * q3 - 2 * q0 * q1;
        Triad t = new Triad(x, y, z);
        return (t);
    }

    // similar function, but rotates [1, 0, 0]
    synchronized Triad mappedX() {
        float x = 2 * q0 * q0 - 1 + 2 * q1 * q1;
        float y = 2 * q1 * q2 - 2 * q0 * q3;
        float z = 2 * q1 * q3 + 2 * q0 * q2;
        Triad t = new Triad(x, y, z);
        return (t);
    }

    // similar function, but rotates [1, 0, 0]
    synchronized Triad rotateVector(Triad V) {
        // This is Equation 7.7 on page 158 of Kuipers
        float m11 = 2 * q0 * q0 - 1 + 2 * q1 * q1;
        float m21 = 2 * q1 * q2 - 2 * q0 * q3;
        float m31 = 2 * q1 * q3 + 2 * q0 * q2;

        float m12 = 2 * q1 * q2 + 2 * q0 * q3;
        float m22 = 2 * q0 * q0 - 1 + 2 * q2 * q2;
        float m32 = 2 * q2 * q3 - 2 * q0 * q1;

        float m13 = 2 * q1 * q3 - 2 * q0 * q2;
        float m23 = 2 * q2 * q3 + 2 * q0 * q1;
        float m33 = 2 * q0 * q0 - 1 + 2 * q3 * q3;

        float w1 = m11 * V.x + m12 * V.y + m13 * V.z;
        float w2 = m21 * V.x + m22 * V.y + m23 * V.z;
        float w3 = m31 * V.x + m32 * V.y + m33 * V.z;
        Triad t = new Triad(w1, w2, w3);
        return (t);
    }
}
