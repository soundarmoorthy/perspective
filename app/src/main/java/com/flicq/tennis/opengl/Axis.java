package com.flicq.tennis.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Axis {
    private FloatBuffer vertexFloatBuffer; // Buffer for vertex-array

    private float[] vertices = { // Vertices for top and bottom
    		10.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f,
            0.0f, 10.0f, 0.0f,
            0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 10.0f,
            0.0f, 0.0f, 0.0f
    };
    

    public Axis() {

        ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        vertexByteBuffer.order(ByteOrder.nativeOrder());
        vertexFloatBuffer = vertexByteBuffer.asFloatBuffer();
        vertexFloatBuffer.put(vertices);
        vertexFloatBuffer.position(0);
    }

    public void draw(GL10 gl) {

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        gl.glPushMatrix();
        
        // Point to our vertex buffer
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexFloatBuffer);
        // Draw the vertices as triangle strip
        gl.glLineWidth(3);
        gl.glColor4f(1.0f, 0.0f, 0.0f, 0.0f);
        gl.glDrawArrays(GL10.GL_LINES, 0, 2);
        gl.glColor4f(0.0f, 1.0f, 0.0f, 0.0f);
        gl.glDrawArrays(GL10.GL_LINES, 2, 2);
        gl.glColor4f(0.0f, 0.0f, 1.0f, 0.0f);
        gl.glDrawArrays(GL10.GL_LINES, 4, 2);
        gl.glLineWidth(1);
        gl.glScalef(-1.0f, -1.0f, -1.0f);
        gl.glColor4f(1.0f, 0.0f, 0.0f, 0.0f);
        gl.glDrawArrays(GL10.GL_LINES, 0, 2);
        gl.glColor4f(0.0f, 1.0f, 0.0f, 0.0f);
        gl.glDrawArrays(GL10.GL_LINES, 2, 2);
        gl.glColor4f(0.0f, 0.0f, 1.0f, 0.0f);
        gl.glDrawArrays(GL10.GL_LINES, 4, 2);
        gl.glPopMatrix();
        //Disable the client state before leaving
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

    }
}

