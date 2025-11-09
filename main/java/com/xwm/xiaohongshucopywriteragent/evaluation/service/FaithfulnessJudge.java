package com.xwm.xiaohongshucopywriteragent.evaluation.service;

/**
 * 可选的忠实度评分模型，通常由大语言模型实现
 */
@FunctionalInterface
public interface FaithfulnessJudge {

    /**
     * 根据上下文与回答输出忠实度得分
     * @param answer 回答文本
     * @param context 上下文文本
     * @return 0-1 之间的分数，null 表示无法给分
     */
    Double score(String answer, String context);
}

