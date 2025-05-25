package AST;

import Midcode.midCode;

/**
 * 跳出语句节点类 - 表示break语句
 * 
 * 当程序员写下break时，表示要立即跳出当前的循环
 * 就像你在走迷宫时突然决定放弃，直接传送到出口一样
 * 
 * 编译器需要生成一个跳转指令，跳到最近的循环结束位置
 */
public class Break extends Stmt {

    /**
     * 生成跳出语句的中间代码
     * 
     * 这里的逻辑是：
     * 1. 从循环标签栈中获取当前循环的标识
     * 2. 生成一个无条件跳转指令，跳到循环结束标签
     * 3. 相当于告诉程序："不管现在在做什么，直接跳到循环外面去！"
     */
    @Override
    public void gen() {
        int currentLoopId = loopstack.peek();
        emit(new midCode(midCode.operation.GOTO, "Loop" + String.valueOf(currentLoopId) + "end"));
    }
}
