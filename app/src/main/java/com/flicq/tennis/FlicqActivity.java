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

import com.flicq.tennis.appengine.FlicqCloud;
import com.flicq.tennis.ble.FlicqDevice;
import com.flicq.tennis.opengl.ShotRenderer;

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
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FlicqCloud cloud = new FlicqCloud();
        cloud.Send();

        flicqDevice = FlicqDevice.getInstance();
        setContentView(R.layout.activity_main);

        Display display = ((WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int initialScreenRotation = display.getRotation();

        GLSurfaceView shotView = (GLSurfaceView) findViewById(R.id.shotView);
        shotRenderer = new ShotRenderer(initialScreenRotation);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
}

