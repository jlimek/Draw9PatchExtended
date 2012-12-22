package com.android.draw9patch.ui;

public class CopiedPatches
{
	int[] top;
	int[] left;
	int[] bottom;
	int[] right;
	
	
	public CopiedPatches(int[] aLeft, int[] aTop, int[] aRight, int[] aBottom)
	{
		super();
		top = aTop;
		left = aLeft;
		bottom = aBottom;
		right = aRight;
	}


	public CopiedPatches(int[] aTop, int[] aLeft)
	{
		super();
		top = aTop;
		left = aLeft;
	}
	
	
	
	
}
