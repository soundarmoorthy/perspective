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

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
* An encapsulation of the standard Android WebView class.  This class provides gesture 
* recognition, page navigation and other functions.
* @author Michael Stanley
*/
public class MyWebView extends WebView {
	private static float SWIPE_MIN_DISTANCE = 200; // gets updated by updateBrowserDimensions();
	private static float SWIPE_MAX_OFF_PATH = 150; // gets updated by updateBrowserDimensions();
	private static float SWIPE_THRESHOLD_VELOCITY = 200;
	private static float LEFT_START = 100;
	private static float RIGHT_START = 500;
	static MyWebView self;
	GestureDetector gd;

	 public void updateBrowserDimensions() {
		float width = this.getWidth();
		float height = this.getHeight();
		MyWebView.SWIPE_MIN_DISTANCE = width/3;
		MyWebView.SWIPE_MAX_OFF_PATH = height/5;
		MyWebView.LEFT_START = width/10;
		MyWebView.RIGHT_START = 0.9f*width;
	}
	public MyWebView(Context context, AttributeSet attributes, int defStyle) {
		super(context, attributes, defStyle);
		self=this;
		gd = new GestureDetector(new SwipeDetector());
		configure(false);
	}

	public MyWebView(Context context, AttributeSet attributes) {
		super(context, attributes);
		self=this;
		gd = new GestureDetector(new SwipeDetector());
		configure(false);
	}

	public MyWebView(Context context, Boolean enableJavascript) {
		super(context);
		self=this;
		gd = new GestureDetector(new SwipeDetector());
		configure(enableJavascript);
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void configure (Boolean enableJavascript) {
		if (enableJavascript) {
			getSettings().setJavaScriptEnabled(true);
		}
		getSettings().setBuiltInZoomControls(true);
		getSettings().setDefaultFontSize(20);
		getSettings().setLoadsImagesAutomatically(true);
		getSettings().setAllowFileAccess(true);
		getSettings().setSaveFormData(true);
		getSettings().setSupportMultipleWindows(true);
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return (gd.onTouchEvent(event) || super.onTouchEvent(event));
	}

	private String getNextPage(String url) {
		int i;
		String retStr = null;
		String[] pages = A_FSL_Sensor_Demo.urls();
		int lastPage = A_FSL_Sensor_Demo.finalPageInRotation();
		for (i = 0; i <= lastPage; i++) {
			if (url.equals(pages[i])) {
				if (i == lastPage) {
					retStr = pages[0];
				} else {
					retStr = pages[i + 1];
				}
				break;
			}
		}
		return (retStr);
	}

	private String getPrevPage(String url) {
		int i;
		String retStr = null;
		String[] pages = A_FSL_Sensor_Demo.urls();
		int lastPage = A_FSL_Sensor_Demo.finalPageInRotation();
		for (i = 0; i <= lastPage; i++) {
			if (url.equals(pages[i])) {
				if (i == 0) {
					retStr = pages[lastPage]; 
				} else {
					retStr = pages[i - 1];
				}
				break;
			}
		}
		return (retStr);
	}

	class SwipeDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			float e1x, e2x;
			updateBrowserDimensions();
			try {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
					return false;
				}
				e1x = e1.getX();
				e2x = e2.getX();
				// right to left swipe
				float distance = Math.abs( e1x - e2x );
				boolean started_at_edge = ((e1x < LEFT_START)||(e1x > RIGHT_START));
				boolean far_enough = (distance>SWIPE_MIN_DISTANCE);
				boolean fast_enough = (Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY);
				boolean swipe_right = (e1x < e2x);
				if (started_at_edge && far_enough && fast_enough) {
					if (swipe_right) {
						String currentPage = self.getUrl();
						String prevPage = getPrevPage(currentPage);
						if (prevPage != null) {
							self.loadUrl(prevPage);
						} else {
							self.goBack();
						}
						return true;
					} else {
						String currentPage = self.getUrl();
						String nextPage = getNextPage(currentPage);
						if (nextPage!=null) {
							self.loadUrl(nextPage);
						} else {
							self.goForward();
						}
						return true;
					}
				}
			} catch (Exception e) {
				// Do nothing
			}
			return false;
		}
	};



}
