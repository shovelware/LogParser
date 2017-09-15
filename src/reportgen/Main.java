package reportgen;

import implemented.HTMLFileWriter;
import implemented.LogFileReader;
import interfaces.FileReader;
import interfaces.HTMLWriter;

//Picking up of test status counts in htmlwriter is ugly
//Picking up of skipped tests in reader is ugly

public class Main {
	public static void main(String[] args) {
		String absoluteCurDir = java.nio.file.Paths.get("").toAbsolutePath().toString();
		String inputDir = absoluteCurDir;
		String outputDir = absoluteCurDir;
		
		FileReader reader_ = new LogFileReader();
		HTMLWriter writer_;
		writer_ = new HTMLFileWriter();
		//writer_ = new RawWriter();
		
		//No arguments, output to a folder
		if (args.length == 0);
		
		//First arg is input directory, second is output
		else if (args.length > 0 && 2 >= args.length)	{
			if (args[0].equalsIgnoreCase("help"))	{
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
		
		System.out.println("Scanning directory...");
		reader_.scanDir(inputDir);

		System.out.println("Preparing...");
		writer_.beginReport(outputDir, reader_.parseSummary());
		
		int count = 1;
		while(reader_.hasNextTest())
		{
			System.out.println("Processing test: " + count);
			writer_.writeTest(reader_.parseNextTest());
			count++;
		}

		System.out.println("Finalizing...");
		writer_.endReport();		
		System.out.println("Complete.");
	}	
	
	//Help function for outputting
	private static void giveHelp() {
		System.console().writer().println("USAGE:");
		System.console().writer().println("\nAll paths should be absolute.\n");
		System.console().writer().println("java Reporter\n Takes input  .logs from current folder, generates report.html in this folder\n");
		System.console().writer().println("java Reporter [inputDirectory]\n Manually set input folder, generates report in this folder\n");
		System.console().writer().println("java Reporter [inputDirectory] [outputDirectory]\n Manually set both directories, operate as normal\n");
	}
}
