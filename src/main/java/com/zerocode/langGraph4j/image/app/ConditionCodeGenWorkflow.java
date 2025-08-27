package com.zerocode.langGraph4j.image.app;

import com.zerocode.ai.entity.GeneratorTypeEnum;
import com.zerocode.exception.BusinessException;
import com.zerocode.exception.ErrorCode;
import com.zerocode.langGraph4j.image.node.*;
import com.zerocode.langGraph4j.image.state.QualityResult;
import com.zerocode.langGraph4j.image.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

/**
 * 工作流（条件边，循环边）
 */
@Slf4j
public class ConditionCodeGenWorkflow {

    /**
     * 创建完整的工作流
     */
    public CompiledGraph<MessagesState<String>> createWorkflow() {
        try {
            return new MessagesStateGraph<String>()
                    // 添加节点 - 使用完整实现的节点
                    .addNode("image_collector", ImageCollectorNode.create())
                    .addNode("prompt_enhancer", PromptEnhancerNode.create())
                    .addNode("router", RouterNode.create())
                    .addNode("code_generator", CodeGeneratorNode.create())
                    .addNode("project_builder", ProjectBuilderNode.create())
                    // 添加节点 - 代码质量检测
                    .addNode("code_quality_check", CodeQualityCheckNode.create())

                    // 添加边
                    .addEdge(START, "image_collector")
                    .addEdge("image_collector", "prompt_enhancer")
                    .addEdge("prompt_enhancer", "router")
                    .addEdge("router", "code_generator")
                    // 条件边+循环边
                    .addConditionalEdges("code_generator",
                            edge_async(this::isQualityCheckPass),
                            Map.of(
                                    "fail","code_generator",
                                    "build", "project_builder",
                                    "skip", END
                            ))
                    .addEdge("project_builder", END)

                    // 编译工作流
                    .compile();
        } catch (GraphStateException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "工作流创建失败");
        }
    }

    private String isQualityCheckPass(MessagesState<String> messagesState) {
        WorkflowContext workflowContext = WorkflowContext.getContext(messagesState);
        GeneratorTypeEnum generatorType = workflowContext.getGeneratorType();
        QualityResult qualityResult = workflowContext.getQualityResult();
        // 判断AI检测代码的结果是否通过
        if (!qualityResult.getIsValid()){
            log.error("代码质量检查未通过: {}", qualityResult.getErrors());
            // 代码检测不通过
            return "fail";
        }
        if (generatorType == GeneratorTypeEnum.VUE_PROJECT) {
            return "build";
        } else {
            return "skip";
        }
    }

    /**
     * 执行工作流
     */
    public WorkflowContext executeWorkflow(String originalPrompt) {
        CompiledGraph<MessagesState<String>> workflow = createWorkflow();

        // 初始化 WorkflowContext
        WorkflowContext initialContext = WorkflowContext.builder()
                .originalPrompt(originalPrompt)
                .currentStep("初始化")
                .build();

        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
        log.info("工作流图:\n{}", graph.content());
        log.info("开始执行代码生成工作流");

        WorkflowContext finalContext = null;
        int stepCounter = 1;
        for (NodeOutput<MessagesState<String>> step : workflow.stream(
                Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
            log.info("--- 第 {} 步完成 ---", stepCounter);
            // 显示当前状态
            WorkflowContext currentContext = WorkflowContext.getContext(step.state());
            if (currentContext != null) {
                finalContext = currentContext;
                log.info("当前步骤上下文: {}", currentContext);
            }
            stepCounter++;
        }
        log.info("代码生成工作流执行完成！");
        return finalContext;
    }


}
