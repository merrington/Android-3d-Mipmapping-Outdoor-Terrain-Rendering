package com.pandabit.android;

public class Patch {
	public float[][] vertArray;	//array of the verticies in the patch
	public int patchX, patchY;	//which patch is this
	public double distTL, distTR, distBL, dirtBR;
	
	public Patch() {
		vertArray = new float[17][17];
	}
	
	public float[] calcDistance(float cx, float cy, float cz)
	{
		float[] returnVal = new float[4];
		
		//get distances
		float leftX = patchX * 16;	//left-most x value is the number of patchX * width of each patch (no overlap)
		float rightX = (patchX+17) * 16;	//right value is number of patchX+
		float topY = patchY * 16;	//top Y value
		float bottomY = (patchY+17) * 16;	//
    	float distTopLeft = (float) Math.sqrt(Math.pow((leftX-cx),2)+Math.pow((topY-cy),2));
    	float distTopRight = (float) Math.sqrt(Math.pow((rightX-cx),2)+Math.pow((topY-cy),2));
    	float distBotLeft = (float) Math.sqrt(Math.pow((leftX-cx),2)+Math.pow((bottomY+17-cy),2));
    	float distBotRight = (float) Math.sqrt(Math.pow((rightX-cx),2)+Math.pow((topY-cy),2));
    	
    	returnVal[0] = distTopLeft;
    	returnVal[1] = distTopRight;
    	returnVal[2] = distBotLeft;
    	returnVal[3] = distBotRight;
    	
    	return returnVal;
	}
}
