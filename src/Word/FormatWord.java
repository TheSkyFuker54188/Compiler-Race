package Word;

/**
 * 扩展符号单元类 - 基于基础Word类的功能增强版本
 * 
 * 该类通过装饰者模式对基础词法符号进行能力扩展
 * 支持数值字面量缓存和格式串验证等高级特性
 * 
 * 设计思路：通过组合方式扩展基础符号的属性域
 */
public class FormatWord extends Word {

    /* 数值缓存域 - 存储符号对应的数学值 */
    private int cachedValue;

    /* 合规性标志位 - 标识该符号是否通过格式验证检查 */
    private boolean complianceFlag;

    /**
     * 富属性符号构造器 - 创建具备扩展功能的词法单元
     * 
     * 该方法支持需要进行深度语义分析的符号类型：
     * - 整型字面量（需要保存解析后的数值）
     * - 格式化输出串（需要验证占位符语法正确性）
     * 
     * @param categoryId     符号分类标识（继承属性）
     * @param rawContent     原始文本内容（继承属性）
     * @param lineIndex      源码位置索引（继承属性）
     * @param cachedValue    关联数值缓存
     * @param complianceFlag 格式合规性检查结果
     */
    public FormatWord(int categoryId, String rawContent, int lineIndex,
            int cachedValue, boolean complianceFlag) {
        /* 初始化父类基础属性 */
        super(categoryId, rawContent, lineIndex);

        /* 设置本类特有的扩展属性 */
        this.cachedValue = cachedValue;
        this.complianceFlag = complianceFlag;
    }

    /**
     * 数值缓存访问器
     * 
     * 针对数字字面量符号，返回其解析后的数学值
     * 对于非数值类型符号，该返回值不具备语义意义
     * 
     * @return 缓存的数值结果
     */
    public int getNum() {
        return this.cachedValue;
    }

    /**
     * 合规性状态查询器
     * 
     * 检查该符号是否满足预定义的格式规范要求
     * true表示通过验证，false表示存在格式缺陷
     * 
     * @return 格式验证的布尔结果
     */
    public boolean isCorrect() {
        return this.complianceFlag;
    }
}
