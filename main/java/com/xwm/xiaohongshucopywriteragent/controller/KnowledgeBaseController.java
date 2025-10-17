package com.xwm.xiaohongshucopywriteragent.controller;

import com.xwm.xiaohongshucopywriteragent.model.KnowledgeBaseMetadata;
import com.xwm.xiaohongshucopywriteragent.service.KnowledgeBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge-base")
public class KnowledgeBaseController {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    /**
     * 获取知识库元数据
     */
    @GetMapping("/metadata")
    public ResponseEntity<KnowledgeBaseMetadata> getMetadata() {
        KnowledgeBaseMetadata metadata = knowledgeBaseService.getKnowledgeBaseMetadata();
        return ResponseEntity.ok(metadata);
    }

    /**
     * 获取分类信息
     */
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getCategories() {
        Map<String, Object> categories = knowledgeBaseService.getCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * 按分类上传文档
     */
    @PostMapping("/upload/category")
    public ResponseEntity<Map<String, Object>> uploadByCategory(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category,
            @RequestParam("contentType") String contentType) {
        
        Map<String, Object> result = knowledgeBaseService.uploadByCategory(file, category, contentType);
        return ResponseEntity.ok(result);
    }

    /**
     * 初始化整个知识库
     */
    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> initKnowledgeBase() {
        Map<String, Object> result = knowledgeBaseService.uploadKnowledgeBase();
        return ResponseEntity.ok(result);
    }

    /**
     * 获取知识库状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        
        KnowledgeBaseMetadata metadata = knowledgeBaseService.getKnowledgeBaseMetadata();
        Map<String, Object> categories = knowledgeBaseService.getCategories();
        
        status.put("knowledgeBaseName", metadata.getKnowledgeBaseName());
        status.put("version", metadata.getVersion());
        status.put("description", metadata.getDescription());
        status.put("lastUpdated", metadata.getLastUpdated());
        status.put("categories", categories);
        status.put("status", "active");
        
        return ResponseEntity.ok(status);
    }
}