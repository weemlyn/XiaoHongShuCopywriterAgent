package com.xwm.xiaohongshucopywriteragent.evaluation.controller;

import com.xwm.xiaohongshucopywriteragent.evaluation.model.ChunkingEvaluationRequest;
import com.xwm.xiaohongshucopywriteragent.evaluation.model.ChunkingEvaluationResult;
import com.xwm.xiaohongshucopywriteragent.evaluation.service.ChunkingEvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 对外暴露分块策略评估接口
 */
@RestController
@RequestMapping("/mingagent/evaluation/chunking")
public class ChunkingEvaluationController {

    private final ChunkingEvaluationService evaluationService;

    public ChunkingEvaluationController(ChunkingEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping
    public ResponseEntity<List<ChunkingEvaluationResult>> evaluate(@RequestBody ChunkingEvaluationRequest request) {
        List<ChunkingEvaluationResult> results = evaluationService.evaluate(request);
        return ResponseEntity.ok(results);
    }
}

