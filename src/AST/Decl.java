package AST;

import java.util.LinkedList;

/**
 * 声明语句节点 - 编译器中的"声明管家"
 * 
 * 在程序中，声明就像是在告诉编译器："嘿，我要用这些名字来代表一些数据！"
 * 这个类就是负责管理一组相关声明的"管家"，它可以处理两种类型的声明：
 * 
 * 1. 变量声明：int a, b, c; (可以在运行时修改的数据)
 * 2. 常量声明：const int MAX = 100; (一旦确定就不能改变的数据)
 * 
 * 就像现实中的房屋登记处，需要把同一批申请的房产证一起处理，
 * 这个类也会把一条声明语句中的所有定义项打包在一起统一管理。
 */
public class Decl extends BlockItem {
    // 定义项列表 - 存储这条声明语句中所有的具体定义
    LinkedList<Def> definitionList;
    // 常量标记 - 标识这是常量声明还是变量声明
    boolean isConstantDeclaration;

    /**
     * 构造一个声明语句节点
     * 
     * @param definitionList        包含所有定义项的列表
     * @param isConstantDeclaration 是否为常量声明的标记
     */
    public Decl(LinkedList<Def> definitionList, boolean isConstantDeclaration) {
        this.definitionList = definitionList;
        this.isConstantDeclaration = isConstantDeclaration;
    }

    /**
     * 生成中间代码 - 逐个处理每个定义项
     * 
     * 就像流水线作业，把声明列表中的每个定义项
     * 都送到下一个处理环节生成对应的中间代码
     */
    @Override
    public void gen() {
        for (Def currentDefinition : definitionList) {
            currentDefinition.gen();
        }
    }
}
