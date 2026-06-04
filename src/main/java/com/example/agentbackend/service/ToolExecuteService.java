package com.example.agentbackend.service;
//根据工具名分发到具体 Java 工具(相当于前端以前的 executeTool(toolName, args))
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ToolExecuteService {

    private final CalcExpressionService calcExpressionService;

    public ToolExecuteService(CalcExpressionService calcExpressionService) {
        this.calcExpressionService = calcExpressionService;
    }

    public Map<String, Object> execute(String toolName, Map<String, Object> args) {
        if ("calcExpression".equals(toolName)) {
            Object expression = args.get("expression");

            if (!(expression instanceof String)) {
                return Map.of("error", "expression 参数缺失或类型错误");
            }

            return calcExpressionService.calculate((String) expression);
        }

        return Map.of("error", "未知工具：" + toolName);
    }
}
