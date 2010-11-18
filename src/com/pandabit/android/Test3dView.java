package com.pandabit.android;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

public class Test3dView extends GLSurfaceView {
	private static final String LOG_TAG = Test3dView.class.getSimpleName();
    private TestRenderer _renderer;
 
    public Test3dView(Context context) {
        super(context);
        _renderer = new TestRenderer();
        setRenderer(_renderer);
    }
    
    public boolean onTouchEvent(final MotionEvent event) {
    	queueEvent(new Runnable() {
			
			@Override
			public void run() {
				_renderer.setColor(event.getX() / getWidth(), event.getY() / getHeight(), 1.0f);
			}
		});
    	
    	return true;
    }
}
