package Word;

/**
 * 词法符号单元 - 编译器前端词法分析器的基础输出单位
 * 该类采用封装模式来表示源码解析过程中提取的语法元素
 * 每个实例承载着编译流程中的一个离散语言构件信息
 */
public class Word {
    /* 符号类别标识符 - 用于分类不同语义角色的语法元素 */
    private final int categoryId;

    /* 符号原始内容 - 保存从源代码提取的字符序列 */
    private final String rawContent;

    /* 位置跟踪信息 - 记录该符号在输入流中的位置索引 */
    private int lineIndex;

    /**
     * 主要实例化方法 - 使用构建者模式创建符号实例
     * 通过传入语义分类、文本载荷和位置元数据来构造完整的词法单元
     */
    public Word(int categoryId, String rawContent, int lineIndex) {
        this.categoryId = categoryId;
        this.rawContent = rawContent;
        this.lineIndex = lineIndex;
    }

    /**
     * 默认初始化方法 - 创建空白状态的符号占位符
     * 所有属性设置为初始值，用于特殊场景的延迟赋值
     */
    public Word() {
        this.categoryId = 0;
        this.rawContent = "";
        this.lineIndex = 0;
    }

    /**
     * 文本专用构造方法 - 针对纯文本内容的轻量级创建方式
     * 适用于字符串字面量等无需复杂分类的场景
     */
    public Word(String rawContent) {
        this.categoryId = 0;
        this.rawContent = rawContent;
        this.lineIndex = 0;
    }

    /**
     * 获取符号分类标识
     * 返回该词法单元所属的语义类别编号
     */
    public int getSymnumber() {
        return this.categoryId;
    }

    /**
     * 提取符号文本载荷
     * 获取该符号承载的原始字符串信息
     */
    public String getContent() {
        return this.rawContent;
    }

    /**
     * 查询位置跟踪信息
     * 返回该符号在源码中的行位置索引
     */
    public int getLine() {
        return this.lineIndex;
    }

    /**
     * 字符串序列化方法
     * 将符号对象转换为其承载的文本表示形式
     */
    @Override
    public String toString() {
        return this.rawContent;
    }
}
