package com.flicq.tennis.events;

import com.flicq.tennis.opengl.ShotRenderer;
import com.flicq.tennis.opengl.TextureCubeRenderer;

import android.view.ScaleGestureDetector;
/**
 * Created by soundararajan on 7/4/2015.
 */
public class ScaleListener
        extends ScaleGestureDetector.SimpleOnScaleGestureListener {

    private final TextureCubeRenderer view;
    public ScaleListener(TextureCubeRenderer view)
    {
        this.view = view;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scaleFactor = detector.getScaleFactor();
        scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
        //view.idealZ /= scaleFactor;
        return true;
    }
}

