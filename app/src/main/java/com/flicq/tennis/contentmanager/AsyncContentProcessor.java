package com.flicq.tennis.contentmanager;

import android.util.FloatMath;

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
    private int count;
    private long startTime;
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
        startTime = System.currentTimeMillis();
        adapter.writeToUi("BLE : Connected Device");
    }

    public void disconnected()
    {
        displayStats();
    }

    private void displayStats()
    {
        endTime = System.currentTimeMillis();
        adapter.writeToUi("BLE : Disconnected Device ");
        adapter.writeToUi("BLE Report");
        adapter.writeToUi("Time taken : " + String.valueOf((endTime - startTime) / 1000) + " seconds");
        adapter.writeToUi("Packets Received" + String.valueOf(count));
    }

    float cx,cy,cz;
    public void RunAsync(final short[] content) {
        executorQueue.submit(new Runnable() {
            @Override
            public void run() {
                UpdateStatus();

        /*  Remember, 2 bytes each
            ------------------------------------------------------------------
           | ax.2 | ay.2 | az.2 | q0.2 | q1.2 | q2.2 | q3.2 | seqNo.1 | n/a |
           ------------------------------------------------------------------ */
                //Android (a x=East, y=North, z=Up or ENU standard. Currently
                //not applicable.

                float[] curr_vector = new float[content.length];
                final float acc_lsb = 0.00012207f;
                if(count !=0) {
                    curr_vector[0] = (content[0] * acc_lsb) - cx;
                    curr_vector[1] = (content[1] * acc_lsb) - cy;
                    curr_vector[2] = (content[2] * acc_lsb) - cz;
                }

                cx = curr_vector[0];
                cy = curr_vector[1];
                cz = curr_vector[2];
                final float quaternionTimeScale = 30000f;
                for (int i = 3; i < 7; i++)
                    curr_vector[i] = content[i] / quaternionTimeScale;

                float normalize = 0.0f;
                for (int i = 3; i < 7; i++)
                    normalize = normalize + (curr_vector[i] * curr_vector[i]);
                normalize = FloatMath.sqrt(normalize);
                for (int i = 3; i < 7; i++)
                    curr_vector[i] /= normalize;

                SensorData sensorData = new SensorData(curr_vector);
                adapter.writeToUi(sensorData.toString());
                store.Dump(sensorData);
                count++;
            }
        });
    }

    public void beginShot()
    {
        store.NewShot();
    }

    public void endShot()
    {
        store.ShotDone();
    }

    private void UpdateStatus() {
        int percent = ((count * 100) / FlicqBluetoothGattCallback.END);
        if (percent > 100) percent = 100;
        adapter.SetStatus(StatusType.INFO, String.valueOf(percent) + " % capture completed");
    }
}
