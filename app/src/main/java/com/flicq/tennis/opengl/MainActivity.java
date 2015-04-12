package com.example.soundararajan.testopengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Toast;
//import android.app.Activity;


public class MainActivity extends ActionBarActivity {

	MyRenderer renderer;
    public MainActivity()
    {
    }

    
	void setupMenu(){
		setContentView(R.layout.activity_main);
    	getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupMenu();
        Display display = ((WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int screenRotation = display.getRotation();

        GLSurfaceView shotView = (GLSurfaceView) findViewById(R.id.shotView);
        renderer = new MyRenderer(this,screenRotation);
        shotView.setRenderer(renderer);
    }

    private Menu menu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_screenshot) {
        	renderer.screenshot_request = true;
        	Toast toast = Toast.makeText(getApplicationContext(), 
         		   R.string.saving_screenshot, Toast.LENGTH_SHORT); 
         		toast.show();
            return true;
        }
        if (id == R.id.animation_use) {
       		item.setTitle(renderer.animation_use?R.string.action_animation_use:R.string.action_animation_not_use);
       		menu.findItem(R.id.animation_play).setVisible(!renderer.animation_use);
        	renderer.animation_use = !renderer.animation_use;
            return true;
        }
        if (id == R.id.animation_play) {
       		item.setTitle(renderer.animation_play?R.string.action_animation_play:R.string.action_animation_not_play);
        	renderer.animation_play = !renderer.animation_play;
            return true;
        }
        if (id == R.id.mode0) {
        	menu.findItem(R.id.mode1).setChecked(false);
        	menu.findItem(R.id.mode2).setChecked(false);
       		item.setChecked(true);
        	renderer.setMode(0);
            return true;
        }
        if (id == R.id.mode1) {
        	menu.findItem(R.id.mode0).setChecked(false);
        	menu.findItem(R.id.mode2).setChecked(false);
       		item.setChecked(true);
        	renderer.setMode(1);
            return true;
        }
        if (id == R.id.mode2) {
        	menu.findItem(R.id.mode0).setChecked(false);
        	menu.findItem(R.id.mode1).setChecked(false);
       		item.setChecked(true);
        	renderer.setMode(2);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) 
	    {
	    	renderer.idealZ*=1.2;
	        return true;
	    }
	    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) 
	    {
	    	renderer.idealZ*=0.8;
	        return true;
	    }
	    super.onKeyDown(keyCode, event);
	    return true;
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP) || (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
           return true;
        }
        return super.onKeyUp(keyCode, event);
    }
    float oldX,oldY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
   	
    	// get masked (not specific to a pointer) action
        int maskedAction = event.getActionMasked();

        switch (maskedAction) {
	        case MotionEvent.ACTION_DOWN: {
	        	oldX=event.getX();
	        	oldY=event.getY();
	        break;	
	        }
	        case MotionEvent.ACTION_UP: {
	        	renderer.deltaX=0;
	        	renderer.deltaY=0;
	        break;	
	        }
	        	
        	case MotionEvent.ACTION_MOVE: { // a pointer was moved
        		
        		float deltaX =  (event.getX()-oldX);
            	float deltaY =  (event.getY()-oldY);
            	renderer.deltaX = deltaX;
            	renderer.deltaY = deltaY;
            	oldX=event.getX();
	        	oldY=event.getY();
	        	
	          break;
	        }
        }
    	return super.onTouchEvent(event);
    }


}
