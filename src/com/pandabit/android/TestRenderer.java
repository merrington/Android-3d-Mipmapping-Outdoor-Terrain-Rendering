//	http://blog.jayway.com/2010/01/01/opengl-es-tutorial-for-android-%E2%80%93-part-iii-%E2%80%93-transformations/
//	http://www.droidnova.com/android-3d-game-tutorial-part-iii,348.html

package com.pandabit.android;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.text.TextPaint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class TestRenderer implements GLSurfaceView.Renderer, OnTouchListener {
	private static final String LOG_TAG = "3dTest";
 
    private float _width, _height;
    
    private Context context;

    //Heightmap
    private float[][] heightmapArray;
    private int step_size = 16;    //size of each 'square'
    private static boolean texture = true;
    
    //look at
    private float oldX, oldY;
    public float roty, rotx;
    private float TOUCH_SCALE = 0.5f;
    private float heading;
    
    //old ?
    private int lx=80, ly=0, lz=100;
    private int cx, cy, cz;

	private ShortBuffer indexBuffer;

	private FloatBuffer vertexBuffer;

	private short[] indArrayld0Tops;

	private short[] indArrayld0Bodies;

	private short[] indArrayld0Bottoms;
	
	public TestRenderer(Context context)
	{
		this.context = context;
	}
    
 
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    	Log.v(LOG_TAG, "Starting app!");
    	
    	//setupLighting();
    	float[] diffuse = {0.5f, 0.5f, 0.5f, 0.5f};
    	gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, diffuse, 0);
    	float[] ambient = {0.2f, 0.2f, 0.2f, 0.8f};
    	gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, ambient, 0);
    	//put light in middle
    	float[] lightPos = {Test3d.getHm().length/2*step_size, 100.0f, Test3d.getHm().length/2*step_size, 0.5f};
    	gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPos, 0);
    	gl.glEnable(GL10.GL_LIGHT0);
    	
    	
    	gl.glShadeModel(GL10.GL_SMOOTH);
    	gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
    	gl.glClearDepthf(1.0f);
    	gl.glEnable(GL10.GL_DEPTH_TEST);
    	gl.glEnable(GL10.GL_LIGHTING);
    	gl.glEnable(GL10.GL_COLOR_MATERIAL);
    	gl.glDisable(GL10.GL_DITHER);
    	gl.glDepthFunc(GL10.GL_LEQUAL);
    	gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
    	
    	gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
    	
    	gl.glEnable(GL10.GL_CULL_FACE);
    	gl.glFrontFace(GL10.GL_CCW);
    	gl.glCullFace(GL10.GL_FRONT);
    	
    	//Load heightmap
    	heightmapArray = Test3d.getHm();
    	
    	//setup buffers
    	setupBuffers();
    	
    	//setup camera position
    	cx = heightmapArray.length / 2 * step_size;
    	cy = 25;
    	cz = cx;
    }
 
    @Override
    public void onSurfaceChanged(GL10 gl, int w, int h) {
        gl.glViewport(0, 0, w, h);
        
        _width = w;
        _height = h;
        
        gl.glViewport(0, 0, (int)_width, (int)_height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 45.0f, (float)w/(float)h, 0.1f, 10000.0f);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }
 
    @Override
    public void onDrawFrame(GL10 gl) {
    	gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
    	gl.glLoadIdentity();
    	
    	//GLU.gluLookAt(gl, cx, cy, cz, lx, ly, lz, 0, 1, 0);
    	gl.glRotatef(360-roty, 0, 1.0f, 0);	//rotate left and right
    	gl.glRotatef(rotx, 1.0f, 0, 1.0f);  //look up and down
    	gl.glTranslatef(-cx, -cy, -cz);
    	
    	gl.glScalef(1, 3.0f, 1);
    	renderHeightMap(gl);
    }
    
    private float getHeight(int X, int Y)
    {
    	int x = X % (heightmapArray.length);
    	int y = Y % (heightmapArray.length);
    	
    	return heightmapArray[x][y];
    }
    
    private void renderHeightMap(GL10 gl)
    {
    	int X=0, Y=0, Z=0;
    	float x, y, z;
    	
    	float[] coords = new float[24];
    	
    	//whether wireframe or solid
    	int shape;
    	if (texture)
    		shape = GL10.GL_TRIANGLES;
    	else
    		shape = GL10.GL_LINES;
    	
    	//map is 257x257, overlap squares by 1 = 17x17 patches
    	for (int patchy=0; patchy<17; patchy++)
    	{
    		for (int patchx=0; patchx<17; patchx++)
    		{
    			//load the entire patch into vertArray
    			float[] vertArray = new float[289];
            	for (int a=patchy*17; a<(patchy*17)+17; a++)
            	{
            		for (int b=firstInRow; b<lastInRow; b++)
            		{
            			//one row at a time
            			vertArray[(a*17)+b] = heightmapArray[a][b];
            		}
            	}
    			
    			int firstInRow, lastInRow;
    			firstInRow = (patchy*17)+patchx;
    			lastInRow = firstInRow+17;
    			//draw edges first, then draw body
    			
    			//need to find distance from camera of 4 corners of patch(cx, cy, cz) to determine LoD
    			//because of overlap patch, can know if LoD changes
    			float lodTopLeft = (patchx*17)
    			
    			
    			
    		}    		
    	}
    	
    	
    	//grab the next 17x17 square and put into an array
    	/*
    	 * OLD
    	 *
    	
    	for (X=0; X<heightmapArray.length; X++)
    	{
    		for (Z=0; Z<heightmapArray.length; Z++)
    		{
    			//bottom left vertex
    			x = X*step_size;
    			y = getHeight(X, Z);
    			z = Z*step_size;
    			
    			int vertex=0;
    			
    			//put into coords
    			coords[vertex++] = x;
    			coords[vertex++] = y;
    			coords[vertex++] = z;
    			
    			//bottom right vertex
    			x = (X*step_size)+step_size;
    			y = getHeight(X+1, Z);
    			z = Z*step_size;
    			
    			//put into coords
    			coords[vertex++] = x;
    			coords[vertex++] = y;
    			coords[vertex++] = z;
    			

    			//if it's drawing wireframe, need to duplicate some coords so it draws all lines
    			if (shape == GL10.GL_LINES)
    			{
    				coords[vertex++] = x;
        			coords[vertex++] = y;
        			coords[vertex++] = z; 
    			}
    			
    			//top right vertex
    			x = (X*step_size)+step_size;
    			y = getHeight(X+1, Z+1);
    			z = (Z*step_size)+step_size;
    			
    			//put into coords
    			coords[vertex++] = x;
    			coords[vertex++] = y;
    			coords[vertex++] = z;
    			
    			//if it's drawing wireframe, need to duplicate some coords so it draws all lines
    			if (shape == GL10.GL_LINES)
    			{
    				coords[vertex++] = x;
        			coords[vertex++] = y;
        			coords[vertex++] = z; 
    			}
    			//but if it's drawing triangles, repeat the first and third points
    			if (shape == GL10.GL_TRIANGLES)
    			{
    				coords[vertex++] = coords[0];
    				coords[vertex++] = coords[1];
    				coords[vertex++] = coords[2];
    				
    				coords[vertex++] = coords[6];
    				coords[vertex++] = coords[7];
    				coords[vertex++] = coords[8];
    			}
    			
    			//top left vertex
    			x = X*step_size;
    			y = getHeight(X, Z+1);
    			z = (Z*step_size)+step_size;
    			
    			//put into coords
    			coords[vertex++] = x;
    			coords[vertex++] = y;
    			coords[vertex++] = z;
    			
    			//if it's drawing wireframe, need to duplicate some coords so it draws all lines
    			if (shape == GL10.GL_LINES)
    			{
    				coords[vertex++] = coords[0];
        			coords[vertex++] = coords[1];
        			coords[vertex++] = coords[2];
        			
        			coords[vertex++] = coords[9];
        			coords[vertex++] = coords[10];
        			coords[vertex++] = coords[11];
    			}
    			
    			vertexBuffer.put(coords);
    			vertexBuffer.position(0);
    			//indexBuffer.put();
    			indexBuffer.position(0);
    			
    			//change color based on height
    			gl.glColor4f(1.0f, 0.0f, 0.0f, 0.5f);
    			
    			//debug
    			//Log.v(LOG_TAG, "Drawing something!");
    			
    			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
    			if (shape == GL10.GL_LINES)
    				gl.glDrawArrays(shape, 0, 8);
    			else
    			{
    				//use level 0 for everything
    			}
    				//gl.glDrawArrays(shape, 0, 6);
    			//gl.glDrawElements(shape, 0, GL10.GL_UNSIGNED_SHORT, indexBuffer);
    		}
    	}
    	*/
    }

	private void setupBuffers() {
		//map is 257x257 - use 17x17 squares
		
		indArrayld0Tops = new short[66];
		//define first 2 triangles, these match the LOD of level 1
		indArrayld0Tops[0] = 0;	indArrayld0Tops[1] = 34;	indArrayld0Tops[2] = 2;
		indArrayld0Tops[3] = 2;	indArrayld0Tops[4] = 34;	indArrayld0Tops[5] = 36;
		for (int x=6; x<60; x+=9)
		{
			//get triangle number
			int num = (x-6)/9;
			indArrayld0Tops[x++] = (short) ((num*2)+2);
			indArrayld0Tops[x++] = (short) (indArrayld0Tops[(x-1)]+34);
			indArrayld0Tops[x++] = (short) (indArrayld0Tops[(x-2)]+2);	//complete first triangle
			
			indArrayld0Tops[x++] = indArrayld0Tops[(x-1)]; //starts at same point last triangle finished
			indArrayld0Tops[x++] = indArrayld0Tops[(x-3)]; //same second point (so both triangles share an edge)
			indArrayld0Tops[x++] = (short) (indArrayld0Tops[(x-1)]+1);		//complete second triangle - this one fixes the crack
			
			indArrayld0Tops[x++] = indArrayld0Tops[(x-3)]; //starts same as last triangle
			indArrayld0Tops[x++] = indArrayld0Tops[(x-2)]; //shares same point as last triangle
			indArrayld0Tops[x++] = (short) (indArrayld0Tops[(x-1)]+1);	//complete third triangle ('complement' of first, completes square)
		}
		//define last 2 triangles, these match the LOD of level 1 (same structure as first 2 triangles)
		indArrayld0Tops[60] = 14;	indArrayld0Tops[61] = 48;	indArrayld0Tops[62] = 16;
		indArrayld0Tops[63] = 16;	indArrayld0Tops[64] = 48;	indArrayld0Tops[65] = 50;
		
		indArrayld0Bodies = new short[162];	
		//define first 3 triangles, these match the LOD of level 1
		indArrayld0Bodies[0] = 0;	indArrayld0Bodies[1] = 34;	indArrayld0Bodies[2] = 2;
		indArrayld0Bodies[3] = 2;	indArrayld0Bodies[4] = 34;	indArrayld0Bodies[5] = 19;
		indArrayld0Bodies[6] = 19;	indArrayld0Bodies[7] = 34;	indArrayld0Bodies[8] = 36;
		for (int x=9; x<72; x+=6)	//each loop will make 4 triangles - 2 top, 2 bottom
		{
			for (int y=0; y<1; y++) //y=0 is first row, (top), y=1 is second row (bottom)
			{
				//get loop/triangle number
				int num = (x-9)/6;
				if (y==1)
					num+=17;
				indArrayld0Bodies[x++] = (short) ((num*2)+2);
				indArrayld0Bodies[x++] = (short) (indArrayld0Bodies[(x-1)]+17);
				indArrayld0Bodies[x++] = (short) (indArrayld0Bodies[(x-2)]+1);	//complete first triangle
				
				indArrayld0Bodies[x++] = indArrayld0Bodies[(x-1)]; //starts at same point last triangle finished
				indArrayld0Bodies[x++] = indArrayld0Bodies[(x-3)]; //same second point (so both triangles share an edge)
				indArrayld0Bodies[x++] = (short) (indArrayld0Bodies[(x-1)]+1);		//complete second triangle		
			}
		}
		indArrayld0Bodies[153] = 0;	indArrayld0Bodies[154] = 34;	indArrayld0Bodies[155] = 2;
		indArrayld0Bodies[156] = 2;	indArrayld0Bodies[157] = 34;	indArrayld0Bodies[158] = 19;
		indArrayld0Bodies[159] = 19;	indArrayld0Bodies[160] = 34;	indArrayld0Bodies[161] = 36;
		
		indArrayld0Bottoms = new short[66];
		//define first 2 triangles, these match the LOD of level 1
		indArrayld0Bottoms[0] = 0;	indArrayld0Bottoms[1] = 34;	indArrayld0Bottoms[2] = 2;
		indArrayld0Bottoms[3] = 2;	indArrayld0Bottoms[4] = 34;	indArrayld0Bottoms[5] = 36;
		for (int x=6; x<60; x+=9)
		{
			//get triangle number
			int num = (x-6)/9;
			indArrayld0Bottoms[x++] = (short) ((num*2)+2);
			indArrayld0Bottoms[x++] = (short) (indArrayld0Bottoms[(x-1)]+34);
			indArrayld0Bottoms[x++] = (short) (indArrayld0Bottoms[(x-2)]+1);	//complete first triangle
			
			indArrayld0Bottoms[x++] = indArrayld0Bottoms[(x-1)]; //starts at same point last triangle finished
			indArrayld0Bottoms[x++] = indArrayld0Bottoms[(x-3)]; //same second point (so both triangles share an edge)
			indArrayld0Bottoms[x++] = (short) (indArrayld0Bottoms[(x-2)]+1);		//complete second triangle - this one fixes the crack
			
			indArrayld0Bottoms[x++] = indArrayld0Bottoms[(x-1)]; //starts same as last triangle
			indArrayld0Bottoms[x++] = indArrayld0Bottoms[(x-2)]; //shares same point as last triangle
			indArrayld0Bottoms[x++] = (short) (indArrayld0Bottoms[(x-1)]+2);	//complete third triangle ('complement' of first, completes square)
		}
		//define last 2 triangles, these match the LOD of level 1 (same structure as first 2 triangles)
		indArrayld0Bottoms[60] = 14;	indArrayld0Bottoms[61] = 48;	indArrayld0Bottoms[62] = 16;
		indArrayld0Bottoms[63] = 16;	indArrayld0Bottoms[64] = 48;	indArrayld0Bottoms[65] = 50;
		/*
		 * end of level 0 arrays
		 */
		
		
		//vertex
		ByteBuffer vbb = ByteBuffer.allocateDirect(8 * 3 * 4); //4 vertices, 3 points/vertice * 4 bytes for float
		vbb.order(ByteOrder.nativeOrder());
		vertexBuffer = vbb.asFloatBuffer();
		
		//index
		ByteBuffer ibb = ByteBuffer.allocate(indArrayld0Bodies.length * 2); //body array has most vertices, 2 bytes for a short
		ibb.order(ByteOrder.nativeOrder());
		indexBuffer = ibb.asShortBuffer();
	}
	
	private void setupLighting() {
		float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
		//float[] lightPos = {
		//ByteBuffer dbb = ByteBuffer.allocateDirect(4*4);
	}
	
	public static void toggleTexture()
	{
		texture = !texture;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		float x = event.getX();
    	float y = event.getY();
    	
    	switch (event.getAction())
    	{
    	case MotionEvent.ACTION_MOVE:
    		float dx = x - oldX;
    		float dy = y - oldY;
    		
    		rotx -= dy * TOUCH_SCALE;
    		heading += dx * TOUCH_SCALE;
    		roty = heading;
    		
    		Log.v(LOG_TAG, "move! roty: " + roty +", rotx: " + rotx);
    		
    		break;
    	case MotionEvent.ACTION_DOWN:
    		//toggleTexture();
    		break;
    	}
    	oldX = x;
    	oldY = y;
    	
    	return true;
    }
}
