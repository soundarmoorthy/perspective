package com.flicq.tennis.ble;

import android.bluetooth.BluetoothAdapter;
import android.os.AsyncTask;

import com.flicq.tennis.contentmanager.ContentStore;
import com.flicq.tennis.framework.Utils;
import com.flicq.tennis.framework.IActivityAdapter;
import com.flicq.tennis.framework.ISystemComponent;
import com.flicq.tennis.framework.StatusType;
import com.flicq.tennis.framework.SystemState;
import com.flicq.tennis.test.SimulateBLEData;

public final class FlicqDevice implements ISystemComponent
{
    IActivityAdapter activityAdapter;

    private static FlicqDevice device;
    public static FlicqDevice getInstance(IActivityAdapter helper) {
        if (device == null)
            device = new FlicqDevice(helper);
        return device;
    }

    private FlicqDevice(IActivityAdapter activityAdapter)
    {
        this.activityAdapter = activityAdapter;
    }

    @Override
    public void SystemStateChanged(SystemState oldState, SystemState newState) {
        if (newState == SystemState.CAPTURE) {
            InitializeBluetoothAdapterAsync();
        } else if (newState == SystemState.STOPPED) {
//            if (gattDevice != null)
//                gattDevice.close();
//            gattDevice = null;
        }
    }


    private void InitializeBluetoothAdapterAsync() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (activityAdapter != null) {
                    activityAdapter.EnableBluetoothAdapter();
                }
                return null;
            }
        }.execute();
    }

    BluetoothAdapter bleAdapter = null;
    boolean android_mode = false;
    //Called from Activity result of FlicqActivity class. The scanning can only be
    //started when the adapter initializes properly which is determined by result of the activity
    public void OnBluetoothAdapterInitialized(final BluetoothAdapter adapter) {
        this.bleAdapter = adapter;
        if (adapter == null) {
            activityAdapter.SetStatus(StatusType.ERROR, "NO BLE SUPPORT");
            return;
        }
        if (android_mode) {
            new SimulateBLEData(activityAdapter).Start();
            return;
        }
        activityAdapter.SetStatus(StatusType.INFO, "Wait !, Finding a Flicq Device");
        final FlicqLeScanCallback callback = new FlicqLeScanCallback(activityAdapter);

        //UUID []flicqServiceUUID = {UUID.fromString(FlicqBluetoothGattCallback.FLICQ_SERVICE_GATT_UUID)};
        //adapter.startLeScan(flicqServiceUUID, callback);
        adapter.startLeScan(callback);
        activityAdapter.writeToUi("BLE : LE Scan started", false);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                while (true) {
                    if (callback.connectionSuccessful()) {
                        adapter.stopLeScan(callback);
                        activityAdapter.writeToUi("BLE : LE Scan stopped.", false);
                        break;
                    }
                    Utils.SleepSomeTime(500);
                    activityAdapter.writeToUi("BLE : StopScan() : Polling for connection complete with 500ms interval", false);
                }
                return null;
            }
        }.execute();
    }
}
