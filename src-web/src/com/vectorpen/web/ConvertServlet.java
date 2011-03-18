package com.vectorpen.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;


import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;

import com.vectorpen.core.FileInput;
import com.vectorpen.core.VectorFile;
import com.vectorpen.core.PDFiTextModule;
import com.vectorpen.core.DocInfoDict;

public class ConvertServlet extends HttpServlet {
    private static final Logger log =
	Logger.getLogger(ConvertServlet.class.getName());

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
              throws IOException {

	resp.setContentType("text/plain");
	resp.getWriter().println("Hello, ");
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws IOException, ServletException {

	try {
	    ServletFileUpload upload = new ServletFileUpload();

	    VectorFile vec = null;
	    String outFormat = "SVG";

	    FileItemIterator iter = upload.getItemIterator(req);
	    while (iter.hasNext()) {
		FileItemStream item = iter.next();
		InputStream stream = item.openStream();

		if (item.isFormField()) {
		    String value = Streams.asString(stream);
		    log.warning("Got a form field: " + item.getFieldName());
		    if ("SVG".equals(item.getFieldName()) && "SVG".equalsIgnoreCase(value))
			outFormat = "SVG";
		    if ("PDF".equals(item.getFieldName()) && "PDF".equalsIgnoreCase(value))
			outFormat = "PDF";
			
		} else {
		    log.warning("Got an uploaded file: " + item.getFieldName() +
				", name = " + item.getName());
		    String [] fields = item.getName().split("\\.");
		    String title = fields[0];
		    String format = "."+fields[1];
		    log.warning("Converting file <" + title + "> using format <" + format + ">");

		    // You now have the filename (item.getName() and the
		    // contents (which you can read from stream). Here we just
		    // print them back out to the servlet output stream, but you
		    // will probably want to do something more interesting (for
		    // example, wrap them in a Blob and commit them to the
		    // datastore).
		    vec = FileInput.readStream(stream, title, format);
		}
	    }

	    if ("SVG".equalsIgnoreCase(outFormat)) {
		resp.setContentType("text/xml" );
		resp.setHeader( "Content-Disposition", "attachment; filename=\"" + vec.getTitle() + ".svg\"" );

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(resp.getOutputStream() , "ISO-8859-1"));
		writer.write(vec.getSVGRepresentation());
		writer.flush();
	    }
	    else if ("PDF".equalsIgnoreCase(outFormat)) {
		resp.setContentType("application/pdf" );
		resp.setHeader( "Content-Disposition", "attachment; filename=\"" + vec.getTitle() + ".pdf\"" );

		List<String> keys = new Vector<String>();
		keys.add("ProducedWithVectorPen");
		List<VectorFile> vecs = new Vector<VectorFile>();
		vecs.add(vec);
		DocInfoDict docInfoDict = new DocInfoDict(vec.getTitle(), "Author", "Subject", keys);
		PDFiTextModule.writePDFData(vecs, docInfoDict, resp.getOutputStream());
	    }
	    
	    log.warning("Conversion completed");
	} catch (Exception ex) {
	    throw new ServletException(ex);
	}
    }
}