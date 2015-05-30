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
                Log.e("BLE", String.valueOf(values[0]) + " " + String.valueOf(values[1]) + " " +
                        String.valueOf(values[2]) + " " + String.valueOf(values[3]) + " " +
                        String.valueOf(values[4]) + " " + String.valueOf(values[5]) + " " +
                        String.valueOf(values[6]) + " " + String.valueOf(values[7]));
                ContentStore.Instance().Dump(values);
            }
        });
    }

    //This will no longer be available when things work.
    private void TemporarySend()
    {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                FlicqCloudRequestHandler handler = new FlicqCloudRequestHandler();
                float[] shotData = SampleData.set;
//                for(int i=0;i< shotData.length /7; i++)
//                {
//                    float[] f = new float[7];
//                    for(int j=0;j<7;j++)
//                        f[j] = shotData[(i*7)+j];
//                    handler.SendCurrentShot(f);
//                    Log.i("Remaining : ", String.valueOf((shotData.length - i)/7));
//                }
                return null;
            }
        }.execute();
    }
}
