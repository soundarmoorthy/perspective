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
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.flicq.tennis.ble.FlicqDevice;
import com.flicq.tennis.contentmanager.ContentStore;
import com.flicq.tennis.contentmanager.FlicqShot;
import com.flicq.tennis.external.ButtonAwesome;
import com.flicq.tennis.external.TextAwesome;
import com.flicq.tennis.framework.IActivityAdapter;
import com.flicq.tennis.framework.StatusType;
import com.flicq.tennis.framework.SystemState;
import com.flicq.tennis.opengl.ShotRenderer;
import com.flicq.tennis.test.LocalSensorDataSimulator;
import com.flicq.tennis.events.*;

public class FlicqActivity extends Activity implements IActivityAdapter, View.OnClickListener
{

    private static final int BLUETOOTH_ENABLE_REQUEST_CODE = 4711;
    private FlicqDevice flicqDevice = null;
    private ShotRenderer shotRenderer = null;
    private SystemState currentSystemState;
    private final boolean simulator_mode = false; //For experimental purposes
    private TextView txtShotDataCached;
    private ScrollView scrollViewTxtShotDataCached;


    private GestureDetectorCompat mDetector;
    private ScaleGestureDetector mScaleDetector;

    public FlicqActivity() {
        super();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        onCreateInitialSetup(savedInstanceState);
        //Do anything after this.
        showSplashScreenAnimation();
        setupLog();
        setupDeviceCapture();
        setupRendering();

        mScaleDetector = new ScaleGestureDetector(this,new ScaleListener(shotRenderer));
        DoubleTapListener dTapListener = new DoubleTapListener(shotRenderer);
        mDetector = new GestureDetectorCompat(getApplicationContext(), dTapListener);
        mDetector.setOnDoubleTapListener(dTapListener);

        setupExitButton();
        this.SetStatus(StatusType.INFO, "Welcome !");
    }

    private void showSplashScreenAnimation() {
        final View image = findViewById(R.id.fullscreen_content_controls);
        Animation fadeOut = new AlphaAnimation(1,0);
        fadeOut.setDuration(2000);
        image.setAnimation(fadeOut);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.fullscreen_content_controls).setVisibility(View.GONE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
        fadeOut.start();

        Animation fadeIn = new AlphaAnimation(0,1);
        fadeIn.setDuration(2000);
        final View panelView = findViewById(R.id.flicq_app_controls);
        panelView.setAnimation(fadeIn);
        fadeIn.setStartTime(System.currentTimeMillis()+1200);
        fadeIn.start();
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.flicq_app_controls).setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
    }

    private void setupDeviceSimulator() {
        if (simulator_mode) {
            IActivityAdapter adapter = this;
            LocalSensorDataSimulator simulator = new LocalSensorDataSimulator(adapter, shotRenderer);
            shotRenderer.setSimulator(simulator);
            simulator.Start();
        }
    }

    private void onCreateInitialSetup(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentSystemState = SystemState.STOPPED;
    }

    private void setupDeviceCapture() {
        if (simulator_mode) {
            this.writeToUi("Simulator mode, no device will be detected");
            return;
        }
        IActivityAdapter adapter = this;
        flicqDevice = new FlicqDevice(adapter);
    }

    private void setupLog() {
        txtShotDataCached = (TextView) findViewById(R.id.txtShotData);
        scrollViewTxtShotDataCached = (ScrollView) findViewById(R.id.txtShotDataScrollView);
        txtShotDataCached.setLineSpacing(0.0f, 1.2f);
    }

    private void setupRendering() {
        int initialScreenRotation = getScreenRotation();
        GLSurfaceView shotView = (GLSurfaceView) findViewById(R.id.shotView);
        shotRenderer = new ShotRenderer( /* Relative acceleration data */ initialScreenRotation);
        shotView.setRenderer(shotRenderer);
        if (simulator_mode)
            setupDeviceSimulator();
        setupUIForRender();
    }


    private int getScreenRotation() {
        Display display = ((WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        return display.getRotation();
    }


    private void setupExitButton() {
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



    @Override
    public void writeToUi(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtShotDataCached.append("\n");
                txtShotDataCached.append(str);
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
    public void onDisconnected() {
        handleUIAction(R.id.btn_capture);
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
    public Context GetApplicationContext() {
        return getApplicationContext();
    }

    private static boolean ble_on = false;

    @Override
    public void onClick(View view) {
        int itemId = view.getId();
        handleUIAction(itemId);
    }

    private void handleUIAction(int itemId) {
        switch (itemId) {
            case R.id.btn_capture:
                handleRealDevice();
                break;
            case R.id.btn_render:
                setupUIForRender();
                renderDeviceData();
                break;
            case R.id.btn_engineering:
                setupUIForLogging();
                break;
            default:
                currentSystemState = SystemState.UNKNOWN;
        }
        updateUI(itemId);
    }

    private void renderDeviceData() {
        if (simulator_mode)
            return;
        FlicqShot shot = ContentStore.Instance().getShot();
        if (shot != null) {
            float[] data = shot.getDataForRendering();
            shotRenderer.Render(data);
        }
    }

    private void setupUIForLogging() {
        setVisibility(View.VISIBLE, View.GONE);
    }

    private void setupUIForRender() {
        setVisibility(View.GONE, View.VISIBLE);
        shotRenderer.resetView();
    }

    private void setVisibility(int display, int render) {
        findViewById(R.id.txtShotDataScrollView).setVisibility(display);
        findViewById(R.id.txtShotData).setVisibility(display);
        findViewById(R.id.shotView).setVisibility(render);
    }

    private void handleRealDevice() {
        ble_on = !ble_on;
        currentSystemState = ble_on ? SystemState.CAPTURE : SystemState.STOPPED;
        if (currentSystemState == SystemState.CAPTURE)
            ConnectDevice();
        if (currentSystemState == SystemState.STOPPED) {
            if (this.flicqDevice != null)
                flicqDevice.requestStopScan();
        }
    }

    private void updateUI(int itemId) {
        int[] ids = {R.id.btn_capture, R.id.btn_render, R.id.btn_engineering};
        for (int id : ids) {
            if (itemId == id) {
                Button button = (Button) findViewById(id);
                button.setBackgroundColor(Color.argb(255, 240, 240, 240));//A more lighter gray
            } else {
                Button button = (Button) findViewById(id);
                button.setBackgroundColor(Color.WHITE);
            }
        }
        ButtonAwesome awesome = (ButtonAwesome) findViewById(R.id.btn_capture);
        if (ble_on)
            awesome.setText(R.string.fa_toggle_on);
        else
            awesome.setText(R.string.fa_toggle_off);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        boolean handled = awardChanceToExternalHandlers(event);
        if(handled)
            return true;
        int maskedAction = event.getActionMasked();
        switch (maskedAction) {
            case MotionEvent.ACTION_DOWN:
                shotRenderer.setXY(event.getX(), event.getY());
                return true;
            case MotionEvent.ACTION_UP:
                shotRenderer.resetDeltaXY();
                return true;
            case MotionEvent.ACTION_MOVE:
                shotRenderer.move(event.getX(), event.getY());
                return true;
        }
        return super.onTouchEvent(event);
    }

    private boolean awardChanceToExternalHandlers(MotionEvent event)
    {
        boolean handled = false;
        if(mDetector !=null) {
            handled = mDetector.onTouchEvent(event);
            if (handled)
                return true;
        }

        handled = false;
        if(mScaleDetector != null) {
            handled = mScaleDetector.onTouchEvent(event) && mScaleDetector.isInProgress();
            if (handled)
                return true;
        }

        return false;
    }
}
