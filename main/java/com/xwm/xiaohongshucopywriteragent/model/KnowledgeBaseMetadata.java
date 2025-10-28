package com.xwm.xiaohongshucopywriteragent.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class KnowledgeBaseMetadata {
    @JsonProperty("knowledge_base_name")
    private String knowledgeBaseName;
    private String version;
    private String description;
    @JsonProperty("last_updated")
    private String lastUpdated;
    @JsonProperty("total_files")
    private Integer totalFiles;
    private Map<String, Category> categories;
    @JsonProperty("content_features")
    private ContentFeatures contentFeatures;
    @JsonProperty("usage_guidelines")
    private UsageGuidelines usageGuidelines;

    @Data
    public static class Category {
        private String description;
        private String purpose;
        @JsonProperty("file_count")
        private Integer fileCount;
        @JsonProperty("file_pattern")
        private String filePattern;
        @JsonProperty("content_types")
        private List<String> contentTypes;
        @JsonProperty("sample_titles")
        private List<String> sampleTitles;
        @JsonProperty("key_topics")
        private List<String> keyTopics;
        @JsonProperty("key_concepts")
        private List<String> keyConcepts;
        @JsonProperty("brand_types")
        private List<String> brandTypes;
        @JsonProperty("framework_types")
        private List<String> frameworkTypes;
    }

    @Data
    public static class ContentFeatures {
        @JsonProperty("update_frequency")
        private String updateFrequency;
        @JsonProperty("data_source")
        private String dataSource;
        @JsonProperty("content_quality")
        private String contentQuality;
        @JsonProperty("applicable_scenarios")
        private List<String> applicableScenarios;
        @JsonProperty("trend_coverage")
        private String trendCoverage;
    }

    @Data
    public static class UsageGuidelines {
        @JsonProperty("target_audience")
        private String targetAudience;
        @JsonProperty("content_goals")
        private List<String> contentGoals;
        @JsonProperty("best_practices")
        private List<String> bestPractices;
        @JsonProperty("recommended_workflow")
        private List<String> recommendedWorkflow;
    }
}