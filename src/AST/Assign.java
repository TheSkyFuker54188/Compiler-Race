package AST;

import Midcode.midCode;

/**
 * 赋值语句节点 - 编译器中的"搬运工"
 * 
 * 赋值语句是程序中最基础的操作之一，就像现实中的搬运工，
 * 负责把一个地方的值"搬运"到另一个地方。这个操作看似简单，
 * 但在编译器内部需要区分不同的情况：
 * 
 * 1. 普通变量赋值：x = 5; (直接把值搬到变量的内存位置)
 * 2. 数组元素赋值：arr[3] = 10; (需要先计算索引，再搬运到对应位置)
 * 
 * 赋值的本质是修改内存中的数据，左边必须是可写的位置(左值)，
 * 右边可以是任何能产生数值的表达式(右值)。
 */
public class Assign extends Stmt {
    // 赋值目标 - 接收数值的左值表达式
    Lval assignmentTarget;
    // 赋值源 - 提供数值的右值表达式
    Expr sourceExpression;

    /**
     * 构造一个赋值语句节点
     * 
     * @param assignmentTarget 赋值的目标位置(左值)
     * @param sourceExpression 赋值的源表达式(右值)
     */
    public Assign(Lval assignmentTarget, Expr sourceExpression) {
        this.assignmentTarget = assignmentTarget;
        this.sourceExpression = sourceExpression;
    }

    /**
     * 生成中间代码 - 执行"搬运"操作
     * 
     * 这个方法就像智能搬运工，会根据目标类型选择不同的搬运方式：
     * - 如果是普通变量：直接搬运到变量位置
     * - 如果是数组元素：先找到正确的索引位置，再搬运
     */
    @Override
    public void gen() {
        // 预处理右值表达式 - 准备要搬运的"货物"
        sourceExpression.canculculate();

        if (assignmentTarget instanceof Id) {
            // 普通变量赋值 - 直接搬运模式
            emit(new midCode(midCode.operation.ASSIGNOP,
                    assignmentTarget.reduce().toString(),
                    sourceExpression.reduce().toString()));
        } else {
            // 数组元素赋值 - 定位搬运模式
            emit(new midCode(midCode.operation.PUTARRAY,
                    assignmentTarget.getcontent(),
                    assignmentTarget.toString(),
                    sourceExpression.reduce().toString()));
        }
    }
}
