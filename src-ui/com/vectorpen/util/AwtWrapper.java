package com.vectorpen.ui.util;

import com.vectorpen.core.util.Color;

public class AwtWrapper {
    public static java.awt.Color convertColor(Color c) {
	return new java.awt.Color(c.getRed(), c.getGreen(), c.getBlue());
    }
    public static Color importColor(java.awt.Color c) {
	return new Color(c.getRed(), c.getGreen(), c.getBlue());
    }
}