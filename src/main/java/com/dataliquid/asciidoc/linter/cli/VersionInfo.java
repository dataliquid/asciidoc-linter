package com.dataliquid.asciidoc.linter.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Provides version information for the AsciiDoc Linter.
 * Reads version details from Maven-generated pom.properties file.
 */
public class VersionInfo {
    private static final String POM_PROPERTIES_PATH = 
        "/META-INF/maven/com.dataliquid/asciidoc-linter/pom.properties";
    private static final String UNKNOWN = "<unknown>";
    
    private static VersionInfo instance;
    private final String version;
    private final String artifactId;
    private final String groupId;
    
    private VersionInfo() {
        Properties props = new Properties();
        
        try (InputStream is = getClass().getResourceAsStream(POM_PROPERTIES_PATH)) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            // Properties remain empty
        }
        
        this.version = props.getProperty("version", UNKNOWN);
        this.artifactId = props.getProperty("artifactId", UNKNOWN);
        this.groupId = props.getProperty("groupId", UNKNOWN);
    }
    
    /**
     * Gets the singleton instance of VersionInfo.
     * 
     * @return the VersionInfo instance
     */
    public static VersionInfo getInstance() {
        if (instance == null) {
            instance = new VersionInfo();
        }
        return instance;
    }
    
    /**
     * Gets the version string.
     * 
     * @return the version, or "&lt;unknown&gt;" if not available
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * Gets the artifact ID.
     * 
     * @return the artifact ID, or "&lt;unknown&gt;" if not available
     */
    public String getArtifactId() {
        return artifactId;
    }
    
    /**
     * Gets the group ID.
     * 
     * @return the group ID, or "&lt;unknown&gt;" if not available
     */
    public String getGroupId() {
        return groupId;
    }
    
    /**
     * Gets the full version string including artifact ID and version.
     * 
     * @return formatted version string
     */
    public String getFullVersion() {
        return String.format("%s %s", artifactId, version);
    }
}