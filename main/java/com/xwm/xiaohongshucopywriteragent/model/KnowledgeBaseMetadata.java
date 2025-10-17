package com.xwm.xiaohongshucopywriteragent.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class KnowledgeBaseMetadata {
    private String knowledgeBaseName;
    private String version;
    private String description;
    private String lastUpdated;
    private Map<String, Category> categories;
    private UsageGuidelines usageGuidelines;

    @Data
    public static class Category {
        private String description;
        private String purpose;
        private Integer fileCount;
        private List<String> contentTypes;
    }

    @Data
    public static class UsageGuidelines {
        private String targetAudience;
        private List<String> contentGoals;
        private List<String> bestPractices;
    }
}