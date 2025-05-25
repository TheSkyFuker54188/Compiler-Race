package AST;

import Midcode.midCode;
import Symbol_table.Symbols.ArraySymbol;
import Symbol_table.Symbols.VarSymbol;

import java.util.ArrayList;

/**
 * 变量定义节点 - 编译器中的"内存分配专员"
 * 
 * 这个类负责处理变量的定义和初始化，就像现实中的房屋分配专员，
 * 不仅要给申请人分配房子(内存空间)，还要帮忙搬家具(初始化数据)。
 * 
 * 支持的变量类型：
 * 1. 普通变量：int x = 5; (分配一个整数大小的空间)
 * 2. 一维数组：int arr[10]; (分配连续的10个整数空间)
 * 3. 二维数组：int matrix[3][4]; (分配3×4的矩阵空间)
 * 
 * 初始化过程就像装修房子，可以选择精装修(有初值)或毛坯房(无初值)。
 */
public class VarDef extends Def {
    // 初始化值的二维列表 - 支持多维数组的初始化
    ArrayList<ArrayList<Expr>> initializationValues = null;

    /**
     * 构造一个变量定义节点
     * 
     * @param targetVariable       被定义的变量(可能是普通变量或数组)
     * @param initializationValues 初始化值列表，null表示不初始化
     */
    public VarDef(Lval targetVariable, ArrayList<ArrayList<Expr>> initializationValues) {
        super(targetVariable);
        this.initializationValues = initializationValues;
    }

    /**
     * 生成中间代码 - 变量定义的"施工过程"
     * 
     * 这个方法就像建筑工程师，根据不同的变量类型选择不同的施工方案：
     * - 普通变量：挖个坑(分配空间)，可选择性埋点东西(初始化)
     * - 数组：挖个大坑(分配连续空间)，按顺序摆放物品(数组初始化)
     */
    @Override
    public void gen() {
        // 预处理所有初始化表达式 - 像提前准备装修材料
        for (ArrayList<Expr> expressionRow : initializationValues) {
            for (Expr singleExpression : expressionRow) {
                singleExpression.canculculate();
            }
        }

        String variableName = targetVariable.op.getContent();

        if (targetVariable instanceof Id) {
            // 处理普通变量定义 - 就像分配单间公寓
            if (initializationValues.size() == 0) {
                // 无初始化的情况 - 毛坯房交付
                emit(new midCode(midCode.operation.VAR, variableName, null, null));
            } else {
                // 有初始化的情况 - 精装修交付
                Expr initExpression = initializationValues.get(0).get(0);
                emit(new midCode(midCode.operation.VAR, variableName, initExpression.reduce().toString(), null));
            }
            inttable.add(variableName, new VarSymbol(variableName, false, 0));

        } else if (targetVariable instanceof Array) {
            // 处理数组定义 - 就像分配连排别墅
            Array arrayTarget = (Array) targetVariable;
            Expr firstDimension = arrayTarget.getOneindex();
            Expr secondDimension = arrayTarget.getTwoindex();
            int firstDimensionSize = firstDimension.calculate();
            int secondDimensionSize = 0;

            if (secondDimension == null) {
                // 一维数组情况 - 分配一排房子
                emit(new midCode(midCode.operation.ARRAY, variableName, String.valueOf(firstDimensionSize), null));
                inttable.add(variableName, new ArraySymbol(variableName, false, 1));
            } else {
                // 二维数组情况 - 分配整个小区
                secondDimensionSize = secondDimension.calculate();
                emit(new midCode(midCode.operation.ARRAY, variableName, String.valueOf(firstDimensionSize),
                        String.valueOf(secondDimensionSize)));
                inttable.add(variableName, new ArraySymbol(variableName, false, 2, secondDimensionSize));
            }

            // 数组初始化 - 逐个房间摆放家具
            for (int rowIndex = 0; rowIndex < initializationValues.size(); rowIndex++) {
                for (int columnIndex = 0; columnIndex < initializationValues.get(rowIndex).size(); columnIndex++) {
                    int linearIndex = rowIndex * secondDimensionSize + columnIndex;
                    String initValue = initializationValues.get(rowIndex).get(columnIndex).reduce().toString();
                    emit(new midCode(midCode.operation.PUTARRAY, variableName, String.valueOf(linearIndex), initValue));
                }
            }
        } else {
            System.out.print("--------------变量定义类型错误-----------");
        }
    }
}
