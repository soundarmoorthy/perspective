package com.flicq.tennis.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Grid {
    private FloatBuffer vertexFloatBuffer; // Buffer for vertex-array

    private float[] vertices = { // Vertices for top and bottom
    };

    public Grid() {
    	final int grid_line_count= 5 ; 
    	vertices = new float[grid_line_count*4*2];
    	for(int i=0;i<grid_line_count;i++){
    		vertices[i*8]=0.0f;
    		vertices[i*8+1]=i;
    		vertices[i*8+2]=grid_line_count;
    		vertices[i*8+3]=i;
    		vertices[i*8+4]=i;
    		vertices[i*8+5]=0;
    		vertices[i*8+6]=i;
    		vertices[i*8+7]=grid_line_count;
    	}
        ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        vertexByteBuffer.order(ByteOrder.nativeOrder());
        vertexFloatBuffer = vertexByteBuffer.asFloatBuffer();
        vertexFloatBuffer.put(vertices);
        vertexFloatBuffer.position(0);
    }

    public void draw(GL10 gl) {

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        gl.glPushMatrix();
        gl.glColor4f(0.5f, 0.5f, 0.5f, 0f);

        // Point to our vertex buffer
        gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexFloatBuffer);
        // Draw the vertices as triangle strip
        gl.glDrawArrays(GL10.GL_LINES, 0, vertices.length / 2);
        gl.glRotatef(90, 1, 0, 0);
        gl.glDrawArrays(GL10.GL_LINES, 0, vertices.length / 2);
        gl.glRotatef(90, 0, 1, 0);
        gl.glDrawArrays(GL10.GL_LINES, 0, vertices.length / 2);
        gl.glPopMatrix();
        //Disable the client state before leaving
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

    }
}

