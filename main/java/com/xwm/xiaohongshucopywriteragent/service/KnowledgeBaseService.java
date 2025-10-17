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
        // 加载元数据
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
            
            // 保存文件
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadDir.resolve(fileName);
            file.transferTo(filePath.toFile());
            
            // 处理文档
            Document document = processDocument(filePath, file.getOriginalFilename());
            
            // 添加元数据
            Map<String, Object> docMetadata = new HashMap<>();
            docMetadata.put("fileName", file.getOriginalFilename());
            docMetadata.put("category", category);
            docMetadata.put("contentType", contentType);
            docMetadata.put("uploadTime", String.valueOf(System.currentTimeMillis()));
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
            
            result.put("success", true);
            result.put("message", "文档上传成功");
            result.put("category", category);
            result.put("contentType", contentType);
            result.put("fileName", file.getOriginalFilename());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "文档上传失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 批量上传整个知识库目录
     */
    public Map<String, Object> uploadKnowledgeBase() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Integer> uploadStats = new HashMap<>();
        int totalUploaded = 0;
        
        try {
            // 扫描知识库目录
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath:data/**/*");
            
            for (Resource resource : resources) {
                if (resource.isReadable() && !resource.getFilename().endsWith(".json")) {
                    try {
                        Path filePath = Paths.get(resource.getURI());
                        Document document = processDocument(filePath, resource.getFilename());
                        
                        // 根据文件路径推断分类
                        String category = inferCategoryFromPath(resource.getFilename());
                        String contentType = inferContentType(resource.getFilename());
                        
                        // 添加元数据
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("fileName", resource.getFilename());
                        metadata.put("category", category);
                        metadata.put("contentType", contentType);
                        metadata.put("uploadTime", String.valueOf(System.currentTimeMillis()));
                        document.metadata().putAll(metadata);
                        
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
                        System.err.println("处理文件失败: " + resource.getFilename() + ", 错误: " + e.getMessage());
                    }
                }
            }
            
            result.put("success", true);
            result.put("message", "知识库上传完成");
            result.put("totalUploaded", totalUploaded);
            result.put("uploadStats", uploadStats);
            
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "知识库上传失败: " + e.getMessage());
        }
        
        return result;
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
                categoryInfo.put("contentTypes", category.getContentTypes());
                categoriesInfo.put(key, categoryInfo);
            });
        }
        
        return categoriesInfo;
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
        
        return cat.getContentTypes().contains(contentType);
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
     * 根据文件名推断分类
     */
    private String inferCategoryFromPath(String fileName) {
        String lowerFileName = fileName.toLowerCase();
        
        if (lowerFileName.contains("sample") || lowerFileName.contains("案例")) {
            return "samples";
        } else if (lowerFileName.contains("trend") || lowerFileName.contains("趋势")) {
            return "industry_trends";
        } else if (lowerFileName.contains("psychology") || lowerFileName.contains("心理")) {
            return "marketing_psychology";
        } else if (lowerFileName.contains("brand") || lowerFileName.contains("品牌")) {
            return "brand_styles";
        } else if (lowerFileName.contains("framework") || lowerFileName.contains("框架")) {
            return "writing_frameworks";
        } else {
            return "samples"; // 默认分类
        }
    }

    /**
     * 根据文件名推断内容类型
     */
    private String inferContentType(String fileName) {
        String lowerFileName = fileName.toLowerCase();
        
        // 根据您的元数据中的contentTypes进行推断
        if (lowerFileName.contains("美妆") || lowerFileName.contains("化妆")) {
            return "美妆";
        } else if (lowerFileName.contains("穿搭") || lowerFileName.contains("服装")) {
            return "穿搭";
        } else if (lowerFileName.contains("美食") || lowerFileName.contains("食物")) {
            return "美食";
        } else if (lowerFileName.contains("旅行") || lowerFileName.contains("旅游")) {
            return "旅行";
        } else {
            return "生活"; // 默认内容类型
        }
    }
}