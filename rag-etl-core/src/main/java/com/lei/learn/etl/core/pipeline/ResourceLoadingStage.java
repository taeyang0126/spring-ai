package com.lei.learn.etl.core.pipeline;

import org.springframework.core.io.Resource;

import java.io.File;

/**
 * <p>
 * ResourceLoadingStage
 * </p>
 *
 * @author 伍磊
 */
public interface ResourceLoadingStage {

    TextSplittingStage fromResource(Resource resource);

    TextSplittingStage fromFile(File file);

    TextSplittingStage fromFile(String filepath);

}
