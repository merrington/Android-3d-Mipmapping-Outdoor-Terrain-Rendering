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
		
		//print heightmap
		for (int i=0; i<size; i++)
		{
			//Log.v(LOG_TAG, "hm["+i+"]: ");
			String row = "";
			for (int j=0; j<size; j++)
			{
				row+= hm[i][j]+", ";
			}
			//Log.v(LOG_TAG, row);
		}
		

		return hm;
	}
	
	private float[][] diamondHm(float[][] hm, int xMin, int xMax, int yMin, int yMax)
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
		
		if (xMid-xMin >= 1)
		{
			//Log.v(LOG_TAG, "squareHM!");
			squareHm(xMin, xMid, yMin, yMid);
			squareHm(xMid, xMax, yMin, yMid);
			squareHm(xMin, xMid, yMid, yMax);
			squareHm(xMid, xMax, yMid, yMax);
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
