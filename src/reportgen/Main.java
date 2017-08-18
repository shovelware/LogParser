package reportgen;

import interfaces.FileReader;
import interfaces.HTMLWriter;
import implemented.HTMLWriterImpl;
import implemented.LogFileReader;

public class Main {
	public static void main(String[] args) {
		String absoluteCurDir = java.nio.file.Paths.get("").toAbsolutePath().toString();
		String inputDir = absoluteCurDir;
		String outputDir = absoluteCurDir;
		
		FileReader reader_ = new LogFileReader();
		HTMLWriter writer_ = new HTMLWriterImpl();
		
		//No arguments, output to a folder
		if (args.length == 0);
		
		//First arg is input directory, second is output
		else if (args.length > 0 && 2 >= args.length)	{
			if (args[0].equals("help"))	{
				//If they ask for help, be nice
				giveHelp();
				return;
			}
			
			else {
				inputDir = args[0];
				outputDir = args[1];
			}
		}
		
		//Add trailing slash
		inputDir += "\\";
		outputDir += "\\";
		
		//Init logic
		System.out.println("INIT " + args.length + " arguments provided");
		System.out.println("Input folder set to: " + inputDir);
		System.out.println("Output folder set to: " + outputDir + "\n");
		
		
		//Actual work is done by classes
		System.out.println("Reading directory...");
		reader_.readDir(inputDir);
		writer_.setData(reader_.getEntries());
		writer_.setSummary(reader_.getReport());
		
		System.out.println("\nWriting report...");
		writer_.writeReport(outputDir);
		System.out.println("Complete.");
	}	
	
	//Help function for outputting
	private static void giveHelp() {
		System.console().writer().println("USAGE:");
		System.console().writer().println("\nAll paths should be absolute.\n");
		System.console().writer().println("java Reporter\n Takes input .xml logs from current folder, generates report.html in this folder\n");
		System.console().writer().println("java Reporter [inputDirectory]\n Manually set input folder, generates report in this folder\n");
		System.console().writer().println("java Reporter [inputDirectory] [outputDirectory]\n Manually set both directories, operate as normal\n");
	}
}

//Filetype specific
//TestEntry
//  LogEntry++
//Output to files
