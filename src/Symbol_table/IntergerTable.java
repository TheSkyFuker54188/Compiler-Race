package Symbol_table;

import Symbol_table.Symbols.NorSymbol;

import java.util.HashMap;

/**
 * 整型符号管理表 - 编译器标识符解析和存储管理的核心组件
 * 
 * 该类实现基于作用域层次的变量符号存储机制，采用链式结构维护嵌套作用域关系
 * 提供变量标识符的注册、检索和内存布局管理功能，支持编译时的语义检查需求
 */
public class IntergerTable {
    /* 符号映射存储器 - 维护标识符名称到符号对象的关联映射 */
    private HashMap<String, NorSymbol> identifierMap = new HashMap<>();

    /* 上层作用域链接 - 指向外围作用域的符号表实例，形成作用域链 */
    private IntergerTable outerScope = null;

    /* 内容容量计数器 - 记录当前作用域中已分配的存储空间大小 */
    private int storageCapacity = 0;

    /**
     * 空白构造器 - 创建独立的符号表实例
     * 用于建立全局作用域或根级变量环境
     */
    public IntergerTable() {

    }

    /**
     * 嵌套构造器 - 创建具有父级作用域关联的符号表
     * 用于建立块级作用域、函数作用域等嵌套环境
     * 
     * @param outerScope 外层作用域的符号表引用
     */
    public IntergerTable(IntergerTable outerScope) {
        this.outerScope = outerScope;
    }

    /**
     * 增量分配存储空间
     * 更新当前作用域的存储容量统计信息
     * 
     * @param spaceSize 新增的存储空间大小
     */
    public void addlength(int spaceSize) {
        storageCapacity += spaceSize;
    }

    /**
     * 获取存储容量统计
     * 
     * @return 当前作用域已分配的存储空间总量
     */
    public int getContentlength() {
        return storageCapacity;
    }

    /**
     * 获取标识符映射容器
     * 
     * @return 内部哈希映射结构的直接访问接口
     */
    public HashMap<String, NorSymbol> getMaps() {
        return identifierMap;
    }

    /**
     * 获取外层作用域引用
     * 
     * @return 父级符号表实例，如无则返回null
     */
    public IntergerTable getOut() {
        return outerScope;
    }

    /**
     * 设置外层作用域关联
     * 建立与上级作用域的链接关系
     * 
     * @param outerScope 目标父级作用域实例
     */
    public void setOut(IntergerTable outerScope) {
        this.outerScope = outerScope;
    }

    /**
     * 注册变量符号实体
     * 在当前作用域中建立标识符到符号对象的映射关系
     * 
     * @param identifier 变量名称标识符
     * @param symbol     变量符号元数据对象
     */
    public void add(String identifier, NorSymbol symbol) {
        identifierMap.put(identifier, symbol);
    }

    /**
     * 检查符号注册状态
     * 验证指定标识符是否已在当前作用域中定义
     * 
     * @param identifier 待检查的变量名称
     * @return 已注册返回true，否则返回false
     */
    public boolean contains(String identifier) {
        return identifierMap.containsKey(identifier);
    }

    /**
     * 检索变量符号实体
     * 根据标识符获取对应的符号描述信息
     * 
     * @param identifier 目标变量名称
     * @return 对应的符号对象，不存在则返回null
     */
    public NorSymbol get(String identifier) {
        return identifierMap.get(identifier);
    }
}
