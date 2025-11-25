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
import com.dataliquid.asciidoc.linter.output.ConsoleWriter;
import com.dataliquid.asciidoc.linter.output.OutputWriter;

/**
 * Main CLI entry point for the AsciiDoc tools.
 */
public class MainCLI {

    private static final Logger logger = LogManager.getLogger(MainCLI.class);

    // Constants
    private static final String NO_SPLASH_ARG = "--no-splash";
    private final CommandRegistry commandRegistry;
    private final OutputWriter outputWriter;

    public MainCLI() {
        this(ConsoleWriter.getInstance());
    }

    public MainCLI(OutputWriter outputWriter) {
        this.outputWriter = outputWriter;
        this.commandRegistry = new CommandRegistry(outputWriter);
    }

    public static void main(String... args) {
        MainCLI cli = new MainCLI();
        int exitCode = cli.run(args);
        System.exit(exitCode);
    }

    public int run(String... args) {
        // Handle no arguments
        if (args.length == 0) {
            printMainHelp();
            return 0;
        }

        // Check for global options first
        if ("--help".equals(args[0]) || "-h".equals(args[0])) {
            printMainHelp();
            return 0;
        }

        if ("--version".equals(args[0]) || "-v".equals(args[0])) {
            printVersion();
            return 0;
        }

        // Check if first argument is a command
        String potentialCommand = args[0];

        // Check if command exists
        Command command = commandRegistry.getCommand(potentialCommand);
        if (command == null) {
            outputWriter.writeError("Unknown command: " + potentialCommand);
            printMainHelp();
            return 2;
        }

        // Parse command-specific arguments
        String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);

        // Check for splash screen suppression in global args
        boolean showSplash = !containsNoSplash(args) && !"guidelines".equals(potentialCommand);
        if (showSplash) {
            SplashScreen.display(outputWriter);
        }

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(command.getOptions(), commandArgs);
            return command.execute(cmd);
        } catch (ParseException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error: {}", e.getMessage());
            }
            outputWriter.writeError("Error: " + e.getMessage());
            outputWriter.writeLine();
            command.printHelp();
            return 2;
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error executing command: {}", e.getMessage(), e);
            }
            outputWriter.writeError("Error: " + e.getMessage());
            return 2;
        }
    }

    private boolean containsNoSplash(String... args) {
        for (String arg : args) {
            if (NO_SPLASH_ARG.equals(arg)) {
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

        outputWriter.writeLine("\n" + programName + " - AsciiDoc Linter");
        outputWriter.writeLine("Version: " + versionInfo.getFullVersion());
        outputWriter.writeLine("\nUsage: " + programName + " [global-options] <command> [command-options]");
        outputWriter.writeLine("\nGlobal Options:");
        outputWriter.writeLine("  -h, --help       Show this help message");
        outputWriter.writeLine("  -v, --version    Show version information");
        outputWriter.writeLine("  --no-splash      Suppress splash screen");

        commandRegistry.printCommandSummary();

        outputWriter.writeLine("\nExamples:");
        outputWriter.writeLine("  " + programName + " lint -i \"**/*.adoc\"");
        outputWriter.writeLine("  " + programName + " guidelines -r rules.yaml -o guide.adoc");
    }

    private void printVersion() {
        outputWriter.writeLine(VersionInfo.getInstance().getFullVersion());
    }

}
