package AST;

import Word.Word;

/**
 * 临时变量节点类 - 表示编译器生成的中间临时变量
 * 
 * 在编译复杂表达式时，编译器经常需要创建临时变量来保存中间结果。
 * 比如表达式 a + b * c，编译器可能先算 b * c 存到临时变量 t1，
 * 然后再算 a + t1。这些临时变量对程序员是不可见的。
 * 
 * 就像你做复杂数学题时在草稿纸上写的中间步骤一样
 */
public class Temp extends Expr {

    /* 全局临时变量计数器，确保每个临时变量都有唯一的编号 */
    private static int globalTempCounter = 0;

    /* 当前临时变量的唯一编号 */
    private int uniqueIdentifier = 0;

    /**
     * 创建一个新的临时变量节点
     * 每次创建都会自动分配一个递增的唯一编号
     * 
     * @param placeholderToken 占位用的词法单元（通常内容不重要）
     */
    public Temp(Word placeholderToken) {
        super(placeholderToken);
        this.uniqueIdentifier = ++globalTempCounter;
    }

    /**
     * 生成临时变量的字符串表示
     * 使用特殊的前缀 "t&" 来区别于用户定义的变量名
     * 这样就不会与程序中的真实变量名冲突
     * 
     * @return 临时变量的标识字符串，如 "t&1", "t&2" 等
     */
    public String toString() {
        return "t&" + uniqueIdentifier;
    }
}
