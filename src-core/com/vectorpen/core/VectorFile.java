/*
 * Copyright (c) 2007-2010, Clemens Akens and Oleg Slobodskoi.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.vectorpen.core;

import com.vectorpen.core.util.Color;
import java.util.ArrayList;
import java.util.List;

public final class VectorFile
{
	public static final float DEFAULT_LINE_WIDTH = 1.0f;
	public static final Color DEFAULT_LINE_COLOR = Color.BLACK;
	public static final int ZOOM_MAX = 24 * 9;
	public static final int ZOOM_MIN = 24;
	public static final int ZOOM_ACTUAL_SIZE = 72;
	public static final float LINE_WIDTH_MIN = 0.1f;

	protected static final int DHW_FILE = 0;
	protected static final int TOP_FILE = 1;
	protected static final int DNT_FILE = 2;

	private ArrayList<Path> paths;
	private float lineWidth;
	private Color lineColor;
	private boolean hasLineColor;
	private Size size;
	private String paperSizeName;
	private String title;
	private int type;
	private boolean hasChanged;
	private int zoom;

	protected VectorFile(ArrayList<Path> paths, Size size, String title, int type)
	{
		this.paths = paths;
		this.lineWidth = DEFAULT_LINE_WIDTH;
		this.lineColor = DEFAULT_LINE_COLOR;
		this.hasLineColor = false;
		this.size = size;
		this.paperSizeName = PaperSizes.getPaperSizeName(size, type);
		this.title = title;
		this.type = type;
		this.hasChanged = false;
		this.zoom = 72;
	}

	public float getLineWidth()
	{
		return this.lineWidth;
	}

	public Color getLineColor()
	{
		int redColor = this.lineColor.getRed();
		int greenColor = this.lineColor.getGreen();
		int blueColor = this.lineColor.getBlue();

		return new Color(redColor, greenColor, blueColor);
	}

	public boolean getHasLineColor()
	{
		return this.hasLineColor;
	}

	public int getAspectRatio()
	{
		return this.size.getAspectRatio();
	}

	public String getPaperSizeName()
	{
		return new String(this.paperSizeName);
	}

	public String getTitle()
	{
		return new String(this.title);
	}

	public boolean getHasChanged()
	{
		return this.hasChanged;
	}

	public int getZoom()
	{
		return this.zoom;
	}

	protected int getType()
	{
		return this.type;
	}

	public void setLineWidth(float lineWidth)
	{
		this.lineWidth = Math.abs(lineWidth);
		this.hasChanged = true;
	}

	public void setLineColor(Color lineColor)
	{
		if (lineColor != null)
		{
			this.lineColor = lineColor;
			this.hasChanged = true;
		}
	}

	public void setHasLineColor(boolean hasLineColor)
	{
		this.hasLineColor = hasLineColor;
		this.hasChanged = true;
	}

	public void setPaperSizeName(String paperSizeName)
	{
		if (PaperSizes.isValidPaperSizeName(paperSizeName))
		{
			this.paperSizeName = paperSizeName;
			this.hasChanged = true;
		}
	}

	public void setTitle(String title)
	{
		if (title != null && !title.equalsIgnoreCase(""))
		{
			this.title = title;
		}
	}

	public void setHasChanged(boolean hasChanged)
	{
		this.hasChanged = hasChanged;
	}

	public void zoomIn()
	{
		if (zoom < ZOOM_MAX)
			zoom = zoom + 24;
	}

	public void zoomOut()
	{
		if (zoom > ZOOM_MIN)
			zoom = zoom - 24;
	}

	public void zoomActualSize()
	{
		zoom = ZOOM_ACTUAL_SIZE;
	}

	public void rotateByAngle(int angle)
	{
		if (angle == -270 || angle == -180 || angle == -90 || angle == 90 || angle == 180 || angle == 270)
		{
			int count = this.paths.size();

			for (int index = 0; index < count; index++)
			{
				this.paths.get(index).rotateByAngle(angle, this.size);
			}

			this.hasChanged = true;
		}

		if (angle == -270 || angle == -90 || angle == 90 || angle == 270)
			this.size.rotate();
	}

	public String getSVGRepresentation()
	{
		StringBuffer svgRepresentation = new StringBuffer();

		Size paperSize = this.getPaperSize(72);
		Scale scale = new Scale(this.size, paperSize);

		svgRepresentation.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r");
		svgRepresentation.append("<!-- SVG Generator: ");
		svgRepresentation.append("VectorPen");
		svgRepresentation.append(" -->\r");
		svgRepresentation.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.0//EN\" \"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">\r");
		svgRepresentation.append("<svg version=\"1.0\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" viewBox=\"0 0 ");
		svgRepresentation.append(String.format("%s %s", Float.toString(paperSize.getWidth()), Float.toString(paperSize.getHeight())));
		svgRepresentation.append("\" xml:space=\"preserve\">\r");

		int count = this.paths.size();

		for (int index = 0; index < count; index++)
		{
			svgRepresentation.append(this.paths.get(index).getSVGRepresentation(scale, this));
			svgRepresentation.append("\r");
		}

		svgRepresentation.append("</svg>");

		return svgRepresentation.toString();
	}

	public Size getPaperSize(int ppi)
	{
		Size size = PaperSizes.getInstance().getPaperSize(this.paperSizeName, ppi);

		if (this.size.getAspectRatio() == Size.LANDSCAPE)
			size.rotate();

		return size;
	}

    public Size getSize() {
	return this.size;
    }

    public List<Path> getPaths() {
	return this.paths;
    }

}