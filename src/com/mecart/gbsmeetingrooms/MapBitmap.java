package com.mecart.gbsmeetingrooms;

import android.graphics.Bitmap;

public class MapBitmap  {

	private final Bitmap bmp;
	private final int bitmapWidth, bitmapHeight;
	
	public MapBitmap(Bitmap bitmap, int w, int h) {
		// TODO Auto-generated constructor stub
		bmp = bitmap;
		bitmapWidth = w;
		bitmapHeight = h;
		

	}

	public Bitmap getBmp() {
		return bmp;
	}

	public int getBitmapWidth() {
		return bitmapWidth;
	}

	public int getBitmapHeight() {
		return bitmapHeight;
	}

}
