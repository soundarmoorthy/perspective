package com.flicq.tennis;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.UUID;


public class FlicqDevice {
    static FlicqDevice self = null;

    private enum ImuRecordType {NONE, DEBUG, ONE, THREE, FOUR}

    public enum BtStatus {UNKNOWN, NOT_SUPPORTED, STARTING, ENABLED, PAIRED, CHOKED, CONNECTED, READY}

    static public short softwareVersionNumber = 0;
    static public final int requestCode = 33; // used in startActivityForResult
    static public BtStatus btStatus = BtStatus.UNKNOWN;

    static private float timeScale = 0.000001f; // 1.00 microseconds per tick
    static private float gyro_lsb = 0.00087266f; // units are radians/sec.  Equivalent to +/- 1600 dps
    static private float acc_lsb = 0.00012207f;
    static private float mag_lsb = 0.1f; // +/-1200 microTeslas
    static String line;

    // Bluetooth variables
    static public boolean listeningToRemoteDevice = false;
    static private BluetoothAdapter myBluetooth = null;
    static private BluetoothSocket myServerSocket = null;
    static private BluetoothInputThread myBluetoothInputThread = null;
    static private OutputStream myBluetoothSocketOutputStream;
    static private String name;
    static private UUID uuid;
    static private BluetoothDevice dev;
    static private String pattern = null;
    static private boolean btReceiverRegistered = false;
    static private boolean enableDebugPacket;
    static float[] quatInputs = null;
    static int keepAliveCounter = 0;
    static boolean alreadySniffed = false;

    protected TimedTriad acc = null;
    protected TimedTriad mag = null;
    protected TimedTriad gyro = null;
    protected TimedQuaternion quaternion = null;
    String LOG_TAG = null;
    FlicqActivity activity;  // a back pointer to the master application

    public FlicqQuaternion quaternion() {
        return quaternion;
    }

    synchronized public void clear() {
        acc.zero();
        mag.zero();
        gyro.zero();
    }

    final int keepAliveInterval = 100;

    private FlicqDevice(FlicqActivity activity) {
        this.activity = activity;
        acc = new TimedTriad();
        mag = new TimedTriad();
        gyro = new TimedTriad();
        quaternion = new TimedQuaternion();
        clear();

        FlicqDevice.pattern = "blue";
        quatInputs = new float[4];
        enableDebugPacket = false; //Set to true, to get debug packet.
        acc.setTimeScale(timeScale);
        mag.setTimeScale(timeScale);
        gyro.setTimeScale(timeScale);
        quaternion.setTimeScale(timeScale);
    }

    static public FlicqDevice getInstance(FlicqActivity demo) {
        if (self == null) {
            self = new FlicqDevice(demo);
        }
        return (self);
    }

    public boolean isListening() {
        return (btStatus == BtStatus.READY);
    }

    private class MyHandler extends Handler {
        public MyHandler(FlicqActivity activity) {
        }

        public synchronized void handleMessage(Message msg) {
            int what = msg.what;
            Payload payload = null;
            // int arg1 = msg.arg1;  // TODO: Available for expansion of capabilities
            // int arg2 = msg.arg2;  // TODO: Available for expansion of capabilities
            payload = (Payload) msg.obj;
            if (listeningToRemoteDevice) {  // only handle messages when I am listening
                switch (what) {
                    case 4: // parsed packet from BluetoothInputThread
                        processBuffer(payload);
                        break;
                    default: // junk packets are discarded
                }
            } else if (!alreadySniffed) {
                switch (what) {
                    case 4: // parsed packet from BluetoothInputThread
                        sniffBoardType(payload);
                        break;
                    default: // junk packets are discarded
                }
            }
            payload.reclaim();
        }
    }

    MyHandler handler = new MyHandler(activity);


    static public void setBtSts(BtStatus sts, String s) {
        btStatus = sts;
    }


    synchronized void adjustForZero(RotationVector rv, FlicqQuaternion q) {
        rv.computeFromQuaternion(q, FlicqUtils.AngleUnits.DEGREES);
    }

    synchronized void getData(RotationVector rv, FlicqQuaternion q) {
        SampleData.getNextQuaternion(q);
        adjustForZero(rv, q);
    }

    private synchronized ImuRecordType processBuffer(Payload payload) {
        String str;
        int i, flags, boardId, systicks;
        short shortInt;
        byte b;
        double n; // used for normalization functions
        ImuRecordType retVal = ImuRecordType.NONE;
        ByteBuffer bb = payload.bb;
        short packetId = bb.get(0);
        int recordSize = bb.position();

        switch (packetId) {
            case 1:
                retVal = ImuRecordType.ONE;
                // byte packetNumber = payload.bb.get(1);
                long timestamp = (long) bb.getInt(2);
                short ax = bb.getShort(6); //  2 bytes each
                short ay = bb.getShort(8);
                short az = bb.getShort(10);
                short mx = bb.getShort(12);
                short my = bb.getShort(14);
                short mz = bb.getShort(16);
                short gx = bb.getShort(18);
                short gy = bb.getShort(20);
                short gz = bb.getShort(22);
                if (recordSize < 41) {
                    quatInputs[0] = (float) bb.getShort(24) / 30000f;
                    quatInputs[1] = (float) bb.getShort(26) / 30000f;
                    quatInputs[2] = (float) bb.getShort(28) / 30000f;
                    quatInputs[3] = (float) bb.getShort(30) / 30000f;
                }
                if (recordSize < 41) {
                    flags = (int) bb.get(32);
                    boardId = (int) bb.get(33);
                } else {
                    flags = (int) bb.get(40);
                    boardId = (int) bb.get(41);
                }
                // normalize the quaternion to fix roundoff errors
                n = quatInputs[0] * quatInputs[0] + quatInputs[1] * quatInputs[1] + quatInputs[2] * quatInputs[2] + quatInputs[3] * quatInputs[3];
                n = Math.sqrt(n);
                quatInputs[0] /= n;
                quatInputs[1] /= n;
                quatInputs[2] /= n;
                quatInputs[3] /= n;
                if (softwareVersionNumber > 417) {
                    float q1, q2, q3;
                    int FoR = (flags & 0x30) >> 4;  // Pull out the Frame of References field
                    if (FoR == 0) {
                        // Incoming quaternion is NED.  We need to do a minor translation on the quaternion axis, as the app needs it in Android form
                        q1 = quatInputs[2];
                        q2 = quatInputs[1];
                        q3 = -quatInputs[3];
                        quatInputs[1] = q1;
                        quatInputs[2] = q2;
                        quatInputs[3] = q3;
                    } // Don't have to do anything if the incoming quaternino is already ENU
                }
                quaternion.set(timestamp, quatInputs);
                // checkFlags() is just a unit test to ensure that we are getting expected packet types
                // There will often be a lag of one or two packets before newly selected packet types take affect.
                // This is considered OK.
                acc.set(timestamp, acc_lsb * ax, acc_lsb * ay, acc_lsb * az);
                mag.set(timestamp, mag_lsb * mx, mag_lsb * my, mag_lsb * mz);
                gyro.set(timestamp, gyro_lsb * gx, gyro_lsb * gy, gyro_lsb * gz);
                break;
            case 2:
                retVal = ImuRecordType.DEBUG;
                if (enableDebugPacket) {
                /* this packet type has variable length.  Must be a multiple of 2 bytes */
                    recordSize = bb.position();
                    if ((recordSize >= 4) && ((recordSize % 2) == 0)) {
                        softwareVersionNumber = bb.getShort(2); // location 0 is packetType, location 1 is the packet number.  This is the software version.
                        str = String.format("Embedded Software Version: %06d", softwareVersionNumber);
                        shortInt = bb.getShort(4); // this location will be the number of systicks/20
                        if (shortInt < 0)
                            systicks = shortInt + 65536;  // the transmitted value is unsigned, but ByteBuffer does not handle them without some adjustment
                        else systicks = shortInt;
                        systicks = systicks * 20;
                        str += String.format("\nSysticks/Orientation: %08d", systicks);
                        for (i = 6; i < recordSize; i += 2) {
                            shortInt = bb.getShort(i);
                            str += String.format("\n%06d", shortInt);
                        }
                    }
                }
                break;
            default:
        }
        keepAlive();
        return (retVal);
    }

    private synchronized void sniffBoardType(Payload payload) {
        int boardId;
        ByteBuffer bb = payload.bb;
        short packetId = bb.get(0);
        int recordSize = bb.position();

        switch (packetId) {
            case 1:
                if (recordSize < 41) {
                    boardId = (int) bb.get(33);
                } else {
                    boardId = (int) bb.get(41);
                }
                alreadySniffed = true;
        }
    }

    // utility function used by checkFlags()
    String expectedPacketDescriptor() {
        return "9-axis";
    }

    boolean isReady() {
        return (btStatus == BtStatus.READY);
    }

    // Possible 4 byte commands:
    // "AVP " = Use physical gyro
    // "AVV " = Use virtual gyro
    // "DB+ " = debug packet on (default)
    // "DB- " = debug packet off
    // "Q3  " = transmit 3-axis quaternion in standard packet
    // "Q6MA" = transmit 6-axis mag/accel quaternion in standard packet
    // "Q6AG" = transmit 6-axis accel/gyro quaternion in standard packet
    // "Q9  " = transmit 9-axis quaternion in standard packet (default)
    // "RPC+" = Roll/Pitch/Compass on (default)
    // "RPC-" = Roll/Pitch/Compass off
    // "RST " = Soft reset
    public void turnDebugOn() {
        sendTo("DB+ ");
    }

    public void turnDebugOff() {
        sendTo("DB- ");
    }  // we will not be using this command, as debug will be left on at the protocol level so that

    // we can get the embedded app software version number (needed for compatibility checks).
    // The Preferences option for debug on/off will only affect what gets displayed.
    public void useQ3() {
        sendTo("Q3  ");
    }

    public void useQ3M() {
        sendTo("Q3M ");
    }

    public void useQ3G() {
        sendTo("Q3G ");
    }

    public void useQ6MA() {
        sendTo("Q6MA");
    }

    public void useQ6AG() {
        sendTo("Q6AG");
    }

    public void useQ9() {
        sendTo("Q9  ");
    }

    public void turnRpcOn() {
        sendTo("RPC+");
    }

    public void turnRpcOff() {
        sendTo("RPC-");
    }

    public void reset() {
        sendTo("RST ");
    }

    public void disableVirtualGyro() {
        sendTo("VG- ");
    }

    public void enableVirtualGyro() {
        sendTo("VG+ ");
    }

    void keepAlive() {
        // see http://stackoverflow.com/questions/18420525/application-using-bluetooth-spp-profile-not-working-after-update-from-android-4/18646072#18646072
        // for why this function was needed
//		if (Build.VERSION.SDK_INT > 17) { 
        keepAliveCounter += 1;
        if (keepAliveCounter >= keepAliveInterval) {
            turnDebugOn();
            keepAliveCounter = 1;
        }
//		}
    }

    public void sendTo(String str) {
        if (isReady()) {
            byte[] bytes = null;
            try {
                bytes = str.getBytes("UTF-8");
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
            try {
                myBluetoothSocketOutputStream.write(bytes);
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        } else {
        }
    }

    public void startBluetooth() {
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (myBluetooth == null) {
            setBtSts(BtStatus.NOT_SUPPORTED, "Bluetooth is not available on this device");
        } else {
            if (myBluetooth.isEnabled()) {
                setBtSts(BtStatus.ENABLED, "Bluetooth previously initialized.  Restarting...");
                stop(true);
                scheduleBtIntents();
                getPairedDevice();
                initializeConnection();
            } else {
                stop(true);
                setBtSts(BtStatus.ENABLED, "Bluetooth is available on this device.  Beginning initialization...");
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableIntent, requestCode);
                setBtSts(BtStatus.STARTING, "Starting Bluetooth.");
            }
        }
    }

    public void scheduleBtIntents() {
        if ((myBluetooth != null) && (myBluetooth.isEnabled())) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivity(enableIntent);
            IntentFilter filter = null;

            filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
            activity.registerReceiver(myReceiver, filter);
            filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            activity.registerReceiver(myReceiver, filter);

            filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            activity.registerReceiver(myReceiver, filter);

            filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            activity.registerReceiver(myReceiver, filter);
            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            activity.registerReceiver(myReceiver, filter);
            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            activity.registerReceiver(myReceiver, filter);
            btReceiverRegistered = true;
        }
    }

    public void getPairedDevice() {

        //Log.v(LOG_TAG, "begin getPairedDevice()");
        boolean sts = false;
        if (btStatus == BtStatus.ENABLED) {
            Set<BluetoothDevice> pairedDevices = myBluetooth.getBondedDevices(); // Get set of currently paired devices
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    //FlicqActivity.write("Already paired: " + device.getName() + " : " + device.getAddress() + "\n");
                    name = device.getName();
                    String address = device.getAddress();
                    dev = device;
                    uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                    sts = name.startsWith(pattern);
                    if (sts) {
                        setBtSts(BtStatus.PAIRED, "Found FlicqDevice.");
                        return;
                    }
                }
            }
        }
    }


    private boolean imuCreateSocket() {
        boolean pass = false;
        //Log.v(LOG_TAG, "begin imuCreateSocket()");
        try {
            myServerSocket = dev.createInsecureRfcommSocketToServiceRecord(uuid);
            myBluetooth.cancelDiscovery();
            myServerSocket.connect();
            pass = true;
        } catch (IOException e) {
            setBtSts(BtStatus.CHOKED, "caught unknown exception trying to to create InsecureRfcommSocket");
        }
        return (pass);
    }

    private boolean multiPassCreateSocket() {
        // The iterative process here was suggested by a posting at:
        // http://code.google.com/p/android/issues/detail?id=29039
        int i = 0;
        boolean sts = false;
        while ((!sts) && i < 6) {
            sts = imuCreateSocket();
            i += 1;
        }
        return sts;
    }

    public void initializeConnection() {
        Log.v(LOG_TAG, "begin initializeConnection()");
        if (btStatus == BtStatus.PAIRED) {
            Thread background = new Thread(new Runnable() {
                public void run() {
                    myServerSocket = null;
                    if (uuid != null) {
                        if (multiPassCreateSocket())
                            try {
                                setBtSts(BtStatus.CONNECTED, "Bluetooth connection established.");
                                myBluetoothInputThread = new BluetoothInputThread(myServerSocket, handler);
                                myBluetoothInputThread.setPriority(Thread.MAX_PRIORITY);
                                myBluetoothInputThread.start();
                                myBluetoothSocketOutputStream = myServerSocket.getOutputStream();
                                setBtSts(BtStatus.READY, "The Bluetooth output stream appears ready.");
                            } catch (ThreadDeath aTD) {
                                setBtSts(BtStatus.CHOKED, "ThreadDeath exception generated while creating the Bluetooth thread or output stream.");
                            } catch (Throwable t) {
                                setBtSts(BtStatus.CHOKED, "caught unknown exception trying to create and start new thread");
                            }
                    }
                }
            }, "BluetoothInputThread");
            FlicqUtils.waitALittle(1000);
            background.start();
        }
    }

    public void start() {
        if (btStatus == BtStatus.READY) listeningToRemoteDevice = true;
    }

    public void stop(boolean cancel_existing_threads) {
        if (cancel_existing_threads) {
            if (btReceiverRegistered) {
                activity.unregisterReceiver(myReceiver);
            }

            if (myBluetoothInputThread != null) {
                myBluetoothInputThread.cancel();
            }
            try {
                if (myServerSocket != null) {
                    myServerSocket.close();
                }
            } catch (Throwable t) {
                Log.e(LOG_TAG, "caught exception trying to close connection to Bluetooth device");
            }
            if (btStatus == BtStatus.READY)
                setBtSts(BtStatus.PAIRED, "Resetting BT connection back to PAIRED.");
        }
        listeningToRemoteDevice = false;
    }

    public final BroadcastReceiver myReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
        }
    };
}