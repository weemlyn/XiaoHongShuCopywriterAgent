package com.xwm.xiaohongshucopywriteragent.evaluation.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 检索侧指标汇总
 */
public class RetrievalMetrics {

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
     * 平均倒数排名
     */
    private Double meanReciprocalRank;
    /**
     * 平均检索延迟（毫秒）
     */
    private Double averageLatencyMillis;
    /**
     * NDCG 占位字段
     */
    private Double ndcg; // 后续可扩展真实计算

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

    public Double getMeanReciprocalRank() {
        return meanReciprocalRank;
    }

    public void setMeanReciprocalRank(Double meanReciprocalRank) {
        this.meanReciprocalRank = meanReciprocalRank;
    }

    public Double getAverageLatencyMillis() {
        return averageLatencyMillis;
    }

    public void setAverageLatencyMillis(Double averageLatencyMillis) {
        this.averageLatencyMillis = averageLatencyMillis;
    }

    public Double getNdcg() {
        return ndcg;
    }

    public void setNdcg(Double ndcg) {
        this.ndcg = ndcg;
    }
}

