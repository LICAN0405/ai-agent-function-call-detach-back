package com.example.agentbackend.service;
//处理多数计算逻辑
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

@Service
public class CalcExpressionService {

    //总流程-调用方法并处理返回结果
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

    //核心计算函数-使用栈算法
    private double eval(String expression) {
        //检查表达式合法性
        if (expression == null || expression.trim().isEmpty()) {
            throw new RuntimeException("表达式不能为空");
        }

        //去掉表达式中所有的空格
        String normalized = expression.replaceAll("\\s+", "");

        //检查表达式中是否至包含 +-*/ 四则运算符号
        if (!normalized.matches("[0-9+\\-*/().]+")) {
            throw new RuntimeException("表达式包含不支持的字符");
        }

        //数字栈+运算符栈
        ArrayDeque<Double> nums = new ArrayDeque<>();
        ArrayDeque<Character> ops = new ArrayDeque<>();

        //从左到右扫描表达式
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);

            //判断当前位置是不是“一个数字整体的开始”
            if (isNumberStart(normalized, i)) {
                //数字可能不止一位，所以需要将多个字符拼接起来
                StringBuilder numText = new StringBuilder();
                //数字前面带有符号时，需要将符号拼接进去
                if (ch == '+' || ch == '-') {
                    numText.append(ch);
                    i++;
                }
                //继续读取表达式
                while (i < normalized.length()) {
                    char current = normalized.charAt(i);
                    if (Character.isDigit(current) || current == '.') {
                        numText.append(current);
                        i++;
                    } else {
                        break;
                    }
                }
                // while 最后一次下标 i 会被多加一个，for循环还会加，所以要减去防止表达式某个被跳了
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

            // 是 （ 则放入运算符栈
            if (ch == '(') {
                ops.push(ch);
                continue;
            }

            // 是 ） 则弹出进行计算，直至遇到左括号
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

            //判断当前入栈是否是符号（其中会比较优先级，如果入栈符号优先级低于前面运算符就把前面进行运算，直至入栈由新阿基高于栈中或者栈中无运算符后，将当前运算符进行入栈操作）
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

        //表达式扫描完，运算符栈还有运算符就报错
        while (!ops.isEmpty()) {
            if (ops.peek() == '(') {
                throw new RuntimeException("括号不匹配");
            }
            applyOp(nums, ops);
        }

        //最后扫描完，数字栈中只能有一个最终结果。如果最后还剩多个数字就报错
        if (nums.size() != 1) {
            throw new RuntimeException("表达式格式错误");
        }

        return nums.pop();
    }

    //判断当前位置是不是“一个数字整体的开始”
    private boolean isNumberStart(String expression, int index) {
        char ch = expression.charAt(index);

        //数字/小数点 默认为数字开始
        if (Character.isDigit(ch) || ch == '.') {
            return true;
        }

        //不是数字/小数点/+/- 就不是数字开始
        if (ch != '+' && ch != '-') {
            return false;
        }

        //当为+或者- 但是最后的了，就不是数字正负号
        if (index + 1 >= expression.length()) {
            return false;
        }

        //+ -后面不是数字/小数点 则这个+ -号不是数字正负号
        char next = expression.charAt(index + 1);
        if (!Character.isDigit(next) && next != '.') {
            return false;
        }

        //+ -在表达式开头则为数字正负号，是数字开始
        if (index == 0) {
            return true;
        }

        //如果 + 或 - 不在开头，那就看它前面的字符。如果前面是运算符或（时则当前为数字正负号，可以作为数字开头。
        //例如：1+-2，此时-就是数字开头；1-2，此时-就不是数字开头
        char prev = expression.charAt(index - 1);
        return isOperator(prev) || prev == '(';
    }

    //判断字符是不是运算符 +-*/
    private boolean isOperator(char ch) {
        return ch == '+' || ch == '-' || ch == '*' || ch == '/';
    }

    //判断运算符优先级
    private int priority(char op) {
        if (op == '+' || op == '-') {
            return 1;
        }
        if (op == '*' || op == '/') {
            return 2;
        }
        return 0;
    }

    //执行一次运算（从数字）
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