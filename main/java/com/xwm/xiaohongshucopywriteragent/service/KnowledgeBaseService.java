package com.xwm.xiaohongshucopywriteragent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xwm.xiaohongshucopywriteragent.model.KnowledgeBaseMetadata;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KnowledgeBaseService {

    @Autowired
    private EmbeddingModel embeddingModel;
    
    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;
    
    @Autowired
    private ObjectMapper objectMapper;

    private final Path uploadDir = Paths.get("uploads");
    private KnowledgeBaseMetadata metadata;

    public KnowledgeBaseService() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录", e);
        }
    }

    /**
     * 在依赖注入完成后初始化
     */
    @PostConstruct
    public void init() {
        loadMetadata();
    }

    /**
     * 加载知识库元数据
     */
    private void loadMetadata() {
        try {
            Resource resource = new PathMatchingResourcePatternResolver()
                    .getResource("classpath:data/kb_metadata.json");
            metadata = objectMapper.readValue(resource.getInputStream(), KnowledgeBaseMetadata.class);
        } catch (IOException e) {
            throw new RuntimeException("无法加载知识库元数据", e);
        }
    }

    /**
     * 根据分类上传文档
     */
    public Map<String, Object> uploadByCategory(MultipartFile file, String category, String contentType) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 验证分类和内容类型
            if (!validateCategoryAndType(category, contentType)) {
                result.put("success", false);
                result.put("message", "无效的分类或内容类型");
                return result;
            }
            
            // 获取分类详细信息
            KnowledgeBaseMetadata.Category categoryInfo = metadata.getCategories().get(category);
            if (categoryInfo == null) {
                result.put("success", false);
                result.put("message", "分类不存在");
                return result;
            }
            
            // 验证文件命名模式
            if (!validateFileNamePattern(file.getOriginalFilename(), categoryInfo.getFilePattern())) {
                result.put("success", false);
                result.put("message", "文件名不符合分类命名规范: " + categoryInfo.getFilePattern());
                return result;
            }
            
            // 保存文件
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadDir.resolve(fileName);
            file.transferTo(filePath.toFile());
            
            // 处理文档
            Document document = processDocument(filePath, file.getOriginalFilename());
            
            // 构建完整的元数据
            Map<String, Object> docMetadata = buildDocumentMetadata(
                    file.getOriginalFilename(), 
                    category, 
                    categoryInfo, 
                    contentType
            );
            
            document.metadata().putAll(docMetadata);
            
            // 存储到向量数据库
            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(DocumentSplitters.recursive(500, 50))
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();
            
            ingestor.ingest(document);
            
            // 清理临时文件
            Files.deleteIfExists(filePath);
            
            // 构建返回结果
            result.put("success", true);
            result.put("message", "文档上传成功");
            result.put("fileName", file.getOriginalFilename());
            result.put("category", category);
            result.put("contentType", contentType);
            result.putAll(docMetadata);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "文档上传失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }

    /**
     * 批量上传整个知识库目录
     */
    public Map<String, Object> uploadKnowledgeBase() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Integer> uploadStats = new HashMap<>();
        Map<String, List<String>> failedFiles = new HashMap<>();
        int totalUploaded = 0;
        int totalFailed = 0;
        
        try {
            // 扫描知识库目录
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath:data/**/*.txt");
            
            for (Resource resource : resources) {
                if (resource.isReadable() && resource.getFilename() != null) {
                    try {
                        Path filePath = Paths.get(resource.getURI());
                        String fileName = resource.getFilename();
                        
                        // 根据文件路径推断分类
                        String category = inferCategoryFromPath(resource.getURI().toString());
                        if (category == null) {
                            failedFiles.computeIfAbsent("未知分类", k -> new ArrayList<>()).add(fileName);
                            totalFailed++;
                            continue;
                        }
                        
                        KnowledgeBaseMetadata.Category categoryInfo = metadata.getCategories().get(category);
                        if (categoryInfo == null) {
                            failedFiles.computeIfAbsent("分类不存在", k -> new ArrayList<>()).add(fileName);
                            totalFailed++;
                            continue;
                        }
                        
                        // 推断内容类型
                        String contentType = inferContentTypeFromFileName(fileName, category, categoryInfo);
                        
                        // 处理文档
                        Document document = processDocument(filePath, fileName);
                        
                        // 构建完整的元数据
                        Map<String, Object> docMetadata = buildDocumentMetadata(
                                fileName, 
                                category, 
                                categoryInfo, 
                                contentType
                        );
                        
                        document.metadata().putAll(docMetadata);
                        
                        // 存储到向量数据库
                        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                                .documentSplitter(DocumentSplitters.recursive(500, 50))
                                .embeddingModel(embeddingModel)
                                .embeddingStore(embeddingStore)
                                .build();
                        
                        ingestor.ingest(document);
                        
                        uploadStats.merge(category, 1, Integer::sum);
                        totalUploaded++;
                        
                    } catch (Exception e) {
                        String errorKey = "处理失败: " + e.getMessage();
                        failedFiles.computeIfAbsent(errorKey, k -> new ArrayList<>()).add(resource.getFilename());
                        totalFailed++;
                        System.err.println("处理文件失败: " + resource.getFilename() + ", 错误: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            
            result.put("success", true);
            result.put("message", "知识库上传完成");
            result.put("totalUploaded", totalUploaded);
            result.put("totalFailed", totalFailed);
            result.put("uploadStats", uploadStats);
            if (!failedFiles.isEmpty()) {
                result.put("failedFiles", failedFiles);
            }
            
            // 添加知识库元数据信息
            result.put("knowledgeBaseName", metadata.getKnowledgeBaseName());
            result.put("version", metadata.getVersion());
            result.put("description", metadata.getDescription());
            result.put("lastUpdated", metadata.getLastUpdated());
            result.put("totalFiles", metadata.getTotalFiles());
            
            // 添加内容特征信息
            if (metadata.getContentFeatures() != null) {
                KnowledgeBaseMetadata.ContentFeatures contentFeatures = metadata.getContentFeatures();
                result.put("updateFrequency", contentFeatures.getUpdateFrequency());
                result.put("dataSource", contentFeatures.getDataSource());
                result.put("contentQuality", contentFeatures.getContentQuality());
                result.put("applicableScenarios", contentFeatures.getApplicableScenarios());
                result.put("trendCoverage", contentFeatures.getTrendCoverage());
            }
            
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "知识库上传失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }

    /**
     * 构建文档元数据
     */
    private Map<String, Object> buildDocumentMetadata(
            String fileName, 
            String category, 
            KnowledgeBaseMetadata.Category categoryInfo, 
            String contentType) {
        
        Map<String, Object> docMetadata = new HashMap<>();
        
        // 基础元数据
        docMetadata.put("fileName", fileName);
        docMetadata.put("category", category);
        docMetadata.put("categoryDescription", categoryInfo.getDescription());
        docMetadata.put("categoryPurpose", categoryInfo.getPurpose());
        docMetadata.put("categoryFileCount", categoryInfo.getFileCount());
        docMetadata.put("categoryFilePattern", categoryInfo.getFilePattern());
        docMetadata.put("contentType", contentType);
        docMetadata.put("uploadTime", String.valueOf(System.currentTimeMillis()));
        
        // 分类特定的元数据
        addCategorySpecificMetadata(docMetadata, fileName, category, categoryInfo, contentType);
        
        // 知识库级别元数据
        docMetadata.put("knowledgeBaseName", metadata.getKnowledgeBaseName());
        docMetadata.put("knowledgeBaseVersion", metadata.getVersion());
        docMetadata.put("knowledgeBaseDescription", metadata.getDescription());
        
        // 规范化元数据：将所有List类型转换为字符串，以满足向量数据库的要求
        return normalizeMetadataForEmbeddingStore(docMetadata);
    }
    
    /**
     * 规范化元数据以供嵌入存储使用
     * 将List类型转换为逗号分隔的字符串，因为向量数据库不支持List类型
     */
    private Map<String, Object> normalizeMetadataForEmbeddingStore(Map<String, Object> metadata) {
        Map<String, Object> normalized = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            Object value = entry.getValue();
            
            // 如果值是List，转换为逗号分隔的字符串
            if (value instanceof List) {
                List<?> list = (List<?>) value;
                if (list.isEmpty()) {
                    normalized.put(entry.getKey(), "");
                } else {
                    // 将List中的每个元素转换为字符串，过滤null值，然后用逗号连接
                    String joined = list.stream()
                            .filter(item -> item != null)
                            .map(Object::toString)
                            .collect(Collectors.joining(","));
                    normalized.put(entry.getKey(), joined);
                }
            }
            // 如果值是Map，也转换为JSON字符串（如果需要，但通常不需要）
            else if (value instanceof Map) {
                try {
                    normalized.put(entry.getKey(), objectMapper.writeValueAsString(value));
                } catch (Exception e) {
                    // 如果转换失败，使用toString
                    normalized.put(entry.getKey(), value.toString());
                }
            }
            // 其他类型直接使用
            else {
                normalized.put(entry.getKey(), value);
            }
        }
        
        return normalized;
    }

    /**
     * 获取知识库元数据
     */
    public KnowledgeBaseMetadata getKnowledgeBaseMetadata() {
        return metadata;
    }

    /**
     * 获取分类信息
     */
    public Map<String, Object> getCategories() {
        Map<String, Object> categoriesInfo = new HashMap<>();
        
        if (metadata != null && metadata.getCategories() != null) {
            metadata.getCategories().forEach((key, category) -> {
                Map<String, Object> categoryInfo = new HashMap<>();
                categoryInfo.put("description", category.getDescription());
                categoryInfo.put("purpose", category.getPurpose());
                categoryInfo.put("fileCount", category.getFileCount());
                categoryInfo.put("filePattern", category.getFilePattern());
                categoryInfo.put("contentTypes", category.getContentTypes());
                
                // 添加分类特定的字段
                if (category.getSampleTitles() != null && !category.getSampleTitles().isEmpty()) {
                    categoryInfo.put("sampleTitles", category.getSampleTitles());
                }
                if (category.getKeyTopics() != null && !category.getKeyTopics().isEmpty()) {
                    categoryInfo.put("keyTopics", category.getKeyTopics());
                }
                if (category.getKeyConcepts() != null && !category.getKeyConcepts().isEmpty()) {
                    categoryInfo.put("keyConcepts", category.getKeyConcepts());
                }
                if (category.getBrandTypes() != null && !category.getBrandTypes().isEmpty()) {
                    categoryInfo.put("brandTypes", category.getBrandTypes());
                }
                if (category.getFrameworkTypes() != null && !category.getFrameworkTypes().isEmpty()) {
                    categoryInfo.put("frameworkTypes", category.getFrameworkTypes());
                }
                
                categoriesInfo.put(key, categoryInfo);
            });
        }
        
        return categoriesInfo;
    }

    /**
     * 获取知识库使用指南
     */
    public Map<String, Object> getUsageGuidelines() {
        Map<String, Object> guidelines = new HashMap<>();
        
        if (metadata != null && metadata.getUsageGuidelines() != null) {
            KnowledgeBaseMetadata.UsageGuidelines usageGuidelines = metadata.getUsageGuidelines();
            guidelines.put("targetAudience", usageGuidelines.getTargetAudience());
            guidelines.put("contentGoals", usageGuidelines.getContentGoals());
            guidelines.put("bestPractices", usageGuidelines.getBestPractices());
            guidelines.put("recommendedWorkflow", usageGuidelines.getRecommendedWorkflow());
        }
        
        return guidelines;
    }

    /**
     * 获取内容特征信息
     */
    public Map<String, Object> getContentFeatures() {
        Map<String, Object> features = new HashMap<>();
        
        if (metadata != null && metadata.getContentFeatures() != null) {
            KnowledgeBaseMetadata.ContentFeatures contentFeatures = metadata.getContentFeatures();
            features.put("updateFrequency", contentFeatures.getUpdateFrequency());
            features.put("dataSource", contentFeatures.getDataSource());
            features.put("contentQuality", contentFeatures.getContentQuality());
            features.put("applicableScenarios", contentFeatures.getApplicableScenarios());
            features.put("trendCoverage", contentFeatures.getTrendCoverage());
        }
        
        return features;
    }

    /**
     * 验证分类和内容类型
     */
    private boolean validateCategoryAndType(String category, String contentType) {
        if (metadata == null || metadata.getCategories() == null) {
            return false;
        }
        
        KnowledgeBaseMetadata.Category cat = metadata.getCategories().get(category);
        if (cat == null) {
            return false;
        }
        
        return cat.getContentTypes() != null && cat.getContentTypes().contains(contentType);
    }

    /**
     * 验证文件名模式
     */
    private boolean validateFileNamePattern(String fileName, String filePattern) {
        if (filePattern == null || filePattern.isEmpty()) {
            return true; // 如果没有文件模式要求，则通过验证
        }
        
        // 将文件模式转换为正则表达式
        // 例如: samples_note_*.txt -> samples_note_.*\.txt
        String regex = filePattern
                .replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".");
        
        return fileName != null && fileName.matches(regex);
    }

    /**
     * 处理文档
     */
    private Document processDocument(Path filePath, String fileName) throws IOException {
        if (fileName.toLowerCase().endsWith(".pdf")) {
            return FileSystemDocumentLoader.loadDocument(filePath, new ApachePdfBoxDocumentParser());
        } else {
            return FileSystemDocumentLoader.loadDocument(filePath, new TextDocumentParser());
        }
    }

    /**
     * 根据文件路径推断分类
     */
    private String inferCategoryFromPath(String filePath) {
        String lowerPath = filePath.toLowerCase();
        
        // 根据路径中的目录名推断分类
        if (lowerPath.contains("/samples/") || lowerPath.contains("\\samples\\")) {
            return "samples";
        } else if (lowerPath.contains("/industry_trends/") || lowerPath.contains("\\industry_trends\\")) {
            return "industry_trends";
        } else if (lowerPath.contains("/marketing_psychology/") || lowerPath.contains("\\marketing_psychology\\")) {
            return "marketing_psychology";
        } else if (lowerPath.contains("/brand_styles/") || lowerPath.contains("\\brand_styles\\")) {
            return "brand_styles";
        } else if (lowerPath.contains("/writing_frameworks/") || lowerPath.contains("\\writing_frameworks\\")) {
            return "writing_frameworks";
        }
        
        return null;
    }

    /**
     * 从文件名推断内容类型
     */
    private String inferContentTypeFromFileName(
            String fileName, 
            String category, 
            KnowledgeBaseMetadata.Category categoryInfo) {
        
        if (fileName == null || categoryInfo == null || categoryInfo.getContentTypes() == null) {
            return "未知";
        }
        
        String lowerFileName = fileName.toLowerCase();
        List<String> availableContentTypes = categoryInfo.getContentTypes();
        
        // 根据分类使用不同的推断策略
        switch (category) {
            case "brand_styles":
                return inferBrandStylesContentType(lowerFileName, availableContentTypes, categoryInfo);
            case "writing_frameworks":
                return inferWritingFrameworksContentType(lowerFileName, availableContentTypes, categoryInfo);
            case "industry_trends":
                return inferIndustryTrendsContentType(lowerFileName, availableContentTypes);
            case "marketing_psychology":
                return inferMarketingPsychologyContentType(lowerFileName, availableContentTypes);
            case "samples":
                return inferSamplesContentType(lowerFileName, availableContentTypes);
            default:
                return inferDefaultContentType(lowerFileName, availableContentTypes);
        }
    }

    /**
     * 品牌风格内容类型推断
     * 文件名格式: brand_styles_brand_XXX博主风格.txt
     */
    private String inferBrandStylesContentType(
            String fileName, 
            List<String> contentTypes,
            KnowledgeBaseMetadata.Category categoryInfo) {
        
        // 从文件名中提取品牌类型关键词
        if (fileName.contains("健康养生")) {
            return matchOrDefault(contentTypes, "健康养生", contentTypes.get(0));
        } else if (fileName.contains("健身")) {
            return matchOrDefault(contentTypes, "健身", contentTypes.get(0));
        } else if (fileName.contains("咖啡馆")) {
            return matchOrDefault(contentTypes, "咖啡馆", contentTypes.get(0));
        } else if (fileName.contains("宠物")) {
            return matchOrDefault(contentTypes, "宠物", contentTypes.get(0));
        } else if (fileName.contains("家居")) {
            return matchOrDefault(contentTypes, "家居", contentTypes.get(0));
        } else if (fileName.contains("投资理财")) {
            return matchOrDefault(contentTypes, "投资理财", contentTypes.get(0));
        } else if (fileName.contains("护肤")) {
            return matchOrDefault(contentTypes, "护肤", contentTypes.get(0));
        } else if (fileName.contains("教育")) {
            return matchOrDefault(contentTypes, "教育", contentTypes.get(0));
        } else if (fileName.contains("旅行")) {
            return matchOrDefault(contentTypes, "旅行", contentTypes.get(0));
        } else if (fileName.contains("时尚穿搭") || fileName.contains("穿搭")) {
            return matchOrDefault(contentTypes, "时尚穿搭", "穿搭");
        } else if (fileName.contains("母婴")) {
            return matchOrDefault(contentTypes, "母婴", contentTypes.get(0));
        } else if (fileName.contains("科技数码") || fileName.contains("数码")) {
            return matchOrDefault(contentTypes, "科技数码", contentTypes.get(0));
        } else if (fileName.contains("美食")) {
            return matchOrDefault(contentTypes, "美食", contentTypes.get(0));
        } else if (fileName.contains("职场")) {
            return matchOrDefault(contentTypes, "职场", contentTypes.get(0));
        }
        
        return contentTypes.isEmpty() ? "品牌调性" : contentTypes.get(0);
    }

    /**
     * 文案框架内容类型推断
     * 文件名格式: writing_frameworks_XXX.txt
     */
    private String inferWritingFrameworksContentType(
            String fileName, 
            List<String> contentTypes,
            KnowledgeBaseMetadata.Category categoryInfo) {
        
        // 从文件名中提取框架类型关键词
        if (fileName.contains("标题公式") || fileName.contains("标题")) {
            return matchOrDefault(contentTypes, "标题公式", contentTypes.get(0));
        } else if (fileName.contains("内容结构") || fileName.contains("结构模板")) {
            return matchOrDefault(contentTypes, "内容结构", contentTypes.get(0));
        } else if (fileName.contains("cta") || fileName.contains("引导转折")) {
            return matchOrDefault(contentTypes, "CTA设计", "引导转折CTA结构");
        } else if (fileName.contains("互动策略")) {
            return matchOrDefault(contentTypes, "互动策略", contentTypes.get(0));
        } else if (fileName.contains("内容优化")) {
            return matchOrDefault(contentTypes, "内容优化", contentTypes.get(0));
        } else if (fileName.contains("情感化") || fileName.contains("情感")) {
            return matchOrDefault(contentTypes, "情感化文案", contentTypes.get(0));
        } else if (fileName.contains("爆款") || fileName.contains("创作公式")) {
            return matchOrDefault(contentTypes, "爆款公式", contentTypes.get(0));
        }
        
        return contentTypes.isEmpty() ? "内容结构" : contentTypes.get(0);
    }

    /**
     * 行业趋势内容类型推断
     */
    private String inferIndustryTrendsContentType(String fileName, List<String> contentTypes) {
        // 从文件名中提取关键词
        if (fileName.contains("趋势报告") || fileName.contains("趋势")) {
            return matchOrDefault(contentTypes, "内容趋势", contentTypes.get(0));
        } else if (fileName.contains("算法") || fileName.contains("推荐")) {
            return matchOrDefault(contentTypes, "算法机制", contentTypes.get(0));
        } else if (fileName.contains("穿搭")) {
            return matchOrDefault(contentTypes, "内容趋势", contentTypes.get(0));
        } else if (fileName.contains("生活方式")) {
            return matchOrDefault(contentTypes, "内容趋势", contentTypes.get(0));
        } else if (fileName.contains("美妆")) {
            return matchOrDefault(contentTypes, "内容趋势", contentTypes.get(0));
        }
        
        return contentTypes.isEmpty() ? "内容趋势" : contentTypes.get(0);
    }

    /**
     * 营销心理学内容类型推断
     */
    private String inferMarketingPsychologyContentType(String fileName, List<String> contentTypes) {
        // 从文件名中提取关键词
        if (fileName.contains("消费心理学")) {
            return matchOrDefault(contentTypes, "消费心理学", contentTypes.get(0));
        } else if (fileName.contains("文案心理学")) {
            return matchOrDefault(contentTypes, "文案心理学", contentTypes.get(0));
        } else if (fileName.contains("影响力") || fileName.contains("说服力")) {
            return matchOrDefault(contentTypes, "影响力原则", contentTypes.get(0));
        } else if (fileName.contains("用户行为")) {
            return matchOrDefault(contentTypes, "用户行为分析", contentTypes.get(0));
        } else if (fileName.contains("品牌心理学")) {
            return matchOrDefault(contentTypes, "品牌心理学", contentTypes.get(0));
        } else if (fileName.contains("数字营销")) {
            return matchOrDefault(contentTypes, "数字营销", contentTypes.get(0));
        } else if (fileName.contains("社交媒体")) {
            return matchOrDefault(contentTypes, "社交媒体营销", contentTypes.get(0));
        } else if (fileName.contains("内容营销")) {
            return matchOrDefault(contentTypes, "内容营销心理学", contentTypes.get(0));
        }
        
        return contentTypes.isEmpty() ? "消费心理学" : contentTypes.get(0);
    }

    /**
     * 样本库内容类型推断
     */
    private String inferSamplesContentType(String fileName, List<String> contentTypes) {
        // 从文件名中提取关键词（虽然文件名通常是 samples_note_XXX.txt）
        // 这里可以根据样本编号或文件内容推断，暂时返回第一个可用的内容类型
        return contentTypes.isEmpty() ? "生活" : contentTypes.get(0);
    }

    /**
     * 默认内容类型推断策略
     */
    private String inferDefaultContentType(String fileName, List<String> contentTypes) {
        // 尝试匹配所有常见关键词
        for (String contentType : contentTypes) {
            if (fileName.contains(contentType.toLowerCase())) {
                return contentType;
            }
        }
        
        return contentTypes.isEmpty() ? "未知" : contentTypes.get(0);
    }

    /**
     * 匹配内容类型或返回默认值
     */
    private String matchOrDefault(List<String> contentTypes, String target, String defaultValue) {
        // 首先尝试精确匹配
        for (String contentType : contentTypes) {
            if (contentType.equals(target)) {
                return contentType;
            }
        }
        
        // 尝试包含匹配
        for (String contentType : contentTypes) {
            if (contentType.contains(target) || target.contains(contentType)) {
                return contentType;
            }
        }
        
        return defaultValue;
    }

    /**
     * 添加分类特定的元数据
     */
    private void addCategorySpecificMetadata(
            Map<String, Object> docMetadata, 
            String fileName,
            String categoryKey, 
            KnowledgeBaseMetadata.Category categoryInfo, 
            String contentType) {
        
        // 根据分类键名添加特定的元数据字段
        switch (categoryKey) {
            case "samples":
                if (categoryInfo.getSampleTitles() != null && !categoryInfo.getSampleTitles().isEmpty()) {
                    docMetadata.put("sampleTitles", categoryInfo.getSampleTitles());
                }
                break;
            case "industry_trends":
                if (categoryInfo.getKeyTopics() != null && !categoryInfo.getKeyTopics().isEmpty()) {
                    docMetadata.put("keyTopics", categoryInfo.getKeyTopics());
                }
                // 从文件名提取具体主题
                extractTopicFromFileName(docMetadata, fileName);
                break;
            case "marketing_psychology":
                if (categoryInfo.getKeyConcepts() != null && !categoryInfo.getKeyConcepts().isEmpty()) {
                    docMetadata.put("keyConcepts", categoryInfo.getKeyConcepts());
                }
                // 从文件名提取具体概念
                extractConceptFromFileName(docMetadata, fileName);
                break;
            case "brand_styles":
                if (categoryInfo.getBrandTypes() != null && !categoryInfo.getBrandTypes().isEmpty()) {
                    docMetadata.put("brandTypes", categoryInfo.getBrandTypes());
                }
                // 从文件名提取具体品牌类型
                extractBrandTypeFromFileName(docMetadata, fileName);
                break;
            case "writing_frameworks":
                if (categoryInfo.getFrameworkTypes() != null && !categoryInfo.getFrameworkTypes().isEmpty()) {
                    docMetadata.put("frameworkTypes", categoryInfo.getFrameworkTypes());
                }
                // 从文件名提取具体框架类型
                extractFrameworkTypeFromFileName(docMetadata, fileName);
                break;
        }
    }

    /**
     * 从文件名提取主题
     */
    private void extractTopicFromFileName(Map<String, Object> docMetadata, String fileName) {
        // 移除扩展名和前缀
        String name = fileName.replace(".txt", "")
                .replace("industry_trends_", "");
        
        if (!name.isEmpty()) {
            docMetadata.put("topic", name);
        }
    }

    /**
     * 从文件名提取概念
     */
    private void extractConceptFromFileName(Map<String, Object> docMetadata, String fileName) {
        // 移除扩展名和前缀
        String name = fileName.replace(".txt", "")
                .replace("marketing_psychology_", "");
        
        if (!name.isEmpty()) {
            docMetadata.put("concept", name);
        }
    }

    /**
     * 从文件名提取品牌类型
     */
    private void extractBrandTypeFromFileName(Map<String, Object> docMetadata, String fileName) {
        // 文件名格式: brand_styles_brand_XXX博主风格.txt
        String name = fileName.replace(".txt", "")
                .replace("brand_styles_brand_", "")
                .replace("博主风格", "")
                .replace("语气", "")
                .trim();
        
        if (!name.isEmpty()) {
            docMetadata.put("brandType", name);
        }
    }

    /**
     * 从文件名提取框架类型
     */
    private void extractFrameworkTypeFromFileName(Map<String, Object> docMetadata, String fileName) {
        // 移除扩展名和前缀
        String name = fileName.replace(".txt", "")
                .replace("writing_frameworks_", "");
        
        if (!name.isEmpty()) {
            docMetadata.put("frameworkType", name);
        }
    }
}
