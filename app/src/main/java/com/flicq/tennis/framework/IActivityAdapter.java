package com.flicq.tennis.framework;

import android.content.Context;

import com.flicq.tennis.ble.FlicqBluetoothGattCallback;

public interface IActivityAdapter {

    void SetStatus(StatusType type, String s);

    Context GetApplicationContext();

    void writeToUi(String str);

    void onDisconnected();

    void notifyWhenReady(Object flicqBluetoothGattCallback);
}
