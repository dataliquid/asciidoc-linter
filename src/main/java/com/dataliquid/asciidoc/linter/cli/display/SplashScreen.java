package com.dataliquid.asciidoc.linter.cli.display;

import java.time.Year;

import com.dataliquid.asciidoc.linter.cli.VersionInfo;
import com.dataliquid.asciidoc.linter.output.ConsoleWriter;
import com.dataliquid.asciidoc.linter.output.OutputWriter;

/**
 * Displays a splash screen for the AsciiDoc Linter CLI.
 */
public class SplashScreen {

    private static final String[] ASCII_ART = { "     _    ____   ____ ___ ___ ____             ",
            "    / \\  / ___| / ___|_ _|_ _|  _ \\  ___   ___ ", "   / _ \\ \\___ \\| |    | | | || | | |/ _ \\ / __|",
            "  / ___ \\ ___) | |___ | | | || |_| | (_) | (__ ", " /_/   \\_\\____/ \\____|___|___|____/ \\___/ \\___|",
            "                                                " };

    /**
     * Displays the splash screen using the default console writer. This method
     * maintains backward compatibility.
     */
    public static void display() {
        display(ConsoleWriter.getInstance());
    }

    /**
     * Displays the splash screen using the specified output writer.
     *
     * @param outputWriter the output writer to use for displaying the splash screen
     */
    public static void display(OutputWriter outputWriter) {
        // ASCII Art
        for (String line : ASCII_ART) {
            outputWriter.writeLine(line);
        }

        // Version line
        VersionInfo versionInfo = VersionInfo.getInstance();
        String versionLine = String.format("        L I N T E R   v%s", versionInfo.getVersion());
        outputWriter.writeLine(versionLine);
        outputWriter.writeLine();

        // Description
        outputWriter.writeLine("  Powerful linter for AsciiDoc documents");

        // Copyright
        String copyright = String.format("  © %d dataliquid - Apache License 2.0", Year.now().getValue());
        outputWriter.writeLine(copyright);
        outputWriter.writeLine();
    }
}
