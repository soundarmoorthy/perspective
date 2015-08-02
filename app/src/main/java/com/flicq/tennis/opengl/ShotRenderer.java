

package com.flicq.tennis.opengl;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Environment;

import com.flicq.tennis.contentmanager.ContentStore;
import com.flicq.tennis.contentmanager.SensorData;
import com.flicq.tennis.test.LocalSensorDataSimulator;


public class ShotRenderer extends AbstractRenderer implements GLSurfaceView.Renderer {

    private final Line line;
    private final Grid grid;
    private final Axis axis;
    private final Helper helper;
    private final boolean animation_use = false;
    private final boolean animation_play = false;

    public ShotRenderer(int screenRotation) {
        super(screenRotation);
        line = new Line();
        grid = new Grid();
        axis = new Axis();
        helper = new Helper();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //Setup
        super.onSurfaceCreatedBase(gl, config);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChangedBase(gl, width, height);
    }

    private final float[] matrix = {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0};
    private int animation = 0;

    private void renderFusionData(GL10 gl, List<SensorData> sensorData, int frame) {
        int count = sensorData.size();
        float x = 0.0f, y = 0.0f, z = 0.0f;
        float vx = 0f, vy = 0f, vz = 0;
        float[] pointData = new float[count * 6 * 2];
        for (int i = 0; i < count; i++) {

            SensorData data = sensorData.get(i);
            gl.glPushMatrix();

            float q0 = data.getQ0();
            float q1 = data.getQ1();
            float q2 = data.getQ2();
            float q3 = data.getQ3();

            float x2 = q0 * q0;
            float y2 = q1 * q1;
            float z2 = q2 * q2;
            float xy = q0 * q1;
            float xz = q0 * q2;
            float yz = q1 * q2;
            float wx = q3 * q0;
            float wy = q3 * q1;
            float wz = q3 * q2;

            matrix[0] = 1.0f - 2.0f * (y2 + z2);
            matrix[1] = 2.0f * (xy - wz);
            matrix[2] = 2.0f * (xz + wy);
            matrix[3] = 0.0f;

            matrix[4] = 2.0f * (xy + wz);
            matrix[5] = 1.0f - 2.0f * (x2 + z2);
            matrix[6] = 2.0f * (yz - wx);
            matrix[7] = 0.0f;

            matrix[8] = 2.0f * (xz - wy);
            matrix[9] = 2.0f * (yz + wx);
            matrix[10] = 1.0f - 2.0f * (x2 + y2);
            matrix[11] = 0.0f;

            float ax = data.getX();
            float ay = data.getY();
            float az = data.getZ();

            float qax = ax * matrix[0] + ay * matrix[4] + az * matrix[8];
            float qay = ax * matrix[1] + ay * matrix[5] + az * matrix[9];
            float qaz = ax * matrix[2] + ay * matrix[6] + az * matrix[10];

            ax = qax;
            ay = qay;
            az = qaz;
            vx += ax;
            vy += ay;
            vz += az;
            float friction = 0.8f;
            vx *= friction;
            vy *= friction;
            vz *= friction;

            x += vx * 0.01f;
            y += vy * 0.01f;
            z += vz * 0.01f;

            pointData[i * 6 + 0] = x;
            pointData[i * 6 + 1] = y;
            pointData[i * 6 + 2] = z;
            pointData[i * 6 + 3] = x + matrix[0];
            pointData[i * 6 + 4] = y + matrix[1];
            pointData[i * 6 + 5] = z + matrix[2];
            matrix[12] = x;
            matrix[13] = y;
            matrix[14] = z;
            matrix[15] = 1.0f;

            gl.glMultMatrixf(matrix, 0);
            if (i == frame) {
                line.draw(gl);
            }
            gl.glPopMatrix();


            if (i == frame) {
                helper.draw(gl, xf, yf, z);
            }
        }

        //The first couple of data are not so nice.
        float[] strippedContent = Arrays.copyOfRange(pointData, (4 * 6), pointData.length);
        if (frame == -1) {
            gl.glEnable(GL10.GL_BLEND);
            gl.glDisable(GL10.GL_DEPTH_TEST);
            gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(strippedContent.length * 4);
            vertexByteBuffer.order(ByteOrder.nativeOrder());
            FloatBuffer vertexFloatBuffer = vertexByteBuffer.asFloatBuffer();
            vertexFloatBuffer.put(strippedContent);
            vertexFloatBuffer.position(0);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexFloatBuffer);
            gl.glColor4f(0.5f, 0.7f, 1.0f, 0.5f);
            gl.glEnable(GL10.GL_POLYGON_OFFSET_FILL);
            gl.glPolygonOffset(1, 1);
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, count);
            gl.glDisable(GL10.GL_POLYGON_OFFSET_FILL);
            gl.glEnable(GL10.GL_DEPTH_TEST);
            gl.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
            gl.glDisable(GL10.GL_BLEND);
            //gl.glDrawArrays(GL10.GL_LINES, 0, count);

            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrameBase(gl);
        render =false;

        if (ContentStore.Instance() != null ) {
            if (ContentStore.Instance().getShot() != null) {
                List<SensorData>  data = ContentStore.Instance().getShot().getDataForRendering();
                if (data != null) {
                    SetData(data);
                    render = true;
                }
            }
        }

        //grid.draw(gl);
        axis.draw(gl);

        if (!render)
            return;

        if (animation_use) {
            int i = (animation) % set.size();
            if (animation_play)
                animation++;
            renderFusionData(gl, set, i);
        } else
            renderFusionData(gl, set, -1);

        if (this.screenshot_request) {
            take_screenshot(gl);
            this.screenshot_request = false;
        }
    }
    private boolean render = false;
}
