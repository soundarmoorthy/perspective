package com.flicq.tennis.ble;


public class FlicqDevice
{

    private static FlicqDevice device;
    public static FlicqDevice getInstance() {
        if (device == null)
            device = new FlicqDevice();
        return device;
    }

}

