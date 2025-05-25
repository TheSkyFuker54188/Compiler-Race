package AST;

import Midcode.midCode;

import java.util.ArrayList;

/**
 * 逻辑与表达式节点类 - 处理 && 运算符
 * 
 * 逻辑与有个特殊性质叫"短路求值"：如果左边的条件是false，
 * 就不需要计算右边了，整个表达式肯定是false。
 * 
 * 就像过安检：如果身份证检查不通过，就不用再检查行李了，
 * 肯定过不了安检。编译器需要生成相应的跳转逻辑。
 */
public class And extends Node {
    /* 参与逻辑与运算的所有表达式列表 */
    ArrayList<Expr> conditionExpressions;

    /* 当前逻辑与表达式的跳转标签编号 */
    private int jumpLabelId;

    /**
     * 创建逻辑与表达式节点
     * 
     * @param conditionExpressions 需要进行与运算的表达式列表
     */
    public And(ArrayList<Expr> conditionExpressions) {
        this.conditionExpressions = conditionExpressions;
    }

    /**
     * 生成逻辑与的短路求值代码
     * 
     * 生成的逻辑：
     * 1. 依次计算每个条件表达式
     * 2. 如果任何一个是false(0)，就跳转到失败标签
     * 3. 只有所有条件都通过，才会继续执行后续代码
     * 
     * @param successLabel 所有条件都为true时要跳转到的标签
     */
    public void gen(int successLabel) {
        // 先尝试常量折叠优化
        for (Expr condition : conditionExpressions)
            condition.canculculate();

        // 为当前And表达式分配失败跳转标签
        jumpLabelId = ++jumps;

        // 为每个条件生成短路跳转代码
        for (Expr condition : conditionExpressions) {
            // 如果条件为false(0)，跳转到And表达式失败标签
            emit(new midCode(midCode.operation.BZ, "Jump" + jumpLabelId, condition.reduce().toString()));
        }

        // 所有条件都通过，跳转到成功标签
        emit(new midCode(midCode.operation.GOTO, "Jump" + successLabel));

        // 设置And表达式失败时的跳转目标
        emit(new midCode(midCode.operation.Jump, "Jump" + jumpLabelId));
    }
}
