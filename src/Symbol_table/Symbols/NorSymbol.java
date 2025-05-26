package Symbol_table.Symbols;

/**
 * 标准符号抽象基类 - 编译器符号表管理系统的核心实体模型
 * 
 * 该类定义了所有符号类型的通用属性和行为规范，采用模板方法模式
 * 为变量符号、数组符号等具体实现提供统一的接口基础
 * 封装了标识符的基本元数据：名称、常量性质、作用域层级等关键信息
 */
public class NorSymbol {
    /* 标识符名称 - 源代码中声明的符号字符串表示 */
    private String symbolName;

    /* 常量属性标记 - 标识该符号是否为编译时常量 */
    private boolean constantFlag;

    /* 作用域深度指示器 - 记录符号所在的嵌套层级，0表示全局作用域 */
    private int scopeDepth = 0;

    /* 源码位置标记 - 记录符号在源文件中的行号信息 */
    private int sourceLineNumber = 0;

    /* 内存偏移量 - 符号在运行时栈帧中的相对位置 */
    private int memoryOffset = 0;

    /* 指针类型标识 - 标记该符号是否为指针类型变量 */
    private boolean pointerTypeFlag = false;

    /**
     * 默认构造器 - 创建空白符号实例
     * 用于需要延迟初始化的场景
     */
    public NorSymbol() {
    }

    /**
     * 基础构造器 - 创建具有名称和常量属性的符号
     * 
     * @param symbolName   符号标识符名称
     * @param constantFlag 常量属性标记
     */
    public NorSymbol(String symbolName, boolean constantFlag) {
        this.symbolName = symbolName;
        this.constantFlag = constantFlag;
    }

    /**
     * 层级构造器 - 创建具有作用域信息的符号
     * 
     * @param symbolName   符号标识符名称
     * @param constantFlag 常量属性标记
     * @param scopeDepth   作用域深度级别
     */
    public NorSymbol(String symbolName, boolean constantFlag, int scopeDepth) {
        this.symbolName = symbolName;
        this.constantFlag = constantFlag;
        this.scopeDepth = scopeDepth;
    }

    /**
     * 偏移构造器 - 创建具有内存布局信息的符号
     * 
     * @param symbolName   符号标识符名称
     * @param memoryOffset 内存偏移位置
     */
    public NorSymbol(String symbolName, int memoryOffset) {
        this.symbolName = symbolName;
        this.memoryOffset = memoryOffset;
    }

    /**
     * 指针构造器 - 创建具有指针类型属性的符号
     * 
     * @param symbolName      符号标识符名称
     * @param memoryOffset    内存偏移位置
     * @param pointerTypeFlag 指针类型标识
     */
    public NorSymbol(String symbolName, int memoryOffset, boolean pointerTypeFlag) {
        this.symbolName = symbolName;
        this.memoryOffset = memoryOffset;
        this.pointerTypeFlag = pointerTypeFlag;
    }

    /**
     * 检查指针类型属性
     * 
     * @return 如果为指针类型返回true，否则返回false
     */
    public boolean isIspointer() {
        return pointerTypeFlag;
    }

    /**
     * 设置指针类型属性
     * 
     * @param pointerTypeFlag 指针类型标识状态
     */
    public void setIspointer(boolean pointerTypeFlag) {
        this.pointerTypeFlag = pointerTypeFlag;
    }

    /**
     * 获取内存偏移量
     * 
     * @return 符号在栈帧中的相对位置
     */
    public int getOffset() {
        return memoryOffset;
    }

    /**
     * 获取符号名称
     * 
     * @return 标识符的字符串表示
     */
    public String getName() {
        return symbolName;
    }

    /**
     * 检查常量属性
     * 
     * @return 如果为常量返回true，否则返回false
     */
    public boolean isConst() {
        return constantFlag;
    }

    /**
     * 获取源码行号
     * 
     * @return 符号在源文件中的位置信息
     */
    public int getLine() {
        return sourceLineNumber;
    }

    /**
     * 设置源码行号
     * 
     * @param sourceLineNumber 目标行号位置
     */
    public void setLine(int sourceLineNumber) {
        this.sourceLineNumber = sourceLineNumber;
    }

    /**
     * 获取作用域深度
     * 
     * @return 符号所在的嵌套层级数值
     */
    public int getLevel() {
        return scopeDepth;
    }
}
