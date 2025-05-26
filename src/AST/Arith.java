package AST;

import Midcode.midCode;
import Word.Word;

/**
 * 算术表达式节点类 - 处理数学运算表达式
 * 
 * 当我们看到 a + b 或者 x * y 这样的算术表达式时，
 * 编译器会创建一个Arith节点来表示。它包含：
 * - 操作符（+, -, *, /, %）
 * - 左操作数和右操作数
 * 
 * 就像数学课上的运算树，每个节点都是一个运算操作
 */
public class Arith extends Expr {
    /* 左侧操作数表达式 */
    public Expr leftOperand, rightOperand;

    /**
     * 创建算术表达式节点
     * 
     * @param operatorToken 运算符词法单元
     * @param leftOperand   左操作数表达式
     * @param rightOperand  右操作数表达式
     */
    public Arith(Word operatorToken, Expr leftOperand, Expr rightOperand) {
        super(operatorToken);
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }

    /**
     * 检查是否可以在编译期计算出结果
     * 
     * 只有当左右两个操作数都是编译期常量时，整个算术表达式才能预先计算。
     * 比如 2 + 3 可以直接算成 5，但 a + 3 就不行（a是变量）
     * 这种编译期计算叫做"常量折叠"优化
     */
    @Override
    public boolean canculculate() {
        boolean leftCanCompute = leftOperand.canculculate();
        boolean rightCanCompute = rightOperand.canculculate();

        if (leftCanCompute && rightCanCompute) {
            this.isvalue = true;
            String operatorSymbol = op.getContent();

            // 根据操作符进行相应的数学运算
            if (operatorSymbol.equals("+")) {
                value = leftOperand.value + rightOperand.value;
            } else if (operatorSymbol.equals("-")) {
                value = leftOperand.value - rightOperand.value;
            } else if (operatorSymbol.equals("*")) {
                value = leftOperand.value * rightOperand.value;
            } else if (operatorSymbol.equals("/")) {
                value = leftOperand.value / rightOperand.value;
            } else if (operatorSymbol.equals("%")) {
                value = leftOperand.value % rightOperand.value;
            }
            return true;
        }
        return false;
    }

    /**
     * 化简表达式并生成中间代码
     * 
     * 如果能预先计算，就返回常量节点；
     * 否则生成三地址码的形式：temp = operand1 op operand2
     */
    public Expr reduce() {
        // 如果已经计算出常量值，直接返回常量节点
        if (isvalue) {
            return new Constant(new Word(String.valueOf(value)));
        }

        // 生成临时变量存储运算结果
        Temp temporaryResult = new Temp(op);
        String operatorSymbol = op.getContent();
        midCode.operation operationType = midCode.operation.DEBUG;

        // 将高级语言的操作符映射到中间代码操作类型
        if (operatorSymbol.equals("+")) {
            operationType = midCode.operation.PLUSOP;
        } else if (operatorSymbol.equals("-")) {
            operationType = midCode.operation.MINUOP;
        } else if (operatorSymbol.equals("*")) {
            operationType = midCode.operation.MULTOP;
        } else if (operatorSymbol.equals("/")) {
            operationType = midCode.operation.DIVOP;
        } else if (operatorSymbol.equals("%")) {
            operationType = midCode.operation.MODOP;
        }

        // 发射中间代码：temp = left op right
        emit(new midCode(operationType, temporaryResult.toString(),
                leftOperand.reduce().toString(), rightOperand.reduce().toString()));
        return temporaryResult;
    }

    /**
     * 执行算术运算的实际计算
     * 递归计算左右操作数的值，然后根据操作符进行运算
     */
    @Override
    public int calculate() {
        int resultValue = 0;
        int leftValue = leftOperand.calculate();
        int rightValue = rightOperand.calculate();
        String operatorSymbol = op.getContent();

        // 进行对应的数学运算
        if (operatorSymbol.equals("+")) {
            resultValue = leftValue + rightValue;
        } else if (operatorSymbol.equals("-")) {
            resultValue = leftValue - rightValue;
        } else if (operatorSymbol.equals("*")) {
            resultValue = leftValue * rightValue;
        } else if (operatorSymbol.equals("/")) {
            resultValue = leftValue / rightValue;
        } else if (operatorSymbol.equals("%")) {
            resultValue = leftValue % rightValue;
        }
        return resultValue;
    }
}
