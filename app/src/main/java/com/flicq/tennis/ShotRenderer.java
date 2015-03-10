package com.flicq.tennis;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ShotRenderer implements GLSurfaceView.Renderer {

    FlicqDevice device;
    private int screenRotation = 0;
    private float[] rotationDegrees = {0.0f, 90.0f, 180.0f, 270.0f};
    private RotationVector rv = new RotationVector();
    private FlicqQuaternion q;
    Line line;

    // Constructor
    public ShotRenderer(FlicqDevice device, int screenRotation) {
        this.device = device;
        this.screenRotation = screenRotation;
        line = new Line();
        q = new FlicqQuaternion();
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if(height == 0) {
            height = 1;
        }
        gl.glClearColor(1.0f,1.0f,1.0f,0.5f);
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 60.0f, (float)width / (float)height, 0.1f, 30.0f);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    int i = 0;

    @Override
    public void onDrawFrame(GL10 gl) {

        gl.glClear(GL10.GL_DEPTH_BUFFER_BIT | GL10.GL_COLOR_BUFFER_BIT);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        if(!render)
            return;

        gl.glTranslatef(0.0f,0.0f, -10.0f);
        gl.glRotatef(rotationDegrees[this.screenRotation], 0, 0, 1);  // portrait/landscape rotation

        for (int i=0;i< SampleData.length();i++ ) {
                device.getData(rv, q);
                gl.glRotatef(rv.a, rv.x, rv.y, rv.z);
                line.draw(gl, i <= 400 && i >= 350);
        }
    }

    boolean render = false;

    public void enable()
    {
        render = true;
    }

    public void disable()
    {
        render = false;
    }
}