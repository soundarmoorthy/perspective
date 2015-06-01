package com.flicq.tennis.framework;

/**
 * Created by soundararajan on 5/18/2015.
 */
public class Utils {
    public static void SleepSomeTime(int milliseconds)
    {
        try{
            Thread.sleep(milliseconds);
        }catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
