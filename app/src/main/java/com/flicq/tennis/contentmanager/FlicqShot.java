package com.flicq.tennis.contentmanager;

import android.text.format.Time;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class FlicqShot {
    private final Object valuesLock = new Object();
    private final ArrayList<Float> values;

    private final Time time;

    public FlicqShot(Time time) {
        this.time = time;
        values = new ArrayList<Float>();
    }

    public float[] getDataForRendering() {
        if(values == null)
            return null;
        synchronized (valuesLock) {
            float[] f = new float[values.size()];
            for (int i = 0; i < values.size(); i++)
                f[i] = values.get(i);
            return f;
        }
    }

    public List<Float> getDataForUpload() {
        return values;
    }

    public void add(float []contents)
    {
        if(contents.length != 7)
            Assert.fail("The expected size of array should be 7, did you changed the size of the BLE input ?");
        //After processing the data we need to make sure that we
        //get them in the order we want, i.e ax,ay,az, q0,q1,q2,q3
        synchronized (valuesLock) {
            for (int i = 0; i < 7; i++)
                this.values.add(contents[i]);
        }
    }
}
