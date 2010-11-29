package com.pandabit.android;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

public class Test3dView extends GLSurfaceView {
	private static final String LOG_TAG = Test3dView.class.getSimpleName();
    private TestRenderer _renderer;

 
    public Test3dView(Context context) {
        super(context);
        
        this.requestFocus();
        this.setFocusableInTouchMode(true);
        
        _renderer = new TestRenderer(context);
        setRenderer(_renderer);
        
        this.setOnTouchListener(_renderer);
    }
}
