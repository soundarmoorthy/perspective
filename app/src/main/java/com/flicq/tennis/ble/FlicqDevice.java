package com.flicq.tennis.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.flicq.tennis.appengine.FlicqCloudRequestHandler;
import com.flicq.tennis.contentmanager.ContentStore;
import com.flicq.tennis.framework.Helper;
import com.flicq.tennis.framework.IActivityHelper;
import com.flicq.tennis.framework.ISystemComponent;
import com.flicq.tennis.framework.SampleData;
import com.flicq.tennis.framework.SystemState;

public final class FlicqDevice implements ISystemComponent, BluetoothAdapter.LeScanCallback
{
    IActivityHelper helper;

    private static FlicqDevice device;
    public static FlicqDevice getInstance(IActivityHelper helper) {
        if (device == null)
            device = new FlicqDevice(helper);
        return device;
    }

    private FlicqDevice(IActivityHelper helper)
    {
        this.helper = helper;
    }

    @Override
    public void SystemStateChanged(SystemState oldState, SystemState newState) {
        if (newState == SystemState.CAPTURE) {
            InitializeBluetoothAdapterAsync();
            ContentStore.Instance().NewShot();
        } else if (newState == SystemState.STOPPED) {
            if (gattDevice != null)
                gattDevice.close();
            gattDevice = null;
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

    //Called from Activity result of FlicqActivity class. The scanning can only be
    //started when the adapter initializes properly which is determined by result of the activity
    public void OnBluetoothAdapterInitialized(BluetoothAdapter adapter) {
        if (adapter == null) {
            helper.SetStatus("The device doesn't support BLE. Cannot continue");
            return;
        }
        adapter.startLeScan(this);
        while (true) {
            if (adapter.getState() == BluetoothAdapter.STATE_CONNECTED) {
                adapter.stopLeScan(this);
                break;
            }
            Helper.SleepSomeTime(300);
        }
    }

    BluetoothGatt gattDevice;
    @Override
    public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
        //TODO : Use service based scan here.
        String address = device.getAddress();
        if(address.equals("00:07:80:06:5A:1A") ||
                address.equals("00:07:80:06:5B:4E")) { //Flicq demo device
            try {
                Log.i("BLE", "Found Device :  " + address + " , Name : " + device.getName());
                gattDevice = device.connectGatt(helper.GetApplicationContext(), false, new FlicqBluetoothGattCallback());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
