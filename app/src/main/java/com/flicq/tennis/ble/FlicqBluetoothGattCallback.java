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

    IActivityAdapter activityAdapter;
    AsyncContentProcessor processor;

    public FlicqBluetoothGattCallback(IActivityAdapter helper) {
        this.activityAdapter = helper;
        processor = new AsyncContentProcessor(helper);
    }

    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            processor.connected();
            Utils.SleepSomeTime(20);
            gatt.discoverServices();
            enough = false;
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            processor.disconnected();
            endShotFormally(gatt);
            activityAdapter.onDisconnected();
        }
    }

    public static final String FLICQ_SERVICE_GATT_UUID = "ffffffff-1111-1111-ccc0-000000000000";
    public static final String FLICQ_SENSOR_DATA_CHARACTERISTICC = "ffffffff-1111-1111-ccc0-000000000001";
    public static final String FLICQ_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        try {
            //Try to first initialize device and gatt database.
            BluetoothGattService service = gatt.getService(UUID.fromString(FLICQ_SERVICE_GATT_UUID));
            activityAdapter.writeToUi("BLE : Found Service : " + service.toString(), false);
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(
                    UUID.fromString(FLICQ_SENSOR_DATA_CHARACTERISTICC));
            gatt.setCharacteristicNotification(characteristic, true);
            activityAdapter.writeToUi("BLE : Found Characteristic : " + characteristic.getUuid(), false);

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(FLICQ_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            boolean success = gatt.writeDescriptor(descriptor);
            activityAdapter.writeToUi("BLE : Enable Notification + " + descriptor.toString() + ", Status = " + String.valueOf(success), false);
            processor.beginShot();
        } catch (Exception ex) {
            Log.e("BLE", "Error in OnServices Discovered implementation. ");
            ex.printStackTrace();
        }
    }

    long previous = 0, current;
    public static final int END = 40;
    private boolean enough = false;
    private static final int PACKET_CONTENT_SIZE = 7;
    byte seqNum;

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (characteristic == null || enough)
            return;
        current = Calendar.getInstance().getTimeInMillis();

        /* packet size 16 bytes, packet content 15 bytes */
        /* ------------------------------------------------------------------
           | ax.2 | ay.2 | az.2 | q0.2 | q1.2 | q2.2 | q3.2 | seqNo.1 | n/a |
           ------------------------------------------------------------------ */
        short[] copied = new short[PACKET_CONTENT_SIZE];
        for (int i = 0; i < PACKET_CONTENT_SIZE; i++)
            copied[i] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, i * 2).shortValue();

        seqNum = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 14).byteValue(); //till 255
        if (seqNum >= END) {
            endShotFormally(gatt);
        }
        processor.RunAsync(current - previous, copied);
        previous = current;
    }

    private void endShotFormally(BluetoothGatt gatt) {
        enough = true;
        processor.endShot();
        gatt.disconnect();
        gatt.close();
        activityAdapter.writeToUi("BLE ; Disconnected after receiving all shot data", false);
        current = 0;
    }
}