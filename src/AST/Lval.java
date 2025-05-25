package AST;

import Word.Word;

/**
 * 左值表达式节点 - 编译器中的"写入地址"概念
 * 
 * 在编译原理中，左值(Left Value)是一个可以出现在赋值语句左边的表达式，
 * 它必须指向一个内存位置，可以被修改。这就像现实生活中的"邮箱地址" -
 * 你可以往里面放东西(写入)，也可以从里面取东西(读取)。
 * 
 * 典型的左值包括：
 * - 变量名: int x; (x就是左值)
 * - 数组元素: arr[5] (可以给数组元素赋值)
 * - 指针解引用: *ptr (可以修改指针指向的内容)
 * 
 * 与之相对的右值(Right Value)只能提供数值，不能被赋值，
 * 比如常量3、表达式(a+b)的结果等。
 */
public class Lval extends Expr {

    /**
     * 构造一个左值表达式节点
     * 
     * @param variableToken 代表这个左值的词法符号
     *                      通常是标识符token，表示变量名或数组名
     */
    public Lval(Word variableToken) {
        super(variableToken);
    }
}
