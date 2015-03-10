package com.flicq.tennis;

public class TimedQuaternion extends FlicqQuaternion {
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

    TimedQuaternion(long time, FlicqQuaternion q) {
        this.set(time, q);
    }

    public synchronized void set(long time, FlicqQuaternion q) {
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
