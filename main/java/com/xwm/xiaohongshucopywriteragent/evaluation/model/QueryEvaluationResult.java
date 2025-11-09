package com.xwm.xiaohongshucopywriteragent.evaluation.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单条查询的评估结果
 */
public class QueryEvaluationResult {

    /**
     * 查询样例 ID
     */
    private String queryId;
    /**
     * 查询内容
     */
    private String queryText;
    /**
     * 各 K 值下的召回率
     */
    private Map<Integer, Double> recallAtK = new HashMap<>();
    /**
     * 各 K 值下的精确率
     */
    private Map<Integer, Double> precisionAtK = new HashMap<>();
    /**
     * 各 K 值下的 F1
     */
    private Map<Integer, Double> f1AtK = new HashMap<>();
    /**
     * 倒数排名
     */
    private Double reciprocalRank;
    /**
     * 检索耗时（毫秒）
     */
    private Double latencyMillis;
    /**
     * 忠实度
     */
    private Double faithfulness;
    /**
     * 幻觉率
     */
    private Double hallucinationRate;
    /**
     * 上下文利用率
     */
    private Double contextUtilization;
    /**
     * 检索到的文档列表
     */
    private List<RetrievedDocument> retrievedDocuments = new ArrayList<>();

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    public Map<Integer, Double> getRecallAtK() {
        return recallAtK;
    }

    public void setRecallAtK(Map<Integer, Double> recallAtK) {
        if (recallAtK == null) {
            this.recallAtK = new HashMap<>();
        } else {
            this.recallAtK = recallAtK;
        }
    }

    public Map<Integer, Double> getPrecisionAtK() {
        return precisionAtK;
    }

    public void setPrecisionAtK(Map<Integer, Double> precisionAtK) {
        if (precisionAtK == null) {
            this.precisionAtK = new HashMap<>();
        } else {
            this.precisionAtK = precisionAtK;
        }
    }

    public Map<Integer, Double> getF1AtK() {
        return f1AtK;
    }

    public void setF1AtK(Map<Integer, Double> f1AtK) {
        if (f1AtK == null) {
            this.f1AtK = new HashMap<>();
        } else {
            this.f1AtK = f1AtK;
        }
    }

    public Double getReciprocalRank() {
        return reciprocalRank;
    }

    public void setReciprocalRank(Double reciprocalRank) {
        this.reciprocalRank = reciprocalRank;
    }

    public Double getLatencyMillis() {
        return latencyMillis;
    }

    public void setLatencyMillis(Double latencyMillis) {
        this.latencyMillis = latencyMillis;
    }

    public Double getFaithfulness() {
        return faithfulness;
    }

    public void setFaithfulness(Double faithfulness) {
        this.faithfulness = faithfulness;
    }

    public Double getHallucinationRate() {
        return hallucinationRate;
    }

    public void setHallucinationRate(Double hallucinationRate) {
        this.hallucinationRate = hallucinationRate;
    }

    public Double getContextUtilization() {
        return contextUtilization;
    }

    public void setContextUtilization(Double contextUtilization) {
        this.contextUtilization = contextUtilization;
    }

    public List<RetrievedDocument> getRetrievedDocuments() {
        return retrievedDocuments;
    }

    public void setRetrievedDocuments(List<RetrievedDocument> retrievedDocuments) {
        if (retrievedDocuments == null) {
            this.retrievedDocuments = new ArrayList<>();
        } else {
            this.retrievedDocuments = retrievedDocuments;
        }
    }
}

