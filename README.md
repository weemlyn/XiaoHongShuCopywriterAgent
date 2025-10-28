# 小红书内容创作助手 - Ming Agent

## 项目简介

小红书内容创作助手是一个智能内容创作平台，专门为小红书平台的内容创作者、品牌方和营销人员提供专业的内容创作支持。系统基于AI技术，结合丰富的知识库和RAG技术，为用户提供高质量的内容创作建议。

## 核心功能概览

### 智能内容创作
基于2025年最新趋势，提供标题优化、内容结构设计、情感化表达技巧和互动策略设计，帮助用户创作出更具吸引力和转化力的内容。

### 营销策略分析
运用营销心理学原理，深度分析用户心理和行为模式，提供消费决策优化、品牌差异化定位和转化率提升策略。

### 品牌风格塑造
提供15种不同博主风格的语言模板，帮助建立独特的品牌调性，实现内容风格统一和专业细分领域覆盖。

### 趋势分析洞察
实时跟踪平台政策变化、用户行为模式和内容趋势，提供前瞻性的竞争策略分析和内容趋势预测。

## 技术架构

### 后端技术栈
- **Spring Boot 3.5.5**：主框架
- **Java 17**：开发语言
- **LangChain4j 1.0.0-beta3**：AI应用开发框架
- **阿里云百炼平台**：大语言模型服务（通义千问）
- **MongoDB**：聊天记忆存储
- **MySQL**：关系型数据存储
- **MyBatis Plus 3.5.11**：ORM框架
- **Pinecone**：向量数据库
- **Knife4j 4.3.0**：API文档

### 核心组件
- **MingAgent**：智能对话助手
- **RAG系统**：检索增强生成
- **向量化存储**：知识库向量化
- **流式输出**：实时响应支持
- **记忆管理**：多轮对话上下文

## 知识库体系

### 知识库版本：3.0.0（2025-10-14更新）

#### 1. 爆款笔记样本库（samples）
- **数量**：10个成功案例
- **领域**：美妆、穿搭、美食、旅行、生活、健身、读书、职场
- **特色**：真实美学、垂直细分、社会责任、文化出海等热门元素

#### 2. 行业趋势分析（industry_trends）
- **2025年Q3趋势报告**
- **小红书穿搭趋势分析**
- **平台算法与推荐机制**
- **生活方式内容趋势**
- **美妆行业趋势分析**

#### 3. 营销心理学（marketing_psychology）
- **内容营销心理学策略**
- **品牌心理学与用户认知**
- **影响力与说服力原则**
- **数字营销心理学**
- **文案心理学摘录**
- **消费心理学应用**
- **用户行为分析**
- **社交媒体营销心理学**

#### 4. 品牌风格语料（brand_styles）
- **15种博主风格**：健康养生、健身、咖啡馆、宠物、家居、投资理财、护肤、教育、旅行、时尚穿搭、母婴、科技数码、美食、职场
- **语言特色**：每种风格都有独特的表达方式和情感色彩

#### 5. 文案创作框架（writing_frameworks）
- **标题公式**：多种标题创作模板
- **内容结构模板**：标准化内容组织方式
- **互动策略设计**：提升用户参与度的方法
- **内容优化策略**：持续改进内容质量
- **引导转折CTA结构**：优化转化路径
- **情感化文案技巧**：增强内容感染力
- **爆款内容创作公式**：系统化创作方法论

## 快速开始

### 环境要求
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- MongoDB 4.4+
- Pinecone账户

### 配置说明

1. **数据库配置**
```properties
# MySQL配置
spring.datasource.url=jdbc:mysql://localhost:3306/test1
spring.datasource.username=root
spring.datasource.password=your_password

# MongoDB配置
spring.data.mongodb.uri=mongodb://localhost:27017/ming_chat_memory
```

2. **AI模型配置**
```properties
# 阿里云百炼平台配置
langchain4j.community.dashscope.chat-model.api-key=your_api_key
langchain4j.community.dashscope.chat-model.model-name=qwen-max
langchain4j.community.dashscope.streaming-chat-model.model-name=qwen-plus
langchain4j.community.dashscope.embedding-model.model-name=text-embedding-v3
```

3. **向量数据库配置**
```properties
# Pinecone配置
pinecone.api-key=[your pinecone.api-key]
pinecone.index=[your pinecone.index]
pinecone.namespace=[your pinecone.namespace]
```


### 知识库管理接口
```http
# 获取知识库元数据
GET /api/knowledge-base/metadata

# 获取分类信息
GET /api/knowledge-base/categories

# 按分类上传文档
POST /api/knowledge-base/upload/category

# 初始化知识库
POST /api/knowledge-base/init

# 获取知识库状态
GET /api/knowledge-base/status
```

## 使用指南

### 1. 内容创作流程
1. **明确目标**：确定内容类型和目标用户
2. **选择风格**：根据品牌调性选择相应的语言风格
3. **应用框架**：使用相应的文案创作框架
4. **优化调整**：基于营销心理学原理进行优化
5. **趋势融合**：融入2025年最新趋势元素

### 2. 最佳实践
- **真实性优先**：所有建议都基于真实数据和实际案例
- **用户导向**：始终以用户需求和体验为中心
- **趋势敏感**：紧跟2025年最新趋势，及时调整策略
- **专业深度**：运用营销心理学和用户行为分析
- **实用性强**：所有建议都具有可操作性和可复制性

### 3. 注意事项
- 严格遵守平台规则，避免违规内容
- 保持内容真实性和原创性
- 关注用户反馈，持续优化建议
- 结合具体场景，提供个性化建议
- 注重社会责任，传播正能量

## 项目结构

```
src/main/java/com/xwm/xiaohongshucopywriteragent/
├── assistant/          # 智能助手
├── bean/              # 数据模型
├── config/            # 配置类
├── controller/        # 控制器
├── mapper/            # 数据访问层
├── model/             # 实体模型
├── service/           # 业务逻辑层
├── store/             # 存储层
└── tools/             # 工具类

src/main/resources/
├── data/              # 知识库数据
│   ├── brand_styles/  # 品牌风格
│   ├── industry_trends/ # 行业趋势
│   ├── marketing_psychology/ # 营销心理学
│   ├── samples/       # 爆款样本
│   └── writing_frameworks/ # 创作框架
├── mapper/            # MyBatis映射文件
└── application.properties # 配置文件
```

## 贡献指南

1. Fork项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建Pull Request

## 联系方式

如有问题或建议，请通过以下方式联系：
- 邮箱：wim66621@gmail.com

## 更新日志

### v1.0.0 (2025-10-14)
- 基于2025年10月最新趋势更新知识库
- 新增真实美学、垂直细分、社会责任等热门元素
- 优化RAG检索算法，提升内容质量
- 增强流式输出功能，改善用户体验
- 完善API文档和接口规范

---

**注意**：本项目仅供学习和研究使用，请遵守相关法律法规和平台规则。
