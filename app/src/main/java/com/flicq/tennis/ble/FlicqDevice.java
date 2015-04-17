package com.flicq.tennis.ble;

import android.bluetooth.BluetoothAdapter;
import android.os.AsyncTask;
import com.flicq.tennis.framework.IActivityHelper;
import com.flicq.tennis.framework.ISystemComponent;
import com.flicq.tennis.framework.SystemState;

public final class FlicqDevice implements ISystemComponent
{
    private FlicqSession session;
    IActivityHelper helper;
    boolean bluetoothAdapterInitialized;

    private static FlicqDevice device;
    public static FlicqDevice getInstance(IActivityHelper helper) {
        if (device == null)
            device = new FlicqDevice(helper);
        return device;
    }

    private FlicqDevice(IActivityHelper helper)
    {
        this.helper = helper;
        bluetoothAdapterInitialized = false;
    }

    private void initialize() {
        if (helper != null) {
            helper.EnableBluetoothAdapter();
            bluetoothAdapterInitialized = true;
        }
    }

    @Override
    public void SystemStateChanged(SystemState oldState, SystemState newState) {
        if (newState == SystemState.CAPTURE)
        {
            if(!bluetoothAdapterInitialized)
                InitializeBluetoothAdapterAsync();
            session = new FlicqSession();
        }
        else if (newState == SystemState.STOPPED) {
            if(oldState == SystemState.CAPTURE)
                session = null;
        }
    }

    private void InitializeBluetoothAdapterAsync() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                initialize();
                return null;
            }
        }.execute();
    }

    //Called from Activity result of FlicqActivity class. The scanning can only be
    //started when the adapter initializes properly which is determined by result of the activity
    public void OnBluetoothAdapterInitialized(BluetoothAdapter adapter) {
        if (adapter == null) {
            helper.SetStatus("The device doesn't support BLE. Cannot continue");
        } else {
            adapter.startLeScan(new FlicqLeScanCallback(helper, session));
        }
    }
}
