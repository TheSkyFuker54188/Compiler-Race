package Word;

/**
 * 格式化词汇单元类 - 这是Word类的增强版本
 * 
 * 有些词汇不仅要记录它们是什么，还要记录额外的信息
 * 比如数字除了文本内容，还要记录它的数值；
 * 格式字符串除了内容，还要记录格式是否正确
 * 
 * 就像给普通标签加上了更多的属性标记
 */
public class FormatWord extends Word {

    /* 数值属性 - 如果这个词汇代表一个数字，这里存储它的实际数值 */
    private int numericalValue;

    /* 有效性标记 - 标记这个词汇是否符合语法规则 */
    private boolean isValidFormat;

    /**
     * 创建一个带有格式信息的词汇单元
     * 
     * 这个构造方法适用于那些需要额外验证的词汇，比如：
     * - 数字字面量（需要验证格式是否正确）
     * - 格式化字符串（需要检查%d、%c等格式符是否合法）
     * 
     * @param tokenCategory  词汇类型（继承自父类）
     * @param lexicalText    词汇文本内容（继承自父类）
     * @param sourcePosition 源码行号（继承自父类）
     * @param numericalValue 关联的数值（如果适用）
     * @param isValidFormat  是否通过格式验证
     */
    public FormatWord(int tokenCategory, String lexicalText, int sourcePosition,
            int numericalValue, boolean isValidFormat) {
        /* 先调用父类构造方法，设置基本的词汇信息 */
        super(tokenCategory, lexicalText, sourcePosition);

        /* 然后设置这个类特有的格式信息 */
        this.numericalValue = numericalValue;
        this.isValidFormat = isValidFormat;
    }

    /**
     * 获取数值属性
     * 
     * 对于数字字面量，返回它们的实际数值
     * 对于其他类型的词汇，这个值可能没有意义
     * 
     * @return 存储的数值
     */
    public int getNum() {
        return this.numericalValue;
    }

    /**
     * 检查格式有效性
     * 
     * 告诉我们这个词汇是否符合预期的格式规则
     * true表示格式正确，false表示存在格式错误
     * 
     * @return 格式验证结果
     */
    public boolean isCorrect() {
        return this.isValidFormat;
    }
}
