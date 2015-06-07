package com.flicq.tennis.framework;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;

public interface IActivityAdapter {

    //
    void EnableBluetoothAdapter();

    void SetStatus(StatusType type, String s);

    Context GetApplicationContext();

    void writeToUi(String str);
}
