package com.dataliquid.asciidoc.linter.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import com.dataliquid.asciidoc.linter.config.common.Severity;
import com.dataliquid.asciidoc.linter.config.output.OutputFormat;

/**
 * Configuration object containing parsed CLI arguments.
 */
public class CLIConfig {

    private final List<String> inputPatterns;
    private final Path baseDirectory;
    private final Path configFile;
    private final OutputFormat outputConfigFormat;
    private final Path outputConfigFile;
    private final String reportFormat;
    private final Path reportOutput;
    private final Severity failLevel;

    private CLIConfig(Builder builder) {
        this.inputPatterns = Objects
                .requireNonNull(builder._inputPatterns,
                        "[" + getClass().getName() + "] inputPatterns must not be null");
        if (this.inputPatterns.isEmpty()) {
            throw new IllegalArgumentException("inputPatterns must not be empty");
        }
        this.baseDirectory = Objects
                .requireNonNull(builder._baseDirectory,
                        "[" + getClass().getName() + "] baseDirectory must not be null");
        this.configFile = builder._configFile;
        this.outputConfigFormat = builder._outputConfigFormat;
        this.outputConfigFile = builder._outputConfigFile;
        this.reportFormat = Objects
                .requireNonNull(builder._reportFormat, "[" + getClass().getName() + "] reportFormat must not be null");
        this.reportOutput = builder._reportOutput;
        this.failLevel = Objects
                .requireNonNull(builder._failLevel, "[" + getClass().getName() + "] failLevel must not be null");
    }

    public List<String> getInputPatterns() {
        return inputPatterns;
    }

    public Path getBaseDirectory() {
        return baseDirectory;
    }

    public Path getConfigFile() {
        return configFile;
    }

    public OutputFormat getOutputConfigFormat() {
        return outputConfigFormat;
    }

    public Path getOutputConfigFile() {
        return outputConfigFile;
    }

    public String getReportFormat() {
        return reportFormat;
    }

    public Path getReportOutput() {
        return reportOutput;
    }

    public Severity getFailLevel() {
        return failLevel;
    }

    public boolean isOutputToFile() {
        return reportOutput != null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<String> _inputPatterns;
        private Path _baseDirectory = Paths.get(System.getProperty("user.dir"));
        private Path _configFile;
        private OutputFormat _outputConfigFormat;
        private Path _outputConfigFile;
        private String _reportFormat = "console";
        private Path _reportOutput;
        private Severity _failLevel = Severity.ERROR;

        public Builder inputPatterns(List<String> inputPatterns) {
            this._inputPatterns = inputPatterns;
            return this;
        }

        public Builder baseDirectory(Path baseDirectory) {
            this._baseDirectory = baseDirectory;
            return this;
        }

        public Builder configFile(Path configFile) {
            this._configFile = configFile;
            return this;
        }

        public Builder outputConfigFormat(OutputFormat outputConfigFormat) {
            this._outputConfigFormat = outputConfigFormat;
            return this;
        }

        public Builder outputConfigFile(Path outputConfigFile) {
            this._outputConfigFile = outputConfigFile;
            return this;
        }

        public Builder reportFormat(String reportFormat) {
            this._reportFormat = reportFormat;
            return this;
        }

        public Builder reportOutput(Path reportOutput) {
            this._reportOutput = reportOutput;
            return this;
        }

        public Builder failLevel(Severity failLevel) {
            this._failLevel = failLevel;
            return this;
        }

        public CLIConfig build() {
            return new CLIConfig(this);
        }
    }
}
