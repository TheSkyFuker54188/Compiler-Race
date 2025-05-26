package AST;

import Midcode.midCode;
import Symbol_table.Symbols.ArraySymbol;
import Symbol_table.Symbols.VarSymbol;

/**
 * 形式参数节点 - 编译器中的"接收窗口"
 * 
 * 形式参数就像函数的接收窗口，定义了函数能够接收什么类型的数据。
 * 它就像邮局的分类窗口，需要明确标注能接收的邮件类型：
 * - 普通信件(普通变量参数)
 * - 包裹(一维数组参数)
 * - 大件货物(二维数组参数)
 * 
 * 参数的维度信息：
 * 0: 普通变量参数 - int x
 * 1: 一维数组参数 - int arr[]
 * 2: 二维数组参数 - int matrix[][n]
 * 
 * 对于二维数组，第二维的大小必须在编译时确定，
 * 这样编译器才能正确计算数组元素的地址。
 */
public class Fparam extends Node {
    // 参数名标识符
    Id parameterName;
    // 参数维度级别 - 0:变量, 1:一维数组, 2:二维数组
    int dimensionLevel;
    // 第二维大小表达式(专用于二维数组)
    Expr secondDimensionSize;

    /**
     * 构造一个形式参数节点
     * 
     * @param parameterName       参数名
     * @param dimensionLevel      维度级别
     * @param secondDimensionSize 第二维大小(仅二维数组需要)
     */
    public Fparam(Id parameterName, int dimensionLevel, Expr secondDimensionSize) {
        this.parameterName = parameterName;
        this.dimensionLevel = dimensionLevel;
        this.secondDimensionSize = secondDimensionSize;
    }

    /**
     * 生成中间代码 - 设置"接收窗口"的规格
     * 
     * 这个方法就像在邮局窗口贴标签，明确说明：
     * "这个窗口接收什么类型的邮件，有什么特殊要求"
     * 
     * 不同类型的参数需要不同的处理方式和存储空间安排。
     */
    @Override
    public void gen() {
        String parameterNameString = parameterName.getcontent();

        if (dimensionLevel == 0) {
            // 普通变量参数 - 设置普通信件接收窗口
            emit(new midCode(midCode.operation.PARAM, parameterNameString, String.valueOf("0")));
            inttable.add(parameterNameString, new VarSymbol(parameterNameString, false, 0));

        } else if (dimensionLevel == 1) {
            // 一维数组参数 - 设置包裹接收窗口
            emit(new midCode(midCode.operation.PARAM, parameterNameString, String.valueOf("1")));
            inttable.add(parameterNameString, new ArraySymbol(parameterNameString, false, 1));

        } else {
            // 二维数组参数 - 设置大件货物接收窗口
            int secondDimensionValue = secondDimensionSize.calculate();
            emit(new midCode(midCode.operation.PARAM, parameterNameString, String.valueOf("2"),
                    String.valueOf(secondDimensionValue)));
            inttable.add(parameterNameString, new ArraySymbol(parameterNameString, false, 2, secondDimensionValue));
        }
    }
}
