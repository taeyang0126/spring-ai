package com.lei.learn.etl.core.pipeline;

import org.springframework.ai.transformer.splitter.TextSplitter;

/**
 * <p>
 * TextSplittingStage
 * </p>
 *
 * @author 伍磊
 */
public interface TextSplittingStage {

    VectorStoringStage withTextSplitter(TextSplitter splitter);

}
