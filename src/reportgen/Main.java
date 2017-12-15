package reportgen;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
		
		//TODO:
		//NEW CLI 
		//-input "dir"
		//-output "dir"
		//-debug [flag]
		//-lang EN NL [fallback to NL]
		
		//Setup cmd flags
		Options opts = new Options();
		opts.addOption(Option.builder("help").desc("Display help").required(false).hasArg(false).build());
		opts.addOption(Option.builder("input").desc("Input directory").required(false).hasArg(true).build());
		opts.addOption(Option.builder("output").desc("Output directory").required(false).hasArg(true).build());
		opts.addOption(Option.builder("debug").desc("Include Debug logs").required(false).hasArg(false).build());
		opts.addOption(Option.builder("lang").longOpt("language").desc("Set language (NL Default)").required(false).hasArg(true).build());

		//Setup app
		FileReader reader_ = new LogFileReader();
		HTMLWriter writer_ = new HTMLFileWriter();
		
		//Parse the options
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(opts, args);
			System.out.println("Parsed args");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		for (Option s : cmd.getOptions())
		{
			System.out.println(s.getOpt() + ( s.getValue() != null ? " : " + s.getValue() : ""));
		}
		System.out.println("===");
		
		if (cmd.hasOption("help")) { showHelp(); return;}
		if (cmd.hasOption("input")) { inputDir = cmd.getOptionValue("input"); }
		if (cmd.hasOption("output")) { outputDir = cmd.getOptionValue("output"); }
		if (cmd.hasOption("language")) { writer_.setOption("language", cmd.getOptionValue("language")); System.out.println("Language: " +  cmd.getOptionValue("language")); }
		if (cmd.hasOption("debug")) { reader_.setFlag("debug"); writer_.setFlag("debug"); System.out.println("Debug logging enabled");}
		//Add trailing slash
		inputDir += "\\";
		outputDir += "\\";
		
		//Init logic, Args complete
		System.out.println("Input folder set to: " + inputDir);
		System.out.println("Output folder set to: " + outputDir + "\n");
		
		System.out.println("Scanning directory...");
		reader_.scanDir(inputDir);
		System.out.println("===");
		
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
	private static void showHelp() {
		System.out.println("USAGE:");
		System.out.println("\nAll paths should be absolute.\n");
		System.out.println("java Reporter\t: Searches for input .logs in current folder, generates report.html in current folder\n");
		System.out.println("-input \"C:\\Path\\To\\Logs\\Folder\"\t: Manually set input folder.\n");
		System.out.println("-output \"C:\\Path\\To\\Report\\Folder\"\t: Manually set output folder.\n");
		System.out.println("-debug \t: Include debug level logs in Report.\n");
		System.out.println("-language \t: Language of Report. Currently supported: NL (Default), EN.\n");
		System.out.println("-help \t: Display this message.\n");
		
		System.out.println("Utilises Apache Commons CLI 1.4: http://commons.apache.org/proper/commons-cli/");
		System.out.println("===");
	}
}
