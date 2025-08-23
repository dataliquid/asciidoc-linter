package com.dataliquid.asciidoc.linter.cli.display;

import java.time.Year;

import com.dataliquid.asciidoc.linter.cli.VersionInfo;

/**
 * Displays a splash screen for the AsciiDoc Linter CLI.
 */
public class SplashScreen {

    private static final String[] ASCII_ART = { "     _    ____   ____ ___ ___ ____             ",
            "    / \\  / ___| / ___|_ _|_ _|  _ \\  ___   ___ ", "   / _ \\ \\___ \\| |    | | | || | | |/ _ \\ / __|",
            "  / ___ \\ ___) | |___ | | | || |_| | (_) | (__ ", " /_/   \\_\\____/ \\____|___|___|____/ \\___/ \\___|",
            "                                                " };

    /**
     * Displays the splash screen on the console.
     */
    public static void display() {
        // ASCII Art
        for (String line : ASCII_ART) {
            System.out.println(line); // NOPMD - intentional CLI output
        }

        // Version line
        VersionInfo versionInfo = VersionInfo.getInstance();
        String versionLine = String.format("        L I N T E R   v%s", versionInfo.getVersion());
        System.out.println(versionLine); // NOPMD - intentional CLI output
        System.out.println(); // NOPMD - intentional CLI output

        // Description
        System.out.println("  Powerful linter for AsciiDoc documents"); // NOPMD - intentional CLI output

        // Copyright
        String copyright = String.format("  © %d dataliquid - Apache License 2.0", Year.now().getValue());
        System.out.println(copyright); // NOPMD - intentional CLI output
        System.out.println(); // NOPMD - intentional CLI output
    }
}
