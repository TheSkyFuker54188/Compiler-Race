package AST;

import Midcode.midCode;
import Word.Word;

/**
 * 一元表达式节点类 - 处理单操作数的运算符
 * 
 * 一元操作符包括：
 * - 负号（-）：取相反数，如 -x
 * - 正号（+）：保持原值，如 +x（通常可以省略）
 * - 逻辑非（!）：布尔取反，如 !flag
 * 
 * 这些操作符只需要一个操作数，就像照镜子一样，
 * 输入一个值，输出一个变换后的值
 */
public class Unary extends Expr {
    /* 一元操作符作用的表达式 */
    public Expr operandExpression;

    /**
     * 创建一元表达式节点
     * 
     * @param unaryOperator     一元操作符（+, -, !）
     * @param operandExpression 被操作的表达式
     */
    public Unary(Word unaryOperator, Expr operandExpression) {
        super(unaryOperator);
        this.operandExpression = operandExpression;
    }

    /**
     * 检查一元表达式是否可以在编译期计算
     * 
     * 只有当操作数是编译期常量时，一元表达式才能预先计算。
     * 比如 -5 可以算成 -5，但 -x 需要运行时才知道结果
     */
    @Override
    public boolean canculculate() {
        if (operandExpression.canculculate()) {
            isvalue = true;
            String operatorSymbol = op.getContent();

            if (operatorSymbol.equals("-")) {
                // 负号：取相反数
                value = 0 - operandExpression.value;
            } else if (operatorSymbol.equals("+")) {
                // 正号：保持原值
                value = operandExpression.value;
            } else if (operatorSymbol.equals("!")) {
                // 逻辑非：0变1，非0变0
                value = operandExpression.value == 0 ? 1 : 0;
            }
            return true;
        }
        return false;
    }

    /**
     * 执行一元运算的实际计算
     */
    @Override
    public int calculate() {
        String operatorSymbol = op.getContent();
        if (operatorSymbol.equals("-"))
            return operandExpression.calculate() * (-1);
        else
            return operandExpression.calculate();
    }

    /**
     * 化简一元表达式并生成中间代码
     */
    @Override
    public Expr reduce() {
        // 如果能预先计算，直接返回常量
        if (isvalue) {
            return new Constant(new Word(String.valueOf(value)));
        }

        String operatorSymbol = op.getContent();

        if (operatorSymbol.equals("+")) {
            // 正号不需要额外操作，直接返回原表达式
            return operandExpression.reduce();
        } else if (operatorSymbol.equals("-")) {
            // 负号：生成 temp = 0 - operand 的代码
            Temp negationResult = new Temp(op);
            emit(new midCode(midCode.operation.MINUOP, negationResult.toString(),
                    "0", operandExpression.reduce().toString()));
            return negationResult;
        } else if (operatorSymbol.equals("!")) {
            // 逻辑非：生成 temp = (operand == 0) 的代码
            Temp notResult = new Temp(op);
            emit(new midCode(midCode.operation.EQLOP, notResult.toString(),
                    "0", operandExpression.reduce().toString()));
            return notResult;
        } else {
            return null;
        }
    }
}
