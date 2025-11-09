package com.xwm.xiaohongshucopywriteragent.evaluation.service;

import com.xwm.xiaohongshucopywriteragent.evaluation.model.ChunkingEvaluationRequest;
import com.xwm.xiaohongshucopywriteragent.evaluation.model.ChunkingEvaluationResult;
import com.xwm.xiaohongshucopywriteragent.evaluation.model.EvaluationQuery;
import com.xwm.xiaohongshucopywriteragent.evaluation.model.EvaluationTarget;
import com.xwm.xiaohongshucopywriteragent.evaluation.model.GenerationMetrics;
import com.xwm.xiaohongshucopywriteragent.evaluation.model.QueryEvaluationResult;
import com.xwm.xiaohongshucopywriteragent.evaluation.model.RetrievalMetrics;
import com.xwm.xiaohongshucopywriteragent.evaluation.model.RetrievedDocument;
import com.xwm.xiaohongshucopywriteragent.evaluation.model.SystemMetrics;
import com.xwm.xiaohongshucopywriteragent.evaluation.model.VectorStoreConfig;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 负责计算分块策略的检索和生成指标
 */
@Service
public class ChunkingEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(ChunkingEvaluationService.class);
    private static final Set<String> STOP_WORDS = Set.of(
            "的", "是", "在", "有", "和", "了", "就", "也", "与", "或",
            "the", "is", "a", "an", "and", "or", "in", "on", "at", "to", "for", "of", "with"
    );

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> defaultEmbeddingStore;
    private final VectorStoreFactory vectorStoreFactory;
    private final FaithfulnessJudge faithfulnessJudge;

    public ChunkingEvaluationService(EmbeddingModel embeddingModel,
                                     EmbeddingStore<TextSegment> embeddingStore,
                                     VectorStoreFactory vectorStoreFactory,
                                     ObjectProvider<FaithfulnessJudge> faithfulnessJudgeProvider) {
        this.embeddingModel = embeddingModel;
        this.defaultEmbeddingStore = embeddingStore;
        this.vectorStoreFactory = vectorStoreFactory;
        this.faithfulnessJudge = faithfulnessJudgeProvider.getIfAvailable();
    }

    public List<ChunkingEvaluationResult> evaluate(ChunkingEvaluationRequest request) {
        if (request == null) {
            return Collections.emptyList();
        }
        if (CollectionUtils.isEmpty(request.getQueries())) {
            return Collections.emptyList();
        }
        List<Integer> sortedKValues = request.getKValues().stream()
                .filter(k -> k != null && k > 0)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        if (sortedKValues.isEmpty()) {
            sortedKValues = List.of(1, 3, 5, 10);
        }

        List<EvaluationTarget> targets = request.getTargets().isEmpty()
                ? List.of(defaultTarget())
                : request.getTargets();

        List<ChunkingEvaluationResult> results = new ArrayList<>();
        for (EvaluationTarget target : targets) {
            EmbeddingStore<TextSegment> store = resolveStore(target);
            try {
                ChunkingEvaluationResult result = evaluateTarget(
                        target,
                        store,
                        request,
                        sortedKValues);
                results.add(result);
            } finally {
                closeIfNecessary(store);
            }
        }
        return results;
    }

    private ChunkingEvaluationResult evaluateTarget(EvaluationTarget target,
                                                    EmbeddingStore<TextSegment> store,
                                                    ChunkingEvaluationRequest request,
                                                    List<Integer> sortedKValues) {
        ChunkingEvaluationResult result = new ChunkingEvaluationResult();
        result.setStrategyName(determineStrategyName(target));

        List<QueryEvaluationResult> perQuery = new ArrayList<>();
        Map<Integer, Double> recallSums = new LinkedHashMap<>();
        Map<Integer, Double> precisionSums = new LinkedHashMap<>();
        Map<Integer, Double> f1Sums = new LinkedHashMap<>();
        double mrrSum = 0.0;
        double latencySum = 0.0;

        double faithfulnessSum = 0.0;
        double utilizationSum = 0.0;
        double hallucinationSum = 0.0;
        int generationCount = 0;

        for (EvaluationQuery query : request.getQueries()) {
            QueryEvaluationResult queryResult = evaluateQuery(
                    query,
                    store,
                    request,
                    sortedKValues);

            perQuery.add(queryResult);

            if (queryResult.getReciprocalRank() != null) {
                mrrSum += queryResult.getReciprocalRank();
            }
            if (queryResult.getLatencyMillis() != null) {
                latencySum += queryResult.getLatencyMillis();
            }

            accumulateAverages(recallSums, queryResult.getRecallAtK());
            accumulateAverages(precisionSums, queryResult.getPrecisionAtK());
            accumulateAverages(f1Sums, queryResult.getF1AtK());

            if (queryResult.getFaithfulness() != null) {
                faithfulnessSum += queryResult.getFaithfulness();
                generationCount++;
            }
            if (queryResult.getContextUtilization() != null) {
                utilizationSum += queryResult.getContextUtilization();
            }
            if (queryResult.getHallucinationRate() != null) {
                hallucinationSum += queryResult.getHallucinationRate();
            }
        }

        result.setQueryResults(perQuery);

        RetrievalMetrics retrievalMetrics = new RetrievalMetrics();
        int queryCount = request.getQueries().size();
        if (queryCount > 0) {
            retrievalMetrics.setRecallAtK(averageMap(recallSums, queryCount));
            retrievalMetrics.setPrecisionAtK(averageMap(precisionSums, queryCount));
            retrievalMetrics.setF1AtK(averageMap(f1Sums, queryCount));
            retrievalMetrics.setMeanReciprocalRank(mrrSum / queryCount);
            retrievalMetrics.setAverageLatencyMillis(latencySum / queryCount);
            retrievalMetrics.setNdcg(0.0); // 后续可替换为真实计算
        }
        result.setRetrievalMetrics(retrievalMetrics);

        if (request.isIncludeGenerationMetrics() && generationCount > 0) {
            GenerationMetrics generationMetrics = new GenerationMetrics();
            generationMetrics.setFaithfulness(faithfulnessSum / generationCount);
            generationMetrics.setContextUtilization(utilizationSum / generationCount);
            generationMetrics.setHallucinationRate(Math.max(0.0, Math.min(1.0, hallucinationSum / generationCount)));
            result.setGenerationMetrics(generationMetrics);
        }

        // 系统指标暂未汇总真实数据
        result.setSystemMetrics(new SystemMetrics());

        return result;
    }

    private QueryEvaluationResult evaluateQuery(EvaluationQuery query,
                                                EmbeddingStore<TextSegment> store,
                                                ChunkingEvaluationRequest request,
                                                List<Integer> sortedKValues) {
        QueryEvaluationResult result = new QueryEvaluationResult();
        result.setQueryId(query.getId());
        result.setQueryText(query.getQuery());

        if (query.getQuery() == null || query.getQuery().isBlank()) {
            return result;
        }

        try {
            int maxResults = request.getMaxResultsOverride() != null
                    ? request.getMaxResultsOverride()
                    : sortedKValues.get(sortedKValues.size() - 1);

            Response<Embedding> embeddingResponse = embeddingModel.embed(query.getQuery());
            Embedding queryEmbedding = embeddingResponse.content();
            long start = System.nanoTime();
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(maxResults)
                    .minScore(request.getMinScore())
                    .build();

            EmbeddingSearchResult<TextSegment> searchResult = store.search(searchRequest);
            long end = System.nanoTime();
            double latencyMs = Duration.ofNanos(end - start).toMillis();
            result.setLatencyMillis(latencyMs);

            List<EmbeddingMatch<TextSegment>> matches = searchResult.matches() == null
                    ? List.of()
                    : searchResult.matches();

            List<EmbeddingMatch<TextSegment>> deduplicated = deduplicateMatches(matches);
            Set<String> relevantDocIds = new LinkedHashSet<>(normalizeIds(query.getRelevantDocumentIds()));

            Map<Integer, Double> recallPerK = new LinkedHashMap<>();
            Map<Integer, Double> precisionPerK = new LinkedHashMap<>();
            Map<Integer, Double> f1PerK = new LinkedHashMap<>();

            double reciprocalRank = computeReciprocalRank(deduplicated, relevantDocIds);
            result.setReciprocalRank(reciprocalRank);

            for (Integer k : sortedKValues) {
                MetricsAccumulator accumulator = computeMetricsForK(deduplicated, relevantDocIds, k);
                recallPerK.put(k, accumulator.recall());
                precisionPerK.put(k, accumulator.precision());
                f1PerK.put(k, accumulator.f1());
            }
            result.setRecallAtK(recallPerK);
            result.setPrecisionAtK(precisionPerK);
            result.setF1AtK(f1PerK);

            List<RetrievedDocument> retrievedDocuments = deduplicated.stream()
                    .map(this::toRetrievedDocument)
                    .collect(Collectors.toList());
            result.setRetrievedDocuments(retrievedDocuments);

            if (request.isIncludeGenerationMetrics() && query.getGeneratedAnswer() != null) {
                GenerationStats generationStats = computeGenerationStats(
                        query.getGeneratedAnswer(),
                        deduplicated.stream().map(EmbeddingMatch::embedded).toList(),
                        request.getContextSimilarityThreshold());
                if (generationStats != null) {
                    result.setFaithfulness(generationStats.faithfulness());
                    result.setContextUtilization(generationStats.contextUtilization());
                    result.setHallucinationRate(generationStats.hallucinationRate());
                }
            }
        } catch (Exception ex) {
            log.warn("Failed to evaluate query '{}': {}", query.getId(), ex.getMessage());
        }
        return result;
    }

    private MetricsAccumulator computeMetricsForK(List<EmbeddingMatch<TextSegment>> matches,
                                                  Set<String> relevantDocIds,
                                                  int k) {
        if (k <= 0) {
            return new MetricsAccumulator(0.0, 0.0, 0.0);
        }
        List<EmbeddingMatch<TextSegment>> topK = matches.size() > k ? matches.subList(0, k) : matches;
        Set<String> retrieved = topK.stream()
                .map(match -> normalizeId(resolveDocumentId(match.embedded(), match.embeddingId())))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (retrieved.isEmpty()) {
            return new MetricsAccumulator(0.0, 0.0, 0.0);
        }

        long relevantCount = retrieved.stream().filter(relevantDocIds::contains).count();
        double recall = relevantDocIds.isEmpty() ? 0.0 : relevantCount / (double) relevantDocIds.size();
        double precision = relevantCount / (double) retrieved.size();
        double f1 = (precision + recall) > 0
                ? (2 * precision * recall) / (precision + recall)
                : 0.0;
        return new MetricsAccumulator(recall, precision, f1);
    }

    private RetrievedDocument toRetrievedDocument(EmbeddingMatch<TextSegment> match) {
        RetrievedDocument doc = new RetrievedDocument();
        TextSegment segment = match.embedded();
        doc.setDocumentId(resolveDocumentId(segment, match.embeddingId()));
        doc.setScore(match.score());
        doc.setTextSnippet(extractSnippet(segment.text()));
        doc.setMetadata(segment.metadata().toMap());
        return doc;
    }

    private GenerationStats computeGenerationStats(String answer,
                                                   List<TextSegment> contexts,
                                                   Double similarityThreshold) {
        String trimmedAnswer = answer == null ? "" : answer.trim();
        if (trimmedAnswer.isEmpty() || contexts == null || contexts.isEmpty()) {
            return null;
        }
        String contextText = contexts.stream()
                .map(TextSegment::text)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("\n"));
        if (contextText.isEmpty()) {
            return null;
        }

        double faithfulness = evaluateFaithfulness(trimmedAnswer, contextText);
        double contextUtilization = evaluateContextUtilization(trimmedAnswer, contexts,
                similarityThreshold == null ? 0.6 : similarityThreshold);
        double hallucinationRate = clamp(1.0 - faithfulness);

        return new GenerationStats(
                clamp(faithfulness),
                clamp(contextUtilization),
                clamp(hallucinationRate));
    }

    private double evaluateFaithfulness(String answer, String context) {
        Double llmScore = scoreFaithfulnessWithLlm(answer, context);
        if (llmScore != null) {
            return clamp(llmScore);
        }

        Double embeddingScore = scoreFaithfulnessWithEmbeddings(answer, context);
        if (embeddingScore != null) {
            return clamp(embeddingScore);
        }

        return clamp(scoreFaithfulnessWithWordOverlap(answer, context));
    }

    private Double scoreFaithfulnessWithLlm(String answer, String context) {
        if (faithfulnessJudge == null) {
            return null;
        }
        try {
            Double score = faithfulnessJudge.score(answer, context);
            if (score != null) {
                return clamp(score);
            }
        } catch (Exception ex) {
            log.warn("LLM faithfulness scoring failed: {}", ex.getMessage());
        }
        return null;
    }

    private Double scoreFaithfulnessWithEmbeddings(String answer, String context) {
        List<String> answerSentences = splitIntoSentences(answer);
        if (answerSentences.isEmpty()) {
            answerSentences = List.of(answer);
        }
        List<String> contextSentences = splitIntoSentences(context);
        if (contextSentences.isEmpty()) {
            return null;
        }

        try {
            List<TextSegment> answerSegments = answerSentences.stream()
                    .map(TextSegment::from)
                    .toList();
            List<TextSegment> contextSegments = contextSentences.stream()
                    .map(TextSegment::from)
                    .toList();

            List<Embedding> answerEmbeddings = embeddingModel.embedAll(answerSegments).content();
            List<Embedding> contextEmbeddings = embeddingModel.embedAll(contextSegments).content();

            double similaritySum = 0.0;
            for (Embedding answerEmbedding : answerEmbeddings) {
                double maxSimilarity = contextEmbeddings.stream()
                        .mapToDouble(ctx -> cosineSimilarity(answerEmbedding.vector(), ctx.vector()))
                        .max()
                        .orElse(0.0);
                similaritySum += maxSimilarity;
            }

            double averageSimilarity = similaritySum / answerEmbeddings.size();
            // 使用 sigmoid 对相似度进行平滑映射
            double normalized = sigmoid((averageSimilarity - 0.5) * 10);
            return clamp(normalized);
        } catch (Exception ex) {
            log.warn("Embedding-based faithfulness scoring failed: {}", ex.getMessage());
            return null;
        }
    }

    private double scoreFaithfulnessWithWordOverlap(String answer, String context) {
        Set<String> answerTokens = tokenize(answer);
        Set<String> contextTokens = tokenize(context);
        answerTokens.removeAll(STOP_WORDS);
        contextTokens.removeAll(STOP_WORDS);
        if (answerTokens.isEmpty()) {
            return 0.0;
        }
        long common = answerTokens.stream()
                .filter(contextTokens::contains)
                .count();
        return common / (double) answerTokens.size();
    }

    private double evaluateContextUtilization(String answer,
                                              List<TextSegment> contexts,
                                              double similarityThreshold) {
        List<String> answerSentences = splitIntoSentences(answer);
        if (answerSentences.isEmpty()) {
            answerSentences = List.of(answer);
        }
        List<String> contextSentences = contexts.stream()
                .flatMap(segment -> splitIntoSentences(segment.text()).stream())
                .map(String::trim)
                .filter(sentence -> sentence.length() >= 3)
                .toList();
        if (contextSentences.isEmpty()) {
            return 0.0;
        }

        try {
            List<Embedding> contextEmbeddings = embeddingModel.embedAll(
                    contextSentences.stream().map(TextSegment::from).toList()
            ).content();

            int utilizedCount = 0;
            int validAnswerSentences = 0;
            for (String answerSentence : answerSentences) {
                String trimmed = answerSentence.trim();
                if (trimmed.length() < 3) {
                    continue;
                }
                try {
                    Embedding answerEmbedding = embeddingModel.embed(trimmed).content();
                    double maxSimilarity = contextEmbeddings.stream()
                            .mapToDouble(ctx -> cosineSimilarity(answerEmbedding.vector(), ctx.vector()))
                            .max()
                            .orElse(0.0);
                    validAnswerSentences++;
                    if (maxSimilarity >= similarityThreshold) {
                        utilizedCount++;
                    }
                } catch (Exception inner) {
                    log.debug("Failed to embed answer sentence for context utilization: {}", inner.getMessage());
                }
            }

            if (validAnswerSentences == 0) {
                return 0.0;
            }
            return utilizedCount / (double) validAnswerSentences;
        } catch (Exception ex) {
            log.warn("Embedding-based context utilization failed: {}", ex.getMessage());
            return 0.0;
        }
    }

    private double cosineSimilarity(float[] a, float[] b) {
        int dimension = Math.min(a.length, b.length);
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < dimension; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private Map<Integer, Double> averageMap(Map<Integer, Double> sums, int divisor) {
        Map<Integer, Double> averaged = new LinkedHashMap<>();
        for (Map.Entry<Integer, Double> entry : sums.entrySet()) {
            averaged.put(entry.getKey(), entry.getValue() / divisor);
        }
        return averaged;
    }

    private void accumulateAverages(Map<Integer, Double> accumulator, Map<Integer, Double> values) {
        if (values == null) {
            return;
        }
        for (Map.Entry<Integer, Double> entry : values.entrySet()) {
            accumulator.merge(entry.getKey(), entry.getValue(), Double::sum);
        }
    }

    private double computeReciprocalRank(List<EmbeddingMatch<TextSegment>> matches, Set<String> relevantDocIds) {
        int rank = 1;
        for (EmbeddingMatch<TextSegment> match : matches) {
            String docId = normalizeId(resolveDocumentId(match.embedded(), match.embeddingId()));
            if (docId != null && relevantDocIds.contains(docId)) {
                return 1.0 / rank;
            }
            rank++;
        }
        return 0.0;
    }

    private List<EmbeddingMatch<TextSegment>> deduplicateMatches(List<EmbeddingMatch<TextSegment>> matches) {
        Map<String, EmbeddingMatch<TextSegment>> unique = new LinkedHashMap<>();
        for (EmbeddingMatch<TextSegment> match : matches) {
            String docId = resolveDocumentId(match.embedded(), match.embeddingId());
            unique.putIfAbsent(docId, match);
        }
        return new ArrayList<>(unique.values());
    }

    private String resolveDocumentId(TextSegment segment, String fallback) {
        if (segment == null) {
            return fallback;
        }
        Metadata metadata = segment.metadata();
        if (metadata != null) {
            String[] preferredKeys = new String[]{
                    "fileName", "filename", "source", "documentId", "id", "doc_id"
            };
            for (String key : preferredKeys) {
                String value = metadata.getString(key);
                if (value != null && !value.isBlank()) {
                    return value;
                }
            }
        }
        return fallback;
    }

    private String extractSnippet(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        return trimmed.length() <= 300 ? trimmed : trimmed.substring(0, 300) + "...";
    }

    private List<String> splitIntoSentences(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String[] sentences = text.split("(?<=[。！？!?\\.])\\s*|\\n+");
        List<String> cleaned = new ArrayList<>();
        for (String sentence : sentences) {
            if (sentence != null) {
                String trimmed = sentence.trim();
                if (!trimmed.isEmpty()) {
                    cleaned.add(trimmed);
                }
            }
        }
        return cleaned;
    }

    private List<String> normalizeIds(List<String> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .map(this::normalizeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String normalizeId(String id) {
        if (id == null) {
            return null;
        }
        String trimmed = id.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private EmbeddingStore<TextSegment> resolveStore(EvaluationTarget target) {
        if (target == null || target.getVectorStore() == null) {
            return defaultEmbeddingStore;
        }
        VectorStoreConfig config = target.getVectorStore();
        return vectorStoreFactory.create(config);
    }

    private EvaluationTarget defaultTarget() {
        EvaluationTarget target = new EvaluationTarget();
        target.setStrategyName("default");
        target.setVectorStore(null);
        return target;
    }

    private void closeIfNecessary(EmbeddingStore<TextSegment> store) {
        if (store == null || store == defaultEmbeddingStore) {
            return;
        }
        if (store instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception ignored) {
                // 关闭异常忽略处理
            }
        }
    }

    private String determineStrategyName(EvaluationTarget target) {
        if (target == null || target.getStrategyName() == null || target.getStrategyName().isBlank()) {
            return "default";
        }
        return target.getStrategyName();
    }

    private double clamp(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }

    private Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return new HashSet<>();
        }
        String normalized = text.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}\\u4e00-\\u9fa5]+", " ");
        return Arrays.stream(normalized.split("\\s+"))
                .map(String::trim)
                .map(token -> token.toLowerCase(Locale.ROOT))
                .filter(token -> !token.isEmpty())
                .collect(Collectors.toCollection(HashSet::new));
    }

    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    private record MetricsAccumulator(double recall, double precision, double f1) {
    }

    private record GenerationStats(double faithfulness, double contextUtilization, double hallucinationRate) {
    }
}

