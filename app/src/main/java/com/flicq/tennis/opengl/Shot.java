package com.flicq.tennis.opengl;

import java.nio.ByteBuffer;
        import java.nio.ByteOrder;
        import java.nio.FloatBuffer;

        import javax.microedition.khronos.opengles.GL10;

class Shot {
    private final FloatBuffer vertexFloatBuffer; // Buffer for vertex-array

    private final float[] vertices = { // Vertices for top and bottom
            0.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            0.0f, 0.2f, 0.0f,
            0.0f, -0.2f, 0.0f,
            0.0f, 0.0f, 0.2f,
            0.0f, 0.0f, -0.2f
    };

    public Shot() {

        ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        vertexByteBuffer.order(ByteOrder.nativeOrder());
        vertexFloatBuffer = vertexByteBuffer.asFloatBuffer();
        vertexFloatBuffer.put(vertices);
        vertexFloatBuffer.position(0);
    }

    public void draw(GL10 gl, boolean asVector) {
    	gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        gl.glPushMatrix();
        
        gl.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
        // Point to our vertex buffer
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexFloatBuffer);
        // Draw the vertices as triangle strip
        if(asVector){
	        gl.glLineWidth(3);
	        gl.glDrawArrays(GL10.GL_LINES, 0, vertices.length / 3);
	        gl.glLineWidth(1);
        }
        else{
        	gl.glDrawArrays(GL10.GL_LINES, 0, 2);
        }
        gl.glPopMatrix();
        //Disable the client state before leaving
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }
    private void draw(GL10 gl) {
    	draw(gl);        
    }
}
