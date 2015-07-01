package com.flicq.tennis.ble;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import com.flicq.tennis.framework.Utils;
import com.flicq.tennis.framework.IActivityAdapter;
import com.flicq.tennis.framework.StatusType;
import com.flicq.tennis.test.LocalSensorDataSimulator;

public final class FlicqDevice
{
    IActivityAdapter activityAdapter;
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
        activityAdapter.writeToUi("BLE : LE Scan started", false);

        stopScanAndConnectGatt(callback, adapter);
    }

    private boolean stopRequested = false;
    public void requestStopScan()
    {
        stopRequested = true;
    }


    private void stopScanAndConnectGatt(final FlicqLeScanCallback callback,final BluetoothAdapter adapter)
    {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                while (true) {
                    if (callback.connectionSuccessful()) {
                        adapter.stopLeScan(callback);
                        activityAdapter.writeToUi("BLE : LE Scan stopped.", false);
                            BluetoothDevice device = callback.device();
                            device.connectGatt(activityAdapter.GetApplicationContext(), false, new FlicqBluetoothGattCallback(activityAdapter));
                        break;
                    }
                    Utils.SleepSomeTime(500);
                    activityAdapter.writeToUi("BLE : StopScan() : Poll conn. complete with 500ms interval", false);
                }
                return null;
            }
        }.execute();
    }
}
