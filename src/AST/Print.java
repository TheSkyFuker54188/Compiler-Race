package AST;

import Midcode.midCode;
import Word.Word;

import java.util.ArrayList;

/**
 * 输出语句节点 - 编译器中的"广播电台"
 * 
 * Print语句就像程序的广播电台，负责把程序内部的信息传递给外界。
 * 它需要处理格式化输出，就像电台主播需要把不同类型的内容
 * (新闻、音乐、广告)按照特定格式播放给听众。
 * 
 * 支持的输出格式：
 * 1. 纯文本：printf("Hello World"); (直接播放文字)
 * 2. 格式化：printf("Number: %d", x); (文字+变量混合播放)
 * 3. 多参数：printf("%d + %d = %d", a, b, c); (复杂格式)
 * 
 * 实现原理：
 * - 解析格式字符串，分离文本和占位符
 * - 为每个部分生成对应的输出指令
 * - 按顺序执行输出，形成最终结果
 */
public class Print extends Stmt {
    // 格式字符串 - 输出的"节目单"
    Word formatTemplate;
    // 表达式列表 - 要输出的变量值，对应格式串中的%d
    ArrayList<Expr> outputExpressions;
    // 解析后的字符串片段 - 分解后的"节目片段"
    ArrayList<String> parsedSegments = new ArrayList<>();

    /**
     * 构造一个输出语句节点
     * 
     * @param formatTemplate    格式字符串，包含文本和%d占位符
     * @param outputExpressions 要输出的表达式列表
     */
    public Print(Word formatTemplate, ArrayList<Expr> outputExpressions) {
        this.formatTemplate = formatTemplate;
        this.outputExpressions = outputExpressions;
    }

    /**
     * 生成中间代码 - 安排"节目播出"
     * 
     * 这个方法就像电台导播，需要：
     * 1. 准备所有要播放的内容(计算表达式)
     * 2. 按照节目单(格式串)安排播出顺序
     * 3. 区分文字播报和数字播报
     */
    @Override
    public void gen() {
        for (Expr expression : outputExpressions) {
            expression.canculculate();
        }

        int expressionIndex = 0;
        // 解析格式字符串 - 制作详细的节目单
        parseFormatString(formatTemplate.getContent());

        // 按照解析结果逐个输出 - 开始播报节目
        for (String segment : parsedSegments) {
            if (!segment.equals("%d")) {
                // 输出文本片段 - 播报文字内容
                emit(new midCode(midCode.operation.PRINT, segment, "string"));
                stringss.add(segment);
            } else {
                // 输出数字 - 播报变量值
                emit(new midCode(midCode.operation.PRINT,
                        outputExpressions.get(expressionIndex++).reduce().toString(),
                        "digit"));
            }
        }
    }

    /**
     * 解析格式字符串 - 制作节目单的详细过程
     * 
     * 这个方法就像节目编辑，把复杂的格式字符串分解成
     * 一个个可以独立处理的片段。
     * 
     * @param formatString 原始格式字符串
     */
    public void parseFormatString(String formatString) {
        int segmentStart = 1; // 跳过开头的引号
        int currentPosition = 1;

        while (currentPosition < formatString.length()) {
            // 检查是否遇到%d占位符
            if (formatString.charAt(currentPosition) == '%' &&
                    currentPosition + 1 < formatString.length() &&
                    formatString.charAt(currentPosition + 1) == 'd') {

                // 先保存%d之前的文本片段
                if (currentPosition != segmentStart) {
                    parsedSegments.add(formatString.substring(segmentStart, currentPosition));
                }

                // 保存%d占位符
                parsedSegments.add("%d");
                currentPosition += 2; // 跳过%d
                segmentStart = currentPosition;
                continue;
            }
            currentPosition++;
        }

        // 处理最后剩余的文本片段
        if (currentPosition != segmentStart) {
            parsedSegments.add(formatString.substring(segmentStart, currentPosition));
        }
    }
}
