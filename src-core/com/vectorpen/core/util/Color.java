package com.vectorpen.core.util;

public class Color {
    public static Color BLACK = new Color(0,0,0);
    public static Color RED = new Color(255,0,0);
    public static Color GREEN = new Color(0,255,0);
    public static Color BLUE = new Color(0,0,255);
    public static Color WHITE = new Color(255,255,255);
    
    private int r;
    private int g;
    private int b;

    public Color(int r, int g, int b) {
	this.r = r;
	this.b = b;
	this.g = g;
    }

    public int getRed() {
	return r;
    }
    public int getGreen() {
	return g;
    }
    public int getBlue() {
	return b;
    }

    public float[] getRGBColorComponents(float[] compArray) {
	float[] res = compArray;
	if (res == null)
	    res = new float[3];
	res[0] = r/255f;
	res[1] = g/255f;
	res[2] = b/255f;
	return res;
    }
}