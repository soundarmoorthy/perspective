package com.flicq.tennis.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.flicq.tennis.contentmanager.AsyncSerialContentProcessor;
import com.flicq.tennis.framework.IActivityAdapter;
import com.flicq.tennis.framework.StatusType;
import com.flicq.tennis.framework.Utils;

import java.util.Calendar;
import java.util.UUID;

/**
 * Created by soundararajan on 4/13/2015.
 */

public class FlicqBluetoothGattCallback extends android.bluetooth.BluetoothGattCallback {

    IActivityAdapter helper;
    public FlicqBluetoothGattCallback(IActivityAdapter helper) {
        this.helper = helper;
    }

    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            helper.writeToUi("BLE : Connected Device : " + gatt.getDevice().getName(),false);
            Utils.SleepSomeTime(20);
            gatt.discoverServices();
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            helper.writeToUi("BLE : Disconnected Device : " + gatt.getDevice().getName(), false);
            helper.SetStatus(StatusType.WARNING, "Disconnected  : " + gatt.getDevice().getName());

            gatt.close();
        }
    }

  public static final String FLICQ_SERVICE_GATT_UUID = "ffffffff-1111-1111-ccc0-000000000000";
    public static final String FLICQ_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        try {
            //Try to first initialize device and gatt database.
            BluetoothGattService service = gatt.getService(UUID.fromString(FLICQ_SERVICE_GATT_UUID));
            helper.writeToUi("BLE : Found Service : " + service.getUuid().toString(), false);
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(
                    UUID.fromString("ffffffff-1111-1111-ccc0-000000000001"));
            gatt.setCharacteristicNotification(characteristic, true);
            helper.writeToUi("BLE : Found Characteristic : " + characteristic.getUuid(), false);

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(FLICQ_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            boolean success = gatt.writeDescriptor(descriptor);
            helper.writeToUi("BLE : Enable Notification for characteristic descriptor + " + descriptor.getUuid().toString() + ", Status = " + String.valueOf(success), false);
            helper.SetStatus(StatusType.INFO, "Ready to receive data");
        } catch (Exception ex) {
            Log.e("BLE", "Error in OnServices Discovered implementation. ");
            ex.printStackTrace();
        }
    }


    public static int i = 0;

    float[] values = new float[7];



    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (characteristic == null)
            return;

        final BluetoothGattDescriptor cccd = characteristic.getDescriptor(UUID.fromString(FLICQ_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID));
        final boolean notification = cccd == null || cccd.getValue() == null || cccd.getValue().length != 2 || cccd.getValue()[0] == 0x01;
        if(notification)
            AsyncSerialContentProcessor.Instance().Process(characteristic, helper);
    }
}