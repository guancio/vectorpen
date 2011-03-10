package com.vectorpen.ui.util;

import com.vectorpen.core.VectorFile;

import com.vectorpen.core.Size;
import com.vectorpen.core.Scale;
import com.vectorpen.core.Path;
import com.vectorpen.core.Point;

import java.awt.image.BufferedImage;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.geom.GeneralPath;

public class ImageRepresentation {
	public static BufferedImage getZoomedImageRepresentation(VectorFile file)
	{
	    return getImageRepresentationByPPI(file, file.getZoom(), true);
	}

	public static BufferedImage getImageRepresentation(VectorFile file)
	{
	    return getImageRepresentationByPPI(file, VectorFile.ZOOM_ACTUAL_SIZE, true);
	}

    public static BufferedImage getImageRepresentationByPPI(VectorFile file, int ppi, boolean opaque)
	{
		Size paperSize = file.getPaperSize(Math.abs(ppi));
		Scale scale = new Scale(file.getSize(), paperSize);

		float lineWidth = file.getLineWidth() * ((float)ppi / 72.0f);
		if (lineWidth < VectorFile.LINE_WIDTH_MIN) lineWidth = VectorFile.LINE_WIDTH_MIN;

		BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

		int width = (int)paperSize.getWidth() + 1;
		int height = (int)paperSize.getHeight() + 1;

		BufferedImage imageRepresentation;

		if (opaque)
		{
			imageRepresentation = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		}
		else
		{
			imageRepresentation = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		}

		Graphics2D graphics = imageRepresentation.createGraphics();
		graphics.setStroke(stroke);

		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		if (opaque)
		{
			graphics.setColor(java.awt.Color.WHITE);
			graphics.fillRect(0, 0, width, height);
		}

		int count = file.getPaths().size();

		for (int index = 0; index < count; index++)
		{
		    Path path = file.getPaths().get(index);

		    if (file.getHasLineColor())
			{
			    graphics.setColor(
                                  AwtWrapper.convertColor(
				     file.getLineColor()
							  )
					      );
			}
			else
			{
			    graphics.setColor(AwtWrapper.convertColor(path.getLineColor()));
			}

		    GeneralPath geom = new GeneralPath();
		    Point point = path.getPoints().get(0).cloneByScale(scale);
		    geom.moveTo(point.getX(), point.getY());
		    int pathSize = path.getPoints().size();
		    for (int i = 1; i < pathSize; i++) {
			point = path.getPoints().get(i).cloneByScale(scale);
			geom.lineTo(point.getX(), point.getY());
		    }

		    graphics.draw(geom);
		}

		imageRepresentation.flush();

		return imageRepresentation;
	}

    public static BufferedImage getImageRepresentationByWidth(VectorFile file, int width)
	{
		Size paperSize = file.getPaperSize(72);

		float height = paperSize.getHeight() / (paperSize.getWidth() / width);

		Size size = new Size(width, height);
		Scale scale = new Scale(file.getSize(), size);

		float lineWidth = file.getLineWidth() * (width / paperSize.getWidth());
		if (lineWidth < VectorFile.LINE_WIDTH_MIN) lineWidth = VectorFile.LINE_WIDTH_MIN;

		BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

		BufferedImage imageRepresentation = new BufferedImage(width, (int)height + 1, BufferedImage.TYPE_4BYTE_ABGR);

		Graphics2D graphics = imageRepresentation.createGraphics();
		graphics.setStroke(stroke);

		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, width, (int)height + 1);

		int count = file.getPaths().size();

		for (int index = 0; index < count; index++)
		{
		    Path path = file.getPaths().get(index);

		    if (file.getHasLineColor())
			{
			    graphics.setColor(AwtWrapper.convertColor(file.getLineColor()));
			}
			else
			{
			    graphics.setColor(AwtWrapper.convertColor(path.getLineColor()));
			}

		    GeneralPath geom = new GeneralPath();
		    Point point = path.getPoints().get(0).cloneByScale(scale);
		    geom.moveTo(point.getX(), point.getY());
		    int pathSize = path.getPoints().size();
		    for (int i = 1; i < pathSize; i++) {
			point = path.getPoints().get(i).cloneByScale(scale);
			geom.lineTo(point.getX(), point.getY());
		    }

		    graphics.draw(geom);
		}

		imageRepresentation.flush();

		return imageRepresentation;
	}

    public static BufferedImage getImageRepresentationByHeight(VectorFile file, int height)
	{
		Size paperSize = file.getPaperSize(72);

		float width = paperSize.getWidth() / (paperSize.getHeight() / height);

		Size size = new Size(width, height);
		Scale scale = new Scale(file.getSize(), size);

		float lineWidth = file.getLineWidth() * (height / paperSize.getHeight());
		if (lineWidth < VectorFile.LINE_WIDTH_MIN) lineWidth = VectorFile.LINE_WIDTH_MIN;

		BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

		BufferedImage imageRepresentation = new BufferedImage((int)width + 1, height, BufferedImage.TYPE_4BYTE_ABGR);

		Graphics2D graphics = imageRepresentation.createGraphics();
		graphics.setStroke(stroke);

		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, (int)width + 1, height);

		int count = file.getPaths().size();

		for (int index = 0; index < count; index++)
		{
		    Path path = file.getPaths().get(index);

		    if (file.getHasLineColor())
			{
			    graphics.setColor(AwtWrapper.convertColor(file.getLineColor()));
			}
			else
			{
			    graphics.setColor(AwtWrapper.convertColor(path.getLineColor()));
			}

		    GeneralPath geom = new GeneralPath();
		    Point point = path.getPoints().get(0).cloneByScale(scale);
		    geom.moveTo(point.getX(), point.getY());
		    int pathSize = path.getPoints().size();
		    for (int i = 1; i < pathSize; i++) {
			point = path.getPoints().get(i).cloneByScale(scale);
			geom.lineTo(point.getX(), point.getY());
		    }
		    graphics.draw(geom);
		}

		imageRepresentation.flush();

		return imageRepresentation;
	}
}