package com.vectorpen.core;

import java.util.List;
import java.util.ArrayList;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfContentByte;

public class PDFiTextModule {
    public static void writePDFData(List<VectorFile> vectorFiles, DocInfoDict docInfoDict, OutputStream stream) throws Exception {
	
	Document document = new Document();
	PdfWriter pdf = PdfWriter.getInstance(document,stream);
	document.open();

	if (!("".equals(docInfoDict.getTitle())))
	    document.addTitle(docInfoDict.getTitle());
	if (!("".equals(docInfoDict.getAuthor())))
	    document.addAuthor(docInfoDict.getAuthor());
	if (!("".equals(docInfoDict.getSubject())))
	    document.addSubject(docInfoDict.getSubject());
	for (String k : docInfoDict.getKeywords())
	    document.addKeywords(k);

	document.addCreator(getCreator(vectorFiles));

	// Not supported by iText
	// String currentDate = getCurrentDate();
	// document.addCreationDate(currentDate);
	// document.addModDate(currentDate);
	// document.addProducer(PDFModule.PRODUCER);

	document.addCreationDate();
	document.addProducer();

	document.add(new Paragraph("Hello Pdf"));

	PdfContentByte cn = pdf.getDirectContent();
	cn.moveTo(100, 100);
	cn.lineTo(200, 200);
	cn.stroke();

	document.close();
    }

    private static String getCreator(List<VectorFile> vectorFiles)
	throws Exception
    {
	StringBuffer creator = new StringBuffer();

	boolean dhw = false;
	boolean top = false;
	boolean dnt = false;

	ArrayList<String> types = new ArrayList<String>();

	int pageCount = vectorFiles.size();

	for (VectorFile file : vectorFiles) {
	    int type = file.getType();
	    if (type == VectorFile.DHW_FILE && !dhw)
		dhw = types.add("DHW");
	    else if (type == VectorFile.TOP_FILE && !top)
		top = types.add("TOP");
	    else if (type == VectorFile.DNT_FILE && !dnt)
		dnt = types.add("DNT");
	}

	int count = types.size();
	for (int index = 0; index < count; index++) {
	    creator.append(types.get(index));	    
	    if (count == 3 && index == 0)
		creator.append(", ");
	    else if (count > 1 && index < count - 1)
		creator.append(" and ");
	    else
		creator.append(" ");
	}

	if (count > 1)
	    creator.append("Files");
	else if (count == 1)
	    creator.append("File");
	
	return creator.toString();
    }

}