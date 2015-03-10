package com.flicq.tennis;

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
