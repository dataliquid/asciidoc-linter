package com.dataliquid.asciidoc.linter.cli.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry for all available CLI commands.
 * Manages command registration and lookup.
 */
public class CommandRegistry {
    
    private final Map<String, Command> commands;
    
    public CommandRegistry() {
        this.commands = new HashMap<>();
        registerDefaultCommands();
    }
    
    /**
     * Registers the default commands.
     */
    private void registerDefaultCommands() {
        register(new LintCommand());
        register(new GuidelinesCommand());
    }
    
    /**
     * Registers a command.
     * 
     * @param command the command to register
     */
    public void register(Command command) {
        commands.put(command.getName(), command);
    }
    
    /**
     * Gets a command by name.
     * 
     * @param name the command name
     * @return the command, or null if not found
     */
    public Command getCommand(String name) {
        return commands.get(name);
    }
    
    /**
     * Checks if a command exists.
     * 
     * @param name the command name
     * @return true if the command exists
     */
    public boolean hasCommand(String name) {
        return commands.containsKey(name);
    }
    
    /**
     * Gets all registered command names.
     * 
     * @return set of command names
     */
    public Set<String> getCommandNames() {
        return commands.keySet();
    }
    
    /**
     * Gets all registered commands.
     * 
     * @return map of commands
     */
    public Map<String, Command> getAllCommands() {
        return new HashMap<>(commands);
    }
    
    /**
     * Prints a summary of all available commands.
     */
    public void printCommandSummary() {
        System.out.println("\nAvailable commands:");
        
        int maxNameLength = commands.keySet().stream()
            .mapToInt(String::length)
            .max()
            .orElse(10);
        
        for (Map.Entry<String, Command> entry : commands.entrySet()) {
            String name = entry.getKey();
            String description = entry.getValue().getDescription();
            String padding = " ".repeat(maxNameLength - name.length() + 2);
            System.out.println("  " + name + padding + description);
        }
        
        System.out.println("\nUse 'asciidoc-linter <command> --help' for more information about a command.");
    }
}