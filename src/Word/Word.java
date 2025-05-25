package Word;

/**
 * 词汇单元类 - 这是编译器词法分析阶段产生的基本单位
 * 每个Word对象代表源代码中的一个有意义的符号，比如关键字、标识符、操作符等
 * 就像把一篇文章拆分成一个个单词一样，编译器也要把代码拆分成这样的词汇单元
 */
public class Word {
    /* 词汇类型编号 - 用来区分这是什么类型的符号（比如1代表if，2代表标识符等） */
    private final int tokenCategory;

    /* 词汇的原始文本 - 就是在源代码中实际写的字符串 */
    private final String lexicalText;

    /* 这个词汇在源文件中的行号 - 方便出错时定位问题 */
    private int sourcePosition;

    /**
     * 最常用的构造方法 - 创建一个完整的词汇单元
     * 就像给每个单词贴上标签：这是什么类型，内容是什么，在第几行
     */
    public Word(int tokenCategory, String lexicalText, int sourcePosition) {
        this.tokenCategory = tokenCategory;
        this.lexicalText = lexicalText;
        this.sourcePosition = sourcePosition;
    }

    /**
     * 空白构造方法 - 有时候需要创建一个占位符
     * 就像准备一张空白的标签纸，等待填写内容
     */
    public Word() {
        this.tokenCategory = 0;
        this.lexicalText = "";
        this.sourcePosition = 0;
    }

    /**
     * 简化构造方法 - 只指定文本内容，类型默认为0
     * 适用于一些特殊情况，比如处理字符串字面量
     */
    public Word(String lexicalText) {
        this.tokenCategory = 0;
        this.lexicalText = lexicalText;
        this.sourcePosition = 0;
    }

    /**
     * 获取词汇类型编号
     * 编译器后续阶段需要根据这个编号来判断如何处理这个词汇
     */
    public int getSymnumber() {
        return this.tokenCategory;
    }

    /**
     * 获取词汇的原始文本内容
     * 这是用户在源代码中实际写的东西
     */
    public String getContent() {
        return this.lexicalText;
    }

    /**
     * 获取词汇所在的行号
     * 调试和错误报告时特别有用
     */
    public int getLine() {
        return this.sourcePosition;
    }

    /**
     * 转换为字符串表示
     * 直接返回词汇内容，这样打印Word对象时就能看到实际的文本
     */
    @Override
    public String toString() {
        return this.lexicalText;
    }
}
