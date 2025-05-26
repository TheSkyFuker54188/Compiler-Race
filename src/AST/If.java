package AST;

import Midcode.midCode;

/**
 * 条件语句节点 - 编译器中的"路口指挥员"
 * 
 * If语句就像十字路口的交通指挥员，根据条件决定程序的执行方向。
 * 它体现了程序的分支控制能力，让程序能够根据不同情况做出不同的反应。
 * 
 * 支持两种形式：
 * 1. if-only: if (condition) statement; (单向分支，像"红灯停")
 * 2. if-else: if (condition) stmt1 else stmt2; (双向分支，像"红灯停，绿灯行")
 * 
 * 实现原理采用跳转指令：
 * - 条件为假时跳转到else分支或语句结束
 * - 条件为真时顺序执行if分支
 * - 需要精心安排跳转标签避免执行流混乱
 */
public class If extends Stmt {
    // 条件表达式 - 决定分支走向的"交通信号灯"
    Or conditionalExpression;
    // if分支语句 - 条件为真时运行的代码块
    Stmt truePathStatement;
    // else分支语句 - 条件为假时运行的代码块(可为null)
    Stmt falsePathStatement;
    // 第一个跳转标签 - 条件为假时的跳转目标
    int falseJumpLabel;
    // 第二个跳转标签 - if分支结束后的跳转目标
    int endJumpLabel;

    /**
     * 构造一个条件语句节点
     * 
     * @param conditionalExpression 条件表达式
     * @param truePathStatement     if分支的语句
     * @param falsePathStatement    else分支的语句(可为null表示没有else)
     */
    public If(Or conditionalExpression, Stmt truePathStatement, Stmt falsePathStatement) {
        this.conditionalExpression = conditionalExpression;
        this.truePathStatement = truePathStatement;
        this.falsePathStatement = falsePathStatement;
    }

    /**
     * 生成中间代码 - 构建程序的"交通控制系统"
     * 
     * 这个方法就像设计交通路线图，需要合理安排跳转路径：
     * - 单分支：条件假→跳转到结束位置
     * - 双分支：条件假→跳转到else，if结束→跳转到整体结束
     */
    @Override
    public void gen() {
        if (falsePathStatement == null) {
            // 处理单分支if语句 - 简单的"红灯停"逻辑
            falseJumpLabel = ++jumps;
            conditionalExpression.gen(falseJumpLabel);
            if (truePathStatement != null) {
                truePathStatement.gen();
            }
            emit(new midCode(midCode.operation.Jump, "Jump" + falseJumpLabel));
        } else {
            // 处理双分支if-else语句 - 复杂的"红绿灯"控制系统
            falseJumpLabel = ++jumps; // 条件为假时跳转到else分支
            endJumpLabel = ++jumps; // if分支结束后跳转到整体结束

            conditionalExpression.gen(falseJumpLabel);
            if (truePathStatement != null) {
                truePathStatement.gen();
            }
            emit(new midCode(midCode.operation.GOTO, "Jump" + endJumpLabel));
            emit(new midCode(midCode.operation.Jump, "Jump" + falseJumpLabel));
            falsePathStatement.gen();
            emit(new midCode(midCode.operation.Jump, "Jump" + endJumpLabel));
        }
    }
}
