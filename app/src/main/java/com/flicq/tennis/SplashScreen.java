package com.flicq.tennis;

import android.view.View;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.view.MotionEvent;
import com.flicq.tennis.util.SystemUiHider;


public class SplashScreen extends Activity {

    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);
        final View controlsView = findViewById(R.id.fullscreen_content_controls);

        SystemUiHider mSystemUiHider = SystemUiHider.getInstance(this, controlsView);
       mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    @Override
                    public void onVisibilityChange(boolean visible) {
                        controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        delayedHide();
                    }
                });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide();
    }

    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            delayedHide();
            return false;
        }
    };

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(SplashScreen.this, FlicqActivity.class));
            finish();
        }
    };

    private void delayedHide() {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, SplashScreen.AUTO_HIDE_DELAY_MILLIS);
    }
}
