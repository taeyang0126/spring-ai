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
import java.util.ArrayList;
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
            throw new IllegalArgumentException("Resource must not be null");
        }
        this.resource = resource;
        return this;
    }

    @Override
    public TextSplittingStage fromFile(File file) {
        if (null == file) {
            throw new IllegalArgumentException("File must not be null");
        }
        if (!file.exists()) {
            throw new IllegalArgumentException(String.format("File does not exist: %s", file.getPath()));
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException(String.format("File must not be a directory: %s", file.getPath()));
        }
        this.resource = new FileSystemResource(file);
        return this;
    }

    @Override
    public TextSplittingStage fromFile(String filepath) {
        if (null == filepath) {
            throw new IllegalArgumentException("File path must not be null");
        }
        File file = new File(filepath);
        if (!file.exists()) {
            throw new IllegalArgumentException(String.format("File does not exist: %s", filepath));
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException(String.format("File must not be a directory: %s", filepath));
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
            throw new IllegalArgumentException("VectorStore must not be null");
        }
        this.vectorStore = vectorStore;
        execute();
    }

    private void execute() {
        // 1. 读取
        DocumentReader reader = getReader();
        if (null == reader) {
            throw new IllegalArgumentException(
                "DocumentReader must not be null. getReader() returned null, please check implementation.");
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

        // 3. 批量保存到向量存储
        int batchSize = getBatchSize();
        List<Integer> failedBatches = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i += batchSize) {
            List<Document> batch = chunks.subList(i, Math.min(i + batchSize, chunks.size()));
            try {
                vectorStore.add(batch);
                log.debug("[rag document init] success | index={}, size={}", i, batch.size());
            } catch (Exception e) {
                log.error("[rag document init] failed to add batch starting at index {}", i, e);
                failedBatches.add(i);
            }
        }

        // 所有批次处理完毕后，如果有失败则抛出异常
        if (!failedBatches.isEmpty()) {
            throw new IllegalStateException(
                String.format("[rag document init] 部分批次处理失败：%d 个批次失败（索引：%s）。" +
                              "注意：部分批次可能已成功写入 VectorStore，请检查数据一致性。",
                              failedBatches.size(), failedBatches));
        }

    }


}
