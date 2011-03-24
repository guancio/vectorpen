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

package com.vectorpen.ui;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.io.OutputStream;
import java.io.FileInputStream;

import javax.swing.ImageIcon;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import com.vectorpen.core.DocInfoDict;
import com.vectorpen.core.PDFiTextModule;
import com.vectorpen.core.Size;
import com.vectorpen.core.VectorFile;

import com.vectorpen.ui.util.ImageRepresentation;
import com.vectorpen.ui.util.AwtWrapper;

import com.jmupdf.PdfDocument;
import com.jmupdf.PdfPage;
import com.jmupdf.exceptions.PDFException;
import com.jmupdf.exceptions.PDFSecurityException;

import com.vectorpen.core.Size;
import com.vectorpen.core.Scale;
import java.awt.Graphics2D;
import java.awt.BasicStroke;


@SuppressWarnings("serial")
public final class VectorFiles extends AbstractTableModel implements TableModelListener
{
	public static final int PREVIEW_SIZE = 150;

	private static VectorFiles instance;

	private String columnNamePreview;
	private String columnNameTitle;
	private ArrayList<VectorFile> vectorFiles;
	private ArrayList<ImageIcon> previews;

	private String background;

    // Open document
    private PdfDocument pdfDoc = null;


	public static synchronized VectorFiles getInstance()
	{
		if (instance == null)
			instance = new VectorFiles();

		return instance;
	}

	private VectorFiles()
	{
		getLocalizedStrings();

		addTableModelListener(this);

		vectorFiles = new ArrayList<VectorFile>();
		previews = new ArrayList<ImageIcon>();

		background = null;
		pdfDoc = null;
	}

	private void getLocalizedStrings()
	{
		ResourceBundle bundle = ResourceBundle.getBundle("Localizable");

		columnNamePreview = bundle.getString("VectorFiles.Column.Name.Preview");
		columnNameTitle = bundle.getString("VectorFiles.Column.Name.Title");
	}

	public String getColumnName(int column)
	{
		if (column == 0) return columnNamePreview;

		return columnNameTitle;
	}

	public boolean isCellEditable(int row, int column)
	{
		if (column == 0) return false;

		return true;
	}

	public Class<?> getColumnClass(int column)
	{
		if (column == 0) return ImageIcon.class;

		return String.class;
	}

	public Object getValueAt(int row, int column)
	{
		VectorFile vectorFile = vectorFiles.get(row);

		if (column == 0)
		{
			if (vectorFile.getHasChanged())
			{
				BufferedImage image;

				if (vectorFile.getAspectRatio() == Size.PORTRAIT)
				{
				    //image = ImageRepresentation.getImageRepresentationByHeight(vectorFile,PREVIEW_SIZE);
				    BufferedImage imgBg = getBasePageByHeight(row, PREVIEW_SIZE);
				    image = ImageRepresentation.overlayImageRepresentationByHeight(imgBg, vectorFile,PREVIEW_SIZE);
				}
				else
				{
				    BufferedImage imgBg = getBasePageByWidth(row, PREVIEW_SIZE);
				
				    image = ImageRepresentation.overlayImageRepresentationByWidth(imgBg, vectorFile, PREVIEW_SIZE);
				}

				ImageIcon preview = previews.remove(row);
				previews.add(row, new ImageIcon(image));

				image.flush();
				image = null;

				if (preview != null) preview = null;

				vectorFile.setHasChanged(false);
			}

			return previews.get(row);
		}

		return vectorFile.getTitle();
	}

	public void setValueAt(Object value, int row, int column)
	{
		if (column == 1)
		{
			VectorFile vectorFile = vectorFiles.get(row);

			if (FileName.check((String)value, true))
				vectorFile.setTitle((String)value);

			fireTableCellUpdated(row, column);
		}
	}

	public int getRowCount()
	{
		return vectorFiles.size();
	}

	public int getColumnCount()
	{
		return 2;
	}

	public void tableChanged(TableModelEvent event)
	{
		int firstRow = event.getFirstRow();
		int lastRow = event.getLastRow();

		switch (event.getType())
		{
		case TableModelEvent.INSERT:
			UIMainTable.getInstance().getSelectionModel().setSelectionInterval(lastRow, lastRow);

			break;
		case TableModelEvent.DELETE:
			UIMainTable.getInstance().getSelectionModel().removeIndexInterval(firstRow, lastRow);

			if (UIMainTable.getInstance().getRowCount() > 0)
			{
				lastRow = UIMainTable.getInstance().getRowCount() - 1;

				UIMainTable.getInstance().getSelectionModel().setSelectionInterval(lastRow, lastRow);
			}

			break;
		case TableModelEvent.UPDATE:
			int selectedRow = UIMainTable.getInstance().getSelectedRow();

			UIImageDisplay.getInstance().setImage(getZoomedImageRepresentation(selectedRow));
		}

		UIMainToolbar.getInstance().updateButtons();
		UIMainMenu.getInstance().updateMenuItems();
		UISecondaryToolbar.getInstance().updateComponents();
	}

	VectorFile getVectorFileAtIndex(int index)
	{
		return vectorFiles.get(index);
	}

	public float getLineWidth(int index)
	{
		return vectorFiles.get(index).getLineWidth();
	}

	public Color getLineColor(int index)
	{
	    return AwtWrapper.convertColor(vectorFiles.get(index).getLineColor());
	}

	public boolean getHasLineColor(int index)
	{
		return vectorFiles.get(index).getHasLineColor();
	}

	public String getPaperSizeName(int index)
	{
		return vectorFiles.get(index).getPaperSizeName();
	}

	public int getZoom(int index)
	{
		return vectorFiles.get(index).getZoom();
	}

	public void setLineWidth(float lineWidth)
	{
		int rows[] = UIMainTable.getInstance().getSelectedRows();

		int count = rows.length;

		for (int index = 0; index < count; index++)
		{
			vectorFiles.get(rows[index]).setLineWidth(lineWidth);
		}

		fireTableRowsUpdated(rows[0], rows[count - 1]);
	}

	public void setDefaultLineColor()
	{
		int rows[] = UIMainTable.getInstance().getSelectedRows();

		int count = rows.length;

		for (int index = 0; index < count; index++)
		{
			vectorFiles.get(rows[index]).setHasLineColor(false);
		}

		fireTableRowsUpdated(rows[0], rows[count - 1]);
	}

	public void setLineColor(Color lineColor)
	{
		int rows[] = UIMainTable.getInstance().getSelectedRows();

		int count = rows.length;

		for (int index = 0; index < count; index++)
		{
			vectorFiles.get(rows[index]).setHasLineColor(true);
			vectorFiles.get(rows[index]).setLineColor(AwtWrapper.importColor(lineColor));
		}

		fireTableRowsUpdated(rows[0], rows[count - 1]);
	}

	public void setPaperSizeName(String paperSizeName)
	{
		int rows[] = UIMainTable.getInstance().getSelectedRows();

		int count = rows.length;

		for (int index = 0; index < count; index++)
		{
			vectorFiles.get(rows[index]).setPaperSizeName(paperSizeName);
		}

		fireTableRowsUpdated(rows[0], rows[count - 1]);
	}

	public void add(ArrayList<VectorFile> vectorFiles)
	{
		int size = this.vectorFiles.size();
		int count = vectorFiles.size();

		for (int index = 0; index < count; index++)
		{
			VectorFile vectorFile = vectorFiles.get(index);
			this.vectorFiles.add(vectorFile);

			BufferedImage image;

			if (vectorFile.getAspectRatio() == Size.PORTRAIT)
			{
			    BufferedImage imgBg = getBasePageByHeight(index, PREVIEW_SIZE);
			    image = ImageRepresentation.overlayImageRepresentationByHeight(imgBg, vectorFile,PREVIEW_SIZE);
			}
			else
			{
			    BufferedImage imgBg = getBasePageByWidth(index, PREVIEW_SIZE);
			    image = ImageRepresentation.overlayImageRepresentationByWidth(imgBg, vectorFile,PREVIEW_SIZE);
			}

			ImageIcon preview = new ImageIcon(image);

			image.flush();
			image = null;

			previews.add(preview);
		}

		fireTableRowsInserted(size, size + count - 1);
	}

	public void remove()
	{
		int rows[] = UIMainTable.getInstance().getSelectedRows();

		int count = rows.length - 1;

		int minPage = -1;

		for (int index = count; index >= 0; index--)
		{
			VectorFile vectorFile = vectorFiles.remove(rows[index]);
			ImageIcon preview = previews.remove(rows[index]);

			if (vectorFile != null) vectorFile = null;
			if (preview != null) preview = null;

			if (minPage < 0 || rows[index] < minPage)
			    minPage = rows[index];
		}



		int size = vectorFiles.size();

		for (int index = minPage; index < size; index++)
		{
			VectorFile vectorFile = vectorFiles.get(index);
			BufferedImage image;

			if (vectorFile.getAspectRatio() == Size.PORTRAIT)
			{
			    BufferedImage imgBg = getBasePageByHeight(index, PREVIEW_SIZE);
			    image = ImageRepresentation.overlayImageRepresentationByHeight(imgBg, vectorFile,PREVIEW_SIZE);
			}
			else
			{
			    BufferedImage imgBg = getBasePageByWidth(index, PREVIEW_SIZE);
			    image = ImageRepresentation.overlayImageRepresentationByWidth(imgBg, vectorFile,PREVIEW_SIZE);
			}

			ImageIcon preview = new ImageIcon(image);

			image.flush();
			image = null;

			previews.set(index, preview);
		}

		fireTableRowsDeleted(rows[0], rows[count]);
	}

	public void setBackground(String file)
	{
		try {
		    if (pdfDoc != null)
			pdfDoc.dispose();
		    pdfDoc = null;
		    background = file;
		    if (file == null)
			return;
		    // Open document
		    pdfDoc = new PdfDocument(file, null);
		}
		catch (PDFSecurityException ex) {
		    ex.printStackTrace();
		}
		catch (PDFException ex) {
		    ex.printStackTrace();
		}

		int rows[] = UIMainTable.getInstance().getSelectedRows();
		int count = rows.length;
		int size = vectorFiles.size();

		for (int index = 0; index < size; index++)
		{
			VectorFile vectorFile = vectorFiles.get(index);
			BufferedImage image;

			if (vectorFile.getAspectRatio() == Size.PORTRAIT)
			{
			    BufferedImage imgBg = getBasePageByHeight(index, PREVIEW_SIZE);
			    image = ImageRepresentation.overlayImageRepresentationByHeight(imgBg, vectorFile,PREVIEW_SIZE);
			}
			else
			{
			    BufferedImage imgBg = getBasePageByWidth(index, PREVIEW_SIZE);
			    image = ImageRepresentation.overlayImageRepresentationByWidth(imgBg, vectorFile,PREVIEW_SIZE);
			}

			ImageIcon preview = new ImageIcon(image);

			image.flush();
			image = null;

			previews.set(index, preview);
		}

		//		fireTableRowsUpdated(rows[0], rows[count-1]);

	}

    public String getBackground() {
	return background;
    }

	public void rotateByAngle(int angle)
	{
		int rows[] = UIMainTable.getInstance().getSelectedRows();

		int count = rows.length;

		for (int index = 0; index < count; index++)
		{
			vectorFiles.get(rows[index]).rotateByAngle(angle);
		}

		fireTableRowsUpdated(rows[0], rows[count - 1]);
	}

	public void zoomIn()
	{
		int selectedRow = UIMainTable.getInstance().getSelectedRow();

		vectorFiles.get(selectedRow).zoomIn();

		fireTableCellUpdated(selectedRow, 0);
	}

	public void zoomOut()
	{
		int selectedRow = UIMainTable.getInstance().getSelectedRow();

		vectorFiles.get(selectedRow).zoomOut();

		fireTableCellUpdated(selectedRow, 0);
	}

	public void zoomActualSize()
	{
		int selectedRow = UIMainTable.getInstance().getSelectedRow();

		vectorFiles.get(selectedRow).zoomActualSize();

		fireTableCellUpdated(selectedRow, 0);
	}

	public ArrayList<String> getTitles()
	{
		ArrayList<String> titles = new ArrayList<String>();

		int rows[] = UIMainTable.getInstance().getSelectedRows();

		int count = rows.length;

		for (int index = 0; index < count; index++)
		{
			titles.add(vectorFiles.get(index).getTitle());
		}

		return titles;
	}

    protected BufferedImage emptyPage(VectorFile file, int ppi,  boolean opaque) {
	Size paperSize = file.getPaperSize(Math.abs(ppi));
	Scale scale = new Scale(file.getSize(), paperSize);
	
	float lineWidth = file.getLineWidth() * ((float)ppi / 72.0f);
	if (lineWidth < VectorFile.LINE_WIDTH_MIN) lineWidth = VectorFile.LINE_WIDTH_MIN;
	
	int width = (int)paperSize.getWidth() + 1;
	int height = (int)paperSize.getHeight() + 1;
	
	BufferedImage imageRepresentation;
	if (opaque) {
	    imageRepresentation = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
	}
	else {
	    imageRepresentation = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
	}

	BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	
	Graphics2D graphics = imageRepresentation.createGraphics();
	graphics.setStroke(stroke);


	if (opaque) {
	    graphics.setColor(java.awt.Color.WHITE);
	    graphics.fillRect(0, 0, width, height);
	}

	return imageRepresentation;
    }

    protected BufferedImage emptyPageByWidth(VectorFile file, int width) {
	Size paperSize = file.getPaperSize(72);

	int height = (int)(paperSize.getHeight() / (paperSize.getWidth() / width));

	Size size = new Size(width, height);
	Scale scale = new Scale(file.getSize(), size);

	float lineWidth = file.getLineWidth() * (width / paperSize.getWidth());
	if (lineWidth < VectorFile.LINE_WIDTH_MIN) lineWidth = VectorFile.LINE_WIDTH_MIN;
	
	BufferedImage imageRepresentation;
	imageRepresentation = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

	BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	
	Graphics2D graphics = imageRepresentation.createGraphics();
	graphics.setStroke(stroke);


	graphics.setColor(java.awt.Color.WHITE);
	graphics.fillRect(0, 0, width, height);
	
	return imageRepresentation;
    }

    protected BufferedImage emptyPageByHeight(VectorFile file, int height) {
	Size paperSize = file.getPaperSize(72);

	int width = (int)(paperSize.getWidth() / (paperSize.getHeight() / height));

	Size size = new Size(width, height);
	Scale scale = new Scale(file.getSize(), size);

	float lineWidth = file.getLineWidth() * (width / paperSize.getWidth());
	if (lineWidth < VectorFile.LINE_WIDTH_MIN) lineWidth = VectorFile.LINE_WIDTH_MIN;
	
	BufferedImage imageRepresentation;
	imageRepresentation = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

	BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	
	Graphics2D graphics = imageRepresentation.createGraphics();
	graphics.setStroke(stroke);


	graphics.setColor(java.awt.Color.WHITE);
	graphics.fillRect(0, 0, width, height);
	
	return imageRepresentation;
    }

    public BufferedImage getBasePage(int index) {
	    int page = index+1;
	    VectorFile file = vectorFiles.get(index);
	    float zoom = ((float)file.getZoom()) / VectorFile.ZOOM_ACTUAL_SIZE;
	    BufferedImage img = null;

	    if (pdfDoc == null)
		return emptyPage(file, file.getZoom(), true);

	    if (pdfDoc.getPageCount() < page)
		return emptyPage(file, file.getZoom(), true);

	    // Get page in RGB
	    PdfPage pdfPage = pdfDoc.getPage(page, PdfDocument.IMAGE_TYPE_RGB, zoom, PdfPage.PAGE_ROTATE_AUTO);
	    if (pdfPage != null) {
		img = pdfPage.getImage();
		// Always make sure to dispose!!!
		pdfPage.dispose();
	    }
	    else {
		return emptyPage(file, file.getZoom(), true);
	    }

	    return img;
    }
    public BufferedImage getBasePageByWidth(int index, int width) {
	int page = index+1;
	VectorFile file = vectorFiles.get(index);
	BufferedImage img = null;
	Size paperSize = file.getPaperSize(72);
	float zoom = width / paperSize.getWidth();

	if (pdfDoc == null)
	    return emptyPageByWidth(file, width);

	if (pdfDoc.getPageCount() < page)
	    return emptyPageByWidth(file, width);

	// Get page in RGB
	PdfPage pdfPage = pdfDoc.getPage(page, PdfDocument.IMAGE_TYPE_RGB, zoom, PdfPage.PAGE_ROTATE_AUTO);
	if (pdfPage != null) {
	    img = pdfPage.getImage();
	    // Always make sure to dispose!!!
	    pdfPage.dispose();

	}
	else {
	    return emptyPageByWidth(file, width);
	}
	
	return img;
    }
    public BufferedImage getBasePageByHeight(int index, int height) {
	    int page = index+1;
	    VectorFile file = vectorFiles.get(index);
	    BufferedImage img = null;
	Size paperSize = file.getPaperSize(72);

	int width = (int)(paperSize.getWidth() / (paperSize.getHeight() / height));

	Size size = new Size(width, height);
	Scale scale = new Scale(file.getSize(), size);
	
	float zoom = width / paperSize.getWidth();

	if (pdfDoc == null)
	    return emptyPageByHeight(file, height);

	if (pdfDoc.getPageCount() < page)
	    return emptyPageByHeight(file, height);

	// Get page in RGB
	PdfPage pdfPage = pdfDoc.getPage(page, PdfDocument.IMAGE_TYPE_RGB, zoom, PdfPage.PAGE_ROTATE_AUTO);
	if (pdfPage != null) {
	    img = pdfPage.getImage();
	    // Always make sure to dispose!!!
	    pdfPage.dispose();

	}
	else {
	    return emptyPageByHeight(file, height);
	}
	
	return img;

    }

	public BufferedImage getZoomedImageRepresentation(int index)
	{
	    VectorFile file = vectorFiles.get(index);
	    BufferedImage img = getBasePage(index);
	    ImageRepresentation.overlayImageRepresentationByPPI(img, file, file.getZoom(), false);
	    return img;
	}

	public BufferedImage getImageRepresentationByPPI(int ppi, int index, boolean opaque)
	{
	    return ImageRepresentation.getImageRepresentationByPPI(vectorFiles.get(index),ppi, opaque);
	}

    public void writePDFRepresentation(DocInfoDict docInfoDict, int index,  OutputStream stream)
	{
		ArrayList<VectorFile> vectorFiles = new ArrayList<VectorFile>();

		try
		{
			if (index == -1)
			{
				int rows[] = UIMainTable.getInstance().getSelectedRows();

				int count = rows.length;

				for (index = 0; index < count; index++)
				{
					vectorFiles.add(this.vectorFiles.get(rows[index]));
				}
				if (background != null) {
				    PDFiTextModule.mergePDF(vectorFiles, docInfoDict, stream, new FileInputStream(background));
				}
				else
				    PDFiTextModule.writePDFData(vectorFiles, docInfoDict, stream);
			}
			else
			{
				vectorFiles.add(this.vectorFiles.get(index));

				System.out.println(background);
				if (background != null)
				    PDFiTextModule.mergePDF(vectorFiles, docInfoDict, stream, new FileInputStream(background));
				else
				    PDFiTextModule.writePDFData(vectorFiles, docInfoDict, stream);
			}
		}
		catch (Exception exception)
		{
			UIExceptionDialog.showDialog(exception);
		}

		vectorFiles = null;
	}

	public ArrayList<String> getSVGRepresentation()
	{
		ArrayList<String> svgRepresentation = new ArrayList<String>();

		int rows[] = UIMainTable.getInstance().getSelectedRows();

		int count = rows.length;

		for (int index = 0; index < count; index++)
		{
			svgRepresentation.add(vectorFiles.get(rows[index]).getSVGRepresentation());
		}

		return svgRepresentation;
	}

	public int size()
	{
		return vectorFiles.size();
	}
}