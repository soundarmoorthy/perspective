package com.flicq.tennis.ble;

import com.flicq.tennis.framework.IActivityAdapter;
import com.flicq.tennis.framework.StatusType;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

/**
 * Created by soundararajan on 4/13/2015.
 */
final class FlicqLeScanCallback implements BluetoothAdapter.LeScanCallback
{
    private final IActivityAdapter helper;
    private BluetoothDevice firstFoundDevice;
    public FlicqLeScanCallback(IActivityAdapter helper)
    {
        this.helper = helper;
        this.connected = false;
    }

    @Override
    public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
        String address = device.getAddress();
        if(address.equals("00:07:80:06:5A:1A") ||
           address.equals("00:07:80:06:5B:4E") ||
           address.equals("00:07:80:06:5B:4E") ||
           address.equals("00:07:80:A4:6F:4D") ||
                (device.getName() != null && device.getName().endsWith("icq Demo"))) { //Flicq demo device
            try {
                helper.SetStatus(StatusType.INFO, "Yo, Connected to " + device.getName() + " !");
                helper.writeToUi("Yo, Found Device " + device.getName() + " !. Let's Pair :-)");
                firstFoundDevice  = device;
            } catch (Exception ex) {
                ex.printStackTrace();
                helper.SetStatus(StatusType.ERROR, ex.getMessage());
            }
            connected = true;
        }
    }

    public BluetoothDevice device()
    {
        return firstFoundDevice;
    }

    private boolean connected;
    public boolean connectionSuccessful()
    {
        return this.connected;
    }
}
