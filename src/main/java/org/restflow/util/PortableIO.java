package org.restflow.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

public class PortableIO {

	public static final String EOL = System.getProperty("line.separator");
	public static final String EscapedEOLString;
	
	static {
		if (EOL.equals("\n")) {
			EscapedEOLString = "\\n";
		} else if (EOL.equals("\r\n")){
			EscapedEOLString = "\\r\\n";
		} else { 
			EscapedEOLString = null;
		}
	}

	public static String readTextFileOnClasspath(String path) throws IOException {
		InputStream stream = PortableIO.class.getClassLoader().getResourceAsStream(path);
		InputStreamReader reader = new InputStreamReader(stream);
		String contents = readTextFromReader(reader);
		return contents;
	}	
	
	public static String readTextFileOnFilesystem(String path) throws IOException {
		FileReader fileReader = new FileReader(path);
		String text = readTextFromReader(fileReader);
		return text;
	}
	
	public static String readTextFile(File file) throws IOException {
		FileReader fileReader = new FileReader(file);
		String text = readTextFromReader(fileReader);
		return text;
	}

	public static String readTextFromReader(InputStreamReader fileReader) throws IOException {
		
		BufferedReader reader = new BufferedReader(fileReader);

		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(EOL);
		}
	
		return stringBuilder.toString();
	}
	
	public static String getCurrentDirectoryPath() throws IOException {

		// get a file handle to the current directory
		File currentDirectory = new File (".");
		
		// get the absolute path of the current directory
		String path = currentDirectory.getAbsolutePath();
		
		// format path in a portable manner
		String normalizedPath = normalizePath(path);
		
		// return the platform-independent current directory path
		return normalizedPath;
	}
	
	public static String normalizePath(String path) {
		
		// replace backslashes in path with forward slashes to prevent problems
		// using the directory as a replacement string
		// (backslashes occur in Windows paths but Java can use forward slashes on Windows)
		path = path.replace('\\', '/');
		
		// return the path minus the final character (a period)
		return path.substring(0, path.length() - 1);
	}

	public static File createUniqueDirectory(File parentDirectory, String baseDirectoryName) {

        String uniqueDirectoryName;
        File uniqueDirectory;
        int uniqueSuffix = 0;
        
        // create path to run directory
        do {
        	uniqueDirectoryName = baseDirectoryName;
        	if (uniqueSuffix > 0) uniqueDirectoryName = uniqueDirectoryName + "_" + uniqueSuffix;
        	uniqueDirectory = new File(parentDirectory, uniqueDirectoryName);
        	uniqueSuffix++;
        }   while (!uniqueDirectory.mkdirs());
        
        return uniqueDirectory;
	}
	
	private static final String[] month_abbreviation = { "Jan", "Feb", "Mar", "Apr", "May", "Jun",
		"Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

	public static String getLogTimeStamp() {
		
		// get time stamp for start of current run
        Calendar runStartTimeStamp = new GregorianCalendar();
        
    	// create the time stamp string
        NumberFormat twoDigitFormat = NumberFormat.getInstance();
        twoDigitFormat.setMinimumIntegerDigits(2);
        String timeStamp = 
        	month_abbreviation[runStartTimeStamp.get(Calendar.MONTH)] + " " +
        	twoDigitFormat.format(runStartTimeStamp.get(Calendar.DAY_OF_MONTH)) + " " +
        	twoDigitFormat.format(runStartTimeStamp.get(Calendar.HOUR_OF_DAY)) + ":" +
        	twoDigitFormat.format(runStartTimeStamp.get(Calendar.MINUTE)) + ":" +
        	twoDigitFormat.format(runStartTimeStamp.get(Calendar.SECOND)) + " ";
        
        return timeStamp;
	}
	
	public static File createUniqueTimeStampedDirectory(String parentDirectoryPath, String baseDirectoryName) throws Exception {
		
		String runStartTimeStampString = createTimeStampString();
		
        // create the parent directory if it doesn't already exist
        File parentDirectory = new File(parentDirectoryPath);
        parentDirectory.mkdirs();
        
        return PortableIO.createUniqueDirectory(parentDirectory, baseDirectoryName + "_" + runStartTimeStampString);
	}
	
	public static String createTimeStampString() {
		// get time stamp for start of current run
        Calendar runStartTimeStamp = new GregorianCalendar();
        
    	// create the time stamp string
        NumberFormat twoDigitFormat = NumberFormat.getInstance();
        twoDigitFormat.setMinimumIntegerDigits(2);
        String runStartTimeStampString = 
        	twoDigitFormat.format(runStartTimeStamp.get(Calendar.DAY_OF_MONTH)) +
        	month_abbreviation[runStartTimeStamp.get(Calendar.MONTH)] +
        	runStartTimeStamp.get(Calendar.YEAR) +
        	"_" +
        	twoDigitFormat.format(runStartTimeStamp.get(Calendar.HOUR_OF_DAY)) +
        	twoDigitFormat.format(runStartTimeStamp.get(Calendar.MINUTE)) +
        	twoDigitFormat.format(runStartTimeStamp.get(Calendar.SECOND));
		return runStartTimeStampString;
	}
	
	public static String normalizeLineEndings(String s) {
	//	return s.replace("\r\n", "\n");
		return s.replace("\r", "");
	}
	
	private static Pattern lineBreakSequencePattern = Pattern.compile("(" + EOL + ")+");
	
	public static String replaceLineBreaksWithSpaces(String multiLineString) {

		// remove any leading or trailing new lines
		String trimmedMultiLineString = multiLineString.trim();
		
		// match and replace each line break sequences with a single space
		// first replacing each system-specific sequence with a space
		// and then each leftover newline character with another space
		// (the latter in case there is a mix of line ending types)
		Matcher matcher = lineBreakSequencePattern.matcher(trimmedMultiLineString);
		String singleLineString = matcher.replaceAll(" ").replace("\n"," ");
		
		return singleLineString;
	}
	
	public static class StreamSink implements Runnable {
		
		private final InputStream _inputStream;
		private String capturedData = "";
		
		public StreamSink(InputStream inputStream) {
			_inputStream = inputStream;
		}

		public void run() {
			
		    StringBuffer buffer = new StringBuffer();
		    byte[] byteArray = new byte[4096];
		    int numBytesRead;
		    
			try {

				while ((numBytesRead = _inputStream.read(byteArray)) != -1) {
			        buffer.append(new String(byteArray, 0, numBytesRead));
			    }
			    
			    _inputStream.close();
			    
			} catch (IOException e) {
				throw new RuntimeException(
						"IOException receiving data from child process.");
			}

			capturedData = buffer.toString();
		}
 
		public String toString() { 
			return capturedData; 
		}
	}
	
	public static StreamSink[] runProcess(String cmdLine, String stdIn, String[] env, File workingDirectory) throws IOException, InterruptedException {
		
		// start the external process using the provided command line string
		Process process = Runtime.getRuntime().exec(cmdLine, env, workingDirectory);

		// send the provided standard input data to the stdin stream of the external process
		PrintStream stdinStream = new PrintStream(process.getOutputStream(), true);
		stdinStream.print(stdIn);
		stdinStream.close();
		
		// create a sink for the process stdout stream
		InputStream stdoutStream = process.getInputStream();
		StreamSink stdoutSink = new StreamSink(stdoutStream);

		// create a sink for the process stderr stream
		InputStream stderrStream = process.getErrorStream();		
		StreamSink stderrSink = new StreamSink(stderrStream);
		
		// create threads to read the stdin and stderr streams and capture them to the sinks
		Thread p1 = new Thread(stdoutSink);
		Thread p2 = new Thread(stderrSink);
		
		// start the two threads
		p1.start();
		p2.start();
		
		// wait for the external process to complete
		process.waitFor();
		
		// wait for the two output stream reading threads to complete
		p1.join();
		p2.join();
		
		// return the two stream sinks as an array
		return new StreamSink[] { stdoutSink, stderrSink };
	}
}


