package AST;

import Midcode.midCode;
import Symbol_table.IntergerTable;
import Symbol_table.Symbols.ArraySymbol;
import Word.Word;

/**
 * 数组访问表达式节点 - 编译器中的"智能仓库管理员"
 * 
 * 数组访问就像在多层仓库中找到指定货物的过程。仓库管理员需要：
 * 1. 根据仓库名找到正确的仓库区域
 * 2. 根据坐标(索引)定位到具体的货架位置
 * 3. 取出或放入货物(读取或写入数据)
 * 
 * 支持的数组类型：
 * - 一维数组：arr[i] (像单排货架，只需要一个坐标)
 * - 二维数组：matrix[i][j] (像多层仓库，需要两个坐标)
 * - 数组指针：arr (当作为参数传递时，表示整个数组的起始地址)
 * 
 * 特殊处理：
 * - 常量数组：编译时可以直接计算出值
 * - 动态数组：运行时需要生成地址计算代码
 * - 参数数组：需要考虑指针传递的情况
 */
public class Array extends Lval {
    // 临时变量 - 用于存储计算过程中的中间结果
    public Expr temporaryStorage;
    // 第二维信息 - 用于指针传递时记录维度信息
    public String secondDimensionInfo;

    // 索引表达式 - 数组访问的"坐标系统"
    private Expr firstIndexExpression = null;
    private Expr secondIndexExpression = null;

    /**
     * 构造二维数组访问节点
     * 
     * @param arrayToken            数组名词法符号
     * @param firstIndexExpression  第一维索引
     * @param secondIndexExpression 第二维索引
     */
    public Array(Word arrayToken, Expr firstIndexExpression, Expr secondIndexExpression) {
        super(arrayToken);
        this.secondIndexExpression = secondIndexExpression;
        this.firstIndexExpression = firstIndexExpression;
    }

    /**
     * 构造一维数组访问节点
     * 
     * @param arrayToken           数组名词法符号
     * @param firstIndexExpression 索引表达式
     */
    public Array(Word arrayToken, Expr firstIndexExpression) {
        super(arrayToken);
        this.firstIndexExpression = firstIndexExpression;
    }

    /**
     * 计算数组元素的值 - 仓库管理员的"查货"过程
     * 
     * 这个方法就像有经验的仓库管理员，能够根据货架编号
     * 直接找到货物并告诉你里面装的是什么。
     */
    public int calculate() {
        // 查找数组符号 - 在仓库目录中找到对应的区域信息
        IntergerTable symbolTable = inttable;
        ArraySymbol arraySymbol = null;
        while (symbolTable != null) {
            if (symbolTable.contains(op.getContent())) {
                arraySymbol = (ArraySymbol) symbolTable.get(op.getContent());
                break;
            }
            symbolTable = symbolTable.getOut();
        }

        // 计算最终索引位置 - 确定货物的精确坐标
        int primaryIndex = 0, finalIndex = 0;
        if (secondIndexExpression == null) {
            // 一维数组情况 - 直接定位
            finalIndex = firstIndexExpression.calculate();
        } else {
            // 二维数组情况 - 二维坐标转一维偏移
            primaryIndex = firstIndexExpression.calculate();
            finalIndex = secondIndexExpression.calculate();
        }

        // 返回指定位置的货物内容
        return arraySymbol.getValue(primaryIndex * arraySymbol.getLevel2() + finalIndex);
    }

    /**
     * 检查是否可以在编译时计算 - 智能预判系统
     * 
     * 这个方法就像智能仓库系统，能够预先判断：
     * "这个货物的信息我现在就能告诉你，还是需要到现场才能确定？"
     */
    @Override
    public boolean canculculate() {
        boolean firstIndexCalculable = firstIndexExpression.canculculate();

        // 查找数组符号信息
        IntergerTable symbolTable = inttable;
        ArraySymbol arraySymbol = null;
        while (symbolTable != null) {
            if (symbolTable.contains(op.getContent())) {
                arraySymbol = (ArraySymbol) symbolTable.get(op.getContent());
                break;
            }
            symbolTable = symbolTable.getOut();
        }

        // 只有常量数组才能在编译时确定值
        if (arraySymbol.isConst()) {
            if (secondIndexExpression == null) {
                // 一维常量数组的处理
                if (arraySymbol.getLevel2() != 0) {
                    // 这是个指针，不能直接计算
                    return false;
                } else {
                    if (firstIndexCalculable) {
                        int indexValue = firstIndexExpression.value;
                        isvalue = true;
                        value = arraySymbol.getValue(indexValue);
                        return true;
                    } else {
                        return false;
                    }
                }
            } else {
                // 二维常量数组的处理
                boolean secondIndexCalculable = secondIndexExpression.canculculate();
                if (firstIndexCalculable && secondIndexCalculable) {
                    int firstIndex = firstIndexExpression.value;
                    int secondIndex = secondIndexExpression.value;
                    isvalue = true;
                    value = arraySymbol.getValue(firstIndex * arraySymbol.getLevel2() + secondIndex);
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            // 变量数组无法在编译时确定值
            return false;
        }
    }

    /**
     * 获取第一维索引表达式
     */
    public Expr getOneindex() {
        return firstIndexExpression;
    }

    /**
     * 获取第二维索引表达式
     */
    public Expr getTwoindex() {
        return secondIndexExpression;
    }

    /**
     * 化简表达式 - 生成数组访问的中间代码
     * 
     * 这个方法就像仓库的自动化系统，根据不同情况
     * 生成相应的取货指令。
     */
    @Override
    public Expr reduce() {
        if (secondIndexExpression == null) {
            // 处理一维数组或数组指针访问
            IntergerTable symbolTable = inttable;
            ArraySymbol arraySymbol = null;
            while (symbolTable != null) {
                if (symbolTable.contains(op.getContent())) {
                    arraySymbol = (ArraySymbol) symbolTable.get(op.getContent());
                    break;
                }
                symbolTable = symbolTable.getOut();
            }

            if (arraySymbol.getLevel2() != 0) {
                // 这是数组指针访问 - 特殊的取货方式
                temporaryStorage = firstIndexExpression.reduce();
                secondDimensionInfo = String.valueOf(arraySymbol.getLevel2());
                return this;
            } else {
                // 普通一维数组访问
                if (isvalue) {
                    // 编译时已知值，直接返回常量
                    return new Constant(new Word(String.valueOf(value)));
                }

                // 生成动态访问代码
                Temp resultStorage = new Temp(op);
                emit(new midCode(midCode.operation.GETARRAY,
                        resultStorage.toString(),
                        op.getContent(),
                        firstIndexExpression.reduce().toString()));
                return resultStorage;
            }
        } else {
            // 处理二维数组访问
            if (isvalue) {
                // 编译时已知值，直接返回常量
                return new Constant(new Word(String.valueOf(value)));
            }

            // 生成二维数组访问的复杂计算代码
            Temp multiplicationResult = new Temp(op);
            Temp additionResult = new Temp(op);
            Temp finalResult = new Temp(op);

            // 获取数组的第二维大小信息
            IntergerTable symbolTable = inttable;
            ArraySymbol arraySymbol = null;
            while (symbolTable != null) {
                if (symbolTable.contains(op.getContent())) {
                    arraySymbol = (ArraySymbol) symbolTable.get(op.getContent());
                    break;
                }
                symbolTable = symbolTable.getOut();
            }

            int secondDimensionSize = arraySymbol.getLevel2();
            // 生成地址计算：index = first_index * second_dim_size + second_index
            emit(new midCode(midCode.operation.MULTOP,
                    multiplicationResult.toString(),
                    firstIndexExpression.reduce().toString(),
                    String.valueOf(secondDimensionSize)));
            emit(new midCode(midCode.operation.PLUSOP,
                    additionResult.toString(),
                    multiplicationResult.toString(),
                    secondIndexExpression.reduce().toString()));
            emit(new midCode(midCode.operation.GETARRAY,
                    finalResult.toString(),
                    op.getContent(),
                    additionResult.reduce().toString()));
            return finalResult;
        }
    }

    /**
     * 转换为字符串 - 用于赋值等需要地址的场合
     * 
     * 当数组作为赋值目标时，需要返回其地址而不是值。
     * 这就像告诉搬运工："把货物放到X号货架的Y位置"。
     */
    @Override
    public String toString() {
        if (secondIndexExpression == null) {
            // 一维数组的地址就是索引本身
            return firstIndexExpression.reduce().toString();
        } else {
            // 二维数组需要计算线性地址
            Temp multiplicationResult = new Temp(op);
            Temp finalAddress = new Temp(op);

            // 获取数组维度信息
            IntergerTable symbolTable = inttable;
            ArraySymbol arraySymbol = null;
            while (symbolTable != null) {
                if (symbolTable.contains(op.getContent())) {
                    arraySymbol = (ArraySymbol) symbolTable.get(op.getContent());
                    break;
                }
                symbolTable = symbolTable.getOut();
            }

            int secondDimensionSize = arraySymbol.getLevel2();

            // 生成地址计算代码
            emit(new midCode(midCode.operation.MULTOP,
                    multiplicationResult.toString(),
                    firstIndexExpression.reduce().toString(),
                    String.valueOf(secondDimensionSize)));
            emit(new midCode(midCode.operation.PLUSOP,
                    finalAddress.toString(),
                    multiplicationResult.toString(),
                    secondIndexExpression.reduce().toString()));
            return finalAddress.toString();
        }
    }
}
