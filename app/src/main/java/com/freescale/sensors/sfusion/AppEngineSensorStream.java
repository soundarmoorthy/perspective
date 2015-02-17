
package com.freescale.sensors.sfusion;

        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.net.NetworkInfo;
        import android.os.Handler;
        import android.os.Message;
        import android.util.Log;

        import com.freescale.sensors.sfusion.A_FSL_Sensor_Demo.Algorithm;

        import java.io.IOException;
        import java.io.OutputStream;
        import java.net.NetworkInterface;
        import java.nio.ByteBuffer;
        import java.util.Set;
        import java.util.UUID;

/**
 * Created by soundararajan on 2/6/2015.
 */
public class AppEngineSensorStream extends SensorsWrapper{

    private boolean stream;
    static float[] quatInputs = null;

    private AppEngineSensorStream(A_FSL_Sensor_Demo demo) {
        super(demo);

         stream = false;
        float timeScale = 0.000001f; // 1.00 microseconds per tick
        acc.setTimeScale(timeScale);
        mag.setTimeScale(timeScale);
        gyro.setTimeScale(timeScale);
        quaternion.setTimeScale(timeScale);
        acc.setName("Accelerometer");
        mag.setName("Magnetometer");
        gyro.setName("Gyroscope");
        acc.setDescription("");
        mag.setDescription("");
        gyro.setDescription("");
        quaternion.setDescription("");
    }

    static private AppEngineSensorStream self;
    static public AppEngineSensorStream Instance(A_FSL_Sensor_Demo demo)
    {
        if(self == null){
            self = new AppEngineSensorStream(demo);
        }
        return self;
    }

    public boolean isListening() {
        return true;
    }

    public void start()
    {
         stream = true;
    }

    public void stop()
    {
         stream = false;
    }



}
