package com.flicq.tennis.contentmanager;

import android.text.format.Time;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.*;

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
        if(values.size() < 2)
            return null;
        synchronized (valuesLock) {
            float[] f = new float[values.size()];
            for (int i = 0; i < values.size(); i++)
                f[i] = values.get(i);
            return f;
        }
    }

    private static float[] medianFilter(float[] f) {
        float[] r =  new float[f.length];
        float[] w = new float[3];
        for(int i=0;i<7;i++)
        r[i] = f[i];
        for(int i=1;i<(f.length/7) - 1; i++) {
            int c = i  * 7,   //Current vector
                p = (i - 1) * 7, //Previous vector
                n = (i + 1) * 7; //Next vector
            int ox = 0, oy = 1, oz = 2, oq0 = 3, oq1 = 4, oq2 = 5, oq3 = 6;

            w[0] = f[p + ox];  w[1] = f[c + ox];  w[2] = f[n + ox];
            sort(w);  //Sorted data will be stored back in w.
            r[c + ox] = w[1];//Take the middle value

            w[0] = f[p + oy];  w[1] = f[c + oy];  w[2] = f[n + oy];
            sort(w);  //Sorted data will be stored back in w.
            r[c + oy] = w[1];//Take the middle value

            w[0] = f[p + oz];  w[1] = f[c + oz];  w[2] = f[n + oz];
            sort(w);  //Sorted data will be stored back in w.
            r[c + oz] = w[1];//Take the middle value

            w[0] = f[p + oq0];  w[1] = f[c + oq0];  w[2] = f[n + oq0];
            sort(w);  //Sorted data will be stored back in w.
            r[c + oq0] = w[1];//Take the middle value

            w[0] = f[p + oq1];  w[1] = f[c + oq1];  w[2] = f[n + oq1];
            sort(w);  //Sorted data will be stored back in w.
            r[c + oq1] = w[1];//Take the middle value

            w[0] = f[p + oq2];  w[1] = f[c + oq2];  w[2] = f[n + oq2];
            sort(w);  //Sorted data will be stored back in w.
            r[c + oq2] = w[1];//Take the middle value

            w[0] = f[p + oq3];  w[1] = f[c + oq3];  w[2] = f[n + oq3];
            sort(w);  //Sorted data will be stored back in w.
            r[c + oq3] = w[1];//Take the middle value
        }
        return r;
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
