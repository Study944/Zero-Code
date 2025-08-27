package com.zerocode.langGraph4j.image.node;

import com.zerocode.langGraph4j.image.state.ImageCollectorPlan;
import com.zerocode.langGraph4j.image.state.ImageResource;
import com.zerocode.langGraph4j.image.state.WorkflowContext;
import com.zerocode.langGraph4j.service.ImageCollectorPlanService;
import com.zerocode.langGraph4j.service.ImageCollectorService;
import com.zerocode.langGraph4j.tool.ImageSearchTool;
import com.zerocode.langGraph4j.tool.MermaidDiagramTool;
import com.zerocode.langGraph4j.tool.UnDrawIllustrationTool;
import com.zerocode.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 图片收集节点（AI生成关键词+描述，系统自主调用工具收集图片）
 */
@Slf4j
public class ImageCollectorPlanNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 图片收集");
            List<ImageResource> imageList = new ArrayList<>();
            try {
                // 调用AI制定图片收集计划
                ImageCollectorPlanService imageCollectorPlanService = SpringContextUtil.getBean(ImageCollectorPlanService.class);
                ImageCollectorPlan imageCollectorPlan = imageCollectorPlanService.collectImages(context.getOriginalPrompt());
                // 创建线程CompletableFuture并行执行图片收集任务
                List<CompletableFuture<List<ImageResource>>> futures = new ArrayList<>();
                // 根据收集计划获取内容图片
                if (imageCollectorPlan.getContentImageTasks() != null) {
                    ImageSearchTool imageSearchTool = SpringContextUtil.getBean(ImageSearchTool.class);
                    for (ImageCollectorPlan.ContentImageTasks contentImageTask : imageCollectorPlan.getContentImageTasks()) {
                        // 并行执行，添加到执行列表中
                        futures.add(
                                CompletableFuture.supplyAsync(() ->
                                        imageSearchTool.searchContentImages(contentImageTask.query()
                                        )
                                ));
                    }
                }
                // 根据收集计划获取插画图片
                if (imageCollectorPlan.getIllustrationTasks() != null) {
                    UnDrawIllustrationTool unDrawIllustrationTool = SpringContextUtil.getBean(UnDrawIllustrationTool.class);
                    for (ImageCollectorPlan.IllustrationImageTasks illustrationImageTasks : imageCollectorPlan.getIllustrationTasks()) {
                        futures.add(
                                CompletableFuture.supplyAsync(() ->
                                        unDrawIllustrationTool.searchIllustrations(illustrationImageTasks.query()
                                        )
                                ));
                    }
                }
                // 根据收集计划获取架构图片
                if (imageCollectorPlan.getDiagramImageTasks() != null) {
                    MermaidDiagramTool mermaidDiagramTool = SpringContextUtil.getBean(MermaidDiagramTool.class);
                    for (ImageCollectorPlan.DiagramImageTasks diagramImageTasks : imageCollectorPlan.getDiagramImageTasks()) {
                        futures.add(
                                CompletableFuture.supplyAsync(() ->
                                        mermaidDiagramTool.generateMermaidDiagram(diagramImageTasks.mermaidCode(), diagramImageTasks.description()
                                        )
                                ));
                    }
                }
                // 等待所有任务完成
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                // 解析图片收集结果
                for (CompletableFuture<List<ImageResource>> future : futures) {
                    List<ImageResource> imageResourceList = future.get();
                    if (imageResourceList != null) {
                        imageList.addAll(imageResourceList);
                    }
                }
                log.info("图片收集结果：{}", imageList.size());
            } catch (Exception e) {
                log.error("图片收集异常", e);
            }
            context.setCurrentStep("图片收集");
            context.setImageList(imageList);
            log.info("图片收集完成，共收集 {} 张图片", imageList.size());
            return WorkflowContext.saveContext(context);
        });
    }
}
