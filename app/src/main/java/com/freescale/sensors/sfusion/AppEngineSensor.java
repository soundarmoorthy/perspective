
package com.freescale.sensors.sfusion;

/**
 * Created by soundararajan on 2/6/2015.
 */
public class AppEngineSensor extends SensorsWrapper{

    private boolean stream;
    static float[] quatInputs = null;

    private AppEngineSensor(FlicqActivity demo) {
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
    }

    static private AppEngineSensor self;
    static public AppEngineSensor Instance(FlicqActivity demo)
    {
        if(self == null){
            self = new AppEngineSensor(demo);
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
