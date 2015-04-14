package com.flicq.tennis.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.io.StringReader;
import java.util.List;
import java.util.UUID;

/**
 * Created by soundararajan on 4/13/2015.
 */

public class FlicqBluetoothGattCallback extends android.bluetooth.BluetoothGattCallback
{
    FlicqSession session;
    String id;
    public FlicqBluetoothGattCallback(FlicqSession session)
    {
        this.session = session;
        this.id = session.getTimestamp();
    }

    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if(newState == 0)
            Log.i("BLE", "Connected Device : " + gatt.getDevice().getName());
        else if(newState == 8)
            Log.i("BLE", "Disconnected Device : " + gatt.getDevice().getName());

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

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.i("BLE", "OnCharacteristicRead");
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.i("BLE", "OnCharacteristicWrite");
    }

    public static int i=0;
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Log.i("BLE", "OnCharacteristicChanged " + String.valueOf(i++));

        Log.i("BLE", characteristic.getStringValue(0));
        //String value = characteristic.getStringValue(0);
        //Queue it in a TaskExecutor to run it in the future
        //session.getCloudManager().SendCurrentShot(value, id);
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Log.i("BLE", "OnDescriptionRead");
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Log.i("BLE", "OnDescriptionWrite");
    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        Log.i("BLE", "OnReliableWriteCompleted");
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        Log.i("BLE", "OnReadRemoteRssi");
    }
}
