package com.dataliquid.asciidoc.linter.cli.command;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dataliquid.asciidoc.linter.cli.CLIConfig;
import com.dataliquid.asciidoc.linter.cli.CLIRunner;
import com.dataliquid.asciidoc.linter.cli.ConfigurationDisplay;
import com.dataliquid.asciidoc.linter.cli.VersionInfo;
import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.output.OutputFormat;

/**
 * Command for linting AsciiDoc files.
 */
public class LintCommand implements Command {

    private static final Logger logger = LogManager.getLogger(LintCommand.class);

    @Override
    public String getName() {
        return "lint";
    }

    @Override
    public String getDescription() {
        return "Validate AsciiDoc files against configured rules";
    }

    @Override
    public Options getOptions() {
        Options options = new Options();

        // Input patterns (required for execution, but not for help)
        options
                .addOption(Option
                        .builder("i")
                        .longOpt("input")
                        .hasArg()
                        .argName("patterns")
                        .desc("Comma-separated Ant file patterns (e.g., '**/*.adoc,docs/**/*.asciidoc')")
                        .build());

        // Configuration file
        options
                .addOption(Option
                        .builder("r")
                        .longOpt("rule")
                        .hasArg()
                        .argName("file")
                        .desc("YAML rule configuration file (default: .linter-rule-config.yaml)")
                        .build());

        // Report format
        options
                .addOption(Option
                        .builder("f")
                        .longOpt("report-format")
                        .hasArg()
                        .argName("format")
                        .desc("Report format: console, json, json-compact (default: console)")
                        .build());

        // Report output
        options
                .addOption(Option
                        .builder("o")
                        .longOpt("report-output")
                        .hasArg()
                        .argName("file/directory")
                        .desc("Report output file or directory (default: stdout)")
                        .build());

        // Fail level
        options
                .addOption(Option
                        .builder("l")
                        .longOpt("fail-level")
                        .hasArg()
                        .argName("level")
                        .desc("Exit code 1 on: error, warn, info (default: error)")
                        .build());

        // Output configuration (predefined)
        options
                .addOption(Option
                        .builder()
                        .longOpt("output-config")
                        .hasArg()
                        .argName("name")
                        .desc("Predefined output configuration: enhanced, simple, compact (default: enhanced)")
                        .build());

        // Output configuration (custom file)
        options
                .addOption(Option
                        .builder()
                        .longOpt("output-config-file")
                        .hasArg()
                        .argName("file")
                        .desc("Custom YAML output configuration file for console formatting")
                        .build());

        // Help
        options.addOption(Option.builder("h").longOpt("help").desc("Show help for lint command").build());

        return options;
    }

    @Override
    public int execute(CommandLine cmd) throws Exception {
        // Handle help
        if (cmd.hasOption("help")) {
            printHelp();
            return 0;
        }

        // Check required input
        if (!cmd.hasOption("input")) {
            System.err.println("Error: --input is required for lint command"); // NOPMD - intentional CLI output
            printHelp();
            return 2;
        }

        try {
            // Parse configuration
            CLIConfig config = parseConfiguration(cmd);

            // Display configuration (if not suppressed globally)
            boolean showConfig = !cmd.hasOption("quiet");
            if (showConfig) {
                ConfigurationDisplay configDisplay = new ConfigurationDisplay();
                configDisplay.display(config);
            }

            // Run linter
            CLIRunner runner = new CLIRunner();
            return runner.run(config);

        } catch (IllegalArgumentException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error: {}", e.getMessage());
            }
            System.err.println("Error: " + e.getMessage()); // NOPMD - intentional CLI output
            return 2;
        }
    }

    @Override
    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(100);

        VersionInfo versionInfo = VersionInfo.getInstance();
        String programName = versionInfo.getArtifactId();

        String header = "\nValidates AsciiDoc files against configurable rules.\n\n";
        String footer = "\nExamples:\n" + "  " + programName + " lint -i \"**/*.adoc\"\n" + "  " + programName
                + " lint -i \"docs/**/*.adoc,examples/**/*.asciidoc\" -f json -o report.json\n" + "  " + programName
                + " lint --input \"src/*/docs/**/*.adoc,README.adoc\" --rule strict.yaml --fail-level warn\n" + "  "
                + programName + " lint -i \"**/*.adoc\" --output-config simple\n" + "  " + programName
                + " lint -i \"**/*.adoc\" --output-config-file my-output.yaml\n" + "\nAnt Pattern Syntax:\n"
                + "  **  - matches any number of directories\n"
                + "  *   - matches any number of characters (except /)\n" + "  ?   - matches exactly one character\n"
                + "\nExit codes:\n" + "  0 - Success, no violations or only below fail level\n"
                + "  1 - Violations at or above fail level found\n" + "  2 - Invalid arguments or runtime error\n";

        formatter.printHelp(programName + " lint -i <patterns> [options]", header, getOptions(), footer, false);
    }

    private CLIConfig parseConfiguration(CommandLine cmd) {
        CLIConfig.Builder builder = CLIConfig.builder();

        // Input patterns (required)
        String inputValue = cmd.getOptionValue("input");
        List<String> patterns = Arrays
                .stream(inputValue.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        if (patterns.isEmpty()) {
            throw new IllegalArgumentException("No input patterns provided");
        }

        builder.inputPatterns(patterns);

        // Config file
        if (cmd.hasOption("rule")) {
            builder.configFile(Paths.get(cmd.getOptionValue("rule")));
        }

        // Output configuration
        if (cmd.hasOption("output-config") && cmd.hasOption("output-config-file")) {
            throw new IllegalArgumentException("Cannot use both --output-config and --output-config-file. Choose one.");
        }

        if (cmd.hasOption("output-config")) {
            String configName = cmd.getOptionValue("output-config");
            try {
                OutputFormat format = OutputFormat.fromValue(configName);
                builder.outputConfigFormat(format);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Invalid output config name: " + configName + ". Valid options: enhanced, simple, compact");
            }
        }

        if (cmd.hasOption("output-config-file")) {
            builder.outputConfigFile(Paths.get(cmd.getOptionValue("output-config-file")));
        }

        // Report format
        if (cmd.hasOption("report-format")) {
            String format = cmd.getOptionValue("report-format");
            if (!format.equals("console") && !format.equals("json") && !format.equals("json-compact")) {
                throw new IllegalArgumentException(
                        "Invalid report format: " + format + ". Valid values are: console, json, json-compact");
            }
            builder.reportFormat(format);
        }

        // Report output
        if (cmd.hasOption("report-output")) {
            builder.reportOutput(Paths.get(cmd.getOptionValue("report-output")));
        }

        // Fail level
        if (cmd.hasOption("fail-level")) {
            String level = cmd.getOptionValue("fail-level").toUpperCase();
            try {
                builder.failLevel(Severity.valueOf(level));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Invalid fail level: " + level + ". Valid values are: error, warn, info");
            }
        }

        return builder.build();
    }
}
