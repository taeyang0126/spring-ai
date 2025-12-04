package com.lei.learn.etl.core.pipeline;

import org.springframework.ai.vectorstore.VectorStore;

/**
 * <p>
 * VectorStoringStage
 * </p>
 *
 * @author 伍磊
 */
public interface VectorStoringStage {

    void toVectorStore(VectorStore vectorStore);

}
