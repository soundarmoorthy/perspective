package com.flicq.tennis.contentmanager;

import android.util.FloatMath;

/**
 * Created by minion on 2/8/15.
 */

public class FlicqSensorDataBuilder implements ISensorDataBuilder {

    private float[] acceleration;
    private float[] quaternion;

    private void init() {
        acceleration = new float[3];
        quaternion = new float[4];
    }


    short[] original;
    private static final float acc_lsb = 0.00012207f;
    private static final float quaternionTimeScale = 30000f;

    public FlicqSensorDataBuilder(short[] content) {
        this.original = content;
        init();
        /*  Remember, 2 bytes each
            ------------------------------------------------------------------
           | ax.2 | ay.2 | az.2 | q0.2 | q1.2 | q2.2 | q3.2 | seqNo.1 | n/a |
           ------------------------------------------------------------------ */
        //Android (a x=East, y=North, z=Up or ENU standard. Currently not applicable
        acceleration[0] = (content[0] * acc_lsb);
        acceleration[1] = (content[1] * acc_lsb);
        acceleration[2] = (content[2] * acc_lsb);

        float normalize = 0.0f;
        for (int i = 3, k = 0; i < 7; i++, k++) {
            quaternion[k] = content[i] / quaternionTimeScale;
            normalize = normalize + (quaternion[k] * quaternion[k]);
        }
        normalize = FloatMath.sqrt(normalize);

        for (int k = 0; k < 4; k++)
            quaternion[k] /= normalize;
    }

    @Override
    public float[] getAcceleration() {
        return acceleration;
    }

    @Override
    public float[] getQuaternion() {
        return quaternion;
    }

    @Override
    public String dump() {
        StringBuilder builder = new StringBuilder();

        for (short i : original)
            builder.append(Integer.toHexString(i)).append(",");

        for (float i : acceleration)
            builder.append(String.valueOf(i)).append(",");

        for (float i : quaternion)
            builder.append(String.valueOf(i)).append(",");

        return builder.toString();
    }
}
