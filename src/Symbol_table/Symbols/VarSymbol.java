package Symbol_table.Symbols;

/**
 * 变量符号实体类 - 继承自标准符号基类的具体实现
 * 
 * 该类专门用于表示源码中的变量声明和定义，扩展了基类功能
 * 增加了数值存储能力，支持编译时常量求值和运行时变量管理
 * 采用继承机制复用基础符号的通用属性和行为
 */
public class VarSymbol extends NorSymbol {
    /* 数值存储域 - 保存变量的实际数值内容 */
    private int storedValue;

    /**
     * 完整变量构造器 - 创建具有初始值的变量符号
     * 用于有明确初值的变量和常量声明
     * 
     * @param symbolName   变量标识符名称
     * @param constantFlag 常量属性标记
     * @param storedValue  变量的初始数值
     */
    public VarSymbol(String symbolName, boolean constantFlag, int storedValue) {
        super(symbolName, constantFlag);
        this.storedValue = storedValue;
    }

    /**
     * 基础变量构造器 - 创建无初值的变量符号
     * 用于声明但未初始化的变量
     * 
     * @param symbolName   变量标识符名称
     * @param constantFlag 常量属性标记
     */
    public VarSymbol(String symbolName, boolean constantFlag) {
        super(symbolName, constantFlag);
    }

    /**
     * 偏移变量构造器 - 创建具有内存位置信息的变量符号
     * 用于代码生成阶段需要内存布局的变量
     * 
     * @param symbolName   变量标识符名称
     * @param memoryOffset 变量在栈帧中的偏移位置
     */
    public VarSymbol(String symbolName, int memoryOffset) {
        super(symbolName, memoryOffset);
    }

    /**
     * 获取变量存储值
     * 返回变量当前保存的数值内容
     * 
     * @return 变量的数值表示
     */
    public int getValue() {
        return storedValue;
    }
}
