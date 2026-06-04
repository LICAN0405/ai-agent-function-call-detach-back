package com.example.agentbackend.service;
//控制Agent流程( Java 版的 processConversation)
import com.example.agentbackend.agent.ToolDefinitions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AgentService {
    private final KimiService kimiService;
    private final ToolExecuteService toolExecuteService;
    private final ObjectMapper objectMapper;

    //这个类 “注入依赖” 的构造方法
    public AgentService(
            KimiService kimiService,
            ToolExecuteService toolExecuteService,
            // JSON 转换工具
            ObjectMapper objectMapper
    ) {
        this.kimiService = kimiService;
        this.toolExecuteService = toolExecuteService;
        this.objectMapper = objectMapper;
    }

    public List<Map<String, Object>> process(Map<String, Object> requestBody) {
        // 从 requestBody 里拿 messages,如果没有 messages → 就返回一个空的 ArrayList
        List<Map<String, Object>> messages =
                (List<Map<String, Object>>) requestBody.getOrDefault("messages", new ArrayList<>());

        //拿到历史数据
        List<Map<String, Object>> currentMessages = new ArrayList<>(messages);

        //构造请求数据
        Map<String, Object> kimiRequest = new HashMap<>();
        kimiRequest.put("model", requestBody.getOrDefault("model", "moonshot-v1-8k"));
        kimiRequest.put("messages", buildMessagesWithSystemPrompt(currentMessages));
        kimiRequest.put("stream", false);
        kimiRequest.put("tools", ToolDefinitions.getTools());//把 ToolDefinitions 这个工具说明书放入请求参数中
        kimiRequest.put("tool_choice", "auto");

        //调用kimiService，使用大模型接口
        Map<String, Object> response = kimiService.chat(kimiRequest);

        //处理模型返回数据并加入历史数据中（使用extractAssistantMessage方法）
        Map<String, Object> assistantMessage = extractAssistantMessage(response);
        currentMessages.add(assistantMessage);

        //拿出模型返回数据中调用本地工具信息
        List<Map<String, Object>> toolCalls = (List<Map<String, Object>>) assistantMessage.get("tool_calls");

        //模型返回数据中没有对本地工具的调用则直接返回数据
        if (toolCalls == null || toolCalls.isEmpty()) {
            return currentMessages;
        }

        //模型返回的需要调用的本地工具
        for (Map<String, Object> toolCall : toolCalls) {
            Map<String, Object> function = (Map<String, Object>) toolCall.get("function");
            if (function == null) {
                continue;
            }
            //获取函数名
            String toolName = (String) function.get("name");

            //获取参数并转化为 CalcExpressionService 方法需要的格式类型（使用parseArguments方法）
            String argumentsJson = (String) function.get("arguments");
            Map<String, Object> args = parseArguments(argumentsJson);

            //调用 toolExecuteService 类判断使用到的本地工具，并运行返回计算后的数据
            Map<String, Object> toolResult = toolExecuteService.execute(toolName, args);

            //构造调用本地工具后的数据，并将数据添加进历史数据中
            Map<String, Object> toolMessage = new HashMap<>();
            toolMessage.put("role", "tool");
            toolMessage.put("name", toolName);
            toolMessage.put("tool_call_id", toolCall.get("id"));
            toolMessage.put("content", toJson(toolResult));//调用toJson方法，将返回给前端的数据格式转化为 JSON 格式
            currentMessages.add(toolMessage);
        }
        return currentMessages;
    }

    private List<Map<String, Object>> buildMessagesWithSystemPrompt(List<Map<String, Object>> messages) {
        List<Map<String, Object>> result = new ArrayList<>();

        result.add(Map.of(
                "role", "system",
                "content", "当用户提出连续计算或复杂数学表达式时，优先调用 calcExpression，并把完整表达式作为 expression 参数传入。"
        ));

        result.addAll(messages);
        return result;
    }

    //从 Kimi 返回的完整响应里，提取需要显示的 AI 消息 方法
    private Map<String, Object> extractAssistantMessage(Map<String, Object> response) {
        //从响应里拿到 choices 数组（Kimi 返回的结果列表）
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");

        //choices为空报错
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("Kimi 未返回 choices");
        }

        //获取需要的数据（ai一般只返回一个回答，结果一般在message里面）
        Map<String, Object> firstChoice = choices.get(0);
        Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");

        //message为空则报错
        if (message == null) {
            throw new RuntimeException("Kimi 未返回 message");
        }

        //返回获取到的 ai 消息
        return message;
    }

    //转化数据格式为 本地工具参数格式 方法
    private Map<String, Object> parseArguments(String argumentsJson) {
        if (argumentsJson == null || argumentsJson.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(argumentsJson, Map.class);
        } catch (JsonProcessingException e) {
            return Map.of("error", "工具参数解析失败");
        }
    }

    //将java对象转化为 JSON 格式 方法
    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{\"error\":\"工具结果序列化失败\"}";
        }
    }
}
