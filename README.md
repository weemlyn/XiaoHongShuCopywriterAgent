# 小红书内容创作助手 - Ming Agent

面向小红书内容创作者的智能文案助手，融合趋势知识库、营销心理学语料与检索增强生成（RAG），覆盖策划—撰写—优化全流程，同时提供分块策略评估能力，支撑持续的检索与生成优化。

## 核心能力
- **对话式文案创作**：结合行业、品牌调性与心理学标签，产出多轮创作建议
- **RAG 检索增强**：支持多向量索引与命名空间切换，动态注入知识库内容
- **知识库运营**：分类上传、批量初始化、元数据规范化，涵盖样本、趋势、心理学与风格模板
- **重排序与融合检索**：内置 Cohere Re-rank，对检索结果按语义得分重排，并配合元数据过滤
- **元数据驱动检索**：可按分类、风格、内容类型等维度定向召回
- **分块策略评测**：统一输出 Recall@K、Precision@K、MRR、NDCG 占位、平均延迟等指标
- **生成质量分析**：忠实度、幻觉率、上下文利用率，采用 LLM 评分 → 嵌入相似度 → 词重合的三级回退策略
- **会话记忆与流式输出**：保留多轮上下文，支持实时响应

## 架构概要
- **对外接口**：`MingAgent` 对话服务、`KnowledgeBaseController` 知识库管理、`ChunkingEvaluationController` 分块评估
- **核心服务**：知识库上传与元数据构建、内容检索与重排序、分块评估流水线
- **向量层**：`EmbeddingStoreConfig` 统一管理 Pinecone 索引，可按策略切换命名空间或切换到内存向量库
- **评估实现**：`ChunkingEvaluationService` 复刻 ChunkingStrategyAnalyst 算法，并通过 `FaithfulnessJudge` 扩展 LLM 评分能力

## 技术栈
- **语言与框架**：Java 17 · Spring Boot 3.5.x
- **AI 能力**：LangChain4j、LangChain4j Easy RAG、DashScope (Qwen) 聊天与嵌入模型
- **向量与评估**：Pinecone、LangChain4j EmbeddingStore、自定义检索/生成指标
- **数据与持久化**：MySQL、MyBatis Plus、MongoDB（会话记忆）
- **配套工具**：LangChain4j Reactor、Knife4j、Cohere Re-rank、Maven

## 快速开始

### 环境要求
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- MongoDB 4.4+
- Pinecone 账号

### 配置说明

1. **数据库配置**
   ```properties
   # MySQL 配置
   spring.datasource.url=jdbc:mysql://localhost:3306/test1
   spring.datasource.username=root
   spring.datasource.password=your_password

   # MongoDB 配置
   spring.data.mongodb.uri=mongodb://localhost:27017/ming_chat_memory
   ```

2. **AI 模型配置**
   ```properties
   # 阿里云百炼平台
   langchain4j.community.dashscope.chat-model.api-key=your_api_key
   langchain4j.community.dashscope.chat-model.model-name=qwen-max
   langchain4j.community.dashscope.streaming-chat-model.model-name=qwen-plus
   langchain4j.community.dashscope.embedding-model.model-name=text-embedding-v3
   ```

3. **向量数据库配置**
   ```properties
   # Pinecone
   pinecone.api-key=[your pinecone.api-key]
   pinecone.index=[your pinecone.index]
   pinecone.namespace=[your pinecone.namespace]
   ```
### 智能问答接口
```http
# 获取知识库元数据
GET /mingAgent/chat
```

### 知识库管理接口
```http
# 获取知识库元数据
GET /api/knowledge-base/metadata

# 获取分类信息
GET /api/knowledge-base/categories

# 按分类上传文档
POST /api/knowledge-base/upload/category

# 批量初始化
POST /api/knowledge-base/init
```
### chunking评估接口
```http
# 获取知识库元数据
GET mingagent/evaluation/chunking
```
