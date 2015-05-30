package com.flicq.tennis.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.flicq.tennis.contentmanager.AsyncSerialContentProcessor;
import com.flicq.tennis.framework.Helper;

import java.util.UUID;

/**
 * Created by soundararajan on 4/13/2015.
 */

public class FlicqBluetoothGattCallback extends android.bluetooth.BluetoothGattCallback {

    public FlicqBluetoothGattCallback() {
    }

    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.i("BLE", "Connected Device : " + gatt.getDevice().getName());
                Helper.SleepSomeTime(300);
            gatt.discoverServices();
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.i("BLE", "Disconnected Device : " + gatt.getDevice().getName());
            gatt.close();
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
            Helper.SleepSomeTime(300);
            gatt.setCharacteristicNotification(characteristic, true);
            Log.i("BLE", "Found Characteristic : " + characteristic.getUuid());

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
            Helper.SleepSomeTime(300);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            boolean success = gatt.writeDescriptor(descriptor);
            Log.i("BLE", "Enable Notification for characteristic descriptor + " + descriptor.getUuid().toString() + ", Status = " + String.valueOf(success));
        } catch (Exception ex) {
            Log.e("BLE", "Error in OnServices Discovered implementation. ");
            ex.printStackTrace();
        }
    }


    public static int i = 0;

    float[] values = new float[7];

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        int offset = 0;
        if(characteristic == null) {
            Log.e("BLE", "null values received");
            return;
        }
        values[0] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset++);
        values[1] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset++);
        values[2] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset++);
        values[3] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset++);
        values[4] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset++);
        values[5] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset++);
        values[6] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset);

        AsyncSerialContentProcessor.Instance().Process(values);

    }
}
