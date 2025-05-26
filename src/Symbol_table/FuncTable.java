package Symbol_table;

import Symbol_table.Symbols.FuncSymbol;

import java.util.HashMap;

/**
 * 函数符号注册表 - 编译器语义分析阶段的函数信息管理器
 * 
 * 该类采用哈希映射机制来实现函数标识符到函数元数据的快速检索
 * 支持嵌套作用域的函数定义管理，通过链式结构维护作用域层次关系
 */
public class FuncTable {
    /* 函数符号存储容器 - 使用字符串键值对应函数符号实体的映射结构 */
    private HashMap<String, FuncSymbol> symbolRegistry = new HashMap<>();

    /* 外层作用域引用 - 指向包含当前作用域的上级函数表实例 */
    private FuncTable parentScope;

    /**
     * 默认构造器 - 创建独立的函数符号表实例
     * 适用于全局作用域或根级函数定义环境
     */
    public FuncTable() {
    }

    /**
     * 获取符号注册容器
     * 
     * @return 内部哈希映射结构的直接引用
     */
    public HashMap<String, FuncSymbol> getMaps() {
        return symbolRegistry;
    }

    /**
     * 获取父级作用域引用
     * 
     * @return 上层函数表实例，如无则返回null
     */
    public FuncTable getOut() {
        return parentScope;
    }

    /**
     * 注册函数符号实体
     * 将函数标识符与其对应的符号描述信息建立映射关系
     * 
     * @param identifier 函数名称标识符
     * @param symbol     函数符号元数据对象
     */
    public void add(String identifier, FuncSymbol symbol) {
        symbolRegistry.put(identifier, symbol);
    }

    /**
     * 检索函数符号实体
     * 根据标识符查找对应的函数符号描述
     * 
     * @param identifier 待查找的函数名称
     * @return 对应的函数符号对象，不存在则返回null
     */
    public FuncSymbol get(String identifier) {
        return symbolRegistry.get(identifier);
    }

    /**
     * 验证符号存在性
     * 检查指定标识符是否已在当前函数表中注册
     * 
     * @param identifier 待验证的函数名称
     * @return 存在返回true，否则返回false
     */
    public boolean contains(String identifier) {
        return symbolRegistry.containsKey(identifier);
    }
}
