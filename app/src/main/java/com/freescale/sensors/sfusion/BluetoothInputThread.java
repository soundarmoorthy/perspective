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

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * This is the definition for a separate thread to run Bluetooth communications.
 *
 * @author Michael Stanley
 */
class BluetoothInputThread extends Thread {
    static private BluetoothSocket myBluetoothSocket;
    static private InputStream myBluetoothSocketInputStream;
    static private boolean escaped = false;
    static public PayloadPool pool = null;
    static public Payload payload = null;
    static public Payload nextPayload = null;
    public int messageNumber;
    public byte[] tempStorage = null;
    Handler handler;

    public BluetoothInputThread(BluetoothSocket socket, Handler h) {
        myBluetoothSocket = socket;
        this.handler = h;
        this.messageNumber = 0;
        pool = new PayloadPool(32);
        payload = pool.getInstance();
        this.tempStorage = new byte[16];
        InputStream tmpIn = null;
        try {    // Get the input streams, using temp object because  member streams are final
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(A_FSL_Sensor_Demo.LOG_TAG, "Problem in BluetoothInputThread constructor");
        }
        myBluetoothSocketInputStream = tmpIn;
    }

    public void addByte(int newByte) {
        if (newByte == 0x7D) {
            escaped = true;
        } else if (newByte == 0x7E) {
            int position = payload.bb.position();
            if (position > 0) {
                nextPayload = pool.getInstance();  // get storage for a new packet
                // payloads are automatically released on the receiving side, and can then be re-used
                if (nextPayload != null) {
                    handler.sendMessage(handler.obtainMessage(4, 1, position, payload));
                    payload = nextPayload;
                } else {
                    // drop this packet and re-use the payload
                    payload.clear();
                    //Log.v(A_FSL_Sensor_Demo.LOG_TAG, "dropping Bluetooth packet because system could not keep up.");
                }
            }
        } else {
            if (escaped) {
                if (newByte == 0x5E) {
                    newByte = 0x7E;
                } else if (newByte == 0x5D) {
                    newByte = 0x7D;
                } else {
                    // this should never happen
                }
                escaped = false;
            }
            payload.bb.put((byte) newByte);
        }
    }

    public synchronized void run() {
        int num;
        //Log.v(A_FSL_Sensor_Demo.LOG_TAG, "begin run() from BluetoothInputThread");
        while (true) {
            try {
                messageNumber++;
                //oneByte =  myBluetoothSocketInputStream.read();
                num = myBluetoothSocketInputStream.read(tempStorage, 0, 1);
                if (num > 0) {
                    addByte(tempStorage[0]);
                }
            } catch (IOException e) {
                break;
            }
        }
    }

    public void cancel() {
        //Log.v(A_FSL_Sensor_Demo.LOG_TAG, "begin cancel() from BluetoothInputThread");
        try {
            myBluetoothSocket.close();
            //Log.v(A_FSL_Sensor_Demo.LOG_TAG, "Closed Bluetooth socket.");
        } catch (IOException e) {
        }
    }
}
