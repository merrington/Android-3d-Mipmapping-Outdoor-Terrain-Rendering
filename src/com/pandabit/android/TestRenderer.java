//	http://blog.jayway.com/2010/01/01/opengl-es-tutorial-for-android-%E2%80%93-part-iii-%E2%80%93-transformations/
//	http://www.droidnova.com/android-3d-game-tutorial-part-iii,348.html

package com.pandabit.android;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

public class TestRenderer implements GLSurfaceView.Renderer {
	private static final String LOG_TAG = "3dTest";
	 
    private float _red = 0.9f;
    private float _green = 0.2f;
    private float _blue = 0.2f;
    
    private float angle = 0.0f;
    
    //Triangle stuff
    private ShortBuffer _indexBuffer;
    private FloatBuffer _vertexBuffer;
    
    private short[] indicesArray={0,1,2,0,2,3};
    private float[] squareCoords = {
			-0.5f, 0.5f, 0f,
			-0.5f, -0.5f, 0f,
			0.5f, -0.5f, 0f,
			0.5f, 0.5f, 0f
	};
 
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    	Log.v(LOG_TAG, "Starting app!");
    	
    	gl.glShadeModel(GL10.GL_SMOOTH);
    	gl.glClearDepthf(1.0f);
    	gl.glEnable(GL10.GL_DEPTH_TEST);
    	gl.glDepthFunc(GL10.GL_LEQUAL);
    	gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
    	
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        drawSquare();
    }
 
    @Override
    public void onSurfaceChanged(GL10 gl, int w, int h) {
        gl.glViewport(0, 0, w, h);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 45.0f, (float)w/(float)h, 0.1f, 100.0f);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }
 
    @Override
    public void onDrawFrame(GL10 gl) {
    	gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
    	gl.glLoadIdentity();
    	gl.glTranslatef(0, 0, -10);
    	
    	gl.glPushMatrix();
    	gl.glRotatef(angle, 0, 0, 1);
    	drawSq(gl);
    	gl.glPopMatrix();
    	
    	gl.glPushMatrix();
    	gl.glRotatef(-angle, 0, 0, 1);
    	gl.glTranslatef(2, 0, 0);
    	gl.glScalef(0.5f, 0.5f, 0.5f);
    	drawSq(gl);
    	
    	gl.glPushMatrix();
    	gl.glRotatef(-angle, 0, 0, 1);
    	gl.glTranslatef(2, 0, 0);
    	gl.glScalef(0.5f, 0.5f, 0.5f);
    	gl.glRotatef(angle*10, 0, 0, 1);
    	drawSq(gl);
    	
    	gl.glPopMatrix();
    	gl.glPopMatrix();
    	
    	angle++;
    	
    }
    
    private void drawSq(GL10 gl)
    {
    	gl.glFrontFace(GL10.GL_CCW);
    	gl.glEnable(GL10.GL_CULL_FACE);
    	gl.glCullFace(GL10.GL_BACK);
    	
    	// define the color we want to be displayed as the "clipping wall"
        gl.glClearColor(_red, _green, _blue, 1.0f);
    	gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
    	
    	//gl.glColor4f(0.5f, 0f, 0f, 0.5f);
    	gl.glVertexPointer(3, GL10.GL_FLOAT, 0, _vertexBuffer);
    	Log.v(LOG_TAG, "Drawing a square");
    	gl.glDrawElements(GL10.GL_TRIANGLES, indicesArray.length, GL10.GL_UNSIGNED_SHORT, _indexBuffer);
    
    	gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    	gl.glDisable(GL10.GL_CULL_FACE);
    }
    
    private void drawSquare()
    {
    	Log.v(LOG_TAG, "Setting up square");
    	
    	ByteBuffer vbb = ByteBuffer.allocateDirect(squareCoords.length*4);
    	vbb.order(ByteOrder.nativeOrder());
    	_vertexBuffer = vbb.asFloatBuffer();
    	_vertexBuffer.put(squareCoords);
    	_vertexBuffer.position(0);
    	
    	ByteBuffer ibb = ByteBuffer.allocateDirect(indicesArray.length*2);
    	ibb.order(ByteOrder.nativeOrder());
    	_indexBuffer = ibb.asShortBuffer();
    	_indexBuffer.put(indicesArray);
    	_indexBuffer.position(0);
    }
    
    public void setColor(float r, float g, float b) {
    	_red = r;
    	_green = g;
    	_blue = b;
    }
}
