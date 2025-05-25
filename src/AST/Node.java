package AST;

import Midcode.midCode;
import Symbol_table.FuncTable;
import Symbol_table.IntergerTable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

/**
 * AST节点基类 - 编译器的"基础建筑模块"
 * 
 * 这个类就像建筑工地的"项目总协调中心"，为所有AST节点提供：
 * 1. 全局资源管理（符号表、函数表、中间代码）
 * 2. 统一的代码生成接口
 * 3. 标签和跳转管理系统
 * 4. 循环嵌套跟踪机制
 * 
 * 每个AST节点都继承这个基类，就像每个建筑部件都需要
 * 遵循统一的建筑标准和使用公共的基础设施一样。
 */
public class Node {
    // ==================== 全局编译环境管理 ====================

    // 中间代码指令序列 - 编译器的"施工图纸"
    public static ArrayList<midCode> midCodes = new ArrayList<>();

    // 全局符号表 - 编译器的"变量档案室"
    public static IntergerTable inttable = new IntergerTable();

    // 函数定义表 - 编译器的"功能模块清单"
    public static FuncTable funcTable = new FuncTable();

    // 字符串常量池 - 编译器的"文本资料库"
    public static LinkedList<String> stringss = new LinkedList<>();

    // ==================== 代码生成控制变量 ====================

    // 标签计数器 - 为代码跳转生成唯一标识
    static int labels = 0;

    // 跳转计数器 - 控制流跳转的编号系统
    static int jumps = 0;

    // 循环嵌套栈 - 跟踪break/continue的目标循环
    static Stack<Integer> loopstack = new Stack<>();

    /**
     * 获取生成的中间代码指令列表
     * 
     * @return 当前编译过程中生成的所有中间代码指令
     */
    public static ArrayList<midCode> getMidCodes() {
        return midCodes;
    }

    /**
     * 获取字符串常量池
     * 
     * @return 编译过程中收集的所有字符串常量
     */
    public static LinkedList<String> getStringss() {
        return stringss;
    }

    /**
     * 生成新的标签ID - 代码跳转的"路标制作工"
     * 
     * 就像在复杂的建筑工地中设置路标一样，
     * 每个关键位置都需要一个独特的标识符。
     * 
     * @return 新的唯一标签编号
     */
    public int newlabel() {
        return ++labels;
    }

    /**
     * 发出标签指令 - 在代码中"插旗标记"
     * 
     * 这个方法就像在施工图上标记重要位置，
     * 告诉后续的工人"这里是第X号集合点"。
     * 
     * @param labelNumber 要标记的标签编号
     */
    public void emitlabel(int labelNumber) {
        midCodes.add(new midCode(midCode.operation.LABEL, String.valueOf(labelNumber)));
    }

    /**
     * 发出中间代码指令 - 编译器的"指令下达"
     * 
     * 这个方法就像项目经理向施工队下达具体的作业指令，
     * 每一条指令都会被记录在施工日志中。
     * 
     * @param instruction 要添加的中间代码指令
     */
    public void emit(midCode instruction) {
        midCodes.add(instruction);
    }

    /**
     * 生成代码的通用接口 - 节点的"施工方法"
     * 
     * 这是所有AST节点都可以重写的方法，用于定义
     * 该节点如何生成对应的中间代码。
     * 默认实现为空，由具体的子类来实现具体的生成逻辑。
     */
    public void gen() {
        // 基类默认不生成任何代码
        // 具体的节点类型会重写这个方法来实现代码生成
    }
}
