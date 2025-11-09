package com.xwm.xiaohongshucopywriteragent.evaluation.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 分块策略评估请求体
 */
public class ChunkingEvaluationRequest {

    /**
     * 需要评估的分块策略集合
     */
    private List<EvaluationTarget> targets = new ArrayList<>();
    /**
     * 评估时使用的查询样例集合
     */
    private List<EvaluationQuery> queries = new ArrayList<>();
    /**
     * 需要统计的 K 值列表
     */
    private List<Integer> kValues = new ArrayList<>(Arrays.asList(1, 3, 5, 10));
    /**
     * 检索结果的最低得分阈值
     */
    private Double minScore = 0.0;
    /**
     * 是否计算生成侧指标
     */
    private boolean includeGenerationMetrics = true;
    /**
     * 生成语句与上下文的相似度阈值
     */
    private Double contextSimilarityThreshold = 0.6;
    /**
     * 覆盖默认的检索返回条数
     */
    private Integer maxResultsOverride;

    public List<EvaluationTarget> getTargets() {
        return targets;
    }

    public void setTargets(List<EvaluationTarget> targets) {
        if (targets == null) {
            this.targets = new ArrayList<>();
        } else {
            this.targets = targets;
        }
    }

    public List<EvaluationQuery> getQueries() {
        return queries;
    }

    public void setQueries(List<EvaluationQuery> queries) {
        if (queries == null) {
            this.queries = new ArrayList<>();
        } else {
            this.queries = queries;
        }
    }

    public List<Integer> getKValues() {
        return kValues;
    }

    public void setKValues(List<Integer> kValues) {
        if (kValues == null || kValues.isEmpty()) {
            this.kValues = new ArrayList<>(Arrays.asList(1, 3, 5, 10));
        } else {
            this.kValues = kValues;
        }
    }

    public Double getMinScore() {
        return minScore;
    }

    public void setMinScore(Double minScore) {
        if (minScore == null) {
            this.minScore = 0.0;
        } else {
            this.minScore = minScore;
        }
    }

    public boolean isIncludeGenerationMetrics() {
        return includeGenerationMetrics;
    }

    public void setIncludeGenerationMetrics(boolean includeGenerationMetrics) {
        this.includeGenerationMetrics = includeGenerationMetrics;
    }

    public Double getContextSimilarityThreshold() {
        return contextSimilarityThreshold;
    }

    public void setContextSimilarityThreshold(Double contextSimilarityThreshold) {
        if (contextSimilarityThreshold == null) {
            this.contextSimilarityThreshold = 0.6;
        } else {
            this.contextSimilarityThreshold = contextSimilarityThreshold;
        }
    }

    public Integer getMaxResultsOverride() {
        return maxResultsOverride;
    }

    public void setMaxResultsOverride(Integer maxResultsOverride) {
        this.maxResultsOverride = maxResultsOverride;
    }
}

