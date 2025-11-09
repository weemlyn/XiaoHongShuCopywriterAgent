package com.xwm.xiaohongshucopywriteragent.evaluation.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 某个策略的整体评估结果
 */
public class ChunkingEvaluationResult {

    /**
     * 分块策略名称
     */
    private String strategyName;
    /**
     * 检索指标汇总
     */
    private RetrievalMetrics retrievalMetrics = new RetrievalMetrics();
    /**
     * 生成指标汇总
     */
    private GenerationMetrics generationMetrics = new GenerationMetrics();
    /**
     * 系统指标汇总
     */
    private SystemMetrics systemMetrics = new SystemMetrics();
    /**
     * 每条查询的评估明细
     */
    private List<QueryEvaluationResult> queryResults = new ArrayList<>();

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public RetrievalMetrics getRetrievalMetrics() {
        return retrievalMetrics;
    }

    public void setRetrievalMetrics(RetrievalMetrics retrievalMetrics) {
        if (retrievalMetrics == null) {
            this.retrievalMetrics = new RetrievalMetrics();
        } else {
            this.retrievalMetrics = retrievalMetrics;
        }
    }

    public GenerationMetrics getGenerationMetrics() {
        return generationMetrics;
    }

    public void setGenerationMetrics(GenerationMetrics generationMetrics) {
        if (generationMetrics == null) {
            this.generationMetrics = new GenerationMetrics();
        } else {
            this.generationMetrics = generationMetrics;
        }
    }

    public SystemMetrics getSystemMetrics() {
        return systemMetrics;
    }

    public void setSystemMetrics(SystemMetrics systemMetrics) {
        if (systemMetrics == null) {
            this.systemMetrics = new SystemMetrics();
        } else {
            this.systemMetrics = systemMetrics;
        }
    }

    public List<QueryEvaluationResult> getQueryResults() {
        return queryResults;
    }

    public void setQueryResults(List<QueryEvaluationResult> queryResults) {
        if (queryResults == null) {
            this.queryResults = new ArrayList<>();
        } else {
            this.queryResults = queryResults;
        }
    }
}

