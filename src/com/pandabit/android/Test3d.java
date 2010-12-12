package com.pandabit.android;

import java.util.Random;
import java.lang.*;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class Test3d extends Activity {
	private static final String LOG_TAG = Test3d.class.getSimpleName();
	private Test3dView _testView;
	
	private int pass = 4; //number of passes to make
	private float d = 4.0f;	//displacement
	
	private static float[][] hm;
	
	public static Patch[][] patches = new Patch[17][17];
	
	private Random rnd;
	
	public Test3d() {
		rnd = new Random();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		_testView = new Test3dView(this);
		setContentView(_testView);
		
		TextView testText = new TextView(this);
		testText.setTextColor(Color.WHITE);
		testText.setText("FPS: ");
		testText.bringToFront();
		
		setHm(generateHeightmap());
	}
	
	private float[][] generateHeightmap()
	{
		//heightmap size
		int size = (int)Math.pow(16, 2)+1;
		Log.v(LOG_TAG, "Heightmap size: "+size);
		hm = new float[size][size];	//heightmap
		
		//make square
		squareHm(0, size-1, 0, size-1);
		
		//debug - print heightmap
		/*for (int i=0; i<size; i++)
		{
			//Log.v(LOG_TAG, "hm["+i+"]: ");
			String row = "";
			for (int j=0; j<size; j++)
			{
				row+= hm[i][j]+", ";
			}
			//Log.v(LOG_TAG, row);
		}*/
		
		//move hm to patches array
		//patches = new Patch[16][16];
		
		
		int patchx_old = 0;
		int patchy_old = 0;
		for (int patch1=0; patch1<17; patch1++)
			for (int patch2=0; patch2<17; patch2++)
				patches[patch1][patch2] = new Patch();
		
		for (int i=0; i<hm.length; i++)	//0-256
		{
			for (int j=0; j<hm[0].length; j++)	//0-256
			{
				//patchx
				int patchx = i/16;
				//patchy
				int patchy = j/16;
				//get position in hm of i and j relative to patch, each patch holds 17 values, the last of one patch should be first in next patch (overlap)
				int hmX = i%16;
				int hmY = j%16;
				
				if ((patchx < 17) && (patchy < 17)) {	//16 is the last row/column - doesn't get its own patch
					//Log.v(LOG_TAG, "patchx,patchy:"+patchx+","+patchy+". hmx,hmy:"+hmX+","+hmY);
					patches[patchx][patchy].vertArray[hmX][hmY] = hm[i][j];
				}
				
				//add in the overlapping vertex
				if (patchx != patchx_old) {	//if this is the start of a new patch, also place the point in the old patch (overlap)
					if (patchy == 15)	//if its the last y of the patch, increase patchx_old, next points are only in current patch
						patchx_old = patchx;
					patches[patchx-1][patchy].vertArray[16][hmY] = hm[i][j];
				}
				if (patchy != patchy_old) {
					patchy_old = patchy;
					if (j != 0)	{//patchy will change when it loops back to 0, don't add the overlap point
						float hmVal = hm[i][j];
						patches[patchx][patchy-1].vertArray[hmX][16] = hmVal;
					}
				}
			}
		}
		
		/*
		 * debug - checking for overlap in patches
		for (int i=0; i<patches.length; i++) {
			for (int j=0; j<patches[0].length; j++) {
				//debug for the first 2 patches to check
				Log.v(LOG_TAG, "Patch: "+0+","+0+". Vertex: "+ i+","+j+". Value:"+patches[0][0].vertArray[i][j]);
				Log.v(LOG_TAG, "Patch: "+1+","+0+". Vertex: "+ i+","+j+". Value:"+patches[1][0].vertArray[i][j]);
			}
		}*/
		
		Log.v(LOG_TAG, "Generatd heightmap");
		return hm;
	}
	
	private float[][] diamondHm(int xMin, int xMax, int yMin, int yMax)
	{
		//get average height of four corners, add displacement
		float avg = avgHeight(xMin, xMax, yMin, yMax);
		
		//set height of middle point
		int xMid = (xMin + xMax) / 2;
		int yMid = (yMin + yMax) / 2;
		hm[xMid][yMid] = avg;
		
		//make squares
		if (xMid-xMin >= 1)
		{
			squareHm(xMin, xMid, yMin, yMid);
			squareHm(xMid, xMax, yMin, yMid);
			squareHm(xMin, xMid, yMid, yMax);
			squareHm(xMid, xMax, yMid, yMax);
		}
		
		return hm;		
	}

	/**
	 * Calculate average height of 4 corners
	 * @param xMin Minimum x value of array
	 * @param xMax Maximum x value of array
	 * @param yMin Minimum y value of array
	 * @param yMax Maximum y value of array
	 * @return
	 */
	private float avgHeight(int xMin, int xMax, int yMin, int yMax) {
		float avg = (hm[xMin][yMin] + hm[xMax][yMin] + hm[xMin][yMax] + hm[xMax][yMax]) / 4;
			//Log.v(LOG_TAG, "avg: " +avg);
			float next = (rnd.nextInt(6)%d);
			//Log.v(LOG_TAG, "next: " + next);
			avg += next;
			//Log.v(LOG_TAG, "new: " + avg);
		
		return avg;
	}
	
	private void squareHm(int xMin, int xMax, int yMin, int yMax)
	{		
		//set height of middle
		int xMid = (xMin + xMax) / 2;
		int yMid = (yMin + yMax) / 2;
		hm[xMid][yMid] = avgHeight(xMin, xMax, yMin, yMax);
		
		if (xMid-xMin > 1)
		{
			//Log.v(LOG_TAG, "squareHM!");
			diamondHm(xMin, xMid, yMin, yMid);
			diamondHm(xMid, xMax, yMin, yMid);
			diamondHm(xMin, xMid, yMid, yMax);
			diamondHm(xMid, xMax, yMid, yMax);
			//if (d > 0.5)
//				d = d/2;
		}
	}

	private void setHm(float[][] hm) {
		this.hm = hm;
	}

	public static float[][] getHm() {
		return hm;
	}
	
	//menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add("Toggle Wireframe/Solid");
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getTitle() == "Toggle Wireframe/Solid")
			TestRenderer.toggleTexture();
		
		return true;
	}

}
