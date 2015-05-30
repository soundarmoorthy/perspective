package com.flicq.tennis.test;

import android.os.Handler;

import com.flicq.tennis.framework.SampleData;
import com.flicq.tennis.opengl.ShotRenderer;

/**
 * Created by soundararajan on 5/10/2015.
 */
public class TestOpenGL {



    public static void Run() {
        //TestAccelerometerX();
        //Handler handler1 = new Handler();
        //handler1.postDelayed(new Runnable() {
         //   @Override
          //  public void run() {
          //      float[] f  = new float[7];
           //     ShotRenderer.SetData(f);
                TestAccelerometerY();
           // }
        //}, 10000);
    }

    private static void TestAccelerometerX() {
        int count = 30;
        float[] values = new float[count * 7];
        int j = 0;
        for(int i=0;i<count;i++)
        {
            values[j++] = 0.01f; //x
            values[j++] = 0.0f; //y
            values[j++] = -5.0f; //z
            values[j++] = 0.0f; //q0
            values[j++] = 0.0f; //q1
            values[j++] = 0.0f; //q2
            values[j++] = 0.0f; //q3
        }
        ShotRenderer.SetData(values);
    }

    private static void TestAccelerometerY() {
        int count = 30;
        float[] values = new float[count * 7];
        int j = 0;
        for(int i=0;i<count;i++)
        {
            values[j++] = 0.0f; //x
            values[j++] = 0.01f; //y
            values[j++] = -5.0f; //z
            values[j++] = 0.0f; //q0
            values[j++] = 0.0f; //q1
            values[j++] = 0.0f; //q2
            values[j++] = 0.0f; //q3
        }
        ShotRenderer.SetData(values);
    }

    private static void TestAccelerometerZ() {
        //We will have 10 data points for some precision
        float[] values = new float[420];
        int j = 0;
        for(int i=0;i<60;i++)
        {
            values[j++] = 0.0f; //x
            values[j++] = -0.0f; //y
            values[j++] =  -6 - 0.05f; //z
            values[j++] = 0.0f; //q0
            values[j++] = 0.0f; //q1
            values[j++] = 0.0f; //q2
            values[j++] = 0.0f; //q3
        }
        ShotRenderer.SetData(values);
    }
}
