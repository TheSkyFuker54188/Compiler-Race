package Symbol_table.Symbols;

import java.util.ArrayList;

/**
 * 数组符号实体类 - 基于标准符号的多维数组特化实现
 * 
 * 该类采用组合模式扩展基础符号功能，专门处理数组类型的变量声明
 * 支持一维和二维数组的维度管理，提供数组元素的存储和访问机制
 * 集成了数组初始化列表的管理能力，满足编译器的数组语义分析需求
 */
public class ArraySymbol extends NorSymbol {
    /* 主维度计数器 - 记录数组的基础维度信息 */
    private int primaryDimension;

    /* 第二维长度 - 二维数组的列数，一维数组时无意义 */
    private int secondDimensionSize = 0;

    /* 初值存储容器 - 保存数组初始化时的元素值列表 */
    private ArrayList<Integer> initialValues;

    /**
     * 基础数组构造器 - 创建简单的数组符号实例
     * 
     * @param symbolName   数组标识符名称
     * @param constantFlag 常量数组标记
     */
    public ArraySymbol(String symbolName, boolean constantFlag) {
        super(symbolName, constantFlag);
    }

    /**
     * 二维数组构造器 - 创建具有维度信息的数组符号
     * 
     * @param symbolName          数组标识符名称
     * @param constantFlag        常量数组标记
     * @param primaryDimension    主维度标识
     * @param secondDimensionSize 第二维的大小
     */
    public ArraySymbol(String symbolName, boolean constantFlag, int primaryDimension, int secondDimensionSize) {
        super(symbolName, constantFlag);
        this.primaryDimension = primaryDimension;
        this.secondDimensionSize = secondDimensionSize;
    }

    /**
     * 偏移数组构造器 - 创建具有内存位置的数组符号
     * 
     * @param symbolName   数组标识符名称
     * @param memoryOffset 数组在栈帧中的偏移位置
     */
    public ArraySymbol(String symbolName, int memoryOffset) {
        super(symbolName, memoryOffset);
    }

    /**
     * 指针数组构造器 - 创建指针类型的数组符号
     * 
     * @param symbolName      数组标识符名称
     * @param memoryOffset    内存偏移位置
     * @param pointerTypeFlag 指针类型标识
     */
    public ArraySymbol(String symbolName, int memoryOffset, boolean pointerTypeFlag) {
        super(symbolName, memoryOffset, pointerTypeFlag);
    }

    /**
     * 层级数组构造器 - 创建具有作用域层级的数组符号
     * 
     * @param symbolName   数组标识符名称
     * @param constantFlag 常量数组标记
     * @param scopeDepth   作用域深度级别
     */
    public ArraySymbol(String symbolName, boolean constantFlag, int scopeDepth) {
        super(symbolName, constantFlag, scopeDepth);
    }

    /**
     * 一维初值数组构造器 - 创建具有初始化列表的一维数组
     * 
     * @param symbolName       数组标识符名称
     * @param constantFlag     常量数组标记
     * @param primaryDimension 主维度标识
     * @param initialValues    数组初始化值列表
     */
    public ArraySymbol(String symbolName, boolean constantFlag, int primaryDimension,
            ArrayList<Integer> initialValues) {
        super(symbolName, constantFlag);
        this.primaryDimension = primaryDimension;
        this.initialValues = initialValues;
    }

    /**
     * 二维初值数组构造器 - 创建具有完整维度和初值的二维数组
     * 
     * @param symbolName          数组标识符名称
     * @param constantFlag        常量数组标记
     * @param primaryDimension    主维度标识
     * @param secondDimensionSize 第二维大小
     * @param initialValues       数组初始化值列表
     */
    public ArraySymbol(String symbolName, boolean constantFlag, int primaryDimension,
            int secondDimensionSize, ArrayList<Integer> initialValues) {
        super(symbolName, constantFlag);
        this.primaryDimension = primaryDimension;
        this.secondDimensionSize = secondDimensionSize;
        this.initialValues = initialValues;
    }

    /**
     * 获取主维度信息
     * 返回数组的基础维度标识
     * 
     * @return 主维度数值
     */
    @Override
    public int getLevel() {
        return primaryDimension;
    }

    /**
     * 获取第二维度大小
     * 返回二维数组的列数信息
     * 
     * @return 第二维的元素个数
     */
    public int getLevel2() {
        return secondDimensionSize;
    }

    /**
     * 按索引获取数组元素值
     * 从初始化值列表中提取指定位置的元素
     * 
     * @param index 目标元素的索引位置
     * @return 对应位置的数组元素值
     */
    public int getValue(int index) {
        return initialValues.get(index);
    }
}
