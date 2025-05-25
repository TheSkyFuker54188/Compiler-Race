package AST;

import Word.Word;

/**
 * 表达式抽象基类 - 编译器中的"计算表达专家"
 * 
 * 这个类是所有表达式节点的基础，就像数学中的"公式模板"。
 * 不管是简单的数字，还是复杂的算术运算，都遵循统一的表达式规范。
 * 
 * 表达式的核心特征：
 * 1. 能够产生一个值(可能在编译时计算，也可能在运行时计算)
 * 2. 可以化简为更简单的形式
 * 3. 能够判断是否为编译时常量
 * 
 * 就像一个万能的计算器，不管输入什么样的数学表达式，
 * 都能告诉你结果，或者至少告诉你如何计算结果。
 */
public class Expr extends Stmt {

    // 表达式的词法符号 - 表达式的"身份证"
    public Word op;

    // 计算出的数值 - 编译时已知的结果
    public int value = 0;

    // 是否为编译时常量 - 标记这个表达式是否可以预先计算
    public boolean isvalue = false;

    /**
     * 构造表达式基类
     * 
     * @param op 表达式对应的词法符号
     */
    public Expr(Word op) {
        this.op = op;
    }

    /**
     * 表达式化简 - 将复杂表达式转换为简单形式
     * 
     * 这个方法就像数学老师教的"化简求值"，
     * 把复杂的表达式一步步简化成最简形式。
     * 
     * @return 化简后的表达式
     */
    public Expr reduce() {
        return this;
    }

    /**
     * 生成中间代码 - 表达式的"执行方案"
     * 
     * 基类默认通过化简表达式来生成代码，
     * 具体的表达式类型会重写此方法实现特定的代码生成逻辑。
     */
    @Override
    public void gen() {
        // 默认通过表达式化简来处理
        // 子类可以重写此方法实现特定的代码生成逻辑
    }

    /**
     * 检查是否可以在编译时计算 - 智能预判系统
     * 
     * 这个方法就像有经验的数学老师，一眼就能看出
     * 哪些题目可以心算得出答案，哪些需要用到计算器。
     * 
     * @return true表示可以在编译时直接计算出结果，false表示需要运行时计算
     */
    public boolean canculculate() {
        return false;
    }

    /**
     * 转换为字符串表示
     * 
     * @return 表达式的字符串形式
     */
    @Override
    public String toString() {
        return op.toString();
    }

    /**
     * 计算表达式的值 - 进行实际的数学运算
     * 
     * 这个方法就像真正按下计算器的"等号"键，
     * 得出最终的计算结果。
     * 
     * @return 表达式计算的结果值
     */
    public int calculate() {
        return 0;
    }

    /**
     * 获取表达式内容的字符串形式
     * 
     * @return 词法符号的内容字符串
     */
    public String getcontent() {
        return op.getContent();
    }
}
