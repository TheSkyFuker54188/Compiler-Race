package AST;

import Midcode.midCode;

/**
 * 返回语句节点 - 编译器中的"快递派送员"
 * 
 * Return语句就像函数的快递派送员，负责把函数计算的结果
 * "打包"并"送回"给函数的调用者。它标志着函数执行的结束，
 * 就像快递员完成最后一次配送后回到配送中心。
 * 
 * 支持两种返回模式：
 * 1. 有返回值：return x + 5; (带货回家，把计算结果交给调用者)
 * 2. 无返回值：return; (空手回家，只是告诉调用者任务完成了)
 * 
 * 这个语句一旦执行，函数就立即结束，控制权交还给调用者，
 * 就像快递员交付包裹后立即离开，不会继续停留。
 */
public class Ret extends Stmt {
    // 返回值表达式 - 要"派送"的包裹内容(可为null表示无返回值)
    Expr returnValueExpression;

    /**
     * 构造一个返回语句节点
     * 
     * @param returnValueExpression 返回值表达式，null表示void返回
     */
    public Ret(Expr returnValueExpression) {
        this.returnValueExpression = returnValueExpression;
    }

    /**
     * 生成中间代码 - 执行"配送任务"
     * 
     * 这个方法就像快递派送流程：
     * 1. 检查是否有包裹要送(返回值)
     * 2. 如果有包裹，先把包裹准备好(计算返回值)
     * 3. 执行配送任务(生成返回指令)
     */
    @Override
    public void gen() {
        // 如果有返回值，先准备"包裹"
        if (returnValueExpression != null) {
            returnValueExpression.canculculate();
        }

        if (returnValueExpression == null) {
            // 空手返回 - 无返回值的情况
            emit(new midCode(midCode.operation.RET, null));
        } else {
            // 带货返回 - 有返回值的情况
            emit(new midCode(midCode.operation.RET, returnValueExpression.reduce().toString()));
        }
    }
}
