package com.xwm.xiaohongshucopywriteragent.evaluation.model;

/**
 * 评估时常用的向量库类型说明
 */
public enum VectorStoreType {

    /**
     * Pinecone 无服务器向量库
     */
    PINECONE,

    /**
     * 内存向量库方便本地快速试验
     */
    IN_MEMORY
}

