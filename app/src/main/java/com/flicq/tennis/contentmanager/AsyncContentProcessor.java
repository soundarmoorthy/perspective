package com.flicq.tennis.contentmanager;

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
    byte previous, count;
    long startTime, endTime;
    ContentStore store;

   public AsyncContentProcessor(IActivityAdapter varAdapter)
   {

       executorQueue = Executors.newSingleThreadExecutor();
       adapter = varAdapter;
       missingPackets = new LinkedList<Byte>();
       store = new ContentStore(varAdapter);
   }

    public void connected() {
        count = 0;
        previous = 0;
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
        for (int i = 0; i < missingPackets.size(); i++) {
            builder.append(String.valueOf(missingPackets.get(i) + 1) + ",");
        }

        adapter.writeToUi("Packets Received" + String.valueOf(count), false);
        adapter.writeToUi(builder.toString(), false);
        missingPackets.clear();
    }

    public void RunAsync(final long relativeTimeDiffInMilli,final byte[] content)
    {
        executorQueue.submit(new Runnable() {
            @Override
            public void run() {
                count++;
                String str = parse(content) + " -> " + relativeTimeDiffInMilli;
                if (content[5] - previous != 1)
                    missingPackets.add(previous);
                previous = content[5];
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

    private LinkedList<Byte> missingPackets;

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
