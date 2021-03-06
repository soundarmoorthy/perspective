package com.flicq.tennis.ble;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import com.flicq.tennis.framework.Utils;
import com.flicq.tennis.framework.IActivityAdapter;
import com.flicq.tennis.framework.StatusType;

public final class FlicqDevice
{
    private final IActivityAdapter activityAdapter;
    public FlicqDevice(IActivityAdapter activityAdapter)
    {
        this.activityAdapter = activityAdapter;
    }
    public void OnBluetoothAdapterInitialized(final BluetoothAdapter adapter) {
        if (adapter == null) {
            activityAdapter.SetStatus(StatusType.ERROR, "NO BLE SUPPORT");
            return;
        }
        initializeDeviceMode(adapter);
    }

    private void initializeDeviceMode(BluetoothAdapter adapter) {
        activityAdapter.SetStatus(StatusType.INFO, "Wait !, Finding a Flicq Device");
        final FlicqLeScanCallback callback = new FlicqLeScanCallback(activityAdapter);
        //UUID []flicqServiceUUID = {UUID.fromString(FlicqBluetoothGattCallback.FLICQ_SERVICE_GATT_UUID)};
        //adapter.startLeScan(flicqServiceUUID, callback);
        adapter.startLeScan(callback);
        activityAdapter.writeToUi("BLE : LE Scan started");

        stopScanAndConnectGatt(callback, adapter);
    }

    public void requestStopScan()
    {
        boolean stopRequested = true;
    }


    private void stopScanAndConnectGatt(final FlicqLeScanCallback callback,final BluetoothAdapter adapter)
    {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                while (true) {
                    if (callback.connectionSuccessful()) {
                        adapter.stopLeScan(callback);
                        activityAdapter.writeToUi("BLE : LE Scan stopped.");
                            BluetoothDevice device = callback.device();
                            device.connectGatt(activityAdapter.GetApplicationContext(), false, new FlicqBluetoothGattCallback(activityAdapter));
                        break;
                    }
                    Utils.SleepSomeTime(500);
                    activityAdapter.writeToUi("BLE : StopScan() : Poll conn. complete with 500ms interval");
                }
                return null;
            }
        }.execute();
    }
}
