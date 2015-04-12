package com.flicq.tennis.ble;


import com.flicq.tennis.ISystemComponent;
import com.flicq.tennis.SystemState;

public final class FlicqDevice implements ISystemComponent
{
    private static FlicqDevice device;
    public static FlicqDevice getInstance() {
        if (device == null)
            device = new FlicqDevice();
        return device;
    }

    @Override
    public void SystemStateChanged(SystemState oldState, SystemState newState) {
        if (newState == SystemState.CAPTURE) {
        } else if (newState == SystemState.RENDER) {
        } else if (newState == SystemState.STOPPED) {

        }
    }
}

