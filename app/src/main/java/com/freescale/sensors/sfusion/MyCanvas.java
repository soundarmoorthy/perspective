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
/**
 * This class implements graphics for basic "air mouse" features (which utilize
 * the standard sensor fusion quaternion output).  See also the VirtualPointer.java.
 * @author - Michael Stanley
 */
package com.freescale.sensors.sfusion;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;
import android.widget.LinearLayout;

public class MyCanvas extends View {
	private int x;  // only used for "mouse mode", same X,Y coordinate system as pointer
	private int y;  // only used for "mouse mode", same X,Y coordinate system as pointer
	private int left;
	private int top;
	private int bottom;
	private int height;
	private int width;
	private int originX;
	private int originY;
	private int halfWidth;
	private int halfHeight;
	private Paint paintBrushBlack = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint paintBrushTitle = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint paintBrushStdText = new Paint(Paint.ANTI_ALIAS_FLAG);
	private RectF rect;
	private DemoQuaternion q;
	private VirtualPointer pointer = null;
	private String str = null;
	private A_FSL_Sensor_Demo demo;

    private void compute_scale() {
		left = getLeft();
		bottom = getBottom();
		top = getTop();
		width = getWidth();
		height = getHeight();
		halfWidth = width/2;
		halfHeight = height/2;
		originX = left + halfWidth;
		originY = (top + bottom)/2;
		pointer.updateSensitivity(halfWidth, halfHeight); 
    }
	public MyCanvas(Context context) {
		super(context);
		x=y=0;
		demo = (A_FSL_Sensor_Demo) context;
		pointer = new VirtualPointer();
		q = new DemoQuaternion();
		str = new String();

		paintBrushBlack.setTextSize(24);
		paintBrushBlack.setColor(Color.WHITE);
		paintBrushBlack.setStrokeWidth(2);
		
		paintBrushTitle.setTypeface(Typeface.MONOSPACE);
		paintBrushTitle.setTextSize(34);
		paintBrushTitle.setColor(Color.WHITE);
		paintBrushTitle.setTextAlign(Paint.Align.CENTER);
		
		paintBrushStdText.setTextSize(16);
		paintBrushStdText.setColor(Color.WHITE);
		paintBrushStdText.setTextAlign(Paint.Align.CENTER);
				
		LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		this.setLayoutParams(tlp);
		this.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				center();			
			}
			
		});
	}
	private void updateCursor(Canvas canvas) {
		// converts from cartesian coordinates to canvas coordinates
		int radius=10;
		int canvasX = originX + x;
		int canvasY = originY - y;
		int left, top, right, bottom;
		left = canvasX + - radius;
		right = canvasX + radius;
		top = canvasY - radius;
		bottom = canvasY + radius;
		rect = new RectF(left, top, right, bottom);
		canvas.drawRect(rect, paintBrushStdText);
	}
	public void center() {
		A_FSL_Sensor_Demo.self.dataSelector.getQuaternion(q);
		pointer.center(q);
		x=y=0;
		invalidate();  // force re-draw
	}
	private void drawLabel(Canvas canvas) {
		int yPtr = originY-200;
		canvas.drawText("Wireless Pointer", originX, yPtr, paintBrushTitle);
		str = String.format("X=%6d Y=%6d", x, y);
		canvas.drawText(str, originX, yPtr+50, paintBrushTitle);		
		canvas.drawText("Touch screen to center cursor", originX, yPtr+100,  paintBrushTitle);						
		canvas.drawText("This function only works with sensor development boards.", originX, yPtr+150,  paintBrushStdText);
		canvas.drawText("Re-center after changing data Source.", originX, yPtr+175,  paintBrushStdText);		
	}
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		boolean mouseMode = !demo.absoluteModeRequired(); // is the demo configured for relative or absolute pointer?
		compute_scale();  // get screen dimensions and compute pointer sensitivity
		A_FSL_Sensor_Demo.self.dataSelector.getQuaternion(q); // update q with the current orientation quaternion
		pointer.update(q, mouseMode); // compute our new pointer values
		if (mouseMode) {
			// integrate in mouse mode
			x += pointer.x;
			y += pointer.y;			
		} else {
			// use as-is in absolute mode
			x = pointer.x;
			y = pointer.y;
		}
		x = MyUtils.limitI(x, halfWidth);  // limit X to +/- the width of the screen
		y = MyUtils.limitI(y, halfHeight); // limit Y to +/- the width of the screen
		drawLabel(canvas); // writes X & Y values to screen
		updateCursor(canvas); // draws the cursor
		demo.makeConsolesStale();  // renders screen consoles stale - not mouse related
	}

}
