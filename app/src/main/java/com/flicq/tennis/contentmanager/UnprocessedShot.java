package com.flicq.tennis.contentmanager;

import java.util.LinkedList;
import java.util.List;

public class UnprocessedShot {
    private LinkedList<float[]> values;
    long key;

    public LinkedList<float[]> getPoints()
    {
        return values;
    }

    public long getId()
    {
        return this.key;
    }

    public UnprocessedShot(long id)
    {
       this.key = id;
        values = new LinkedList<float[]>();
    }

    public float[] getDataForRendering() {
        float[] f = new float[values.size() * 7];
        int index = 0;
        for (int i = 0; i < values.size(); i++)
        {
            float[] point = values.get(i);
            int j;
            for(j=0;j<point.length;j++)
            {
                f[index++] = point[j];
            }
            while(j++ < 7)
                f[index++] = 0.1f;//Till we get all the values we should set quaternion to 0.0;
        }
        return f;
    }

    public void add(float []sensorValues)
    {
            this.values.add(sensorValues);
    }
}
