package com.flicq.tennis.contentmanager;

import android.os.AsyncTask;
import android.util.Log;

import com.flicq.tennis.appengine.FlicqCloudRequestHandler;
import com.flicq.tennis.framework.SampleData;

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
