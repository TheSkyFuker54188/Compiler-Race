package Symbol_table.Symbols;

import java.util.ArrayList;

/**
 * 函数符号描述类 - 编译器函数声明和定义的元数据容器
 * 
 * 该类采用数据传输对象模式，封装函数的签名信息和类型特征
 * 包含函数名称、参数列表、返回类型等编译器语义分析所需的关键属性
 * 为函数调用检查、类型匹配验证提供数据支撑
 */
public class FuncSymbol {
    /* 函数标识符 - 函数在源代码中的名称字符串 */
    private String functionIdentifier;

    /* 形参列表容器 - 存储函数参数的符号对象集合 */
    private ArrayList<NorSymbol> parameterList;

    /* 返回类型编码 - 数值表示函数的返回值类型（0表示void，1表示int） */
    private int returnTypeCode;

    /**
     * 完整函数构造器 - 创建具有完整签名信息的函数符号
     * 
     * @param functionIdentifier 函数名称标识符
     * @param parameterList      函数形参符号列表
     * @param returnTypeCode     返回类型编码值
     */
    public FuncSymbol(String functionIdentifier, ArrayList<NorSymbol> parameterList, int returnTypeCode) {
        this.functionIdentifier = functionIdentifier;
        this.parameterList = parameterList;
        this.returnTypeCode = returnTypeCode;
    }

    /**
     * 简化函数构造器 - 仅指定返回类型的函数符号
     * 适用于函数原型声明或特殊函数处理场景
     * 
     * @param returnTypeCode 返回类型编码值
     */
    public FuncSymbol(int returnTypeCode) {
        this.returnTypeCode = returnTypeCode;
    }

    /**
     * 获取函数名称标识符
     * 
     * @return 函数的字符串名称表示
     */
    public String getName() {
        return functionIdentifier;
    }

    /**
     * 获取函数参数列表
     * 
     * @return 形参符号对象的动态数组
     */
    public ArrayList<NorSymbol> getParams() {
        return parameterList;
    }

    /**
     * 获取函数返回类型
     * 
     * @return 返回类型的数值编码
     */
    public int getReturntype() {
        return returnTypeCode;
    }
}
