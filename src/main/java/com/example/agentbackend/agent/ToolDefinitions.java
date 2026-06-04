package com.example.agentbackend.agent;
//工具说明书
import java.util.List;
import java.util.Map;

public class ToolDefinitions {

    public static List<Map<String, Object>> getTools() {
        return List.of(
                Map.of(
                        "type", "function",
                        "function", Map.of(
                                "name", "calcExpression",
                                "description", "计算完整数学表达式，适合处理 1+2+3+4+5、(1+2)*3 这类连续或复杂运算",
                                "parameters", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "expression", Map.of(
                                                        "type", "string",
                                                        "description", "完整数学表达式，例如 \"1+2+3+4+5\""
                                                )
                                        ),
                                        "required", List.of("expression")
                                )
                        )
                )
        );
    }
}