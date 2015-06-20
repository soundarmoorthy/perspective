package com.flicq.tennis.contentmanager;

import android.text.format.Time;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

public class FlicqShot {
    private ArrayList<Float> values;

    Time time;

    public List<Float> getPoints()
    {
        return values;
    }

    public Time getTimeStamp()
    {
        return this.time;
    }

    public FlicqShot(Time time)
    {
       this.time = time;
        values = new ArrayList<Float>();
    }

    public float[] getDataForRendering() {
        float[] f = new float[values.size()];
        for (int i=0;i<values.size();i++)
            f[i] = values.get(i);
        return f;
    }

    public List<Float> getDataForUpload() {
        return values;
    }

    float[] cache = new float[7];
    public void add(float []values) {

        if(values.length != 7)
            Assert.fail("The expected size of array should be 7, did you changed the size of the BLE input ?");
        //After processing the data we need to make sure that we
        //get them in the order we want, i.e ax,ay,az, q0,q1,q2,q3
        cache[0] = values[4];
        cache[1] = values[5];
        cache[2] = values[6];

        cache[3] = values[0];
        cache[4] = values[1];
        cache[5] = values[2];
        cache[6] = values[3];
        for(int i=0;i<7;i++)
        {
            this.values.add(cache[i]);
        }
    }
}
