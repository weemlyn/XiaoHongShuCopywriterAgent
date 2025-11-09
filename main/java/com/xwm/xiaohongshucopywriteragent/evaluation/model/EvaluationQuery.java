package com.xwm.xiaohongshucopywriteragent.evaluation.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 单条用于评估的查询样例
 */
public class EvaluationQuery {

    /**
     * 查询 ID
     */
    private String id;
    /**
     * 查询内容
     */
    private String query;
    /**
     * 需要命中的文档 ID 列表
     */
    private List<String> relevantDocumentIds = new ArrayList<>();
    /**
     * 实际生成的回答
     */
    private String generatedAnswer;
    /**
     * 参考答案列表
     */
    private List<String> referenceAnswers = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<String> getRelevantDocumentIds() {
        return relevantDocumentIds;
    }

    public void setRelevantDocumentIds(List<String> relevantDocumentIds) {
        if (relevantDocumentIds == null) {
            this.relevantDocumentIds = new ArrayList<>();
        } else {
            this.relevantDocumentIds = relevantDocumentIds;
        }
    }

    public String getGeneratedAnswer() {
        return generatedAnswer;
    }

    public void setGeneratedAnswer(String generatedAnswer) {
        this.generatedAnswer = generatedAnswer;
    }

    public List<String> getReferenceAnswers() {
        return referenceAnswers;
    }

    public void setReferenceAnswers(List<String> referenceAnswers) {
        if (referenceAnswers == null) {
            this.referenceAnswers = new ArrayList<>();
        } else {
            this.referenceAnswers = referenceAnswers;
        }
    }
}

