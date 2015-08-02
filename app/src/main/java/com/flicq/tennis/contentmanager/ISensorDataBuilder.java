package com.flicq.tennis.contentmanager;

/**
 * Created by minion on 26/7/15.
 */
public interface ISensorDataBuilder {
    float[] getAcceleration();
    float[] getQuaternion();

    String dump();
}
