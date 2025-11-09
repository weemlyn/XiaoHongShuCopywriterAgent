package com.xwm.xiaohongshucopywriteragent.evaluation.model;

/**
 * 评估过程中的系统指标
 */
public class SystemMetrics {

    /**
     * 构建向量索引耗时（秒）
     */
    private Double buildSpeedSeconds;
    /**
     * 索引规模（向量条目数量）
     */
    private Integer indexSize;

    public Double getBuildSpeedSeconds() {
        return buildSpeedSeconds;
    }

    public void setBuildSpeedSeconds(Double buildSpeedSeconds) {
        this.buildSpeedSeconds = buildSpeedSeconds;
    }

    public Integer getIndexSize() {
        return indexSize;
    }

    public void setIndexSize(Integer indexSize) {
        this.indexSize = indexSize;
    }
}

