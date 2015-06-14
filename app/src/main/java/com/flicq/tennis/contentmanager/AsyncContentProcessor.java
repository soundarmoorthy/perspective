package com.flicq.tennis.contentmanager;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.flicq.tennis.framework.IActivityAdapter;
import com.flicq.tennis.framework.StatusType;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by soundararajan on 5/10/2015.
 */

/*Creates a single-threaded executor that can invoke commands to run after a
given delay, or to execute periodically. (Note however that if this single thread
terminates due to a failure during execution prior to shutdown, a new one will take
its place if needed to execute subsequent tasks.) Tasks are guaranteed to execute sequentially,
and no more than one task will be active at any given time. Unlike the otherwise equivalent
newScheduledThreadPool(1) the returned executor is guaranteed not to be reconfigurable
to use additional threads.*/
public class AsyncContentProcessor {

    ExecutorService executorQueue;
    private IActivityAdapter adapter;
    byte previous, count;
    long startTime, endTime;

   public AsyncContentProcessor(IActivityAdapter varAdapter)
   {
       executorQueue = Executors.newSingleThreadExecutor();
       adapter = varAdapter;
       previous = 0;
       counters.clear();
   }

    public void connected()
    {
        if(counters == null)
            counters = new LinkedList<Byte>();
        count = 0;
        startTime = System.currentTimeMillis();
        adapter.writeToUi("BLE : Connected Device", false);
    }

    public void disconnected()
    {
        endTime = System.currentTimeMillis();
        adapter.writeToUi("BLE : Disconnected Device ", false);
        adapter.writeToUi("BLE Report", false);
        adapter.writeToUi("Time taken : " + String.valueOf((endTime - startTime) / 1000) + " seconds", false);

        StringBuilder builder = new StringBuilder();
        builder.append("Missing packets : ");
        for (int i = 0; i < counters.size(); i++) {
            builder.append(String.valueOf(counters.get(i) + 1) + ",");
        }

        adapter.writeToUi("Packets Received" + String.valueOf(count), false);
        adapter.writeToUi(builder.toString(), false);
        counters.clear();
    }

    public void RunAsync(final long relativeTimeDiffInMilli,final byte[] content)
    {
        executorQueue.submit(new Runnable() {
            @Override
            public void run() {
                String str = parse(content) + " -> " + relativeTimeDiffInMilli;
                if(content[5] - previous !=1)
                    counters.add(previous);
                previous = content[5];
                adapter.writeToUi(str, false);
                UpdateStatus(adapter);

            }
        });
    }

    private LinkedList<Byte> counters = new LinkedList<Byte>();

    public String parse(final byte[] data) {
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
    private void UpdateStatus(IActivityAdapter adapter) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime - previousTime < 500) //Update only for once in 500 milliseconds.
            return;
        if (show)
            adapter.SetStatus(StatusType.INFO, "Receiving data");
        else
            adapter.SetStatus(StatusType.INFO, "");
        show = !show;
        previousTime = currentTime;
    }
}
