package com.vectorpen.core.util;

public class Color {
    public static Color BLACK = new Color(0,0,0);
    public static Color RED = new Color(1,0,0);
    public static Color GREEN = new Color(0,1,0);
    public static Color BLUE = new Color(0,0,1);
    public static Color WHITE = new Color(1,1,1);
    
    private float r;
    private float g;
    private float b;

    public Color(float r, float g, float b) {
	this.r = r;
	this.b = b;
	this.g = g;
    }
}