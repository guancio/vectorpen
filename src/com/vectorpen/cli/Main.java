package com.vectorpen.cli;

import java.io.File;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;

import com.vectorpen.core.FileInput;
import com.vectorpen.core.VectorFile;
import com.vectorpen.core.Path;

public class Main {
    static public void main(String args[]) {
	try {
	    String fileName = "samples/top/aiptek-mynote.top";
	    VectorFile file = FileInput.readFile(new File(fileName));
	    System.out.println("File successfully opened");

	    FileOutputStream fileOutputStream = new FileOutputStream(fileName+".svg");
	    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fileOutputStream , "ISO-8859-1"));
	    writer.write(file.getSVGRepresentation());
	    writer.close();
	    writer = null;
	    
	    fileOutputStream.close();
	    
	    System.gc();
	}
	catch (java.io.IOException ex) {
	}
	catch (java.util.zip.DataFormatException ex) {
	}
    }
}