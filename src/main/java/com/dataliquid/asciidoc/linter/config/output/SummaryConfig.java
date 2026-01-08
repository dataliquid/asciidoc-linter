package com.dataliquid.asciidoc.linter.config.output;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.ENABLED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.SHOW_FILE_LIST;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.SHOW_MOST_COMMON;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.SHOW_STATISTICS;

/**
 * Configuration for validation summary in console output.
 */
public final class SummaryConfig {
    private static final boolean DEFAULT_ENABLED = true;
    private static final boolean DEFAULT_SHOW_STATISTICS = true;
    private static final boolean DEFAULT_SHOW_MOST_COMMON = true;
    private static final boolean DEFAULT_SHOW_FILE_LIST = false;

    private final boolean enabledValue;
    private final boolean showStatisticsValue;
    private final boolean showMostCommonValue;
    private final boolean showFileListValue;

    @JsonCreator
    public SummaryConfig(@JsonProperty(ENABLED) Boolean enabled, @JsonProperty(SHOW_STATISTICS) Boolean showStatistics,
            @JsonProperty(SHOW_MOST_COMMON) Boolean showMostCommon,
            @JsonProperty(SHOW_FILE_LIST) Boolean showFileList) {
        this.enabledValue = enabled != null ? enabled : DEFAULT_ENABLED;
        this.showStatisticsValue = showStatistics != null ? showStatistics : DEFAULT_SHOW_STATISTICS;
        this.showMostCommonValue = showMostCommon != null ? showMostCommon : DEFAULT_SHOW_MOST_COMMON;
        this.showFileListValue = showFileList != null ? showFileList : DEFAULT_SHOW_FILE_LIST;
    }

    public boolean isEnabled() {
        return this.enabledValue;
    }

    public boolean isShowStatistics() {
        return this.showStatisticsValue;
    }

    public boolean isShowMostCommon() {
        return this.showMostCommonValue;
    }

    public boolean isShowFileList() {
        return this.showFileListValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SummaryConfig that = (SummaryConfig) o;
        return enabledValue == that.enabledValue && showStatisticsValue == that.showStatisticsValue
                && showMostCommonValue == that.showMostCommonValue && showFileListValue == that.showFileListValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabledValue, showStatisticsValue, showMostCommonValue, showFileListValue);
    }
}
