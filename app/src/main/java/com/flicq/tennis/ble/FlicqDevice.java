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

    BluetoothAdapter bleAdapter = null;
    boolean android_mode = false;
    //Called from Activity result of FlicqActivity class. The scanning can only be
    //started when the adapter initializes properly which is determined by result of the activity
    public void OnBluetoothAdapterInitialized(final BluetoothAdapter adapter) {
        this.bleAdapter = adapter;
        if (adapter == null) {
            helper.SetStatus(StatusType.ERROR, "NO BLE SUPPORT");
            return;
        }
        if (android_mode) {
            new SimulateBLEData(helper).Start();
            return;
        }
        helper.SetStatus(StatusType.INFO, "Wait !, Finding a Flicq Device");
        final FlicqLeScanCallback callback = new FlicqLeScanCallback(helper);

        //UUID []flicqServiceUUID = {UUID.fromString(FlicqBluetoothGattCallback.FLICQ_SERVICE_GATT_UUID)};
        //adapter.startLeScan(flicqServiceUUID, callback);
        adapter.startLeScan(callback);
        helper.writeToUi("BLE : LE Scan started", false);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                while (true) {
                    if (callback.connectionSuccessful()) {
                        adapter.stopLeScan(callback);
                        helper.writeToUi("BLE : LE Scan stopped. No more channel usage for scanning. WoW", false);
                        break;
                    }
                    Utils.SleepSomeTime(200);
                    helper.writeToUi("BLE : StopScan() : Polling for connection complete with 200ms interval",false);
                }
                return null;
            }
        }.execute();
    }
}
