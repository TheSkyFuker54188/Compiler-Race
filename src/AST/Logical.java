package AST;

import Midcode.midCode;
import Word.Word;

/**
 * 逻辑比较表达式节点类 - 处理关系运算符
 * 
 * 当程序中出现比较操作，如 a < b、x == y、count >= limit 时，
 * 编译器会创建Logical节点。这些操作的结果总是布尔值，
 * 在C语言风格中用0表示false，1表示true。
 * 
 * 这就像判官做裁决：给出两个事物，判断它们的关系是否成立
 */
public class Logical extends Expr {
    /* 比较操作的左右两个表达式 */
    private Expr leftExpression, rightExpression;

    /**
     * 创建逻辑比较表达式节点
     * 
     * @param comparisonOperator 比较运算符（<, <=, >, >=, ==, !=）
     * @param leftExpression     左侧被比较的表达式
     * @param rightExpression    右侧被比较的表达式
     */
    public Logical(Word comparisonOperator, Expr leftExpression, Expr rightExpression) {
        super(comparisonOperator);
        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
    }

    /**
     * 检查比较表达式是否可以在编译期求值
     * 
     * 只有当两个操作数都是编译期常量时，比较结果才能预先确定。
     * 比如 5 > 3 肯定是true，但 a > 3 需要运行时才知道结果
     */
    @Override
    public boolean canculculate() {
        boolean leftIsConstant = leftExpression.canculculate();
        boolean rightIsConstant = rightExpression.canculculate();

        if (leftIsConstant && rightIsConstant) {
            this.isvalue = true;
            String comparisonSymbol = op.getContent();

            // 进行比较运算，结果用1/0表示true/false
            if (comparisonSymbol.equals("<")) {
                value = leftExpression.value < rightExpression.value ? 1 : 0;
            } else if (comparisonSymbol.equals("<=")) {
                value = leftExpression.value <= rightExpression.value ? 1 : 0;
            } else if (comparisonSymbol.equals(">")) {
                value = leftExpression.value > rightExpression.value ? 1 : 0;
            } else if (comparisonSymbol.equals(">=")) {
                value = leftExpression.value >= rightExpression.value ? 1 : 0;
            } else if (comparisonSymbol.equals("==")) {
                value = leftExpression.value == rightExpression.value ? 1 : 0;
            } else if (comparisonSymbol.equals("!=")) {
                value = leftExpression.value != rightExpression.value ? 1 : 0;
            }
            return true;
        }
        return false;
    }

    /**
     * 化简表达式并生成中间代码
     * 
     * 如果比较结果是编译期常量，就直接返回常量节点；
     * 否则生成三地址码形式的比较指令
     */
    public Expr reduce() {
        // 编译期可确定的比较直接返回结果常量
        if (isvalue) {
            return new Constant(new Word(String.valueOf(value)));
        }

        // 为比较结果创建临时变量
        Temp comparisonResult = new Temp(op);
        String comparisonSymbol = op.getContent();
        midCode.operation operationType = midCode.operation.DEBUG;

        // 将比较运算符映射到中间代码操作
        if (comparisonSymbol.equals("<")) {
            operationType = midCode.operation.LSSOP;
        } else if (comparisonSymbol.equals("<=")) {
            operationType = midCode.operation.LEQOP;
        } else if (comparisonSymbol.equals(">")) {
            operationType = midCode.operation.GREOP;
        } else if (comparisonSymbol.equals(">=")) {
            operationType = midCode.operation.GEQOP;
        } else if (comparisonSymbol.equals("==")) {
            operationType = midCode.operation.EQLOP;
        } else if (comparisonSymbol.equals("!=")) {
            operationType = midCode.operation.NEQOP;
        }

        // 发射比较指令的中间代码
        emit(new midCode(operationType, comparisonResult.toString(),
                leftExpression.reduce().toString(), rightExpression.reduce().toString()));
        return comparisonResult;
    }
}
