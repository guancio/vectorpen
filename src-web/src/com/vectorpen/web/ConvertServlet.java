package com.vectorpen.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import java.util.logging.Logger;

import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;

import com.vectorpen.core.FileInput;
import com.vectorpen.core.VectorFile;

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
	    resp.setContentType("text/svg");

	    FileItemIterator iterator = upload.getItemIterator(req);
	    while (iterator.hasNext()) {
		FileItemStream item = iterator.next();
		InputStream stream = item.openStream();

		if (item.isFormField()) {
		    log.warning("Got a form field: " + item.getFieldName());
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
		    VectorFile vec = FileInput.readStream(stream, title, format);
		    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(resp.getOutputStream() , "ISO-8859-1"));
		    writer.write(vec.getSVGRepresentation());

		    log.warning("Conversion completed");
		}
	    }
	} catch (Exception ex) {
	    throw new ServletException(ex);
	}
    }
}