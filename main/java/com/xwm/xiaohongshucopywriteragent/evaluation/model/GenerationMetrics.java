package com.xwm.xiaohongshucopywriteragent.evaluation.model;

/**
 * 生成侧指标汇总
 */
public class GenerationMetrics {

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
}

