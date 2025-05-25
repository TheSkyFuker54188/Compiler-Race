package AST;

import Midcode.midCode;

/**
 * 继续语句节点类 - 表示continue语句
 * 
 * 当程序员写下continue时，表示跳过本次循环的剩余部分，
 * 直接开始下一轮循环。就像你在跑步时跳过了一个障碍，
 * 但继续向前跑，而不是停下来。
 * 
 * 与break不同，continue不是跳出循环，而是跳到循环开头
 */
public class Continue extends Stmt {

    /**
     * 生成继续语句的中间代码
     * 
     * 这里的处理逻辑：
     * 1. 从循环标签栈中取出当前循环的标识符
     * 2. 生成跳转指令，目标是循环开始位置
     * 3. 相当于说："跳过后面的代码，回到循环开头重新开始！"
     */
    @Override
    public void gen() {
        int currentLoopId = loopstack.peek();
        emit(new midCode(midCode.operation.GOTO, "Loop" + String.valueOf(currentLoopId) + "begin"));
    }
}
