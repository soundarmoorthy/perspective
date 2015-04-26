package com.flicq.tennis.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.AsyncTask;
import android.util.Log;

import com.flicq.tennis.appengine.FlicqCloudRequestHandler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by soundararajan on 4/13/2015.
 */

public class FlicqBluetoothGattCallback extends android.bluetooth.BluetoothGattCallback
{
    FlicqSession session;
    String id;
    BluetoothAdapter adapter;
    public FlicqBluetoothGattCallback(FlicqSession session)
    {
        this.session = session;
        this.id = session.getTimestamp();
        this.adapter = adapter;
    }

    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.i("BLE", "Connected Device : " + gatt.getDevice().getName());
            gatt.discoverServices();
            try {
                File f= File.createTempFile("flicq-thurst", ".log");
                Log.e("Log File : ", f.getAbsolutePath());
                fs = new FileOutputStream(f);
            } catch (Exception ex) {

            }
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.i("BLE", "Disconnected Device : " + gatt.getDevice().getName());
            gatt.close();
            try {
                fs.close();
            } catch (Exception ex) {

            }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Log.i("BLE", "OnServicesDiscovered");

        try {
            //Try to first initialize device and gatt database.
            BluetoothGattService service = gatt.getService(UUID.fromString("ffffffff-1111-1111-ccc0-000000000000"));
            Log.i("BLE", "Found Service : " + service.getUuid().toString());
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(
                    UUID.fromString("ffffffff-1111-1111-ccc0-000000000001"));
            gatt.setCharacteristicNotification(characteristic, true);
            Log.i("BLE", "Found Characteristic : " + characteristic.getUuid());

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            boolean success = gatt.writeDescriptor(descriptor);
            Log.i("BLE", "Enable Notification for characteristic descriptor + " + descriptor.getUuid().toString() + ", Status = " + String.valueOf(success));
        } catch (Exception ex) {
            Log.e("BLE", "Error in OnServices Discovered implementation. ");
            ex.printStackTrace();
        }
    }


    FileOutputStream fs;
    public static int i=0;
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        byte[] value = characteristic.getValue();
            //fs.write(value[0]);
            Log.e("BLE", String.valueOf(value));
        //Use the data from BLE and send it to cloud
//            new AsyncTask<Void, Void, Void>() {
//                @Override
//                protected Void doInBackground(Void... params) {
//                    FlicqCloudRequestHandler f = new FlicqCloudRequestHandler();
//                    f.SendCurrentShot(String.format("%d;%d;%d;%d;%d;%d;%d",i, i++,i++,i++, i++, i++,i++));
//                    Log.e("Upload", f.getTimestamp() );
//                    return null;
//                }
//            }.execute();

    }
}
