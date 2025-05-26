package AST;

import Midcode.midCode;

/**
 * 循环语句节点 - 编译器中的"旋转木马控制器"
 * 
 * While循环就像游乐场的旋转木马，只要条件满足就一直转圈，
 * 直到条件不满足才停下来。这是程序中实现重复执行的基本机制。
 * 
 * While循环的执行流程：
 * 1. 检查条件 - 就像检查旋转木马的安全状况
 * 2. 条件为真 - 执行循环体，然后回到步骤1
 * 3. 条件为假 - 跳出循环，继续执行后续代码
 * 
 * 实现时需要：
 * - 循环开始标签：供continue跳转回来
 * - 循环结束标签：供break跳转出去
 * - 条件检查：决定是否继续循环
 * - 循环栈管理：支持嵌套循环的break/continue
 */
public class While extends Stmt {
    // 循环条件 - 决定是否继续转圈的"安全检查员"
    Or loopCondition;
    // 循环体语句 - 每次循环执行的内容
    Stmt loopBodyStatement;
    // 跳转标签 - 标识这个循环的唯一ID
    int loopIdentifier;

    /**
     * 构造一个while循环节点
     * 
     * @param loopCondition     循环条件表达式
     * @param loopBodyStatement 循环体中要执行的语句
     */
    public While(Or loopCondition, Stmt loopBodyStatement) {
        this.loopCondition = loopCondition;
        this.loopBodyStatement = loopBodyStatement;
    }

    /**
     * 生成中间代码 - 构建程序的"循环控制系统"
     * 
     * 这个方法就像设计旋转木马的控制电路：
     * 1. 设置循环开始点
     * 2. 检查安全条件(循环条件)
     * 3. 执行一轮旋转(循环体)
     * 4. 回到开始点继续检查
     * 5. 条件不满足时设置停止点
     */
    @Override
    public void gen() {
        loopIdentifier = ++jumps;
        // 将当前循环ID压入栈，支持嵌套循环的break/continue
        loopstack.push(loopIdentifier);

        // 设置循环开始标签 - 旋转木马的"启动点"
        emit(new midCode(midCode.operation.Jump, "Loop" + loopIdentifier + "begin"));

        // 生成条件检查代码 - 安全检查，条件为假时跳出循环
        loopCondition.gen(loopIdentifier, true);

        // 生成循环体代码 - 执行一轮循环
        if (loopBodyStatement != null) {
            loopBodyStatement.gen();
        }

        // 回到循环开始处 - 回到启动点准备下一轮
        emit(new midCode(midCode.operation.GOTO, "Loop" + String.valueOf(loopIdentifier) + "begin"));

        // 设置循环结束标签 - 旋转木马的"停止点"
        emit(new midCode(midCode.operation.Jump, "Loop" + String.valueOf(loopIdentifier) + "end"));

        // 从循环栈中弹出当前循环ID
        loopstack.pop();
    }
}
