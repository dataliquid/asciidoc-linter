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
        this.enabled = builder.enabled;
        this.showStatistics = builder.showStatistics;
        this.showMostCommon = builder.showMostCommon;
        this.showFileList = builder.showFileList;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public boolean isShowStatistics() {
        return showStatistics;
    }
    
    public boolean isShowMostCommon() {
        return showMostCommon;
    }
    
    public boolean isShowFileList() {
        return showFileList;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SummaryConfig that = (SummaryConfig) o;
        return enabled == that.enabled &&
                showStatistics == that.showStatistics &&
                showMostCommon == that.showMostCommon &&
                showFileList == that.showFileList;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(enabled, showStatistics, showMostCommon, showFileList);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @JsonPOJOBuilder(withPrefix = EMPTY)
    public static final class Builder {
        private boolean enabled = DEFAULT_ENABLED;
        private boolean showStatistics = DEFAULT_SHOW_STATISTICS;
        private boolean showMostCommon = DEFAULT_SHOW_MOST_COMMON;
        private boolean showFileList = DEFAULT_SHOW_FILE_LIST;
        
        private Builder() {
        }
        
        @JsonProperty(ENABLED)
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        @JsonProperty(SHOW_STATISTICS)
        public Builder showStatistics(boolean showStatistics) {
            this.showStatistics = showStatistics;
            return this;
        }
        
        @JsonProperty(SHOW_MOST_COMMON)
        public Builder showMostCommon(boolean showMostCommon) {
            this.showMostCommon = showMostCommon;
            return this;
        }
        
        @JsonProperty(SHOW_FILE_LIST)
        public Builder showFileList(boolean showFileList) {
            this.showFileList = showFileList;
            return this;
        }
        
        public SummaryConfig build() {
            return new SummaryConfig(this);
        }
    }
}