package com.dataliquid.asciidoc.linter.cli.command;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dataliquid.asciidoc.linter.cli.VersionInfo;
import com.dataliquid.asciidoc.linter.config.LinterConfiguration;
import com.dataliquid.asciidoc.linter.config.loader.ConfigurationLoader;
import com.dataliquid.asciidoc.linter.documentation.AsciiDocAuthorGuidelineGenerator;
import com.dataliquid.asciidoc.linter.documentation.RuleDocumentationGenerator;
import com.dataliquid.asciidoc.linter.documentation.VisualizationStyle;
import com.dataliquid.asciidoc.linter.output.ConsoleWriter;
import com.dataliquid.asciidoc.linter.output.OutputWriter;

/**
 * Command for generating author guidelines from linter configuration.
 */
public class GuidelinesCommand implements Command {

    private static final Logger logger = LogManager.getLogger(GuidelinesCommand.class);
    private final ConfigurationLoader configLoader;
    private final OutputWriter outputWriter;

    public GuidelinesCommand() {
        this(ConsoleWriter.getInstance());
    }

    public GuidelinesCommand(OutputWriter outputWriter) {
        this.configLoader = new ConfigurationLoader();
        this.outputWriter = outputWriter;
    }

    @Override
    public String getName() {
        return "guidelines";
    }

    @Override
    public String getDescription() {
        return "Generate author guidelines showing all validation requirements";
    }

    @Override
    public Options getOptions() {
        Options options = new Options();

        // Configuration file (required for execution, but not for help)
        options
                .addOption(Option
                        .builder("r")
                        .longOpt("rule")
                        .hasArg()
                        .argName("file")
                        .desc("YAML rule configuration file (required)")
                        .build());

        // Output file
        options
                .addOption(Option
                        .builder("o")
                        .longOpt("output")
                        .hasArg()
                        .argName("file")
                        .desc("Output file for generated guidelines (default: stdout)")
                        .build());

        // Visualization style
        options
                .addOption(Option
                        .builder("s")
                        .longOpt("style")
                        .hasArg()
                        .argName("styles")
                        .desc("Comma-separated visualization styles: tree, nested, breadcrumb, table (default: tree)")
                        .build());

        // Help
        options.addOption(Option.builder("h").longOpt("help").desc("Show help for guidelines command").build());

        return options;
    }

    @Override
    public int execute(CommandLine cmd) throws Exception {
        // Handle help
        if (cmd.hasOption("help")) {
            printHelp();
            return 0;
        }

        // Check required rule file
        if (!cmd.hasOption("rule")) {
            outputWriter.writeError("Error: --rule is required for guidelines command");
            printHelp();
            return 2;
        }

        try {
            // Load configuration
            String configPath = cmd.getOptionValue("rule");
            LinterConfiguration config = loadConfiguration(configPath);

            // Parse visualization styles
            Set<VisualizationStyle> styles = parseVisualizationStyles(cmd.getOptionValue("style"));

            // Create generator
            RuleDocumentationGenerator generator = new AsciiDocAuthorGuidelineGenerator(styles);

            // Determine output
            String outputPath = cmd.getOptionValue("output");

            if (outputPath != null) {
                // Write to file
                generateToFile(generator, config, outputPath);
                outputWriter.writeLine("Guidelines generated successfully: " + outputPath);
            } else {
                // Write to stdout
                generateToStdout(generator, config);
            }

            return 0;

        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error generating guidelines: {}", e.getMessage());
            }
            outputWriter.writeError("Error generating guidelines: " + e.getMessage());
            return 2;
        }
    }

    @Override
    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(100);

        VersionInfo versionInfo = VersionInfo.getInstance();
        String programName = versionInfo.getArtifactId();

        String header = "\nGenerates author guidelines from linter rule configuration.\n\n";
        String footer = "\nExamples:\n" + "  " + programName + " guidelines -r rules.yaml\n" + "  " + programName
                + " guidelines -r rules.yaml -o guidelines.adoc\n" + "  " + programName
                + " guidelines --rule strict.yaml --output guide.md --style tree,table\n" + "\nVisualization Styles:\n"
                + "  tree       - Hierarchical tree structure\n" + "  nested     - Nested sections\n"
                + "  breadcrumb - Breadcrumb navigation\n" + "  table      - Tabular format\n";

        formatter.printHelp(programName + " guidelines -r <file> [options]", header, getOptions(), footer, false);
    }

    private LinterConfiguration loadConfiguration(String configPath) throws IOException {
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            throw new IOException("Configuration file not found: " + configPath);
        }

        return configLoader.loadConfiguration(configFile.toPath());
    }

    private Set<VisualizationStyle> parseVisualizationStyles(String stylesArg) {
        if (stylesArg == null || stylesArg.trim().isEmpty()) {
            // Default to tree visualization
            return Set.of(VisualizationStyle.TREE);
        }

        return Arrays
                .stream(stylesArg.split(","))
                .map(String::trim)
                .map(VisualizationStyle::fromName)
                .collect(Collectors.toSet());
    }

    private void generateToFile(RuleDocumentationGenerator generator, LinterConfiguration config, String outputPath)
            throws IOException {

        File outputFile = new File(outputPath);

        // Create parent directories if needed
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Failed to create output directory: " + parentDir);
            }
        }

        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(Files.newOutputStream(outputFile.toPath()), StandardCharsets.UTF_8))) {
            generator.generate(config, writer);
        }
    }

    private void generateToStdout(RuleDocumentationGenerator generator, LinterConfiguration config) {
        PrintWriter writer = new PrintWriter(System.out);
        generator.generate(config, writer);
        writer.flush();
    }
}
