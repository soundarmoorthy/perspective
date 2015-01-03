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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.freescale.sensors.sfusion.A_FSL_Sensor_Demo.Algorithm;
import com.freescale.sensors.sfusion.A_FSL_Sensor_Demo.GuiState;

/**
* Implements the command interpreter for Avnet's WiGo module.
* @author Michael Stanley
*/
class WiGo extends SensorsWrapper {
	static public boolean wiGoStarted = false;
	static String hexString = new String("");
	static private float timeScale = 0.02f; // 50Hz
	
	// Communication variables
	static private String ip = null;
	static private final int port = 15000;
	
	static private OutputStream outputStream = null;  
	static private DataInputStream dataInputStream = null;
	static private String LOG_TAG;
//	This is the Java equivalent of the format sent from the WiGo 
//	class Packet {
//			short packet_id;  
//			short light; // Light sensor
//			int timestamp; // Counts at a 0.020MS interval when sensor data is processed			
//			short acc_x, acc_y, acc_z; // Integer data from accelerometer	
//			short mag_x, mag_y, mag_z; // Integer data from Magnetometer
//			short roll, pitch, yaw, compass; // Roll, Pitch, Yaw and Compass from the eCompass algorithm, in degrees
//			short alt, temp; // Altitude in meters, and temperature in degrees C			
//			float fax, fay, faz; // Data from Accelerometer converted to floating point			
//			float fGax, fGay, fGaz; // Accelerometer data converted to G's			
//			float fmx, fmy, fmz; // Data from Magnetometer converted to floating point	
//			float fUTmx, fUTmy, fUTmz; // Magnetometer data converted to UT's
//			float q0, q1, q2, q3; // quaternion values
//	}
	public WiGo(A_FSL_Sensor_Demo demo) {
		super(demo);
		String defaultIpCode  = demo.getString(R.string.ipCode);
		WiGo.ip = demo.myPrefs.getString("ipCode", defaultIpCode);
		acc.setTimeScale(timeScale); 	
		mag.setTimeScale(timeScale); 	
		quaternion.setTimeScale(timeScale);
 		acc.setName("Accelerometer");
 		mag.setName("Magnetometer");
  		acc.setDescription("Remote acceleromter = Freescale MMA8451Q.\nConfiguration not available.\n");
 		mag.setDescription("Remote magnetometer = Freescale MAG3110.\nConfiguration not available.\n");
 		gyro.setDisabled();
 		quaternion.setDescription("Quaternion sensor fusion by Freescale Semiconductor.\nConfiguration not available");
	}
	public void stop() {
		WiGo.wiGoStarted = false;
		Log.i(WiGo.LOG_TAG, "Stop WiGo Comms\n");
	}

	Handler handler=new Handler() {
		@Override
		public void handleMessage(Message msg) {    		
			int what = msg.what;
			Object obj = msg.obj;
			byte[] buffer = (byte[]) obj;
			switch (what) {
			case 1:
				demo.developmentBoard = A_FSL_Sensor_Demo.DevelopmentBoard.WIGO;
				ByteBuffer bb = ByteBuffer.wrap(buffer);
				bb.order(null); // Little endian
				long timestamp = (long) bb.getInt(4);
				float ax = bb.getFloat(44);                //  4 bytes
				float ay = bb.getFloat(48);                //  4 bytes
				float az = bb.getFloat(52);                //  4 bytes
				float mx = bb.getFloat(68);                //  4 bytes
				float my = bb.getFloat(72);                //  4 bytes
				float mz = bb.getFloat(76);                //  4 bytes		
				float [] q = new float[4];
				q[0] = bb.getFloat(80);                //  4 bytes
				q[1] = bb.getFloat(84);                //  4 bytes
				q[2] = bb.getFloat(88);                //  4 bytes
				q[3] = bb.getFloat(92);                //  4 bytes	

				acc.update(timestamp, ax, ay, az);
				mag.update(timestamp, mx, my, mz);
				quaternion.set(timestamp, q);
				if (A_FSL_Sensor_Demo.hexDumpEnabled) {
					String str = "";
					for (int i=0; i<79; i++) str += String.format("%02x ", bb.get(i));
					str += String.format("%02x\n", bb.get(79));
					A_FSL_Sensor_Demo.write(false, str);
				}
				if (demo.legacyDumpEnabled) {
					dump_acc();
					dump_mag();
					dump_quaternion();
				}
				if (demo.csvDumpEnabled) {
					dump9AxisCsv();
				}
				if (demo.guiState==GuiState.CANVAS) {
					demo.canvasApplet.invalidate();
				}
				break;
			case 2: // after ten messages, send by byte
				break;
			default:
			}
		}
	};


	public void run() {
		if (!wiGoStarted) {
			wiGoStarted = true;
			A_FSL_Sensor_Demo.write(true, "About to initialize the network connection to the WiGo.\n");
			Log.i(A_FSL_Sensor_Demo.LOG_TAG, "About to initialize the network connection to the WiGo.\n");
			Thread background=new Thread(new Runnable() {
				public void run() {        
					byte[] buffer = new byte[96];
					byte[] triggerMsg = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57};
					boolean onceConnected=false;
					Socket socket = null;
					try {
						socket = new Socket();
						socket.setSoTimeout(10000);
						SocketAddress addr = new InetSocketAddress(ip, port);
						socket.connect(addr);
						dataInputStream = new DataInputStream(socket.getInputStream());
						outputStream = socket.getOutputStream();
						outputStream.write(triggerMsg);
						try {
							onceConnected=true;
							while (wiGoStarted) {
								dataInputStream.read(buffer, 0, 96);
								handler.sendMessage(handler.obtainMessage(1,1,96,buffer));    
							}
						} finally {
							// A "normal close" of the socket
							dataInputStream.close();
							outputStream.close();
							socket.close();
							wiGoStarted=false;
						}
					} catch (IOException e) {
						if (onceConnected) {
							issueWarning2();					
						} else {
							issueWarning1();					
						}
						wiGoStarted=false;
					}
				};
			});
			background.start();
		}
	}

	void computeQuaternion(DemoQuaternion result, Algorithm algorithm) {
		switch (algorithm) {
		case ACC_ONLY:
			float[] axis = new float[3];
			float[] baseline_axis = {0f, 0f, 1.0f}; 
			float angle = MyUtils.axisAngle(axis, baseline_axis, acc().array());
			result.computeFromAxisAngle(angle, axis);
			// Need to check polarity of this rotation once we have communications
			break;
		case ACC_MAG:
			result.set(super.quaternion());
			break;
		default:
		}
	}
	private void issueWarning1() {
		String msg = "ERROR!  You cannot use your this option just yet.\n" +
				"You must have the following to use this option:\n" +
				"1. an Avnet WiGo Board to use this option.\n" +
				"2. Access to a WiFi network (a local hot spot is OK)\n" +
				"3. The IP address and port number of your WiGo board entered in the Preferences screen\n" +
				"If all of these are satisfied, exit and restart the app and try again.";
				A_FSL_Sensor_Demo.alertHandler.sendMessage(A_FSL_Sensor_Demo.alertHandler.obtainMessage(1,0,0,msg));  
		}
	private void issueWarning2() {
		String msg = "Warning!  You've lost communications with your WiGo module.  This may be a result of excess network traffic/delays." +
				"Deselect and then reselect the same option on the Source/Algorithm spinner to restart.";
				A_FSL_Sensor_Demo.alertHandler.sendMessage(A_FSL_Sensor_Demo.alertHandler.obtainMessage(1,0,0,msg));  
		}

}
