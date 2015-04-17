package com.flicq.tennis.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.util.Log;

import com.flicq.tennis.framework.IActivityHelper;

/**
 * Created by soundararajan on 4/13/2015.
 */
public final class FlicqLeScanCallback implements BluetoothAdapter.LeScanCallback
{
    IActivityHelper helper;
    FlicqSession session;
    public FlicqLeScanCallback(IActivityHelper helper, FlicqSession session)
    {
        this.helper = helper;
        this.session = session;
    }
    @Override
    public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
        //TODO : Use service based scan here.
        String address = device.getAddress();
        //if(address.equals("00:07:80:06:5A:1A")) { //2 Flicq demo device
        if (address.equals("00:07:80:06:5B:4E")) { //Flicq demo device
            try {
                Log.i("BLE", "Found Device :  " + address + " , Name : " + device.getName());
                BluetoothGatt gatt = device.connectGatt(helper.GetApplicationContext(), false, new FlicqBluetoothGattCallback(session));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
