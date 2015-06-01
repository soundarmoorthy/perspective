package com.flicq.tennis.ble;

import com.flicq.tennis.framework.IActivityAdapter;
import com.flicq.tennis.framework.StatusType;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;



/**
 * Created by soundararajan on 4/13/2015.
 */
public final class FlicqLeScanCallback implements BluetoothAdapter.LeScanCallback
{
    IActivityAdapter helper;
    public FlicqLeScanCallback(IActivityAdapter helper)
    {
        this.helper = helper;
    }

    @Override
    public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
        //TODO : Use service based scan here.
        String address = device.getAddress();
        if(address.equals("00:07:80:06:5A:1A") ||
                address.equals("00:07:80:06:5B:4E")) { //Flicq demo device
            try {
                helper.SetStatus(StatusType.INFO, "Yo, Connected to " + device.getName() + " !");
                device.connectGatt(helper.GetApplicationContext(), false, new FlicqBluetoothGattCallback(helper));
            } catch (Exception ex) {
                ex.printStackTrace();
                helper.SetStatus(StatusType.ERROR, ex.getMessage());
            }
            connected = true;
        }
    }

    private boolean connected;
    public boolean connectionSuccessful()
    {
        return this.connected;
    }
}
