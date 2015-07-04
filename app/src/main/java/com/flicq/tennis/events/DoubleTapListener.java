package com.flicq.tennis.events;


import android.view.GestureDetector;
import android.view.MotionEvent;

import com.flicq.tennis.opengl.ShotRenderer;

/**
 * Created by soundararajan on 7/4/2015.
 */
public class DoubleTapListener implements GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener
{

    private final ShotRenderer renderer;
    public DoubleTapListener(ShotRenderer renderer)
    {
        this.renderer = renderer;
    }


    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        renderer.resetView();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }
}

