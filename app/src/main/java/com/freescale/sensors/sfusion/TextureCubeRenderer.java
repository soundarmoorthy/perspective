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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

import com.freescale.sensors.sfusion.A_FSL_Sensor_Demo.GuiState;

/**
* Renders cubes defined by the TextureCube class.  Used for both device and panorama views.
* @author Michael Stanley
*/
public class TextureCubeRenderer implements GLSurfaceView.Renderer {
   
   private TextureCube[] cubes = new TextureCube[20];
   private A_FSL_Sensor_Demo demo;
   private int screenHeight;
   private int screenWidth;
   private int screenRotation = 0;
   private int lastCube = -1;
   private int firstDisplayedCube=-1;
   private int lastDisplayedCube=-1;
   private float[] rotationDegrees = {0.0f, 90.0f, 180.0f, 270.0f};
   private RotationVector rv = new RotationVector();

   // Constructor
   public TextureCubeRenderer(A_FSL_Sensor_Demo demo, int screenRotation) {
	   assert((screenRotation>=0)&&(screenRotation<=3));
	   this.demo = demo;   
	   this.screenRotation = screenRotation;
   }
   public void addCube(int[] surfaces, float[] dimensions, String desc) {
	   this.lastCube = this.lastCube+1;
	   this.cubes[this.lastCube] = new TextureCube(surfaces, dimensions, desc);
	   this.firstDisplayedCube = this.lastCube; // default value
	   this.lastDisplayedCube = this.lastCube; // default value
   }
   public boolean selectCube(int choice) {
	   boolean sts=false;
	   if (choice<0) {
		   Log.e(A_FSL_Sensor_Demo.LOG_TAG, "ERROR in selectCube() function call.  New cube choice for = " + choice + "\n");
	   } else if ((choice==this.firstDisplayedCube)&&(choice==this.lastDisplayedCube)) {
		   // no change
		   sts = true;
	   } else {
		   sts = (choice<=this.lastCube);
		   if (sts) {
			   this.firstDisplayedCube = choice;
			   this.lastDisplayedCube = choice;			   
		   }
	   }
	   return(sts);
   }
   public boolean selectCubes(int first, int last) {
	   boolean sts = ((first<=this.lastCube) && (last<=this.lastCube));
	   if (sts) {
		   this.firstDisplayedCube = first;
		   this.lastDisplayedCube = last;
	   }
	   return(sts);
   }
   // Call back when the surface is first created or re-created.
   @Override
   public void onSurfaceCreated(GL10 gl, EGLConfig config) {
	  TextureCube c=null;
      gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); // specify clear values for the color buffer
      gl.glClearDepthf(1.0f);            // Set depth's clear-value to farthest
      gl.glEnable(GL10.GL_DEPTH_TEST);   // Enables depth-buffer for hidden surface removal
      gl.glDepthFunc(GL10.GL_LEQUAL);    // The type of depth testing to do
      gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);  // nice perspective view
  
      // Setup Texture, each time the surface is created
      for (int i = 0; i<= this.lastCube; i++) {
    	  c = this.cubes[i];
    	  c.loadTextures(gl, demo);
     }
   }
   
   // Call back after onSurfaceCreated() or whenever the window's size changes.
   @Override
   public void onSurfaceChanged(GL10 gl, int width, int height) {
   	/* Called when the surface changed size. 
   	 * Called after the surface is created and whenever the OpenGL ES surface size changes. 
   	 * Typically you will set your viewport here. If your camera is fixed then you could also set your projection matrix here
   	 */
   	this.screenWidth = width;
   	this.screenHeight = height;
   	gl.glMatrixMode(GL10.GL_PROJECTION); // specify which matrix is the current matrix
   	gl.glLoadIdentity();
   	float ratio = (float) width / height;
   	
   	/* GLU.gluPerspective sets up a perspective projection matrix.  Parameters: 
   	 * gl 	: a GL10 interface 
   	 * fovy : specifies the field of view angle, in degrees, in the Y direction. 
   	 * aspect:  specifies the aspect ratio that determines the field of view in the x direction. 
   	 * 			The aspect ratio is the ratio of x (screenWidth) to y (screenHeight). 
   	 * zNear: specifies the distance from the viewer to the near clipping plane (always positive). 
   	 * zFar : specifies the distance from the viewer to the far clipping plane (always positive).  
   	 */
   	GLU.gluPerspective(gl, 60.0f, ratio , 0.1f, 30.0f);
   	
   	/* void glViewport(GLint x, GLint y, GLsizei screenWidth, GLsizei screenHeight)
   	 * x, y : Specify the lower left corner of the viewport rectangle, in pixels. The initial value is (0, 0).
   	 * screenWidth, screenHeight : 	Specify the screenWidth and screenHeight of the viewport. When a GL context is first attached to a 
   	 * 					surface (e.g. window), screenWidth and screenHeight are set to the dimensions of that surface.
   	 */
   	gl.glViewport(0, 0, width, height);

   	gl.glMatrixMode(GL10.GL_MODELVIEW);
   	gl.glLoadIdentity();
   }
  
   // Call back to draw the current frame.
   @Override
   public void onDrawFrame(GL10 gl) {
	   gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);  // clear buffers to preset values      
	   gl.glMatrixMode(GL10.GL_MODELVIEW);
	   gl.glLoadIdentity();  // replace the current matrix with the identity matrix

	   demo.dataSelector.getData(rv, this.screenRotation);  // screenRotation only affects fixed rotations
	   
	   if (this.lastDisplayedCube<0) {
		   A_FSL_Sensor_Demo.write(null, "problem found in onDrawFrame\n");
	   }
		   
	   gl.glTranslatef(0.0f, 0.0f, 2*cubes[this.lastDisplayedCube].offset);   // Translate into the screen
	   gl.glRotatef(rotationDegrees[this.screenRotation], 0, 0, 1);  // portrait/landscape rotation
	   gl.glRotatef(rv.a, rv.x, rv.y, rv.z); // parameters are angle and axis of rotation  


	   // Do fixed corrections based on portrait/landscape and Device/Panorama
	   if (demo.guiState == GuiState.DEVICE) {
			switch (demo.dataSource) {
			case LOCAL:
			case REMOTE: 
				break;
			case STOPPED:
			case FIXED: 
				if (this.screenRotation==0) {
				} else {
					gl.glRotatef(+90.0f, 0.0f, 0.0f, 1.0f);  // correct for portrait
				}
			}		   
	   } else { // Panorama mode
			switch (demo.dataSource) {
			case LOCAL:
			case REMOTE: 
					gl.glRotatef(-90.0f, 0.0f, 0.0f, 1.0f);  // correct for portrait
				break;
			case STOPPED:
			case FIXED: 
				if (this.screenRotation==0) {
					gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);  // correct for landscape
				} else {
					gl.glRotatef(-90.0f, 0.0f, 1.0f, 0.0f);  // correct for portrait
				}
			}
	   }
	   for (int i = this.firstDisplayedCube; i<= this.lastDisplayedCube; i++) {
		   this.cubes[i].draw(gl); 
	   }

	   demo.makeConsolesStale();

   }


}