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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.view.View;
//import android.content.Context;

/**
* Utility class responsible for creating graphic dump files in PNG format.
* <P>This class is loosely based upon code found at: http://www.anddev.org/opengl_performance_question-t751.html
* @author Michael Stanley
*/
public class GraphicScreenDump {

	static public Bitmap SavePixels(GL10 gl, int Width, int Height)
	{
		int b[] = new int[Width * Height];
		IntBuffer ib = IntBuffer.wrap(b);
		ib.position(0);
		gl.glReadPixels(0, 0, Width, Height, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);

		// The bytes within the ints are in the wrong order for android, but convert into a
		// bitmap anyway. They're also bottom-to-top rather than top-to-bottom. We'll fix
		// this up soon using some fast API calls.
		Bitmap glbitmap = Bitmap.createBitmap(b, Width, Height, Bitmap.Config.ARGB_4444);
		ib = null; // we're done with ib
		b = null; // we're done with b, so allow the memory to be freed

		final float[] cmVals = { 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0 };

		Paint paint = new Paint();
		paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(cmVals))); // our R<->B swapping paint

		Bitmap bitmap = Bitmap.createBitmap(Width, Height, Bitmap.Config.ARGB_4444); // the bitmap we're going to draw onto
		Canvas canvas = new Canvas(bitmap); // we draw to the bitmap through a canvas
		canvas.drawBitmap(glbitmap, 0, 0, paint); // draw the opengl bitmap onto the canvas, using the color swapping paint
		glbitmap = null; // we're done with glbitmap, let go of its memory

		// the image is still upside-down, so vertically flip it
		Matrix matrix = new Matrix();
		matrix.preScale(1.0f, -1.0f); // scaling: x = x, y = -y, i.e. vertically flip
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); // new bitmap, using the flipping matrix
	}

	public static Uri SavePNG(View topView, String name)
	{
		// This version gets only the Android elements without the OpenGL
		// contents - assumption is that the OpenGL frame buffer is not available
		// to the functions used.
		Bitmap bmp;
		topView.setDrawingCacheEnabled(true);
		bmp = Bitmap.createBitmap(topView.getDrawingCache());
		topView.setDrawingCacheEnabled(false);
		return(savedFile(bmp, name));
	}

	public static Uri SavePNG(GL10 gl, int width, int height, String name)
	{
		// This version only gets the contents of the OpenGL window
		Bitmap bmp=SavePixels(gl, width, height);
		return(savedFile(bmp, name));
	}
	private static Uri savedFile(Bitmap bmp, String name) {
		Uri uri = null;
		boolean passed = true;
		try {
			File f = MyUtils.getFile(name);
			String fullPath = f.getAbsolutePath();
			uri  = Uri.fromFile(f);
			FileOutputStream fos = new FileOutputStream(fullPath);
			bmp.compress(CompressFormat.PNG, 100, fos);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			passed = false;
		} 
		if (passed) return uri;
		return null;
	}
}
