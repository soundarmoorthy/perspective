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
        for(int i=0;i<4;i++)
        {
            old_vector[i] = 0.0f;
        }
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

    private float old_vector[] = new float[4];
    public void RunAsync(final short[] content) {
        executorQueue.submit(new Runnable() {
            @Override
            public void run() {
                UpdateStatus();

        /*  Remember, 2 bytes each
            ------------------------------------------------------------------
           | ax.2 | ay.2 | az.2 | q0.2 | q1.2 | q2.2 | q3.2 | seqNo.1 | n/a |
           ------------------------------------------------------------------ */
                //Android (a x=East, y=North, z=Up or ENU standard.

                float[] curr_vector = new float[content.length];
                final float acc_lsb = 0.00012207f;
                curr_vector[0] = content[0] * acc_lsb;
                curr_vector[1] = content[1] * acc_lsb;
                curr_vector[2] = content[2] * acc_lsb;
                final float quaternionTimeScale = 30000f;
                for (int i = 3; i < 7; i++)
                    curr_vector[i] = content[i] / quaternionTimeScale;

                float normalize = 0.0f;
                for (int i = 3; i < 7; i++)
                    normalize = normalize + (curr_vector[i] * curr_vector[i]);
                normalize = FloatMath.sqrt(normalize);
                for (int i = 3; i < 7; i++)
                    curr_vector[i] /= normalize;

                /*
                Apply a low pass filter to the input data, there is some noise with the data.
                For details on why the coefficient and the formula are designed as below refer
                https://docs.google.com/a/flicq.com/spreadsheets/d/1dJ7NqB53NAxAjDWXhSqR6GC9I7OR9dAnqYcR0ZxxliU/edit?usp=sharing
                You should have a authorized flicq account to see the above document.
                */
                final float filterCoefficient = 0.9f;
                for(int i=3,k=0;i < 7; i++, k++)
                {
                    //Don't do this for the first data point that we get.
                    if(count !=0) {
                        curr_vector[i] = ((1 - filterCoefficient) * curr_vector[i]) + (filterCoefficient * old_vector[k]);
                        old_vector[k] = curr_vector[i]; // for next sample
                    }
                }

                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < 7; i++)
                    builder.append(String.valueOf(curr_vector[i])).append(",");
                adapter.writeToUi(builder.toString());
                store.Dump(curr_vector);
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
