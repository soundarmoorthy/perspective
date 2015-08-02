package com.flicq.tennis.opengl;

import android.graphics.Bitmap;
import android.opengl.GLU;
import android.os.Environment;

import com.flicq.tennis.contentmanager.SensorData;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by minion on 19/7/15.
 */
public class AbstractRenderer {

    protected int screenRotation;
    protected int width;
    protected int height;
    private final float[] rotationDegrees = {0.0f, 90.0f, 180.0f, 270.0f};
    private float deltaX;
    private float deltaY;
    private int cameraAngleX;
    private int cameraAngleY;

    protected float xf, yf;

    private float oldX;
    private float oldY;

    protected static final int CAMERA_ANGLE_X_RESET_VALUE = -45;
    protected static final int CAMERA_ANGLE_Y_RESET_VALUE = 45;
    protected static final float IDEAL_Z_RESET_VALUE = -6.0f;

    public float getIdealZ() {
        return idealZ;
    }

    public void setIdealZ(float idealZ) {
        this.idealZ = idealZ;
    }

    private float idealZ = IDEAL_Z_RESET_VALUE ;

    protected boolean screenshot_request = false;

    protected List<SensorData> set;

    protected void SetData(List<SensorData> set) {
        this.set = set;
    }


    protected AbstractRenderer(int screenRotation) {

        this.screenRotation = screenRotation;

        cameraAngleX = CAMERA_ANGLE_X_RESET_VALUE;
        cameraAngleY = CAMERA_ANGLE_Y_RESET_VALUE;
    }


    public void resetDeltaXY() {
        deltaX = 0.0f;
        deltaY = 0.0f;
    }

    public void setXY(float x, float y) {
        oldX = x;
        oldY = y;
    }

    public void move(float x, float y) {
        float _deltaX = (x - oldX);
        float _deltaY = (y - oldY);
        deltaX = _deltaX;
        deltaY = _deltaY;
        oldX = x;
        oldY = y;
    }

    public void resetView()
    {
        idealZ = IDEAL_Z_RESET_VALUE;
        cameraAngleX = CAMERA_ANGLE_X_RESET_VALUE;
        cameraAngleY = CAMERA_ANGLE_Y_RESET_VALUE;
    }


    protected void onDrawFrameBase(GL10 gl)
    {
        cameraAngleX += deltaX;
        cameraAngleY += deltaY;
        deltaX = 0;
        deltaY = 0;
        gl.glClear(GL10.GL_DEPTH_BUFFER_BIT | GL10.GL_COLOR_BUFFER_BIT);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslatef(0.0f, 0.0f, idealZ);
        gl.glRotatef(cameraAngleY, 1f, 0f, 0);
        gl.glRotatef(cameraAngleX, 0f, 1f, 0);

        gl.glRotatef(rotationDegrees[this.screenRotation], 0f, 0f, 1);  // portrait/landscape rotation

    }

    protected void take_screenshot(GL10 gl) {
        int screenshotSize = this.width * this.height;
        ByteBuffer bb = ByteBuffer.allocateDirect(screenshotSize * 4);
        bb.order(ByteOrder.nativeOrder());
        gl.glReadPixels(0, 0, width, height, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, bb);
        int pixelsBuffer[] = new int[screenshotSize];
        bb.asIntBuffer().get(pixelsBuffer);
        bb = null;

        for (int i = 0; i < screenshotSize; ++i) {
            // The alpha and green channels' positions are preserved while the red and blue are swapped
            pixelsBuffer[i] = ((pixelsBuffer[i] & 0xff00ff00)) | ((pixelsBuffer[i] & 0x000000ff) << 16) | ((pixelsBuffer[i] & 0x00ff0000) >> 16);
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixelsBuffer, screenshotSize - width, -width, 0, 0, width, height);
        // TODO Auto-generated method stub
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/fusion_screenshots");
        myDir.mkdirs();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date now = new Date();
        String fileName = formatter.format(now) + ".jpg";
        File file = new File(myDir, fileName);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out); // ERROR 341 LINE
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onSurfaceCreatedBase(GL10 gl, EGLConfig config) {
        gl.glClearColor(1.0f, 1.0f, 1.0f, 0.1f);
        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
    }

    protected void onSurfaceChangedBase(GL10 gl, int width, int height) {
        if (height == 0) {
            height = 1;
        }
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        float ratio = (float) width / height;
        GLU.gluPerspective(gl, 60.0f, ratio, 0.1f, 30.0f);
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        this.width = width;
        this.height = height;
    }
}
