package com.flicq.tennis;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.flicq.tennis.ble.FlicqDevice;
import com.flicq.tennis.contentmanager.ContentStore;
import com.flicq.tennis.contentmanager.UnprocessedShot;
import com.flicq.tennis.framework.IActivityHelper;
import com.flicq.tennis.framework.ISystemComponent;
import com.flicq.tennis.framework.SampleData;
import com.flicq.tennis.framework.SystemState;
import com.flicq.tennis.opengl.ShotRenderer;
import com.flicq.tennis.test.TestOpenGL;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class FlicqActivity extends Activity implements IActivityHelper, View.OnClickListener {
    public FlicqDevice flicqDevice = null;

    public ShotRenderer shotRenderer = null;

    private static final int BLUETOOTH_ENABLE_REQUEST_CODE = 4711;

    SystemState currentState;
    ArrayList<ISystemComponent> systemComponents;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentState = SystemState.STOPPED;

        flicqDevice = FlicqDevice.getInstance(this);
        setContentView(R.layout.activity_main);

        Display display = ((WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int initialScreenRotation = display.getRotation();

        GLSurfaceView shotView = (GLSurfaceView) findViewById(R.id.shotView);

        int mode = 1;
        shotRenderer = new ShotRenderer(initialScreenRotation, mode, this);
        shotView.setRenderer(shotRenderer);


        systemComponents = new ArrayList<ISystemComponent>();
        systemComponents.add(0, flicqDevice);
        systemComponents.add(1, shotRenderer);

        Button exitButton = (Button) findViewById(R.id.exit_button);
        exitButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                finish();
                onStop();
                System.exit(0);
            }
        });
        //This is to test the OpenGL rendering with a known set of data.
        //TestOpenGL.Run();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BLUETOOTH_ENABLE_REQUEST_CODE)
        {
            runOnUiThread(InitializeAdapterTask);
        }

    }

    private Runnable InitializeAdapterTask = new Runnable() {
        @Override
        public void run() {
            //Now the BLE adapter is initialized.
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter adapter =  bluetoothManager.getAdapter();
            flicqDevice.OnBluetoothAdapterInitialized(adapter);
            Log.e("OnActivityResult", "Initialized adapter");
        }
    };

    @Override
    public void onRestart() {
        super.onRestart();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onStop() {
        //do here
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // do here
    }

    @Override
    public void onPause() {
        //do here
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        //do here
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void EnableBluetoothAdapter() {
        boolean supported = false;
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            //This will start the enable activity. To see the result look into
            //activityResult
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BLUETOOTH_ENABLE_REQUEST_CODE);
        }
    }

    @Override
    public void SetStatus(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public Context GetApplicationContext()
    {
        return getApplicationContext();
    }

    @Override
    public void RunOnUIThread(Runnable action) {
        runOnUiThread(action);
    }

    private BluetoothGatt currentDevice;
    @Override
    public void SetGatt(BluetoothGatt currentGattDevice) {
        this.currentDevice = currentGattDevice;
    }

    @Override
    public void onClick(View view) {

        int itemId = view.getId();
        SystemState prevState = currentState;

        switch (itemId) {
            case R.id.btn_stop:
                currentState = SystemState.STOPPED;
                break;
            case R.id.btn_capture:
                currentState = SystemState.CAPTURE;
                break;
            case R.id.btn_render:
                currentState = SystemState.RENDER;
                break;
            default:
                currentState = SystemState.UNKNOWN;
        }

        for (int i = 0; i < systemComponents.size(); i++) {
            ISystemComponent component = systemComponents.get(i);
            component.SystemStateChanged(prevState, currentState);
        }
    }
}
