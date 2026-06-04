package com.example.agentbackend.service;
//处理多数计算逻辑
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

@Service
public class CalcExpressionService {

    public Map<String, Object> calculate(String expression) {
        Map<String, Object> response = new HashMap<>();
        response.put("expression", expression);
        try {
            double result = eval(expression);
            response.put("result", result);
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        return response;
    }

    private double eval(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new RuntimeException("表达式不能为空");
        }

        String normalized = expression.replaceAll("\\s+", "");

        if (!normalized.matches("[0-9+\\-*/().]+")) {
            throw new RuntimeException("表达式包含不支持的字符");
        }

        ArrayDeque<Double> nums = new ArrayDeque<>();
        ArrayDeque<Character> ops = new ArrayDeque<>();

        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);

            if (isNumberStart(normalized, i)) {
                StringBuilder numText = new StringBuilder();

                if (ch == '+' || ch == '-') {
                    numText.append(ch);
                    i++;
                }

                while (i < normalized.length()) {
                    char current = normalized.charAt(i);

                    if (Character.isDigit(current) || current == '.') {
                        numText.append(current);
                        i++;
                    } else {
                        break;
                    }
                }

                i--;

                double num;
                try {
                    num = Double.parseDouble(numText.toString());
                } catch (NumberFormatException e) {
                    throw new RuntimeException("数字格式错误");
                }

                nums.push(num);
                continue;
            }

            if (ch == '(') {
                ops.push(ch);
                continue;
            }

            if (ch == ')') {
                while (!ops.isEmpty() && ops.peek() != '(') {
                    applyOp(nums, ops);
                }

                if (ops.isEmpty()) {
                    throw new RuntimeException("括号不匹配");
                }

                ops.pop();
                continue;
            }

            if (isOperator(ch)) {
                while (
                        !ops.isEmpty()
                                && ops.peek() != '('
                                && priority(ops.peek()) >= priority(ch)
                ) {
                    applyOp(nums, ops);
                }

                ops.push(ch);
                continue;
            }

            throw new RuntimeException("表达式格式错误");
        }

        while (!ops.isEmpty()) {
            if (ops.peek() == '(') {
                throw new RuntimeException("括号不匹配");
            }

            applyOp(nums, ops);
        }

        if (nums.size() != 1) {
            throw new RuntimeException("表达式格式错误");
        }

        return nums.pop();
    }

    private boolean isNumberStart(String expression, int index) {
        char ch = expression.charAt(index);

        if (Character.isDigit(ch) || ch == '.') {
            return true;
        }

        if (ch != '+' && ch != '-') {
            return false;
        }

        if (index + 1 >= expression.length()) {
            return false;
        }

        char next = expression.charAt(index + 1);

        if (!Character.isDigit(next) && next != '.') {
            return false;
        }

        if (index == 0) {
            return true;
        }

        char prev = expression.charAt(index - 1);

        return isOperator(prev) || prev == '(';
    }

    private boolean isOperator(char ch) {
        return ch == '+' || ch == '-' || ch == '*' || ch == '/';
    }

    private int priority(char op) {
        if (op == '+' || op == '-') {
            return 1;
        }

        if (op == '*' || op == '/') {
            return 2;
        }

        return 0;
    }

    private void applyOp(ArrayDeque<Double> nums, ArrayDeque<Character> ops) {
        if (nums.size() < 2 || ops.isEmpty()) {
            throw new RuntimeException("表达式格式错误");
        }

        double right = nums.pop();
        double left = nums.pop();
        char op = ops.pop();

        double result;

        switch (op) {
            case '+':
                result = left + right;
                break;
            case '-':
                result = left - right;
                break;
            case '*':
                result = left * right;
                break;
            case '/':
                if (right == 0) {
                    throw new RuntimeException("除数不能为 0");
                }
                result = left / right;
                break;
            default:
                throw new RuntimeException("未知运算符");
        }

        nums.push(result);
    }
}