package AST;

import Midcode.midCode;
import Symbol_table.Symbols.ArraySymbol;
import Symbol_table.Symbols.VarSymbol;

import java.util.ArrayList;

/**
 * 常量定义节点 - 编译器中的"永久文件存档员"
 * 
 * 常量定义就像在图书馆里建立永久档案，一旦登记就不能修改。
 * 这个类专门处理常量的定义和初始化，确保这些值在编译时就确定下来，
 * 运行时不能再改变。
 * 
 * 常量的特点：
 * 1. 必须在定义时初始化 - 就像身份证号码，一经分配就不能更改
 * 2. 值在编译时确定 - 编译器可以直接用具体数值替换常量名
 * 3. 提供编译时优化 - 编译器可以进行常量折叠等优化
 * 
 * 支持的常量类型包括普通常量和常量数组。
 */
public class ConstDef extends Def {
    // 初始化表达式列表 - 存储常量的初始值表达式
    ArrayList<Expr> initializationExpressions;
    // 计算后的具体数值列表 - 存储编译时计算出的常量值
    ArrayList<Integer> computedConstantValues = new ArrayList<>();

    /**
     * 构造一个常量定义节点
     * 
     * @param targetVariable            被定义的常量(可能是普通常量或常量数组)
     * @param initializationExpressions 初始化表达式列表，常量必须有初值
     */
    public ConstDef(Lval targetVariable, ArrayList<Expr> initializationExpressions) {
        super(targetVariable);
        this.initializationExpressions = initializationExpressions;
    }

    /**
     * 生成中间代码 - 常量定义的"存档过程"
     * 
     * 这个方法就像档案管理员，把常量值"永久封存"起来：
     * 1. 首先计算所有初始化表达式的值
     * 2. 然后根据常量类型选择不同的存储方式
     * 3. 最后在符号表中登记，标记为不可修改
     */
    @Override
    public void gen() {
        for (Expr expression : initializationExpressions) {
            expression.canculculate();
        }

        // 计算所有常量值 - 在编译时就把值确定下来
        for (int i = 0; i < initializationExpressions.size(); i++) {
            computedConstantValues.add(initializationExpressions.get(i).calculate());
        }

        String constantName = targetVariable.op.getContent();

        if (targetVariable instanceof Id) {
            int constantValue = computedConstantValues.get(0);
            emit(new midCode(midCode.operation.CONST, constantName, String.valueOf(constantValue), null));
            inttable.add(constantName, new VarSymbol(constantName, true, constantValue));

        } else if (targetVariable instanceof Array) {
            Array arrayTarget = (Array) targetVariable;
            Expr firstDimension = arrayTarget.getOneindex();
            Expr secondDimension = arrayTarget.getTwoindex();
            int firstDimensionSize = firstDimension.calculate();

            if (secondDimension == null) {
                emit(new midCode(midCode.operation.ARRAY, constantName, String.valueOf(firstDimensionSize), null));
                for (int i = 0; i < initializationExpressions.size(); i++) {
                    emit(new midCode(midCode.operation.PUTARRAY, constantName, String.valueOf(i),
                            String.valueOf(computedConstantValues.get(i))));
                }
                inttable.add(constantName, new ArraySymbol(constantName, true, 1, computedConstantValues));
            } else {
                int secondDimensionSize = secondDimension.calculate();
                emit(new midCode(midCode.operation.ARRAY, constantName, String.valueOf(firstDimensionSize),
                        String.valueOf(secondDimensionSize)));
                for (int i = 0; i < initializationExpressions.size(); i++) {
                    emit(new midCode(midCode.operation.PUTARRAY, constantName, String.valueOf(i),
                            String.valueOf(computedConstantValues.get(i))));
                }
                inttable.add(constantName,
                        new ArraySymbol(constantName, true, 2, secondDimensionSize, computedConstantValues));
            }
        } else {
            System.out.print("--------------常量定义类型错误-----------");
        }
    }
}
