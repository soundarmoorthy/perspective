package com.flicq.tennis.framework;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

public interface IActivityHelper {

    //
    public void EnableBluetoothAdapter();

    void SetStatus(String s);

    Context GetApplicationContext();
    void RunOnUIThread(Runnable action);
}
