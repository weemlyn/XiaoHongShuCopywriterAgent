package com.xwm.xiaohongshucopywriteragent.evaluation.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 向量库连接参数
 */
public class VectorStoreConfig {

    /**
     * 向量库类型
     */
    private VectorStoreType type = VectorStoreType.PINECONE;
    /**
     * 访问凭证
     */
    private String apiKey;
    /**
     * 云服务环境或区域
     */
    private String environment;
    /**
     * 项目标识（Pinecone 专用）
     */
    private String projectId;
    /**
     * 向量索引名称
     */
    private String index;
    /**
     * 命名空间名称
     */
    private String namespace;
    /**
     * 是否在缺失时自动创建索引
     */
    private boolean createIndexIfMissing = true;
    /**
     * 额外可选参数
     */
    private Map<String, String> options = new HashMap<>();

    public VectorStoreType getType() {
        return type;
    }

    public void setType(VectorStoreType type) {
        this.type = type;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public boolean isCreateIndexIfMissing() {
        return createIndexIfMissing;
    }

    public void setCreateIndexIfMissing(boolean createIndexIfMissing) {
        this.createIndexIfMissing = createIndexIfMissing;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        if (options == null) {
            this.options = new HashMap<>();
        } else {
            this.options = options;
        }
    }
}

