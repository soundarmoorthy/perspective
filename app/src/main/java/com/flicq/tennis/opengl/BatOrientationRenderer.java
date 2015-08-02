package com.flicq.tennis.opengl;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by minion on 20/7/15.
 */
public class BatOrientationRenderer extends AbstractRenderer implements GLSurfaceView.Renderer {

   public BatOrientationRenderer(int screenRotation) {
        super(screenRotation);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
     super.onSurfaceCreatedBase(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
     super.onSurfaceChangedBase(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
     super.onDrawFrameBase(gl);
    }
}
