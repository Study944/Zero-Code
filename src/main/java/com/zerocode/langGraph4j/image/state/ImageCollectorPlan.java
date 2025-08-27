package com.zerocode.langGraph4j.image.state;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 图片收集计划
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageCollectorPlan {

    /**
     * 内容图片任务
     */
    private List<ContentImageTasks> contentImageTasks;

    /**
     * 插画图片任务
     */
    private List<IllustrationImageTasks> illustrationTasks;

    /**
     * 架构图片任务
     */
    private List<DiagramImageTasks> diagramImageTasks;


    public record ContentImageTasks(String query) {}

    public record IllustrationImageTasks(String query) {}

    public record DiagramImageTasks(String mermaidCode, String description) {}

}
