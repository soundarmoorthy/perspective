package com.flicq.tennis.contentmanager;

import com.flicq.tennis.ble.FlicqBluetoothGattCallback;
import com.flicq.tennis.framework.IActivityAdapter;
import com.flicq.tennis.framework.StatusType;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by soundararajan on 5/10/2015.
 */

/*Creates a single-threaded executor that can invoke commands to execute in order.
(Note however that if this single thread terminates due to a failure during execution
prior to shutdown, a new one will take its place if needed to execute subsequent tasks.)
Tasks are guaranteed to execute sequentially, and no more than one task will be active
at any given time. Unlike the otherwise equivalent newScheduledThreadPool(1) the returned
executor is guaranteed not to be reconfigurable to use additional threads.*/
public class AsyncContentProcessor {

    private final ExecutorService executorQueue;
    private final IActivityAdapter adapter;
    private static int count; private long startTime;
    private long endTime;
    private final ContentStore store;

   public AsyncContentProcessor(IActivityAdapter varAdapter)
   {
       store = ContentStore.Instance();
       executorQueue = Executors.newSingleThreadExecutor();
       adapter = varAdapter;
   }

    public void connected() {
        count = 0;
        adapter.writeToUi("BLE : Connected Device");
    }

    public void disconnected()
    {

    }

    private void displayStats()
    {
        adapter.writeToUi("BLE : Disconnected Device ");
        adapter.writeToUi("BLE Report");
        adapter.writeToUi("-------------------------");
        adapter.writeToUi("Time taken : " + String.valueOf((endTime - startTime) / 1000) + " seconds");
        adapter.writeToUi("Packets Received : " + String.valueOf(count));
        adapter.writeToUi("-------------------------");
    }

    SensorData previous  = null;
    public void RunAsync(final short[] content) {
        executorQueue.submit(new Runnable() {
            @Override
            public void run() {
                SensorData current = new SensorData(content, previous);
                adapter.writeToUi(current.toString());
                store.Dump(current);
                count++;
                UpdateStatus();
                previous = current;
            }
        });
    }

    public void beginShot()
    {
        startTime = System.currentTimeMillis();
        store.NewShot();
    }

    public void endShot()
    {
        endTime = System.currentTimeMillis();
        store.ShotDone();
        displayStats();
    }

    private void UpdateStatus() {
        int percent = ((count * 100) / FlicqBluetoothGattCallback.END);
        if (percent > 100) percent = 100;
        adapter.SetStatus(StatusType.INFO, String.valueOf(percent) + " % capture completed");
    }
}
