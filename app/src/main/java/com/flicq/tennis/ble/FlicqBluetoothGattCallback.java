package com.flicq.tennis.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.flicq.tennis.contentmanager.AsyncContentProcessor;
import com.flicq.tennis.framework.IActivityAdapter;
import com.flicq.tennis.framework.Utils;

import java.util.Calendar;
import java.util.UUID;

/**
 * Created by soundararajan on 4/13/2015.
 */

public class FlicqBluetoothGattCallback extends android.bluetooth.BluetoothGattCallback {

    IActivityAdapter helper;
    AsyncContentProcessor processor;

    public FlicqBluetoothGattCallback(IActivityAdapter helper) {
        this.helper = helper;
        processor = new AsyncContentProcessor(helper);
    }

    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            processor.connected();
            Utils.SleepSomeTime(20);
            gatt.discoverServices();
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            processor.disconnected();
            gatt.close();
        }
    }

    public static final String FLICQ_SERVICE_GATT_UUID = "ffffffff-1111-1111-ccc0-000000000000";
    public static final String FLICQ_SENSOR_DATA_CHARACTERISTICC ="ffffffff-1111-1111-ccc0-000000000001";
    public static final String FLICQ_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        try {
            //Try to first initialize device and gatt database.
            BluetoothGattService service = gatt.getService(UUID.fromString(FLICQ_SERVICE_GATT_UUID));
            helper.writeToUi("BLE : Found Service : " + service.toString(), false);
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(
                    UUID.fromString(FLICQ_SENSOR_DATA_CHARACTERISTICC));
            gatt.setCharacteristicNotification(characteristic, true);
            helper.writeToUi("BLE : Found Characteristic : " + characteristic.getUuid(), false);

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(FLICQ_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            boolean success = gatt.writeDescriptor(descriptor);
            helper.writeToUi("BLE : Enable Notification + " + descriptor.toString() + ", Status = " + String.valueOf(success), false);
        } catch (Exception ex) {
            Log.e("BLE", "Error in OnServices Discovered implementation. ");
            ex.printStackTrace();
        }
    }

    long previous = 0, current;
    private static final int BEGIN=1;
    private static final int END=250;
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (characteristic == null)
            return;
        current = Calendar.getInstance().getTimeInMillis();
        byte[] content = characteristic.getValue();
        byte[] copied = new byte[content.length];
        for (int i = 0; i < content.length; i++)
            copied[i] = content[i];

       int seqNum  = -1;//TODO : Find which index has seq number
        if(seqNum == BEGIN)
            processor.beginShot();
        else if(seqNum == END)
            processor.endShot();
        processor.RunAsync(current - previous, copied);
        previous = current;
    }
}