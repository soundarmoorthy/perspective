package com.flicq.tennis.contentmanager;

import android.bluetooth.BluetoothGattCallback;

import com.flicq.tennis.ble.FlicqBluetoothGattCallback;
import com.flicq.tennis.framework.IActivityAdapter;
import com.flicq.tennis.framework.StatusType;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by soundararajan on 5/10/2015.
 */

/*Creates a single-threaded executor that can invoke commands to execute periodically.
(Note however that if this single thread terminates due to a failure during execution
prior to shutdown, a new one will take its place if needed to execute subsequent tasks.)
Tasks are guaranteed to execute sequentially, and no more than one task will be active
at any given time. Unlike the otherwise equivalent newScheduledThreadPool(1) the returned
executor is guaranteed not to be reconfigurable to use additional threads.*/
public class AsyncContentProcessor {

    ExecutorService executorQueue;
    private IActivityAdapter adapter;
    int count;
    long startTime, endTime;
    ContentStore store;

   public AsyncContentProcessor(IActivityAdapter varAdapter)
   {

       store = ContentStore.Instance();
       executorQueue = Executors.newSingleThreadExecutor();
       adapter = varAdapter;
   }

    public void connected() {
        count = 0;
        startTime = System.currentTimeMillis();
        adapter.writeToUi("BLE : Connected Device", false);
    }

    public void disconnected()
    {
        displayStats();
    }

    private void displayStats()
    {
        endTime = System.currentTimeMillis();
        adapter.writeToUi("BLE : Disconnected Device ", false);
        adapter.writeToUi("BLE Report", false);
        adapter.writeToUi("Time taken : " + String.valueOf((endTime - startTime) / 1000) + " seconds", false);
        adapter.writeToUi("Packets Received" + String.valueOf(count), false);
    }

    public void RunAsync(final long relativeTimeDiffInMilli,final byte[] content)
    {
        executorQueue.submit(new Runnable() {
            @Override
            public void run() {
                count++;
                String str = parse(content) + " -> " + relativeTimeDiffInMilli;
                adapter.writeToUi(str, false);
                UpdateStatus();

                float[] parsedContent = getParsedContent(content);
                store.Dump(parsedContent);
            }
        });
    }

    private static float[] getParsedContent(byte[] content) {
       float[] parsedData = new float[content.length /2];
        for(int i=0;i<parsedData.length;i++)
        {
            parsedData[i] = content[i] + (content[i+1] << 8);
        }
        return parsedData;
    }

    public void beginShot()
    {
        store.NewShot();
    }

    public void endShot()
    {
        store.ShotDone();
    }

    private static String parse(final byte[] data) {
        if (data == null || data.length == 0)
            return "";
        final char[] out = new char[data.length * 3 - 1];
        for (int j = 0; j < data.length; j++) {
            int v = data[j] & 0xFF;
            out[j * 3] = HEX_ARRAY[v >>> 4];
            out[j * 3 + 1] = HEX_ARRAY[v & 0x0F];
            if (j != data.length - 1)
                out[j * 3 + 2] = '-';
        }
        return new String(out);
    }
    final private static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private long previousTime = 0;
    private boolean show = true;
    private void UpdateStatus() {
        int percent = (count * 100) / FlicqBluetoothGattCallback.END;
        adapter.SetStatus(StatusType.INFO, String.valueOf(percent) + " % capture completed");
    }
}
