package com.lei.learn.spring.ai.rag;

import com.alibaba.cloud.ai.document.DocumentWithScore;
import com.alibaba.cloud.ai.model.RerankModel;
import com.alibaba.cloud.ai.model.RerankRequest;
import com.alibaba.cloud.ai.model.RerankResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;

import java.util.List;

/**
 * <p>
 * 重排序
 * </p>
 *
 * @author 伍磊
 */
@Log4j2
public class ReRankDocumentPostProcessor implements DocumentPostProcessor {

    private final RerankModel reRankChatModel;
    private final double scoreThreshold;


    public ReRankDocumentPostProcessor(RerankModel reRankChatModel) {
        this(reRankChatModel, DEFAULT_SCORE_THRESHOLD);
    }

    public ReRankDocumentPostProcessor(RerankModel reRankChatModel, double scoreThreshold) {
        this.reRankChatModel = reRankChatModel;
        this.scoreThreshold = scoreThreshold;
    }

    // 默认过滤分数
    private static final double DEFAULT_SCORE_THRESHOLD = 0.5;

    @Override
    public List<Document> process(Query query, List<Document> documents) {
        String queryText = query.text();
        RerankResponse rerankResponse = reRankChatModel.call(new RerankRequest(queryText, documents));
        List<DocumentWithScore> results = rerankResponse.getResults();
        // 过滤出 score
        List<DocumentWithScore> filteredResults = results.stream()
                .filter(t -> t.getScore() > scoreThreshold)
                .toList();
        log.info("[ReRankDocumentPostProcessor] size: {}, results: {}", filteredResults.size(),
                filteredResults.stream().map(t -> t.getOutput().getId() + ":"
                        + t.getScore()).toList() + System.lineSeparator()
        );
        return filteredResults.stream()
                .map(DocumentWithScore::getOutput)
                .toList();
    }
}



