package com.dataliquid.asciidoc.linter.config.output;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Common.ENABLED;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.SHOW_FILE_LIST;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.SHOW_MOST_COMMON;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.Output.SHOW_STATISTICS;
import static com.dataliquid.asciidoc.linter.config.common.JsonPropertyNames.EMPTY;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Configuration for validation summary in console output.
 */
@JsonDeserialize(builder = SummaryConfig.Builder.class)
public final class SummaryConfig {
    private static final boolean DEFAULT_ENABLED = true;
    private static final boolean DEFAULT_SHOW_STATISTICS = true;
    private static final boolean DEFAULT_SHOW_MOST_COMMON = true;
    private static final boolean DEFAULT_SHOW_FILE_LIST = false;

    private final boolean enabled;
    private final boolean showStatistics;
    private final boolean showMostCommon;
    private final boolean showFileList;

    private SummaryConfig(Builder builder) {
        this.enabled = builder._enabled;
        this.showStatistics = builder._showStatistics;
        this.showMostCommon = builder._showMostCommon;
        this.showFileList = builder._showFileList;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isShowStatistics() {
        return this.showStatistics;
    }

    public boolean isShowMostCommon() {
        return this.showMostCommon;
    }

    public boolean isShowFileList() {
        return this.showFileList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SummaryConfig that = (SummaryConfig) o;
        return enabled == that.enabled && showStatistics == that.showStatistics && showMostCommon == that.showMostCommon
                && showFileList == that.showFileList;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, showStatistics, showMostCommon, showFileList);
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = EMPTY)
    @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
    public static final class Builder {
        private boolean _enabled = DEFAULT_ENABLED;
        private boolean _showStatistics = DEFAULT_SHOW_STATISTICS;
        private boolean _showMostCommon = DEFAULT_SHOW_MOST_COMMON;
        private boolean _showFileList = DEFAULT_SHOW_FILE_LIST;

        private Builder() {
        }

        @JsonProperty(ENABLED)
        public Builder enabled(boolean enabled) {
            this._enabled = enabled;
            return this;
        }

        @JsonProperty(SHOW_STATISTICS)
        public Builder showStatistics(boolean showStatistics) {
            this._showStatistics = showStatistics;
            return this;
        }

        @JsonProperty(SHOW_MOST_COMMON)
        public Builder showMostCommon(boolean showMostCommon) {
            this._showMostCommon = showMostCommon;
            return this;
        }

        @JsonProperty(SHOW_FILE_LIST)
        public Builder showFileList(boolean showFileList) {
            this._showFileList = showFileList;
            return this;
        }

        public SummaryConfig build() {
            return new SummaryConfig(this);
        }
    }
}
