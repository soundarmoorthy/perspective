package com.flicq.tennis.events;

import com.flicq.tennis.opengl.AbstractRenderer;
import com.flicq.tennis.opengl.ShotRenderer;
import android.view.ScaleGestureDetector;
/**
 * Created by soundararajan on 7/4/2015.
 */
public class ScaleListener
        extends ScaleGestureDetector.SimpleOnScaleGestureListener {

    private final AbstractRenderer view;
    public ScaleListener(AbstractRenderer view)
    {
        this.view = view;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scaleFactor = detector.getScaleFactor();
        //The scale factor is too fast. so slow it down
        scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
        view.setIdealZ(view.getIdealZ() /scaleFactor );
        return true;
    }
}

