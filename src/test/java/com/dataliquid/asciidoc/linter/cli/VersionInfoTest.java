package com.dataliquid.asciidoc.linter.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for VersionInfo class.
 */
class VersionInfoTest {

    @Test
    @DisplayName("getInstance should return singleton instance")
    void testSingletonInstance() {
        VersionInfo instance1 = VersionInfo.getInstance();
        VersionInfo instance2 = VersionInfo.getInstance();

        assertSame(instance1, instance2, "Should return the same instance");
    }

    @Test
    @DisplayName("getArtifactId should return expected value")
    void testGetArtifactId() {
        VersionInfo versionInfo = VersionInfo.getInstance();

        assertEquals("asciidoc-linter", versionInfo.getArtifactId());
    }

    @Test
    @DisplayName("getGroupId should return expected value")
    void testGetGroupId() {
        VersionInfo versionInfo = VersionInfo.getInstance();

        assertEquals("com.dataliquid", versionInfo.getGroupId());
    }

    @Test
    @DisplayName("getVersion should return non-null value")
    void testGetVersion() {
        VersionInfo versionInfo = VersionInfo.getInstance();

        assertNotNull(versionInfo.getVersion());
        // In development environment, it should be "<unknown>"
        // In built JAR, it would be the actual version from pom.properties
    }

    @Test
    @DisplayName("getFullVersion should return formatted string")
    void testGetFullVersion() {
        VersionInfo versionInfo = VersionInfo.getInstance();
        String fullVersion = versionInfo.getFullVersion();

        assertNotNull(fullVersion);
        assertEquals("asciidoc-linter <unknown>", fullVersion);
        // Should be "<unknown> <unknown>" in dev or "asciidoc-linter X.Y.Z" in JAR
    }
}
