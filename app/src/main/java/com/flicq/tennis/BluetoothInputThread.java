package com.flicq.tennis;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

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
            Log.e("Flicq", "Problem in BluetoothInputThread constructor");
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
                    //Log.v(FlicqActivity.LOG_TAG, "dropping Bluetooth packet because system could not keep up.");
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
        //Log.v(FlicqActivity.LOG_TAG, "begin run() from BluetoothInputThread");
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
        //Log.v(FlicqActivity.LOG_TAG, "begin cancel() from BluetoothInputThread");
        try {
            myBluetoothSocket.close();
            //Log.v(FlicqActivity.LOG_TAG, "Closed Bluetooth socket.");
        } catch (IOException e) {
        }
    }
}
