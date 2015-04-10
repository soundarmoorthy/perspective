package com.flicq.tennis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class FlicqActivity extends Activity implements OnMenuItemClickListener {
    public FlicqDevice flicqDevice = null;

    public ShotRenderer shotRenderer = null;


    static private class MyHandler extends Handler {
        @SuppressWarnings("unused")
        private final WeakReference<FlicqActivity> myActivity;

        public MyHandler(FlicqActivity activity) {
            myActivity = new WeakReference<FlicqActivity>(activity);
        }

        public void handleMessage(Message msg) {
        }
    }

    public void showDataSelector(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.data_source_options, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    public boolean onMenuItemClick(MenuItem item) {
        boolean sts = true;
        int itemId = item.getItemId();
        if (itemId == R.id.btn_stop) {
            if (flicqDevice != null)
                flicqDevice.stop(false);
                shotRenderer.disable();
        } else if (itemId == R.id.btn_render) {
            shotRenderer.enable();
            flicqDevice.stop(true); //This will disable uploading data to cloud
        } else if (itemId == R.id.btn_capture) {
            flicqDevice.start();

            shotRenderer.disable();
        } else {
            sts = false;
        }
        return (sts);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FlicqCloud cloud = new FlicqCloud();
        cloud.Send();

        flicqDevice = FlicqDevice.getInstance(this);
        setContentView(R.layout.activity_main);

        Display display = ((WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int screenRotation = display.getRotation();

        GLSurfaceView shotView = (GLSurfaceView) findViewById(R.id.shotView);
        shotRenderer = new ShotRenderer(this.flicqDevice, screenRotation);
        shotView.setRenderer(shotRenderer);

        Button exitButton = (Button) findViewById(R.id.exit_button);
        exitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
                onStop();
                System.exit(0);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
        }

    @Override
    public void onStart() {
        super.onStart();
        if ((flicqDevice != null) && (!flicqDevice.isListening())) {
            flicqDevice.startBluetooth();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FlicqDevice.requestCode) {
            if (resultCode == RESULT_OK) {
                flicqDevice.getPairedDevice();
                flicqDevice.initializeConnection();
            }
        }
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
        if (flicqDevice != null)
            flicqDevice.stop(false);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (flicqDevice != null) {
            flicqDevice.stop(true); // release BT threads
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}
