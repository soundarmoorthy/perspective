package com.flicq.tennis.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.os.AsyncTask;
import android.util.Log;

import com.flicq.tennis.contentmanager.ContentStore;
import com.flicq.tennis.framework.Helper;
import com.flicq.tennis.framework.IActivityAdapter;
import com.flicq.tennis.framework.ISystemComponent;
import com.flicq.tennis.framework.StatusType;
import com.flicq.tennis.framework.SystemState;
import com.flicq.tennis.test.SimulateBLEData;

public final class FlicqDevice implements ISystemComponent
{
    IActivityAdapter helper;

    private static FlicqDevice device;
    public static FlicqDevice getInstance(IActivityAdapter helper) {
        if (device == null)
            device = new FlicqDevice(helper);
        return device;
    }

    private FlicqDevice(IActivityAdapter helper)
    {
        this.helper = helper;
    }

    @Override
    public void SystemStateChanged(SystemState oldState, SystemState newState) {
        if (newState == SystemState.CAPTURE) {
            InitializeBluetoothAdapterAsync();
            ContentStore.Instance().NewShot();
        } else if (newState == SystemState.STOPPED) {
//            if (gattDevice != null)
//                gattDevice.close();
//            gattDevice = null;
            ContentStore.Instance().ShotDone();
        }
    }


    private void InitializeBluetoothAdapterAsync() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (helper != null) {
                    helper.EnableBluetoothAdapter();
                }
                return null;
            }
        }.execute();
    }

    boolean android_mode = false;
    //Called from Activity result of FlicqActivity class. The scanning can only be
    //started when the adapter initializes properly which is determined by result of the activity
    public void OnBluetoothAdapterInitialized(final BluetoothAdapter adapter) {
        if (adapter == null) {
            helper.SetStatus(StatusType.ERROR, "NO BLE SUPPORT");
            return;
        }
        if (android_mode) {
            new SimulateBLEData(helper).Start();
            return;
        }
        helper.SetStatus(StatusType.INFO, "Finding a Flicq Device");

        BluetoothAdapter.LeScanCallback callback = new FlicqLeScanCallback(helper, adapter);
        adapter.startLeScan(callback);
    }
}
