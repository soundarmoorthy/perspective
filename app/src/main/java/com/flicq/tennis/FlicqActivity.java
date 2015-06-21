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
import android.os.AsyncTask;
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
import com.flicq.tennis.contentmanager.ContentStore;
import com.flicq.tennis.contentmanager.FlicqShot;
import com.flicq.tennis.external.ButtonAwesome;
import com.flicq.tennis.external.TextAwesome;
import com.flicq.tennis.framework.IActivityAdapter;
import com.flicq.tennis.framework.SampleData;
import com.flicq.tennis.framework.StatusType;
import com.flicq.tennis.framework.SystemState;
import com.flicq.tennis.opengl.ShotRenderer;
import com.flicq.tennis.test.LocalSensorDataSimulator;

public class FlicqActivity extends Activity implements IActivityAdapter, View.OnClickListener {

    private static final int BLUETOOTH_ENABLE_REQUEST_CODE = 4711;

    public FlicqActivity() {
        super();
    }

    public FlicqDevice flicqDevice = null;
    public ShotRenderer shotRenderer = null;
    SystemState currentSystemState;
    public LocalSensorDataSimulator simulator;
    boolean simulator_mode = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        onCreateInitialSetup(savedInstanceState);
        //Do anything after this.
        setupDeviceCapture();
        setupDeviceSimulator();
        setupRendering();
        setupLog();
        setupExitButton();
        this.SetStatus(StatusType.INFO, "Welcome !");
    }

    private void setupDeviceSimulator() {
        IActivityAdapter adapter = this;
        simulator = new LocalSensorDataSimulator(adapter);
    }

    private void onCreateInitialSetup(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentSystemState = SystemState.STOPPED;
    }

    private void setupDeviceCapture()
    {
        IActivityAdapter adapter = this;
        flicqDevice = new FlicqDevice(adapter);
    }

    private void setupLog()
    {
        txtShotDataCached = (TextView) findViewById(R.id.txtShotData);
        scrollViewTxtShotDataCached = (ScrollView) findViewById(R.id.txtShotDataScrollView);
        txtShotDataCached.setLineSpacing(0.0f, 1.2f);
    }

    private void setupRendering()
    {
        int initialScreenRotation = getScreenRotation();
        GLSurfaceView shotView = (GLSurfaceView) findViewById(R.id.shotView);
        shotRenderer = new ShotRenderer(initialScreenRotation, 1);
        shotView.setRenderer(shotRenderer);
        setupUIForRender();
    }

    int getScreenRotation()
    {
        Display display = ((WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int initialScreenRotation = display.getRotation();
        return initialScreenRotation;
    }


    private void setupExitButton()
    {
        Button exitButton = (Button) findViewById(R.id.exit_button);
        exitButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                confirmWithUserAndExit();
            }
        });

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

    private void ConnectDevice() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BLUETOOTH_ENABLE_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BLUETOOTH_ENABLE_REQUEST_CODE) {
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter adapter = bluetoothManager.getAdapter();
            if (adapter.getState() == BluetoothAdapter.STATE_ON) {
                SetStatus(StatusType.INFO, "Bluetooth On");
                fireOnBluetoothSetupReady();
            }
        }
    }

    private void fireOnBluetoothSetupReady() {
        //Now the BLE adapter is initialized.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter btAdapter = bluetoothManager.getAdapter();
        Log.e("OnActivityResult", "Initialized adapter");
        flicqDevice.OnBluetoothAdapterInitialized(btAdapter);
    }

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
        switch (itemId) {
            case R.id.btn_capture:
                handleCapture();
                break;
            case R.id.btn_render:
                setupUIForRender();
                getLastShotAndDraw();
                break;
            case R.id.btn_engineering:
                setupUIForLogging();
            default:
                currentSystemState = SystemState.UNKNOWN;
        }
        updateUI(itemId);
    }

    private void getLastShotAndDraw() {
        if(simulator_mode)
            renderLocalSensorData();
        else
            renderDeviceData();
    }

    private void renderLocalSensorData() {
        float[] data = simulator.getSensorData();
        shotRenderer.Render(SampleData.set);
    }

    private void renderDeviceData() {
        FlicqShot shot = ContentStore.Instance().getShot();
        float[] data = shot.getDataForRendering();
        shotRenderer.Render(data);
    }


    private void setupUIForLogging() {
        setVisibility(View.VISIBLE, View.GONE);
    }

    private void setupUIForRender(){
        setVisibility(View.GONE, View.VISIBLE);
    }

    private void setVisibility(int display, int render)
    {
        findViewById(R.id.txtShotDataScrollView).setVisibility(display);
        findViewById(R.id.txtShotData).setVisibility(display);
        findViewById(R.id.shotView).setVisibility(render);
    }

    private void handleCapture()
    {
        if(!simulator_mode)
            handleRealDevice();
        else
            handleSimulator();
    }

    private void handleSimulator()
    {
        final LocalSensorDataSimulator simu = simulator;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
               simu.Start();
                return null;
            }
        }.execute();
    }

    private void handleRealDevice()
    {
        ble_on = !ble_on;
        currentSystemState = ble_on ? SystemState.CAPTURE : SystemState.STOPPED;
        if(currentSystemState == SystemState.CAPTURE)
            ConnectDevice();
    }

    private void updateUI(int itemId) {
        int []ids = {R.id.btn_capture, R.id.btn_render, R.id.btn_engineering};
        for(int i=0;i<ids.length;i++) {
            if (itemId == ids[i]) {
                Button button = (Button) findViewById(ids[i]);
                button.setBackgroundColor(Color.argb(255, 240, 240, 240));//A more lighter gray
            } else {
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
