package com.dataliquid.asciidoc.linter.cli;

import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dataliquid.asciidoc.linter.cli.command.Command;
import com.dataliquid.asciidoc.linter.cli.command.CommandRegistry;
import com.dataliquid.asciidoc.linter.cli.display.SplashScreen;

/**
 * Main CLI entry point for the AsciiDoc tools.
 */
public class MainCLI {

    private static final Logger logger = LogManager.getLogger(MainCLI.class);
    private final CommandRegistry commandRegistry;

    public MainCLI() {
        this.commandRegistry = new CommandRegistry();
    }

    public static void main(String[] args) {
        MainCLI cli = new MainCLI();
        int exitCode = cli.run(args);
        System.exit(exitCode);
    }

    public int run(String[] args) {
        // Handle no arguments
        if (args.length == 0) {
            printMainHelp();
            return 0;
        }

        // Check for global options first
        if (args[0].equals("--help") || args[0].equals("-h")) {
            printMainHelp();
            return 0;
        }

        if (args[0].equals("--version") || args[0].equals("-v")) {
            printVersion();
            return 0;
        }

        // Check if first argument is a command
        String potentialCommand = args[0];

        // Check if command exists
        Command command = commandRegistry.getCommand(potentialCommand);
        if (command == null) {
            System.err.println("Unknown command: " + potentialCommand);
            printMainHelp();
            return 2;
        }

        // Parse command-specific arguments
        String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);

        // Check for splash screen suppression in global args
        boolean showSplash = !containsNoSplash(args) && !potentialCommand.equals("guidelines");
        if (showSplash) {
            SplashScreen.display();
        }

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(command.getOptions(), commandArgs);
            return command.execute(cmd);
        } catch (ParseException e) {
            logger.error("Error: {}", e.getMessage());
            System.err.println("Error: " + e.getMessage());
            System.err.println();
            command.printHelp();
            return 2;
        } catch (Exception e) {
            logger.error("Error executing command: {}", e.getMessage(), e);
            System.err.println("Error: " + e.getMessage());
            return 2;
        }
    }

    private boolean containsNoSplash(String[] args) {
        for (String arg : args) {
            if (arg.equals("--no-splash")) {
                return true;
            }
        }
        return false;
    }

    private void printMainHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(100);

        VersionInfo versionInfo = VersionInfo.getInstance();
        String programName = versionInfo.getArtifactId();

        System.out.println("\n" + programName + " - AsciiDoc Linter");
        System.out.println("Version: " + versionInfo.getFullVersion());
        System.out.println("\nUsage: " + programName + " [global-options] <command> [command-options]");
        System.out.println("\nGlobal Options:");
        System.out.println("  -h, --help       Show this help message");
        System.out.println("  -v, --version    Show version information");
        System.out.println("  --no-splash      Suppress splash screen");

        commandRegistry.printCommandSummary();

        System.out.println("\nExamples:");
        System.out.println("  " + programName + " lint -i \"**/*.adoc\"");
        System.out.println("  " + programName + " guidelines -r rules.yaml -o guide.adoc");
    }

    private void printVersion() {
        System.out.println(VersionInfo.getInstance().getFullVersion());
    }

}
