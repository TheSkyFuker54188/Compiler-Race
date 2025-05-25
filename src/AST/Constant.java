package AST;

import Word.Word;

/**
 * 常量节点类 - 表示程序中的数字字面量
 * 
 * 当我们在代码中写下具体的数字，比如 42、0、-100 这些，
 * 编译器就会创建Constant节点来表示它们。
 * 
 * 常量的特点是值在编译期就完全确定了，不需要运行时计算，
 * 这让编译器可以做很多优化，比如常量折叠：2+3 直接算成 5
 */
public class Constant extends Expr {

    /**
     * 创建一个常量节点
     * 
     * @param literalToken 包含数字文本的词法单元
     */
    public Constant(Word literalToken) {
        super(literalToken);
    }

    /**
     * 计算常量的数值
     * 对于常量来说，这就是简单地把文本转换成数字
     * 比如把字符串"42"转换成整数42
     */
    @Override
    public int calculate() {
        return Integer.parseInt(op.getContent());
    }

    /**
     * 检查是否可以计算
     * 常量当然总是可以计算的，而且值就是它自己
     * 这个方法同时会设置内部的缓存标志，避免重复解析
     */
    @Override
    public boolean canculculate() {
        isvalue = true;
        value = Integer.parseInt(op.getContent());
        return true;
    }
}
