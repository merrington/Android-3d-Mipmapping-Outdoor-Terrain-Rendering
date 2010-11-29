package com.pandabit.android;

public class Heightmap {
	public static hmPatch[] patches = new hmPatch[17*17];
	public static float[] vertArray = new float[257*257];
	
	/*
	 * Patch class
	 */
	private class hmPatch {
		public float[] vertArray = new float[17*17];
		public double distTL, distTR, distBL, dirtBR;
		
		//public static 
	}

}
