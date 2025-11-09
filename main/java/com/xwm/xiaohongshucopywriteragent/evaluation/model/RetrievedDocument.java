package com.xwm.xiaohongshucopywriteragent.evaluation.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 单条检索结果与其元数据
 */
public class RetrievedDocument {

    /**
     * 文档 ID 或文件名
     */
    private String documentId;
    /**
     * 检索得分
     */
    private Double score;
    /**
     * 文本片段预览
     */
    private String textSnippet;
    /**
     * 文档元数据
     */
    private Map<String, Object> metadata = new HashMap<>();

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getTextSnippet() {
        return textSnippet;
    }

    public void setTextSnippet(String textSnippet) {
        this.textSnippet = textSnippet;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        if (metadata == null) {
            this.metadata = new HashMap<>();
        } else {
            this.metadata = metadata;
        }
    }
}

