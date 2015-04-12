package com.flicq.tennis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

import com.flicq.tennis.ble.FlicqDevice;
import com.flicq.tennis.opengl.ShotRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;


public class FlicqActivity extends Activity implements OnMenuItemClickListener {
    public FlicqDevice flicqDevice = null;

    public ShotRenderer shotRenderer = null;


    public void showDataSelector(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.data_source_options, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }




    public boolean onMenuItemClick(MenuItem item) {
        int itemId = item.getItemId();
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

        if(currentState == prevState)
            return false; //Return false to let others consume this click.

        for(int i=0;i<systemComponents.size(); i++)
        {
            ISystemComponent component = systemComponents.get(i);
            component.SystemStateChanged(prevState, currentState);
        }

        return true; //Return true to prevent others from handling this click
    }

    SystemState currentState;
    ArrayList<ISystemComponent> systemComponents;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentState = SystemState.STOPPED;

        flicqDevice = FlicqDevice.getInstance();
        setContentView(R.layout.activity_main);

        Display display = ((WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int initialScreenRotation = display.getRotation();

        GLSurfaceView shotView = (GLSurfaceView) findViewById(R.id.shotView);

        int mode = 1;
        float[] set = new float[1];
        shotRenderer = new ShotRenderer(initialScreenRotation, set,  mode );
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

;

