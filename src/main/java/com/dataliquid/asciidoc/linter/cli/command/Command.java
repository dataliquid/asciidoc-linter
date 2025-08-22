package com.dataliquid.asciidoc.linter.cli.command;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 * Interface for CLI commands following the command pattern. Each command represents a specific operation mode of the
 * linter.
 */
public interface Command {

    /**
     * Gets the name of the command. This is used as the subcommand in the CLI.
     *
     * @return the command name
     */
    String getName();

    /**
     * Gets a brief description of what this command does. Used in help text generation.
     *
     * @return the command description
     */
    String getDescription();

    /**
     * Gets the command-specific options.
     *
     * @return the options for this command
     */
    Options getOptions();

    /**
     * Executes the command with the given parsed command line arguments.
     *
     * @param cmd
     *            the parsed command line
     * @return exit code (0 for success, non-zero for error)
     * @throws Exception
     *             if an error occurs during execution
     */
    int execute(CommandLine cmd) throws Exception;

    /**
     * Prints help for this specific command.
     */
    void printHelp();
}
