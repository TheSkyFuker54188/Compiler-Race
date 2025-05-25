package AST;

/**
 * 定义节点基类 - 编译器中的"身份证办理处"
 * 
 * 在编程语言中，定义就是给一个标识符赋予具体的意义和属性。
 * 这个类是所有定义类型的抽象基类，就像身份证办理处的通用流程，
 * 无论是办理普通身份证还是学生证，都有一些共同的基础步骤。
 * 
 * 定义通常包括：
 * - 变量定义：int x = 5; (给x这个名字分配内存空间)
 * - 常量定义：const int MAX = 100; (给MAX这个名字绑定一个不变的值)
 * - 数组定义：int arr[10]; (给arr这个名字分配一块连续的内存)
 * 
 * 每个定义都会有一个左值(lval)作为被定义的目标标识符。
 */
public class Def extends Node {
    // 被定义的左值 - 通常是变量名或数组名
    Lval targetVariable;

    /**
     * 构造一个定义节点
     * 
     * @param targetVariable 被定义的左值表达式，代表要定义的标识符
     */
    public Def(Lval targetVariable) {
        this.targetVariable = targetVariable;
    }
}
