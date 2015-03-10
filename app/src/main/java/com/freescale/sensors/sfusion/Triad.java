package com.flicq.tennis;

public class Triad {
    protected float x, y, z;

    Triad() {
        this.zero();
    }

    Triad(float x, float y, float z) {
        this.set(x, y, z);
    }

    Triad(Triad old) {
        this.set(old);
    }

    public synchronized void set(Triad old) {
        this.x = old.x;
        this.y = old.y;
        this.z = old.z;
    }

    public synchronized void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public synchronized void zero() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public synchronized void scale(float m) {
        this.x = m * this.x;
        this.y = m * this.y;
        this.z = m * this.z;
    }

    public synchronized float[] array() {
        float[] a = new float[3];
        a[0] = this.x;
        a[1] = this.y;
        a[2] = this.z;
        return (a);
    }

    public float x() {
        return (x);
    }

    public float y() {
        return (y);
    }

    public float z() {
        return (z);
    }

    public synchronized String toString() {
        String str;
        str = x() + " " + y() + " " + z();
        return (str);
    }

    public synchronized String toCsvString() {
        String str;
        str = x() + ", " + y() + ", " + z();
        return (str);
    }

}
