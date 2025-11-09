package com.xwm.xiaohongshucopywriteragent.evaluation.model;

/**
 * 评估目标即一套分块策略
 */
public class EvaluationTarget {

    /**
     * 分块策略名称
     */
    private String strategyName;
    /**
     * 指定向量库配置
     */
    private VectorStoreConfig vectorStore;

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public VectorStoreConfig getVectorStore() {
        return vectorStore;
    }

    public void setVectorStore(VectorStoreConfig vectorStore) {
        this.vectorStore = vectorStore;
    }
}

