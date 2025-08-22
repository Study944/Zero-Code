package com.zerocode.langGraph4j;

import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.GraphStateException;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.StateGraph.END;

import java.util.Map;

/**
 * 简单工作流图 Demo
 */
public class SimpleGraphApp {

    public static void main(String[] args) throws GraphStateException {
        // 1.初始化节点
        GreeterNode greeterNode = new GreeterNode();
        ResponderNode responderNode = new ResponderNode();

        // 2.定义图形结构 StateGraph
       var stateGraph = new StateGraph<>(SimpleState.SCHEMA, initData -> new SimpleState(initData))
             // 添加节点到图形
            .addNode("greeter", node_async(greeterNode))
            .addNode("responder", node_async(responderNode))
            // 定义边 Edges
            .addEdge(START, "greeter") // 从 START 节点开始
            .addEdge("greeter", "responder")
            .addEdge("responder", END)   // 在 END 节点之后结束
             ;
        // 3.编译图表
        var compiledGraph = stateGraph.compile();

        // 4.获取图表的图形展示
        GraphRepresentation stateGraphGraph = stateGraph.getGraph(GraphRepresentation.Type.MERMAID, "Demo");
        System.out.println(stateGraphGraph.content());

        // 5.运行图表
        for (var item : compiledGraph.stream( Map.of( SimpleState.MESSAGES_KEY, "让我们开始吧！" ) ) ) {
            System.out.println( item );
        }

    }
}