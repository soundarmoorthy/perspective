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
import com.flicq.tennis.contentmanager.ContentStore;
import com.flicq.tennis.contentmanager.UnprocessedShot;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by soundararajan on 4/13/2015.
 */

public class FlicqBluetoothGattCallback extends android.bluetooth.BluetoothGattCallback {

    public FlicqBluetoothGattCallback() {
    }

    long getTimestamp() {
        return Calendar.getInstance(Locale.getDefault()).getTimeInMillis();
    }

    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.i("BLE", "Connected Device : " + gatt.getDevice().getName());
            gatt.discoverServices();
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
    public static int i = 0;

    int no_of_data = 3;
    float[] values = new float[no_of_data];

    int ax, ay, az;
    //float q0, q1, q2, q3;
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        int offset = 0;
        if(characteristic == null) {
            Log.e("BLE", "null values received");
            return;
        }
        az = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset++);
        ay = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset++);
        ax = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset++);
//        q0 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset++) / 30000f;
//        q1 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset++) / 30000f;
//        q2 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset++) / 30000f;
//        q3 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset++) / 30000f;
        values[0] = ax * 0.00012207f;
        values[1] = ay * 0.00012207f;
        values[2] = az * 0.00012207f;
        ContentStore.Instance().Dump(values);
//        float normalize = (q0 * q0) + (q1 * q1) + (q2 * q2) + (q3 * q3);
//        values[3] = q0 / normalize;
//        values[4] = q1 / normalize;
//        values[5] = q2 / normalize;
//        values[6] = q3 / normalize;

//        Find out whether they are NED or ENU and do the translation appropriately.
//        Android needs ENU inputs
//        q1 = quatInputs[2];
//        q2 = quatInputs[1];
//        q3 = -quatInputs[3];
//        quatInputs[1] = q1;
//        quatInputs[2] = q2;
//        quatInputs[3] = q3;
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

