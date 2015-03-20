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
        gl.glClearColor(1.0f, 1.0f, 1.0f, 0.1f);
        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

    }

    final float idealZ = -6.0f;
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if(height == 0) {
            height = 1;
        }
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        float ratio = (float)width / height;
        GLU.gluPerspective(gl, 60.0f, ratio, 0.1f, 30.0f);
        gl.glViewport(0, 0, width, height);
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

        gl.glTranslatef(0.0f,0.0f, idealZ);
        gl.glRotatef(rotationDegrees[this.screenRotation], 0, 0, 1);  // portrait/landscape rotation

        for (int i=0;i< SampleData.length();i++ ) {
            device.getData(rv, q);
            gl.glPushMatrix();

            gl.glTranslatef(rv.x,rv.y,idealZ+rv.z);
            gl.glRotatef(rv.a, rv.x, rv.y, rv.z);
            line.draw(gl, i <= 400 && i >= 350);
            gl.glPopMatrix();
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