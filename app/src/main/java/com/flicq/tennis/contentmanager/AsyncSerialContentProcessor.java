package com.flicq.tennis.contentmanager;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.flicq.tennis.framework.IActivityAdapter;
import com.flicq.tennis.framework.StatusType;

import java.util.Calendar;
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
public class AsyncSerialContentProcessor {
   private AsyncSerialContentProcessor() {
       executorQueue = Executors.newSingleThreadExecutor();
   }

    static AsyncSerialContentProcessor state;
    public static AsyncSerialContentProcessor Instance()
    {
        if(state == null)
            state = new AsyncSerialContentProcessor();
        return state;
    }

    ExecutorService executorQueue;

    byte previous = 0,current = 0;
    public void Process(final BluetoothGattCharacteristic characteristic, final IActivityAdapter adapter)
    {
        byte[] content = characteristic.getValue();
        String str = parse(content);
        adapter.writeToUi(str, false);
        UpdateStatus(adapter);
//        executorQueue.submit(new Runnable() {
//            @Override
//            public void run() {
//                byte[] content = characteristic.getValue();
//                String str = parse(content);
//                adapter.writeToUi(str, false);
//                UpdateStatus(adapter);
//            }
//        });
    }
    public static String parse(final byte[] data) {
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
    private void UpdateStatus(IActivityAdapter helper) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime - previousTime < 500) //Update only for once in 500 milliseconds.
            return;
        if (show)
            helper.SetStatus(StatusType.INFO, "Receiving data");
        else
            helper.SetStatus(StatusType.INFO, "");
        show = !show;
        previousTime = currentTime;
    }

    public void Process(final float[] values)
    {
        executorQueue.submit(new Runnable() {

            @Override
            public void run() {

                Log.e("BLE :", values[0] + "," + values[1] + "," + values[2] + "," + values[3] + "," + values[4] + "," + values[5] + "," + values[6]);
                //ContentStore.Instance().Dump(values);
            }
        });
    }
}
