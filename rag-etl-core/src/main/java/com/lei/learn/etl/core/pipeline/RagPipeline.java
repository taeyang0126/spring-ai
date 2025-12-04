package com.lei.learn.etl.core.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.List;

/**
 * <p>
 * RagPipeline
 * </p>
 *
 * @author 伍磊
 */
public abstract class RagPipeline implements ResourceLoadingStage,
        TextSplittingStage,
        VectorStoringStage {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected Resource resource;
    protected TextSplitter splitter;
    protected VectorStore vectorStore;

    protected abstract DocumentReader getReader();

    protected int getBatchSize() {
        return 10;
    }

    @Override
    public TextSplittingStage fromResource(Resource resource) {
        if (null == resource) {
            throw new IllegalArgumentException();
        }
        this.resource = resource;
        return this;
    }

    @Override
    public TextSplittingStage fromFile(File file) {
        if (null == file) {
            throw new IllegalArgumentException();
        }
        if (!file.exists() || file.isDirectory()) {
            throw new IllegalArgumentException();
        }
        this.resource = new FileSystemResource(file);
        return this;
    }

    @Override
    public TextSplittingStage fromFile(String filepath) {
        if (null == filepath) {
            throw new IllegalArgumentException();
        }
        File file = new File(filepath);
        if (!file.exists() || file.isDirectory()) {
            throw new IllegalArgumentException();
        }
        this.resource = new FileSystemResource(file);
        return this;
    }

    @Override
    public VectorStoringStage withTextSplitter(TextSplitter splitter) {
        this.splitter = splitter;
        return this;
    }

    @Override
    public void toVectorStore(VectorStore vectorStore) {
        if (null == vectorStore) {
            throw new IllegalArgumentException();
        }
        this.vectorStore = vectorStore;
        execute();
    }

    private void execute() {
        // 1. 读取
        DocumentReader reader = getReader();
        if (null == reader) {
            throw new IllegalArgumentException();
        }
        List<Document> documents = reader.get();
        if (CollectionUtils.isEmpty(documents)) {
            throw new IllegalArgumentException("not found document!");
        }

        // 2. 切分
        List<Document> chunks;
        if (splitter != null) {
            chunks = splitter.apply(documents);
        } else {
            chunks = documents;
        }

        // 3. 保存
        int batchSize = getBatchSize();
        for (int i = 0; i < chunks.size(); i += batchSize) {
            List<Document> batch = chunks.subList(i, Math.min(i + batchSize, chunks.size()));
            try {
                vectorStore.add(batch);
                log.info("[rag document init] success | index={}, size={}", i, batch.size());
            } catch (Exception e) {
                log.error("ailed to add batch starting at index {}", i, e);
                throw e;
            }
        }

    }


}
