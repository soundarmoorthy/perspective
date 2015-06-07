package com.flicq.tennis;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.flicq.tennis.ble.FlicqDevice;
import com.flicq.tennis.external.ButtonAwesome;
import com.flicq.tennis.external.TextAwesome;
import com.flicq.tennis.framework.IActivityAdapter;
import com.flicq.tennis.framework.ISystemComponent;
import com.flicq.tennis.framework.StatusType;
import com.flicq.tennis.framework.SystemState;
import com.flicq.tennis.opengl.ShotRenderer;

import java.util.ArrayList;

public class FlicqActivity extends Activity implements IActivityAdapter, View.OnClickListener {
    public FlicqDevice flicqDevice = null;

    public ShotRenderer shotRenderer = null;

    private static final int BLUETOOTH_ENABLE_REQUEST_CODE = 4711;

    SystemState currentState;
    ArrayList<ISystemComponent> systemComponents;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Do anything after this
        currentState = SystemState.STOPPED;


        flicqDevice = FlicqDevice.getInstance(this);

        txtShotDataCached = (TextView) findViewById(R.id.txtShotData);
        scrollViewTxtShotDataCached = (ScrollView) findViewById(R.id.txtShotDataScrollView);
        txtShotDataCached.setLineSpacing(0.0f, 1.2f);

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
                confirmWithUserAndExit();
            }
        });

        this.SetStatus(StatusType.INFO, "Ready");
    }

    private void confirmWithUserAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Confirm");
        builder.setMessage("Are you sure, you want to exit ?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
                onStop();
                System.exit(0);
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
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
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter adapter =  bluetoothManager.getAdapter();
            if(adapter.getState() == BluetoothAdapter.STATE_ON) {
                SetStatus(StatusType.INFO, "Bluetooth On");
                runOnUiThread(InitializeAdapterTask);
            }
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

    TextView txtShotDataCached;
    ScrollView scrollViewTxtShotDataCached;
    @Override
    public void writeToUi(final String str, final boolean differs) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtShotDataCached.append("\n");
                if(differs)
                    txtShotDataCached.setTextColor(Color.RED);
                txtShotDataCached.append(str);

                txtShotDataCached.setTextColor(Color.BLACK);
            }
        });

        //Automatically scroll to end;
        scrollViewTxtShotDataCached.post(new Runnable() {
            public void run() {
                scrollViewTxtShotDataCached.smoothScrollTo(0, txtShotDataCached.getBottom());
            }
        });
    }

    private Runnable UpdateUI = new Runnable() {
        @Override
        public void run() {
        }
    };

    @Override
    public void SetStatus(final StatusType type, final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextAwesome iconText = (TextAwesome) findViewById(R.id.txt_status_icon);
                if (type == StatusType.ERROR)
                    iconText.setText(R.string.fa_sign_out);
                else if (type == StatusType.WARNING)
                    iconText.setText(R.string.fa_warning);
                else if (type == StatusType.INFO)
                    iconText.setText(R.string.fa_info);

                TextView view = (TextView) findViewById(R.id.txt_status);
                view.setText(s);
            }
        });
    }

    @Override
    public Context GetApplicationContext()
    {
        return getApplicationContext();
    }

    static boolean ble_on = false;
    @Override
    public void onClick(View view) {
        int itemId = view.getId();
        SystemState prevState = currentState;
        switch (itemId) {
            case R.id.btn_capture:
                ble_on = !ble_on;
                currentState = ble_on ? SystemState.CAPTURE : SystemState.STOPPED;
                break;
            case R.id.btn_render:
                currentState = SystemState.RENDER;
                break;
            default:
                currentState = SystemState.UNKNOWN;
        }

        updateUI(itemId);
        for (int i = 0; i < systemComponents.size(); i++) {
            ISystemComponent component = systemComponents.get(i);
            component.SystemStateChanged(prevState, currentState);
        }
    }

    private void updateUI(int itemId) {
        int []ids = {R.id.btn_capture, R.id.btn_render};
        for(int i=0;i<ids.length;i++)
        {
            if(itemId == ids[i]) {
                Button  button = (Button) findViewById(ids[i]);
                button.setBackgroundColor(Color.argb(255,240,240,240));//A more lighter gray
            }
            else {
                Button button = (Button) findViewById(ids[i]);
                button.setBackgroundColor(Color.WHITE);
            }
        }
        ButtonAwesome awesome = (ButtonAwesome) findViewById(R.id.btn_capture);
        if(ble_on)
            awesome.setText(R.string.fa_toggle_on);
        else
            awesome.setText(R.string.fa_toggle_off);
    }
}
