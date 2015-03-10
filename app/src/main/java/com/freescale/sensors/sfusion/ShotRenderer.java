/*
Copyright (c) 2013, 2014, Freescale Semiconductor, Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
 * Neither the name of Freescale Semiconductor, Inc. nor the
   names of its contributors may be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL FREESCALE SEMICONDUCTOR, INC. BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.freescale.sensors.sfusion;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Renders cubes defined by the Line class.  Used for both device and panorama views.
 *
 * @author Michael Stanley
 */
public class ShotRenderer implements GLSurfaceView.Renderer {

    private Line[] lines;
    private Line line;
    private Line line2;
    FlicqDevice device;
    private int screenHeight;
    private int screenWidth;
    private int screenRotation = 0;
    private float[] rotationDegrees = {0.0f, 90.0f, 180.0f, 270.0f};
    private RotationVector rv = new RotationVector();
    private FlicqQuaternion q;

    // Constructor
    public ShotRenderer(FlicqDevice device, int screenRotation) {
        this.device = device;
        this.screenRotation = screenRotation;
        this.lines = new Line[256];
        line = new Line();
        line2 = new Line();
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

        device.getData(rv, q);
        gl.glTranslatef(1.0f, 1.0f, -15.0f);
        gl.glRotatef(rotationDegrees[this.screenRotation], 0, 0, 1);  // portrait/landscape rotation
        gl.glRotatef(rv.a, rv.x, rv.y, rv.z);
        line.draw(gl);
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