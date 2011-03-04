package com.vectorpen.cli;

import java.io.File;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;

import java.util.List;
import java.util.Vector;

import com.vectorpen.core.FileInput;
import com.vectorpen.core.VectorFile;
import com.vectorpen.core.Path;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;

import org.apache.commons.cli.ParseException;

public class Main {
    static public void main(String args[]) {
	Options options = new Options();
	options.addOption(
			  OptionBuilder.withArgName( "help" )
			  .withDescription(  "this help page" )
			  .withLongOpt("help") 
			  .create( "h" )
			  );
	options.addOption(
			  OptionBuilder.withArgName( "input" )
			  .hasArg()
			  .withDescription(  "use given file/path for input" )
			  .withLongOpt("input") 
			  .create( "i" )
			  );
	options.addOption(
			  OptionBuilder.withArgName( "format" )
			  .hasArg()
			  .withDescription(  "process stdin using the given format. e.g. top" )
			  .withLongOpt("format") 
			  .create( "f" )
			  );
	options.addOption(
			  OptionBuilder.withArgName( "title" )
			  .hasArg()
			  .withDescription(  "process stdin using the given title" )
			  .withLongOpt("title") 
			  .create( "t" )
			  );
	options.addOption(
			  OptionBuilder.withArgName( "output" )
			  .hasArg()
			  .withDescription(  "use given file/path for output" )
			  .withLongOpt("output") 
			  .create( "o" )
			  );

	CommandLineParser parser = new GnuParser();
	CommandLine line = null;
	try {
	    line = parser.parse( options, args );
	}
	catch (ParseException ex) {
	    HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp( "vectorpen", options );
	    return;
	}

	String input = null;
	String format = null;
	String title = null;
	String output = null;

	if (line.hasOption("h")) {
	    HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp( "vectorpen", options, true);
	    return;
	}

	input = line.getOptionValue("i");
	if (input != null)
	    input = input.trim();
	format = line.getOptionValue("f");
	if (format != null)
	    format = format.trim();
	title = line.getOptionValue("t");
	if (title != null)
	    title = title.trim();
	output = line.getOptionValue("o");
	if (output != null)
	    output = output.trim();

	// System.out.println(input);
	// System.out.println(title);
	// System.out.println(format);

	
	try {
	    List<VectorFile> files = new Vector<VectorFile>();

	    if (input != null) {
		File in = new File(input);
		if (in.isDirectory()) {
		    for (String iInput : in.list()) {
			// System.out.println(input + "/" + iInput);
			File iIn = new File(input + "/" + iInput);
			files.add(FileInput.readFile(iIn));
		    }
		}
		else {
		    files.add(FileInput.readFile(in));
		}
	    }
	    else {
		files.add(FileInput.readStream(System.in, title, format));
	    }

	    for (VectorFile f : files) {
		// System.out.println(f.getTitle());
		OutputStream out = System.out;
		if (output != null && files.size() == 1)
		    out = new FileOutputStream(output);
		else if (output != null)
		    out = new FileOutputStream(output + "/" + f.getTitle() + ".svg");
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out , "ISO-8859-1"));
		writer.write(f.getSVGRepresentation());
		writer.flush();
		
		System.gc();
	    }
	    
	}
	catch (java.io.IOException ex) {
	    ex.printStackTrace();
	}
	catch (java.util.zip.DataFormatException ex) {
	    ex.printStackTrace();
	}
    }
}